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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.Admin;
import model.Invoice;
import model.Reservation;
import model.Room;

public class AdminDashboard extends Application {

    private TableView<Reservation> reservationTable;
    private TableView<Invoice> invoiceTable;
    private TableView<Room> roomStatusTable;

    @Override
    public void start(Stage stage) {

        Scene scene = createScene();

        scene.getStylesheets().add(
                getClass()
                        .getResource("/style.css")
                        .toExternalForm()
        );

        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public Scene createScene() {

        BorderPane root = new BorderPane();

        root.setLeft(createSidebar());

        ScrollPane scrollPane = new ScrollPane(
                createMainContent()
        );

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;"
        );

        root.setCenter(scrollPane);

        return new Scene(root, 1400, 800);
    }



    private VBox createSidebar() {

        VBox sidebar = new VBox();

        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setMaxWidth(240);

        sidebar.setPadding(new Insets(25, 16, 25, 16));
        sidebar.setSpacing(6);
        sidebar.getStyleClass().add("sidebar");

        VBox logoBox = new VBox(4);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(10, 0, 32, 0));

        Label hotelIcon = new Label("\uD83C\uDFE8");
        hotelIcon.setStyle(
                "-fx-font-size: 36;" +
                        "-fx-text-fill: #F59E0B;"
        );

        Label logo = new Label("Hotel");
        logo.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 26;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitle = new Label("Management System");
        subtitle.setStyle(
                "-fx-text-fill: #94A3B8;" +
                        "-fx-font-size: 12;"
        );

        logoBox.getChildren().addAll(hotelIcon, logo, subtitle);

        Button dashboardBtn  = createActiveSidebarButton("Dashboard",    "\uD83D\uDCCA");
        Button manageBtn  = createSidebarButton("Manage Staff", "\uD83D\uDC64");
        Button browseBtn     = createSidebarButton("Browse Rooms",       "\uD83D\uDEAA");
        Button reservationsBtn = createSidebarButton("Reservations",     "\uD83D\uDCC5");
        Button invoicesBtn   = createSidebarButton("Invoices",           "\uD83D\uDCC4");
        Button roomTypesBtn  = createSidebarButton("Room Types & Amenities", "\u26F3");


        VBox navLinks = new VBox(4);
        navLinks.getChildren().addAll(
                dashboardBtn,
                manageBtn,
                browseBtn,
                reservationsBtn,
                invoicesBtn,
                roomTypesBtn

        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("  Logout");
        logoutBtn.setPrefWidth(Double.MAX_VALUE);
        logoutBtn.setMinHeight(46);
        logoutBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #F87171;" +
                        "-fx-font-size: 14;" +
                        "-fx-alignment: center-left;" +
                        "-fx-padding: 0 0 0 12;" +
                        "-fx-cursor: hand;"
        );

        logoutBtn.setOnAction(e -> {
            SessionManager.setCurrentUser(null);
            SceneManager.switchScene("/FXML/auth.fxml");
        });

        sidebar.getChildren().addAll(
                logoBox,
                navLinks,
                spacer,
                logoutBtn
        );

