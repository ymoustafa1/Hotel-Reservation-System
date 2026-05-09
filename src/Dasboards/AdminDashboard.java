package Dasboards;

import app.SceneManager;
import app.SessionManager;
import database.HotelDatabase;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Admin;
import model.Invoice;
import model.Reservation;
import model.Room;
import util.SidebarAdmin;

import java.util.List;

public class AdminDashboard extends Application {

    private Admin admin;

    private TableView<Reservation> reservationTable;
    private TableView<Invoice>     invoiceTable;
    private TableView<Room>        roomStatusTable;

    private Label guestCountLabel;
    private Label roomCountLabel;
    private Label reservationCountLabel;
    private Label revenueLabel;

    public AdminDashboard() {}

    public AdminDashboard(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void start(Stage stage) {
        Scene scene = createScene();
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();

        root.setLeft(SidebarAdmin.createSidebar("Dashboard"));

        VBox mainContent = createMainContent();

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;"
        );

        root.setCenter(scrollPane);

        Scene scene = SceneManager.buildScene(root, 1400, 800);

        loadDashboardDataAsync();

        return scene;
    }



    private void loadDashboardDataAsync() {

        Task<DashboardData> task = new Task<>() {
            @Override
            protected DashboardData call() {
                List<Reservation> reservations = List.copyOf(HotelDatabase.reservations);
                List<Invoice>     invoices     = List.copyOf(HotelDatabase.invoices);
                List<Room>        rooms        = List.copyOf(HotelDatabase.rooms);
                int guestCount = HotelDatabase.guests.size();

                double revenue = 0;
                for (Invoice inv : invoices) revenue += inv.getTotalAmount();

                return new DashboardData(reservations, invoices, rooms, guestCount, revenue);
            }
        };

        task.setOnSucceeded(e -> {
            DashboardData data = task.getValue();

            if (guestCountLabel       != null) guestCountLabel.setText(String.valueOf(data.guestCount));
            if (roomCountLabel        != null) roomCountLabel.setText(String.valueOf(data.rooms.size()));
            if (reservationCountLabel != null) reservationCountLabel.setText(String.valueOf(data.reservations.size()));
            if (revenueLabel          != null) revenueLabel.setText("$" + String.format("%.0f", data.revenue));

            if (reservationTable != null) {
                reservationTable.getItems().setAll(data.reservations);
            }
            if (invoiceTable != null) {
                invoiceTable.getItems().setAll(data.invoices);
            }
            if (roomStatusTable != null) {
                roomStatusTable.getItems().setAll(data.rooms);
            }
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        Thread t = new Thread(task, "admin-dashboard-loader");
        t.setDaemon(true);
        t.start();
    }

    private record DashboardData(
            List<Reservation> reservations,
            List<Invoice>     invoices,
            List<Room>        rooms,
            int               guestCount,
            double            revenue
    ) {}



    private VBox createMainContent() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(28));
        main.getStyleClass().add("dashboard-pane");

        if (admin == null) {
            admin = (Admin) SessionManager.getCurrentUser();
        }

        main.getChildren().addAll(
                createHeader(admin),
                createStatisticsCards(),
                createMidRow(),
                createRoomStatusCard()
        );

        return main;
    }

    private HBox createHeader(Admin admin) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(4);

        Label title = new Label("Welcome back, Admin!");
        title.setStyle(
                "-fx-font-size: 28;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #041E42;"
        );

        Label sub = new Label("Here's what's happening in your hotel today.");
        sub.setStyle("-fx-font-size: 13; -fx-text-fill: #6B7280;");

        left.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userInfo = new VBox(1);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Label uname = new Label(admin != null ? admin.getUsername() : "Admin");
        uname.setStyle(
                "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );

        Label urole = new Label("Administrator");
        urole.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

        userInfo.getChildren().addAll(uname, urole);

        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER);
        right.getChildren().add(userInfo);

