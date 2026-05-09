package network;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Multi-threaded Chat Server.
 *
 * Features:
 *  - Each connecting client gets its own ClientHandler thread (ServerSocket/Socket).
 *  - Messages are routed between named users (guest <-> receptionist).
 *  - Offline buffering: messages to offline users are queued and flushed on reconnect.
 *  - File persistence: every message is appended to chat_history/<sender>_<recipient>.log
 *    so history survives app restarts.
 *  - On registration, the server replays saved history to the connecting client via
 *    HISTORY: prefixed lines so the UI can pre-populate the chat pane.
 */
public class ChatServer {

    public static final int PORT = 5555;

    // History files stored relative to the working directory
    private static final Path HISTORY_DIR = Paths.get("chat_history");

    // Connected clients keyed by username
    private static final Map<String, ClientHandler> connectedClients =
            new ConcurrentHashMap<>();

    // Offline message buffer: recipientUsername -> queued outgoing lines
    private static final Map<String, List<String>> messageBuffer =
            new ConcurrentHashMap<>();

    private static ServerSocket serverSocket;
    private static volatile boolean running = false;

    // -----------------------------------------------------------------------
    // Start / Stop
    // -----------------------------------------------------------------------

    public static void start() {
        if (running) return;
        running = true;

        // Ensure history directory exists
        try { Files.createDirectories(HISTORY_DIR); }
        catch (IOException e) { System.err.println("[ChatServer] Cannot create history dir: " + e.getMessage()); }

        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                System.out.println("[ChatServer] Listening on port " + PORT);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket);
                        Thread t = new Thread(handler);
                        t.setDaemon(true);
                        t.start();
                    } catch (SocketException e) {
                        if (!running) break;
                    }
                }
            } catch (IOException e) {
                System.err.println("[ChatServer] Could not start: " + e.getMessage());
            }
        });

        serverThread.setDaemon(true);
        serverThread.setName("ChatServer-Main");
        serverThread.start();
    }

    public static void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ignored) {}
    }

    // -----------------------------------------------------------------------
    // Registration & history replay
    // -----------------------------------------------------------------------

    static void register(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        System.out.println("[ChatServer] Registered: " + username);

        // Replay saved history for this user
        replayHistory(username, handler);

        // Flush offline buffer
        List<String> buffered = messageBuffer.remove(username);
        if (buffered != null) {
            System.out.println("[ChatServer] Flushing " + buffered.size() + " buffered message(s) to " + username);
            for (String msg : buffered) handler.send(msg);
        }
    }

    static void unregister(String username) {
        connectedClients.remove(username);
        System.out.println("[ChatServer] Disconnected: " + username);
    }

    // -----------------------------------------------------------------------
    // History file helpers
    // -----------------------------------------------------------------------

    /**
     * Returns the canonical history file for a conversation between two users.
     * Always uses alphabetical order so guest_receptionist == receptionist_guest.
     */
    private static Path historyFile(String userA, String userB) {
        String[] pair = {userA, userB};
        Arrays.sort(pair);
        return HISTORY_DIR.resolve(pair[0] + "_" + pair[1] + ".log");
    }

    /**
     * Append one message line to the history file.
     * Format: TIMESTAMP|sender|text
     */
    static void appendHistory(String sender, String recipient, String text) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String line = timestamp + "|" + sender + "|" + text;
        Path file = historyFile(sender, recipient);
        try {
            Files.writeString(file, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[ChatServer] Could not write history: " + e.getMessage());
        }
    }

    /**
     * Send all stored history lines for this user to the freshly connected handler.
     * Scans all history files that involve this username and sends them as
     * HISTORY:<sender>:<timestamp>:<text>
     * so the client can render them as past bubbles.
     */
    private static void replayHistory(String username, ClientHandler handler) {
        try {
            if (!Files.exists(HISTORY_DIR)) return;

            Files.list(HISTORY_DIR)
                    .filter(p -> {
                        String name = p.getFileName().toString().replace(".log", "");
                        String[] parts = name.split("_", 2);
                        return parts.length == 2 &&
                                (parts[0].equals(username) || parts[1].equals(username));
                    })
                    .sorted() // consistent ordering
                    .forEach(file -> {
                        try {
                            List<String> lines = Files.readAllLines(file);
                            for (String line : lines) {
                                // FORMAT: timestamp|sender|text
                                String[] parts = line.split("\\|", 3);
                                if (parts.length == 3) {
                                    // Send as: HISTORY:<sender>:<timestamp>:<text>
                                    handler.send("HISTORY:" + parts[1] + ":" + parts[0] + ":" + parts[2]);
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("[ChatServer] Could not read history file: " + file);
                        }
                    });

        } catch (IOException e) {
            System.err.println("[ChatServer] History replay error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Routing
    // -----------------------------------------------------------------------

    /**
     * Protocol: TO:<recipient>:<sender>:<text>
     *
     * Saves to history, delivers to recipient (or buffers if offline),
     * and echoes back to sender.
     */
    static void route(String protocol) {
        if (protocol.startsWith("TO:")) {
            String[] parts = protocol.split(":", 4);
            if (parts.length == 4) {
                String recipient = parts[1];
                String sender    = parts[2];
                String text      = parts[3];

                // Persist to history file
                appendHistory(sender, recipient, text);

                String outgoing = "MSG:" + sender + ":" + text;

                ClientHandler recipientHandler = connectedClients.get(recipient);
                if (recipientHandler != null) {
                    recipientHandler.send(outgoing);
                } else {
                    // Buffer for offline recipient
                    messageBuffer
                            .computeIfAbsent(recipient, k -> Collections.synchronizedList(new ArrayList<>()))
                            .add(outgoing);
                    System.out.println("[ChatServer] Buffered for offline: " + recipient);
                }

                // Echo to sender
                ClientHandler senderHandler = connectedClients.get(sender);
                if (senderHandler != null) {
                    senderHandler.send("ECHO:" + sender + ":" + text);
                }
                return;
            }
        }
        broadcast(protocol, null);
    }

    static void broadcast(String message, String excludeUsername) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (!entry.getKey().equals(excludeUsername)) {
                entry.getValue().send(message);
            }
        }
    }

    public static Set<String> getConnectedUsers() {
        return Collections.unmodifiableSet(connectedClients.keySet());
    }

    // -----------------------------------------------------------------------
    // ClientHandler
    // -----------------------------------------------------------------------

    static class ClientHandler implements Runnable {

        private final Socket socket;
        private PrintWriter out;
        private String username;

        ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {

                out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                        true);

                String firstLine = in.readLine();
                if (firstLine != null && firstLine.startsWith("REGISTER:")) {
                    username = firstLine.substring("REGISTER:".length()).trim();
                    register(username, this); // replays history + flushes buffer
                    out.println("OK:Registered as " + username);
                } else {
                    out.println("ERR:First message must be REGISTER:<username>");
                    socket.close();
                    return;
                }

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("[ChatServer] From " + username + ": " + line);
                    route(line);
                }

            } catch (IOException e) {
                System.err.println("[ChatServer] Client error (" + username + "): " + e.getMessage());
            } finally {
                if (username != null) unregister(username);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        void send(String message) {
            if (out != null) out.println(message);
        }
    }

    // -----------------------------------------------------------------------
    // main
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        start();
        System.out.println("[ChatServer] Running. Press ENTER to stop.");
        try { System.in.read(); } catch (IOException ignored) {}
        stop();
    }
}