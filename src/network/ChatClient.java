package network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * Lightweight Socket-based chat client.
 *
 * Usage:
 *  ChatClient client = new ChatClient("guestUsername", msg -> handleIncomingMessage(msg));
 *  client.connect();
 *  client.sendTo("receptionist", "Hello!");
 *  client.disconnect();
 *
 * Incoming messages arrive on a background daemon thread and are delivered
 * to the provided messageListener callback (call Platform.runLater inside
 * the listener if you update JavaFX nodes).
 */
public class ChatClient {

    private final String username;
    private final Consumer<ChatMessage> messageListener;

    private Socket socket;
    private PrintWriter out;
    private volatile boolean connected = false;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * @param username        The local user's display name (used for REGISTER).
     * @param messageListener Callback invoked whenever a message arrives.
     *                        Called on a background thread — use Platform.runLater
     *                        before touching JavaFX nodes.
     */
    public ChatClient(String username, Consumer<ChatMessage> messageListener) {
        this.username        = username;
        this.messageListener = messageListener;
    }

    // -----------------------------------------------------------------------
    // Connect / Disconnect
    // -----------------------------------------------------------------------

    /**
     * Connect to localhost:ChatServer.PORT and register the username.
     * Spawns a daemon reader thread.
     *
     * @throws IOException if the server is unreachable.
     */
    public void connect() throws IOException {
        socket = new Socket("localhost", ChatServer.PORT);
        out    = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                true);

        connected = true;

        // Register with the server
        out.println("REGISTER:" + username);

        // Start reader thread
        Thread reader = new Thread(this::readLoop);
        reader.setDaemon(true);
        reader.setName("ChatClient-Reader-" + username);
        reader.start();
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() { return connected; }

    // -----------------------------------------------------------------------
    // Send
    // -----------------------------------------------------------------------

    /**
     * Send a chat message to a specific recipient.
     * Protocol: TO:<recipient>:<sender>:<text>
     */
    public void sendTo(String recipientUsername, String text) {
        if (!connected || out == null) return;
        out.println("TO:" + recipientUsername + ":" + username + ":" + text);
    }

    // -----------------------------------------------------------------------
    // Internal reader loop
    // -----------------------------------------------------------------------

    private void readLoop() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String line;
            while (connected && (line = in.readLine()) != null) {
                parseAndDeliver(line);
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("[ChatClient:" + username + "] Lost connection: " + e.getMessage());
            }
        } finally {
            connected = false;
        }
    }

    /**
     * Parse server messages:
     *  OK:<info>            — registration confirmation
     *  MSG:<sender>:<text>  — incoming message from someone else
     *  ECHO:<sender>:<text> — echo of our own sent message (confirmed delivery)
     *  ERR:<reason>         — error from server
     */
    private void parseAndDeliver(String line) {
        if (line.startsWith("OK:")) {
            System.out.println("[ChatClient:" + username + "] Server: " + line);
            return;
        }

        if (line.startsWith("MSG:")) {
            String[] parts = line.split(":", 3);
            if (parts.length == 3) {
                messageListener.accept(new ChatMessage(parts[1], parts[2], false));
            }
            return;
        }

        if (line.startsWith("ECHO:")) {
            String[] parts = line.split(":", 3);
            if (parts.length == 3) {
                // Confirmed echo of our own message
                messageListener.accept(new ChatMessage(parts[1], parts[2], true));
            }
            return;
        }

        if (line.startsWith("ERR:")) {
            System.err.println("[ChatClient:" + username + "] Server error: " + line);
        }
    }

    // -----------------------------------------------------------------------
    // Inner record: ChatMessage
    // -----------------------------------------------------------------------

    public record ChatMessage(
            String sender,
            String text,
            boolean isSelf   // true if this is the echo of our own sent message
    ) {}
}