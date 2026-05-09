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
import model.Receptionist;
import network.ChatClient;
import network.ChatServer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Receptionist live chat — compact floating popup, no sidebar.
 * Left panel lists active guests; right panel shows the conversation.
 */
public class ReceptionistChatDashboard extends Application {

    public static final String RECEPTIONIST_REGISTER_NAME = "receptionist";

    private Receptionist receptionist;
    private ChatClient chatClient;

    private final Map<String, VBox> conversations = new LinkedHashMap<>();
    private String activeGuest = null;

    private VBox guestListBox;
    private VBox messagePane;
    private ScrollPane messageScroll;
    private TextField inputField;
    private Label statusLabel;
    private Circle statusDot;
    private Label activeChatLabel;
    private static Stage chatStage;

    public ReceptionistChatDashboard() {}
    public ReceptionistChatDashboard(Receptionist receptionist) {
        this.receptionist = receptionist;
    }

    @Override
    public void start(Stage stage) {
        HBox root = new HBox(0);
        root.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 24, 0, 0, 6);"
        );

        root.getChildren().addAll(buildLeftPanel(), buildRightPanel(stage));

        Scene scene = new Scene(root, 620, 560);
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
        stage.setTitle("Guest Chat Console");
        stage.setOnCloseRequest(e -> disconnect());
        chatStage = stage;
        stage.show();

        enableDrag(root, stage);

        ChatServer.start();
        connectClient();
    }


    private VBox buildLeftPanel() {
        VBox panel = new VBox(0);
        panel.setPrefWidth(180);
        panel.setMinWidth(180);
        panel.setStyle(
                "-fx-background-color: #1E3A8A;" +
                        "-fx-background-radius: 16 0 0 16;"
        );

        Label title = new Label("Guests");
        title.setStyle(
                "-fx-font-size: 13; -fx-font-weight: bold;" +
                        "-fx-text-fill: #BFDBFE; -fx-padding: 18 14 10 14;"
        );

        guestListBox = new VBox(4);
        guestListBox.setPadding(new Insets(0, 8, 8, 8));

        Label empty = new Label("No guests yet…");
        empty.setStyle("-fx-font-size: 11; -fx-text-fill: #6B91C9; -fx-padding: 8 6 0 6;");
        guestListBox.getChildren().add(empty);

        ScrollPane scroll = new ScrollPane(guestListBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-width: 0;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel.getChildren().addAll(title, scroll);
        return panel;
    }


    private VBox buildRightPanel(Stage stage) {
        VBox panel = new VBox(0);
        VBox.setVgrow(panel, Priority.ALWAYS);
        HBox.setHgrow(panel, Priority.ALWAYS);

        panel.getChildren().addAll(
                buildHeader(stage),
                buildMessages(),
                buildInputBar()
        );

        return panel;
    }

    private HBox buildHeader(Stage stage) {
        HBox header = new HBox(10);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: #1D4ED8;" +
                        "-fx-background-radius: 0 16 0 0;"
        );

        activeChatLabel = new Label("Chat Console");
        activeChatLabel.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: white;"
        );

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

        header.getChildren().addAll(activeChatLabel, spacer, statusBox, closeBtn);
        return header;
    }

    private ScrollPane buildMessages() {
        messagePane = new VBox(10);
        messagePane.setPadding(new Insets(16));
        messagePane.setStyle("-fx-background-color: #F9FAFB;");

        Label placeholder = new Label("Select a guest to view the conversation.");
        placeholder.setStyle("-fx-font-size: 12; -fx-text-fill: #9CA3AF;");
        HBox ph = new HBox(placeholder);
        ph.setAlignment(Pos.CENTER);
        ph.setPadding(new Insets(20));
        messagePane.getChildren().add(ph);

        messageScroll = new ScrollPane(messagePane);
        messageScroll.setFitToWidth(true);
        messageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
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

    private HBox buildInputBar() {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(10, 12, 12, 12));
        bar.setAlignment(Pos.CENTER);
        bar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-width: 1 0 0 0;" +
                        "-fx-background-radius: 0 0 16 0;"
        );

        inputField = new TextField();
        inputField.setPromptText("Reply to guest…");
        inputField.setStyle(
                "-fx-background-color: #F3F4F6;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-width: 0;" +
                        "-fx-padding: 8 14 8 14;" +
                        "-fx-font-size: 13;"
        );
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setOnAction(e -> sendReply());

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
        sendBtn.setOnAction(e -> sendReply());

        bar.getChildren().addAll(inputField, sendBtn);
        return bar;
    }


    private void addGuestToList(String guestUsername) {
        guestListBox.getChildren().removeIf(
                n -> n instanceof Label l && l.getText().equals("No guests yet…")
        );

        Button guestBtn = new Button(guestUsername);
        guestBtn.setMaxWidth(Double.MAX_VALUE);
        guestBtn.setAlignment(Pos.CENTER_LEFT);
        guestBtn.setStyle(guestRowStyle(false));

        guestBtn.setOnAction(e -> switchToGuest(guestUsername, guestBtn));
        guestBtn.setOnMouseEntered(e -> {
            if (!guestUsername.equals(activeGuest))
                guestBtn.setStyle(guestRowStyle(false) +
                        "-fx-background-color: #1E40AF;");
        });
        guestBtn.setOnMouseExited(e ->
                guestBtn.setStyle(guestRowStyle(guestUsername.equals(activeGuest)))
        );

        guestBtn.setUserData(guestUsername);
        guestListBox.getChildren().add(guestBtn);

        if (activeGuest == null) {
            switchToGuest(guestUsername, guestBtn);
        }
    }

    private void switchToGuest(String guestUsername, Button selectedBtn) {
        activeGuest = guestUsername;
        activeChatLabel.setText("Chatting with: " + guestUsername);

        guestListBox.getChildren().forEach(n -> {
            if (n instanceof Button b) {
                boolean active = guestUsername.equals(b.getUserData());
                b.setStyle(guestRowStyle(active));
            }
        });

        VBox pane = conversations.get(guestUsername);
        if (pane != null) {
            messageScroll.setContent(pane);
            Platform.runLater(() -> messageScroll.setVvalue(1.0));
        }
    }

    private String guestRowStyle(boolean active) {
        return "-fx-background-color: " + (active ? "#2563EB" : "transparent") + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 10 8 10;" +
                "-fx-cursor: hand;";
    }


    private void sendReply() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || activeGuest == null ||
                chatClient == null || !chatClient.isConnected()) return;
        inputField.clear();
        chatClient.sendTo(activeGuest, text);
    }

    private void onMessageReceived(ChatClient.ChatMessage msg) {
        Platform.runLater(() -> {
            if (msg.isSelf()) {
                VBox pane = conversations.get(activeGuest);
                if (pane != null) addBubble(pane, msg.text(), true);
            } else {
                String sender = msg.sender();
                boolean isNew = !conversations.containsKey(sender);

                VBox pane = conversations.computeIfAbsent(sender, k -> {
                    VBox p = new VBox(10);
                    p.setPadding(new Insets(16));
                    p.setStyle("-fx-background-color: #F9FAFB;");
                    addSystemMessage(p, sender + " joined the chat.");
                    return p;
                });

                if (isNew) addGuestToList(sender);

                addBubble(pane, msg.text(), false);

                if (sender.equals(activeGuest)) {
                    messageScroll.setContent(pane);
                    Platform.runLater(() -> messageScroll.setVvalue(1.0));
                }
            }
        });
    }


    private void addBubble(VBox pane, String text, boolean isSelf) {
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
            pane.getChildren().add(row);
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

            HBox row = new HBox(bubbleBox);
            row.setAlignment(Pos.CENTER_LEFT);
            pane.getChildren().add(row);
        }
    }

    private void addSystemMessage(VBox pane, String text) {
        Label sys = new Label(text);
        sys.setStyle(
                "-fx-font-size: 11; -fx-text-fill: #9CA3AF;" +
                        "-fx-background-color: #F3F4F6;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 3 10 3 10;"
        );
        HBox row = new HBox(sys);
        row.setAlignment(Pos.CENTER);
        pane.getChildren().add(row);
    }


    private void connectClient() {
        chatClient = new ChatClient(RECEPTIONIST_REGISTER_NAME, this::onMessageReceived);

        Thread t = new Thread(() -> {
            try {
                chatClient.connect();
                Platform.runLater(() -> {
                    statusDot.setFill(Color.web("#4ADE80"));
                    statusLabel.setText("Online");
                });
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    statusDot.setFill(Color.web("#F87171"));
                    statusLabel.setText("Offline");
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void disconnect() {
        if (chatClient != null) chatClient.disconnect();
    }


    private void enableDrag(HBox root, Stage stage) {
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