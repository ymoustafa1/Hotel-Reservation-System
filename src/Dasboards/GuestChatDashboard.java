package Dasboards;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Guest;
import network.ChatClient;
import network.ChatServer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Guest live chat — compact floating popup, no sidebar.
 * Opens as a small always-on-top window so the guest can
 * keep browsing while chatting.
 */
public class GuestChatDashboard extends Application {

    public static final String RECEPTIONIST_USERNAME = "receptionist";

    private Guest guest;
    private ChatClient chatClient;

    private VBox messagePane;
    private ScrollPane messageScroll;
    private TextField inputField;
    private Label statusLabel;
    private Circle statusDot;
    private static Stage chatStage;

    public GuestChatDashboard() {}
    public GuestChatDashboard(Guest guest) { this.guest = guest; }

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(0);
        root.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 24, 0, 0, 6);"
        );

        root.getChildren().addAll(
                buildHeader(stage),
                buildMessages(),
                buildInputBar()
        );

        Scene scene = new Scene(root, 380, 560);
        scene.setFill(Color.TRANSPARENT);

        try {
            scene.getStylesheets().add(
                    getClass().getResource("/style.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setTitle("Chat Support");
        stage.setOnCloseRequest(e -> disconnect());
        chatStage = stage;
        stage.show();

        enableDrag(root, stage);

        ChatServer.start();
        connectClient();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private HBox buildHeader(Stage stage) {
        HBox header = new HBox(10);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: #1D4ED8;" +
                        "-fx-background-radius: 16 16 0 0;"
        );

        Circle avatar = new Circle(18);
        avatar.setFill(Color.web("#BFDBFE"));

        VBox info = new VBox(1);
        Label name = new Label("Support Team");
        name.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: white;");
        Label role = new Label("Hotel Receptionist");
        role.setStyle("-fx-font-size: 11; -fx-text-fill: #BFDBFE;");
        info.getChildren().addAll(name, role);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusDot = new Circle(5);
        statusDot.setFill(Color.web("#6B7280"));
        statusLabel = new Label("Connecting…");
        statusLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #BFDBFE;");
        HBox statusBox = new HBox(5, statusDot, statusLabel);
        statusBox.setAlignment(Pos.CENTER);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #BFDBFE;" +
                        "-fx-font-size: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 0 0 8;"
        );
        closeBtn.setOnAction(e -> { disconnect(); stage.close(); });

        header.getChildren().addAll(avatar, info, spacer, statusBox, closeBtn);
        return header;
    }

    // ── Message area ──────────────────────────────────────────────────────────

    private ScrollPane buildMessages() {
        messagePane = new VBox(10);
        messagePane.setPadding(new Insets(16));
        messagePane.setStyle("-fx-background-color: #F9FAFB;");

        messageScroll = new ScrollPane(messagePane);
        messageScroll.setFitToWidth(true);
        messageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messageScroll.setPrefHeight(400);
        messageScroll.setStyle(
                "-fx-background: #F9FAFB;" +
                        "-fx-background-color: #F9FAFB;" +
                        "-fx-border-width: 0;"
        );
        VBox.setVgrow(messageScroll, Priority.ALWAYS);

        messagePane.heightProperty().addListener((obs, o, n) ->
                messageScroll.setVvalue(1.0)
        );

        return messageScroll;
    }

    // ── Input bar ─────────────────────────────────────────────────────────────

    private HBox buildInputBar() {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(10, 12, 12, 12));
        bar.setAlignment(Pos.CENTER);
        bar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-width: 1 0 0 0;" +
                        "-fx-background-radius: 0 0 16 16;"
        );

        inputField = new TextField();
        inputField.setPromptText("Type a message…");
        inputField.setStyle(
                "-fx-background-color: #F3F4F6;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-width: 0;" +
                        "-fx-padding: 8 14 8 14;" +
                        "-fx-font-size: 13;"
        );
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setOnAction(e -> sendMessage());

        Button sendBtn = new Button("➤");
        sendBtn.setStyle(
                "-fx-background-color: #1D4ED8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13;" +
                        "-fx-background-radius: 20;" +
                        "-fx-min-width: 36;" +
                        "-fx-min-height: 36;" +
                        "-fx-cursor: hand;"
        );
        sendBtn.setOnAction(e -> sendMessage());

        bar.getChildren().addAll(inputField, sendBtn);
        return bar;
    }

    // ── Messaging ─────────────────────────────────────────────────────────────

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || chatClient == null || !chatClient.isConnected()) return;
        inputField.clear();
        chatClient.sendTo(RECEPTIONIST_USERNAME, text);
    }

    private void onMessageReceived(ChatClient.ChatMessage msg) {
        Platform.runLater(() -> addBubble(msg.text(), msg.isSelf()));
    }

    // ── Bubble builder ────────────────────────────────────────────────────────

    private void addBubble(String text, boolean isSelf) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(240);
        bubble.setPadding(new Insets(8, 12, 8, 12));

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #9CA3AF;");

        VBox bubbleBox = new VBox(2, bubble, timeLabel);

        if (isSelf) {
            bubble.setStyle(
                    "-fx-background-color: #1D4ED8;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 14 14 4 14;" +
                            "-fx-font-size: 13;"
            );
            timeLabel.setAlignment(Pos.CENTER_RIGHT);
            bubbleBox.setAlignment(Pos.CENTER_RIGHT);
            HBox row = new HBox(bubbleBox);
            row.setAlignment(Pos.CENTER_RIGHT);
            messagePane.getChildren().add(row);
        } else {
            bubble.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #111827;" +
                            "-fx-background-radius: 14 14 14 4;" +
                            "-fx-font-size: 13;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 4, 0, 0, 1);"
            );
            timeLabel.setAlignment(Pos.CENTER_LEFT);
            bubbleBox.setAlignment(Pos.CENTER_LEFT);

            Label sender = new Label("Support");
            sender.setStyle("-fx-font-size: 10; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
            VBox withSender = new VBox(2, sender, bubbleBox);
            HBox row = new HBox(withSender);
            row.setAlignment(Pos.CENTER_LEFT);
            messagePane.getChildren().add(row);
        }
    }

    private void addSystemMessage(String text) {
        Label sys = new Label(text);
        sys.setStyle(
                "-fx-font-size: 11; -fx-text-fill: #9CA3AF;" +
                        "-fx-background-color: #F3F4F6;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 3 10 3 10;"
        );
        HBox row = new HBox(sys);
        row.setAlignment(Pos.CENTER);
        messagePane.getChildren().add(row);
    }

    // ── Connection ────────────────────────────────────────────────────────────

    private void connectClient() {
        String username = (guest != null) ? guest.getUsername() : "guest";
        chatClient = new ChatClient(username, this::onMessageReceived);

        Thread t = new Thread(() -> {
            try {
                chatClient.connect();
                Platform.runLater(() -> {
                    statusDot.setFill(Color.web("#4ADE80"));
                    statusLabel.setText("Online");
                    addSystemMessage("Connected — we'll reply shortly!");
                });
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    statusDot.setFill(Color.web("#F87171"));
                    statusLabel.setText("Offline");
                    addSystemMessage("Could not connect. Try again later.");
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void disconnect() {
        if (chatClient != null) chatClient.disconnect();
    }

    // ── Draggable window ──────────────────────────────────────────────────────

    private void enableDrag(VBox root, Stage stage) {
        final double[] offset = new double[2];
        root.setOnMousePressed(e -> {
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
        });
        root.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - offset[0]);
            stage.setY(e.getScreenY() - offset[1]);
        });
    }
    public static void closeWindow() {

        if (chatStage != null) {

            chatStage.close();
            chatStage = null;
        }
    }

    public static void main(String[] args) { launch(args); }
}