        return sidebar;
    }


    private VBox createMainContent() {

        VBox main = new VBox(20);
        main.setPadding(new Insets(28, 28, 28, 28));
        main.getStyleClass().add("dashboard-pane");

        Admin admin = (Admin) SessionManager.getCurrentUser();

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
        sub.setStyle(
                "-fx-font-size: 13;" +
                        "-fx-text-fill: #6B7280;"
        );

        left.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        VBox userInfo = new VBox(1);

        userInfo.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label uname = new Label(
                admin != null
                        ? admin.getUsername()
                        : "Admin"
        );

        uname.setStyle(
                "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );

        Label urole = new Label(
                "Administrator"
        );

        urole.setStyle(
                "-fx-font-size: 11;" +
                        "-fx-text-fill: #6B7280;"
        );

        userInfo.getChildren().addAll(
                uname,
                urole
        );


        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER);
        right.getChildren().addAll(
                userInfo
        );
        header.getChildren().addAll(left, spacer, right);

        return header;
    }


    private HBox createStatisticsCards() {

        HBox stats = new HBox(16);

        stats.getChildren().addAll(

                createStatCard(
                        "Total Guests",
                        String.valueOf(
                                HotelDatabase.guests.size()
                        )
                ),

                createStatCard(
                        "Total Rooms",
                        String.valueOf(
                                HotelDatabase.rooms.size()
                        )
                ),

                createStatCard(
                        "Total Reservations",
                        String.valueOf(
                                HotelDatabase.reservations.size()
                        )
                ),

                createStatCard(
                        "Total Revenue",
                        "$" + String.format(
                                "%.0f",
                                calculateRevenue()
                        )
                )
        );

        return stats;
    }

    private VBox createStatCard(
            String labelText,
            String valueText
    ) {

        VBox card = new VBox(10);

        card.setPrefWidth(280);
        card.setPrefHeight(120);

        card.setPadding(
                new Insets(18)
        );

        card.getStyleClass().add("card");

        card.setStyle(
                "-fx-cursor: default;"
        );

        Label label = new Label(labelText);

        label.setStyle(
                "-fx-font-size: 12;" +
                        "-fx-text-fill: #6B7280;"
        );

        Label value = new Label(valueText);

        value.setStyle(
                "-fx-font-size: 26;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );

        card.getChildren().addAll(
                label,
                value
        );

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

        header.getChildren().addAll(title, spacer, viewAll);

        reservationTable = new TableView<>();
        reservationTable.setPrefHeight(240);
        reservationTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<Reservation, String> idCol =
                new TableColumn<>("Reservation ID");
        idCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        String.valueOf(c.getValue().getReservationId())
                )
        );

        TableColumn<Reservation, String> guestCol =
                new TableColumn<>("Guest Name");
        guestCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getGuest().getUsername()
                )
        );

        TableColumn<Reservation, String> checkInCol =
                new TableColumn<>("Check-in");
        checkInCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        String.valueOf(c.getValue().getCheckInDate())
                )
        );

        TableColumn<Reservation, String> checkOutCol =
                new TableColumn<>("Check-out");
        checkOutCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        String.valueOf(c.getValue().getCheckOutDate())
                )
        );

        TableColumn<Reservation, String> statusCol =
                new TableColumn<>("Status");
        statusCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getStatus().toString()
                )
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

        reservationTable.getColumns().addAll(
                idCol, guestCol, checkInCol, checkOutCol, statusCol
        );

        reservationTable.getItems().addAll(
                HotelDatabase.reservations
        );

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

        header.getChildren().addAll(title, spacer, viewAll);

        invoiceTable = new TableView<>();
        invoiceTable.setPrefHeight(240);
        invoiceTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<Invoice, String> idCol =
                new TableColumn<>("Invoice ID");
        idCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        String.valueOf(c.getValue().getInvoiceId())
                )
        );

        TableColumn<Invoice, String> guestCol =
                new TableColumn<>("Guest Name");

        guestCol.setCellValueFactory(c ->

                new SimpleStringProperty(
                        c.getValue()
                                .getReservation()
                                .getGuest()
                                .getUsername()
                )
        );

        TableColumn<Invoice, String> dateCol =
                new TableColumn<>("Date");

        dateCol.setCellValueFactory(c ->

                new SimpleStringProperty(
                        c.getValue()
                                .getPaymentDate()
                                .toString()
                )
        );

        TableColumn<Invoice, String> amountCol =
                new TableColumn<>("Amount");

        amountCol.setCellValueFactory(c ->

                new SimpleStringProperty(
                        "$" + String.format(
                                "%.2f",
                                c.getValue().getTotalAmount()
                        )
                )
        );




        invoiceTable.getColumns().addAll(
                idCol, guestCol, dateCol, amountCol
        );

        invoiceTable.getItems().addAll(
                HotelDatabase.invoices
        );

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

        header.getChildren().addAll(title, spacer, viewAll);

        roomStatusTable = new TableView<>();
        roomStatusTable.setPrefHeight(240);
        roomStatusTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<Room, String> numCol =
                new TableColumn<>("Room Number");
        numCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        String.valueOf(c.getValue().getRoomId())
                )
        );

        TableColumn<Room, String> typeCol =
                new TableColumn<>("Room Type");
        typeCol.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getRoomType().getName()
                )
        );


        TableColumn<Room, String> guestCol =
                new TableColumn<>("Current Guest");
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

        TableColumn<Room, String> checkoutCol =
                new TableColumn<>("Check-out Date");
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

        roomStatusTable.getColumns().addAll(
                numCol, typeCol,
                 guestCol, checkoutCol
        );

        roomStatusTable.getItems().addAll(HotelDatabase.rooms);

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

    private Label createRoomStatusBadge(String status) {

        Label badge = new Label(status);
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle(
                "-fx-background-radius: 99;" +
                        "-fx-font-size: 11;" +
                        "-fx-font-weight: bold;" +
                        getRoomStatusStyle(status)
        );

        return badge;
    }

    private String getStatusStyle(String status) {

        if (status == null) return "";

        return switch (status.toLowerCase()) {

            case "reserved", "confirmed" ->

                    "-fx-background-color: #DCFCE7;" +
                            "-fx-text-fill: #166534;";

            case "pending" ->

                    "-fx-background-color: #FEF9C3;" +
                            "-fx-text-fill: #854D0E;";

            case "cancelled" ->

                    "-fx-background-color: #FEE2E2;" +
                            "-fx-text-fill: #991B1B;";

            case "completed" ->

                    "-fx-background-color: #DBEAFE;" +
                            "-fx-text-fill: #1E40AF;";

            default ->

                    "-fx-background-color: #F3F4F6;" +
                            "-fx-text-fill: #374151;";
        };
    }

    private String getRoomStatusStyle(String status) {

        if (status == null) return "";

        return switch (status.toLowerCase()) {
            case "occupied"   -> "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;";
            case "available"  -> "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;";
            case "cleaning"   -> "-fx-background-color: #FEF9C3; -fx-text-fill: #854D0E;";
            case "maintenance"-> "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;";
            default           -> "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;";
        };
    }


    private double calculateRevenue() {

        double revenue = 0;

        for (Invoice invoice : HotelDatabase.invoices) {

            revenue += invoice.getTotalAmount();
        }

        return revenue;
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

    private Button createSidebarButton(String text, String icon) {

        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setMinHeight(46);
        btn.getStyleClass().add("sidebar-button");
        btn.setStyle(
                "-fx-alignment: center-left;" +
                        "-fx-padding: 0 0 0 12;"
        );

        return btn;
    }

    private Button createActiveSidebarButton(String text, String icon) {

        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setMinHeight(46);
        btn.getStyleClass().add("sidebar-button-active");
        btn.setStyle(
                "-fx-background-color: #1D4ED8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 10;" +
                        "-fx-alignment: center-left;" +
                        "-fx-padding: 0 0 0 12;" +
                        "-fx-cursor: hand;"
        );

        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}