package network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;


public class ChatClient {

    private final String username;
    private final Consumer<ChatMessage> messageListener;

    private Socket socket;
    private PrintWriter out;
    private volatile boolean connected = false;


    public ChatClient(String username, Consumer<ChatMessage> messageListener) {
        this.username        = username;
        this.messageListener = messageListener;
    }

    public void connect() throws IOException {
        socket = new Socket("localhost", ChatServer.PORT);
        out    = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                true);

        connected = true;

        out.println("REGISTER:" + username);

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


    public void sendTo(String recipientUsername, String text) {
        if (!connected || out == null) return;
        out.println("TO:" + recipientUsername + ":" + username + ":" + text);
    }



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
                messageListener.accept(new ChatMessage(parts[1], parts[2], true));
            }
            return;
        }

        if (line.startsWith("ERR:")) {
            System.err.println("[ChatClient:" + username + "] Server error: " + line);
        }
        if (line.startsWith("HISTORY:")) {
            String[] parts = line.split(":", 4);
            if (parts.length == 4) {
                String displayText = "[" + parts[2] + "] " + parts[3];
                boolean isSelf = parts[1].equals(username);
                messageListener.accept(new ChatMessage(parts[1], displayText, isSelf));
            }
            return;
        }
    }


    public record ChatMessage(
            String sender,
            String text,
            boolean isSelf
    ) {}
}