        header.getChildren().addAll(left, spacer, right);
        return header;
    }

    private HBox createStatisticsCards() {
        HBox stats = new HBox(16);

        guestCountLabel       = new Label("…");
        roomCountLabel        = new Label("…");
        reservationCountLabel = new Label("…");
        revenueLabel          = new Label("…");

        stats.getChildren().addAll(
                createStatCard("Total Guests",       guestCountLabel),
                createStatCard("Total Rooms",        roomCountLabel),
                createStatCard("Total Reservations", reservationCountLabel),
                createStatCard("Total Revenue",      revenueLabel)
        );
        return stats;
    }

    private VBox createStatCard(String labelText, Label valueLabel) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setPrefHeight(120);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: default;");

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");

        valueLabel.setStyle(
                "-fx-font-size: 26;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );

        card.getChildren().addAll(label, valueLabel);
        return card;
    }

    private HBox createMidRow() {
        HBox row = new HBox(16);

        VBox reservationsCard = createReservationTable();
        VBox invoicesCard     = createInvoiceTable();

        HBox.setHgrow(reservationsCard, Priority.ALWAYS);
        HBox.setHgrow(invoicesCard, Priority.ALWAYS);

        row.getChildren().addAll(reservationsCard, invoicesCard);
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
        viewAll.setOnMouseClicked(e -> SceneManager.switchToDashboard(new AdminReservationDashboard()));
        header.getChildren().addAll(title, spacer, viewAll);

        reservationTable = new TableView<>();
        reservationTable.setPrefHeight(240);
        reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reservationTable.setPlaceholder(new Label("Loading reservations…"));

        TableColumn<Reservation, String> idCol = new TableColumn<>("Reservation ID");
        idCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getReservationId()))
        );

        TableColumn<Reservation, String> guestCol = new TableColumn<>("Guest Name");
        guestCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getGuest().getUsername())
        );

        TableColumn<Reservation, String> checkInCol = new TableColumn<>("Check-in");
        checkInCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCheckInDate()))
        );

        TableColumn<Reservation, String> checkOutCol = new TableColumn<>("Check-out");
        checkOutCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCheckOutDate()))
        );

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatus().toString())
        );
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createStatusBadge(item));
                }
            }
        });

        reservationTable.getColumns().addAll(idCol, guestCol, checkInCol, checkOutCol, statusCol);

        card.getChildren().addAll(header, reservationTable);
        return card;
    }

    private VBox createInvoiceTable() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: default;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Recent Invoices");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = createLinkButton("View All");
        viewAll.setOnAction(e ->
                SceneManager.switchToDashboard(new AdminInvoiceDashboard())
        );

        header.getChildren().addAll(title, spacer, viewAll);

        invoiceTable = new TableView<>();
        invoiceTable.setPrefHeight(240);
        invoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        invoiceTable.setPlaceholder(new Label("Loading invoices…"));

        TableColumn<Invoice, String> idCol = new TableColumn<>("Invoice ID");
        idCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getInvoiceId()))
        );

        TableColumn<Invoice, String> guestCol = new TableColumn<>("Guest Name");
        guestCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getReservation().getGuest().getUsername()
                )
        );

        TableColumn<Invoice, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPaymentDate().toString())
        );

        TableColumn<Invoice, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        "$" + String.format("%.2f", c.getValue().getTotalAmount())
                )
        );

        invoiceTable.getColumns().addAll(idCol, guestCol, dateCol, amountCol);

        card.getChildren().addAll(header, invoiceTable);
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

        Button viewAll = createLinkButton("View All Rooms");
        viewAll.setOnMouseClicked(e -> SceneManager.switchToDashboard(new AdminRoomBrowseView()));
        header.getChildren().addAll(title, spacer, viewAll);

        roomStatusTable = new TableView<>();
        roomStatusTable.setPrefHeight(240);
        roomStatusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        roomStatusTable.setPlaceholder(new Label("Loading room status…"));

        TableColumn<Room, String> numCol = new TableColumn<>("Room Number");
        numCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getRoomId()))
        );

        TableColumn<Room, String> typeCol = new TableColumn<>("Room Type");
        typeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRoomType().getName())
        );

        TableColumn<Room, String> guestCol = new TableColumn<>("Current Guest");
        guestCol.setCellValueFactory(c -> {
            Room room = c.getValue();
            String guestName = HotelDatabase.reservations.stream()
                    .filter(r -> r.getRoom() != null
                            && r.getRoom().getRoomId() == room.getRoomId())
                    .map(r -> r.getGuest().getUsername())
                    .findFirst()
                    .orElse("—");
            return new SimpleStringProperty(guestName);
        });

        TableColumn<Room, String> checkoutCol = new TableColumn<>("Check-out Date");
        checkoutCol.setCellValueFactory(c -> {
            Room room = c.getValue();
            String date = HotelDatabase.reservations.stream()
                    .filter(r -> r.getRoom() != null
                            && r.getRoom().getRoomId() == room.getRoomId())
                    .map(r -> String.valueOf(r.getCheckOutDate()))
                    .findFirst()
                    .orElse("—");
            return new SimpleStringProperty(date);
        });

        roomStatusTable.getColumns().addAll(numCol, typeCol, guestCol, checkoutCol);

        card.getChildren().addAll(header, roomStatusTable);
        return card;
    }


    private Label createStatusBadge(String status) {
        Label badge = new Label(status);
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle(
                "-fx-background-radius: 99;" +
                        "-fx-font-size: 11;" +
                        "-fx-font-weight: bold;" +
                        getStatusStyle(status)
        );
        return badge;
    }

    private String getStatusStyle(String status) {
        if (status == null) return "";
        return switch (status.toLowerCase()) {
            case "reserved", "confirmed" ->
                    "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;";
            case "pending" ->
                    "-fx-background-color: #FEF9C3; -fx-text-fill: #854D0E;";
            case "cancelled" ->
                    "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;";
            case "completed" ->
                    "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;";
            default ->
                    "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;";
        };
    }

    private Button createLinkButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2563EB;" +
                        "-fx-font-size: 13;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0;"
        );
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
