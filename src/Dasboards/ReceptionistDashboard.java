package Dasboards;

import app.SceneManager;
import app.SessionManager;
import database.HotelDatabase;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Receptionist;
import model.Reservation;
import model.ReservationStatus;
import model.Room;
import util.SidebarReceptionist;

public class ReceptionistDashboard extends Application {

    private Receptionist receptionist;

    public ReceptionistDashboard() {}

    public ReceptionistDashboard(Receptionist receptionist) {
        this.receptionist = receptionist;
    }

    @Override
    public void start(Stage stage) {
        Scene scene = createScene();
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );
        stage.setTitle("Receptionist Dashboard");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setLeft(SidebarReceptionist.createSidebar("Dashboard"));

        ScrollPane scrollPane = new ScrollPane(createMainContent());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setCenter(scrollPane);

        return new Scene(root, 1400, 800);
    }

    private VBox createMainContent() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(28));
        main.getStyleClass().add("dashboard-pane");

        if (receptionist == null) {
            receptionist = (Receptionist) SessionManager.getCurrentUser();
        }

        main.getChildren().addAll(
                createHeader(),
                createStatisticsCards(),
                createMidRow(),
                createRoomStatusCard(),
                createChatCard()      // ◀ new chat shortcut card
        );

        return main;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(4);
        Label title = new Label("Welcome back, " + (receptionist != null ? receptionist.getUsername() : "Receptionist") + "!");
        title.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #041E42;");

        Label sub = new Label("Manage check-ins, check-outs, and reservations.");
        sub.setStyle("-fx-font-size: 13; -fx-text-fill: #6B7280;");
        left.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userInfo = new VBox(1);
        userInfo.setAlignment(Pos.CENTER_RIGHT);
        Label uname = new Label(receptionist != null ? receptionist.getUsername() : "Receptionist");
        uname.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label urole = new Label("Receptionist");
        urole.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");
        userInfo.getChildren().addAll(uname, urole);

        header.getChildren().addAll(left, spacer, userInfo);
        return header;
    }

    private HBox createStatisticsCards() {
        long totalReservations = HotelDatabase.reservations.size();
        long pending = HotelDatabase.reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING).count();
        long occupied = HotelDatabase.reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED).count();
        long available = HotelDatabase.rooms.stream()
                .filter(r -> HotelDatabase.reservations.stream()
                        .noneMatch(res -> res.getRoom() != null
                                && res.getRoom().getRoomId() == r.getRoomId()
                                && res.getStatus() == ReservationStatus.RESERVED))
                .count();

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                createStatCard("Total Reservations", String.valueOf(totalReservations)),
                createStatCard("Pending Check-ins",  String.valueOf(pending)),
                createStatCard("Occupied Rooms",     String.valueOf(occupied)),
                createStatCard("Available Rooms",    String.valueOf(available))
        );
        return stats;
    }

    private VBox createStatCard(String labelText, String valueText) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setPrefHeight(120);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: default;");

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");

        Label value = new Label(valueText);
        value.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: #111827;");

        card.getChildren().addAll(label, value);
        return card;
    }

    private HBox createMidRow() {
        HBox row = new HBox(16);

        VBox reservationsCard = createReservationTable();
        HBox.setHgrow(reservationsCard, Priority.ALWAYS);

        row.getChildren().add(reservationsCard);
        return row;
    }

    private VBox createReservationTable() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: default;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Recent Reservations");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = createLinkButton("View All");
        viewAll.setOnAction(e -> SceneManager.switchToDashboard(new ReceptionistReservationDashboard()));
        header.getChildren().addAll(title, spacer, viewAll);

        TableView<Reservation> table = new TableView<>();
        table.setPrefHeight(280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Reservation, String> idCol = new TableColumn<>("Reservation ID");
        idCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getReservationId())));

        TableColumn<Reservation, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getGuest().getUsername()));

        TableColumn<Reservation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(c -> {
            Room r = c.getValue().getRoom();
            return new SimpleStringProperty(r != null ? "#" + r.getRoomId() + " " + r.getRoomType().getName() : "—");
        });

        TableColumn<Reservation, String> checkInCol = new TableColumn<>("Check-in");
        checkInCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCheckInDate())));

        TableColumn<Reservation, String> checkOutCol = new TableColumn<>("Check-out");
        checkOutCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCheckOutDate())));

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatus().toString()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : createStatusBadge(item));
            }
        });

        table.getColumns().addAll(idCol, guestCol, roomCol, checkInCol, checkOutCol, statusCol);
        table.getItems().addAll(HotelDatabase.reservations);

        card.getChildren().addAll(header, table);
        return card;
    }

    private VBox createRoomStatusCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: default;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Room Status Overview");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer);

        TableView<Room> roomTable = new TableView<>();
        roomTable.setPrefHeight(220);
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Room, String> numCol = new TableColumn<>("Room #");
        numCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getRoomId())));

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRoomType().getName()));

        TableColumn<Room, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> {
            Room r = c.getValue();
            boolean occ = HotelDatabase.reservations.stream()
                    .anyMatch(res -> res.getRoom() != null
                            && res.getRoom().getRoomId() == r.getRoomId()
                            && res.getStatus() == ReservationStatus.RESERVED);
            return new SimpleStringProperty(occ ? "Occupied" : "Available");
        });
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : createStatusBadge(item));
            }
        });

        TableColumn<Room, String> guestCol = new TableColumn<>("Current Guest");
        guestCol.setCellValueFactory(c -> {
            Room r = c.getValue();
            String g = HotelDatabase.reservations.stream()
                    .filter(res -> res.getRoom() != null
                            && res.getRoom().getRoomId() == r.getRoomId()
                            && res.getStatus() == ReservationStatus.RESERVED)
                    .map(res -> res.getGuest().getUsername())
                    .findFirst().orElse("—");
            return new SimpleStringProperty(g);
        });

        TableColumn<Room, String> checkoutCol = new TableColumn<>("Check-out");
        checkoutCol.setCellValueFactory(c -> {
            Room r = c.getValue();
            String d = HotelDatabase.reservations.stream()
                    .filter(res -> res.getRoom() != null
                            && res.getRoom().getRoomId() == r.getRoomId()
                            && res.getStatus() == ReservationStatus.RESERVED)
                    .map(res -> String.valueOf(res.getCheckOutDate()))
                    .findFirst().orElse("—");
            return new SimpleStringProperty(d);
        });

        roomTable.getColumns().addAll(numCol, typeCol, statusCol, guestCol, checkoutCol);
        roomTable.getItems().addAll(HotelDatabase.rooms);

        card.getChildren().addAll(header, roomTable);
        return card;
    }

    // ── NEW: Chat shortcut card ───────────────────────────────────────────────
    private VBox createChatCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: default;");

        Label title = new Label("Guest Chat");
        title.getStyleClass().add("section-title");

        Label sub = new Label("Reply to guest messages in real-time via the live chat panel.");
        sub.setStyle("-fx-font-size: 13; -fx-text-fill: #6B7280;");
        sub.setWrapText(true);

        Button openChatBtn = new Button("Open Chat Console");
        openChatBtn.getStyleClass().add("button");
        openChatBtn.setOnAction(e -> {
            ReceptionistChatDashboard chatDash =
                    new ReceptionistChatDashboard(receptionist);
            Stage chatStage = new Stage();
            try {
                chatDash.start(chatStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(title, sub, openChatBtn);
        return card;
    }

    private Label createStatusBadge(String status) {
        Label badge = new Label(status);
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle("-fx-background-radius: 99; -fx-font-size: 11; -fx-font-weight: bold;" + getStatusStyle(status));
        return badge;
    }

    private String getStatusStyle(String status) {
        if (status == null) return "";
        return switch (status.toLowerCase()) {
            case "reserved", "available" -> "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;";
            case "pending"               -> "-fx-background-color: #FEF9C3; -fx-text-fill: #854D0E;";
            case "cancelled"             -> "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;";
            case "completed"             -> "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;";
            case "occupied"              -> "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;";
            default                      -> "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;";
        };
    }

    private Button createLinkButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563EB;" +
                "-fx-font-size: 13; -fx-cursor: hand; -fx-padding: 0;");
        return btn;
    }

    public static void main(String[] args) { launch(args); }
}