package network;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {

    public static final int PORT = 5555;

    private static final Path HISTORY_DIR = Paths.get("chat_history");

    private static final Map<String, ClientHandler> connectedClients =
            new ConcurrentHashMap<>();

    private static final Map<String, List<String>> messageBuffer =
            new ConcurrentHashMap<>();

    private static ServerSocket serverSocket;
    private static volatile boolean running = false;


    public static void start() {
        if (running) return;
        running = true;

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

    static void register(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        System.out.println("[ChatServer] Registered: " + username);

        replayHistory(username, handler);

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

    private static Path historyFile(String userA, String userB) {
        String[] pair = {userA, userB};
        Arrays.sort(pair);
        return HISTORY_DIR.resolve(pair[0] + "_" + pair[1] + ".log");
    }


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
                    .sorted()
                    .forEach(file -> {
                        try {
                            List<String> lines = Files.readAllLines(file);
                            for (String line : lines) {
                                String[] parts = line.split("\\|", 3);
                                if (parts.length == 3) {
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


    static void route(String protocol) {
        if (protocol.startsWith("TO:")) {
            String[] parts = protocol.split(":", 4);
            if (parts.length == 4) {
                String recipient = parts[1];
                String sender    = parts[2];
                String text      = parts[3];

                appendHistory(sender, recipient, text);

                String outgoing = "MSG:" + sender + ":" + text;

                ClientHandler recipientHandler = connectedClients.get(recipient);
                if (recipientHandler != null) {
                    recipientHandler.send(outgoing);
                } else {
                    messageBuffer
                            .computeIfAbsent(recipient, k -> Collections.synchronizedList(new ArrayList<>()))
                            .add(outgoing);
                    System.out.println("[ChatServer] Buffered for offline: " + recipient);
                }

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
                    register(username, this);
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

    public static void main(String[] args) {
        start();
        System.out.println("[ChatServer] Running. Press ENTER to stop.");
        try { System.in.read(); } catch (IOException ignored) {}
        stop();
    }
}