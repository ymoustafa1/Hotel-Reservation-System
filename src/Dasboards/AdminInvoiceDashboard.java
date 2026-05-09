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
import model.Invoice;
import util.SidebarAdmin;

import java.util.ArrayList;

public class AdminInvoiceDashboard extends Application {
    private TableView<Invoice> invoiceTable;
    private TextField searchField;

    private ComboBox<String> methodCombo;

    private DatePicker fromDatePicker;

    private DatePicker toDatePicker;
    public AdminInvoiceDashboard() {}

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1400, 850);

        var css = getClass().getResource("/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        root.setLeft(SidebarAdmin.createSidebar("Invoices"));

        ScrollPane scroll = new ScrollPane(createMain());
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scroll);

        stage.setScene(scene);
        stage.setTitle("Invoice Dashboard");
        stage.setMaximized(true);
        stage.show();
    }

    private VBox createMain() {
        VBox main = new VBox(20);
        main.getStyleClass().add("dashboard-pane");
        main.setPadding(new Insets(28));
        main.getChildren().addAll(header(), stats(), filters(), table());
        return main;
    }

    private VBox header() {
        VBox box = new VBox(5);

        Label title = new Label("Invoice Management");
        title.getStyleClass().add("title-label");

        Label sub = new Label("Create, view and manage all hotel invoices.");
        sub.getStyleClass().add("subtitle-label");

        box.getChildren().addAll(title, sub);
        return box;
    }

    private HBox stats() {
        HBox row = new HBox(20);

        int total = HotelDatabase.invoices.size();
        double totalAmount = 0;
        for (Invoice inv : HotelDatabase.invoices) {
            totalAmount += inv.getTotalAmount();
        }

        row.getChildren().addAll(
                createStatCard("Total Invoices", String.valueOf(total)),
                createStatCard("Paid", String.valueOf(total)),
                createStatCard("Pending", "0"),
                createStatCard("Total Revenue", "$" + String.format("%.2f", totalAmount))
        );

        return row;
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "stat-card");
        card.setPrefSize(200, 110);
        card.setStyle("-fx-cursor: default;");

        Label t = new Label(title);
        t.getStyleClass().add("small-label");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        card.getChildren().addAll(t, v);
        return card;
    }

    private HBox filters() {

        HBox row = new HBox(15);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        searchField =
                new TextField();

        searchField.setPromptText(
                "Search invoice..."
        );

        searchField.setPrefWidth(220);

        methodCombo =
                new ComboBox<>();

        methodCombo.getItems().addAll(
                "All",
                "CASH",
                "CREDIT_CARD",
                "DEBIT_CARD",
                "ONLINE"
        );

        methodCombo.setValue("All");

        fromDatePicker =
                new DatePicker();

        fromDatePicker.setPromptText(
                "From Date"
        );

        toDatePicker =
                new DatePicker();

        toDatePicker.setPromptText(
                "To Date"
        );

        Button clear =
                new Button(
                        "Clear Filters"
                );

        clear.getStyleClass()
                .add("secondary-button");

        clear.setOnAction(e -> {

            searchField.clear();

            methodCombo.setValue("All");

            fromDatePicker.setValue(null);

            toDatePicker.setValue(null);

            applyFilters();
        });

        // AUTO FILTER

        searchField.textProperty()
                .addListener(
                        (a,b,c) ->
                                applyFilters()
                );

        methodCombo.valueProperty()
                .addListener(
                        (a,b,c) ->
                                applyFilters()
                );

        fromDatePicker.valueProperty()
                .addListener(
                        (a,b,c) ->
                                applyFilters()
                );

        toDatePicker.valueProperty()
                .addListener(
                        (a,b,c) ->
                                applyFilters()
                );

        row.getChildren().addAll(
                searchField,
                methodCombo,
                fromDatePicker,
                toDatePicker,
                clear
        );

        return row;
    }
    private VBox table() {
        VBox container = new VBox(10);
        container.getStyleClass().add("card");
        container.setStyle("-fx-cursor: default;");
        container.setPadding(new Insets(20));

        Label title = new Label("All Invoices");
        title.getStyleClass().add("section-title");

        invoiceTable =
                new TableView<>();
        invoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        invoiceTable.setPrefHeight(500);
        VBox.setVgrow(invoiceTable, Priority.ALWAYS);

        // Invoice ID
        TableColumn<Invoice, String> idCol = new TableColumn<>("Invoice ID");
        idCol.setCellValueFactory(data ->
                new SimpleStringProperty("INV-" + data.getValue().getInvoiceId())
        );

        // Guest Name
        TableColumn<Invoice, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getReservation().getGuest().getUsername()
                )
        );

        // Room
        TableColumn<Invoice, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getReservation().getRoom().getRoomType().getName()
                )
        );

        // Check-in
        TableColumn<Invoice, String> checkInCol = new TableColumn<>("Check In");
        checkInCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getReservation().getCheckInDate().toString()
                )
        );

        // Check-out
        TableColumn<Invoice, String> checkOutCol = new TableColumn<>("Check Out");
        checkOutCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getReservation().getCheckOutDate().toString()
                )
        );

        // Amount
        TableColumn<Invoice, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        "$" + String.format("%.2f", data.getValue().getTotalAmount())
                )
        );

        // Payment Method
        TableColumn<Invoice, String> methodCol = new TableColumn<>("Payment");
        methodCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPaymentMethod().toString())
        );

        // Status
        TableColumn<Invoice, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty("Paid")
        );
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setPadding(new Insets(3, 10, 3, 10));
                    badge.setStyle(
                            "-fx-background-color: #DCFCE7;" +
                                    "-fx-text-fill: #166534;" +
                                    "-fx-background-radius: 99;" +
                                    "-fx-font-size: 11;" +
                                    "-fx-font-weight: bold;"
                    );
                    setGraphic(badge);
                }
            }
        });

        // Action
        TableColumn<Invoice, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.getStyleClass().add("button");
                viewBtn.setOnAction(e -> {
                    Invoice inv = getTableView().getItems().get(getIndex());
                    showInvoiceDetail(inv);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });

        invoiceTable.getColumns().addAll(
                idCol, guestCol, roomCol, checkInCol, checkOutCol,
                amountCol, methodCol, statusCol, actionCol
        );

        applyFilters();

        container.getChildren().addAll(title, invoiceTable);
        return container;
    }
    private void applyFilters() {

        if (invoiceTable == null) {

            return;
        }

        invoiceTable.getItems().clear();

        String search =

                searchField.getText()
                        .toLowerCase()
                        .trim();

        String payment =

                methodCombo.getValue();

        for (Invoice inv : HotelDatabase.invoices) {

            boolean matches = true;

            // SEARCH

            String invoiceId =
                    "inv-" + inv.getInvoiceId();

            String guest =
                    inv.getReservation()
                            .getGuest()
                            .getUsername()
                            .toLowerCase();

            String room =
                    inv.getReservation()
                            .getRoom()
                            .getRoomType()
                            .getName()
                            .toLowerCase();

            if (
                    !search.isBlank()

                            &&

                            !invoiceId.contains(search)

                            &&

                            !guest.contains(search)

                            &&

                            !room.contains(search)
            ) {

                matches = false;
            }

            // PAYMENT METHOD

            if (
                    payment != null

                            &&

                            !payment.equals("All")
            ) {

                if (
                        !inv.getPaymentMethod()
                                .toString()
                                .equals(payment)
                ) {

                    matches = false;
                }
            }

            // FROM DATE

            if (
                    fromDatePicker.getValue()
                            != null
            ) {

                if (
                        inv.getReservation()
                                .getCheckInDate()
                                .isBefore(
                                        fromDatePicker.getValue()
                                )
                ) {

                    matches = false;
                }
            }

            // TO DATE

            if (
                    toDatePicker.getValue()
                            != null
            ) {

                if (
                        inv.getReservation()
                                .getCheckOutDate()
                                .isAfter(
                                        toDatePicker.getValue()
                                )
                ) {

                    matches = false;
                }
            }

            if (matches) {

                invoiceTable.getItems()
                        .add(inv);
            }
        }
    }

    private void showInvoiceDetail(Invoice inv) {
        Stage popup = new Stage();

        VBox root = new VBox(18);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("card");

        Label title = new Label("Invoice Details");
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setHgap(30);
        details.setVgap(14);

        addDetailRow(details, "Invoice ID",     "INV-" + inv.getInvoiceId(), 0);
        addDetailRow(details, "Guest",          inv.getReservation().getGuest().getUsername(), 1);
        addDetailRow(details, "Room Type",      inv.getReservation().getRoom().getRoomType().getName(), 2);
        addDetailRow(details, "Check In",       inv.getReservation().getCheckInDate().toString(), 3);
        addDetailRow(details, "Check Out",      inv.getReservation().getCheckOutDate().toString(), 4);
        addDetailRow(details, "Payment Method", inv.getPaymentMethod().toString(), 5);
        addDetailRow(details, "Total Amount",   "$" + String.format("%.2f", inv.getTotalAmount()), 6);
        addDetailRow(details, "Status",         "Paid", 7);

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("button");
        closeBtn.setOnAction(e -> popup.close());

        root.getChildren().addAll(title, details, closeBtn);

        Scene scene = new Scene(root, 440, 430);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        popup.setScene(scene);
        popup.setTitle("Invoice #" + inv.getInvoiceId());
        popup.setResizable(false);
        popup.show();
    }

    private void addDetailRow(GridPane grid, String label, String value, int row) {
        Label l = new Label(label);
        l.getStyleClass().add("section-title");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14; -fx-text-fill: #374151;");

        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    public static void main(String[] args) {
        launch(args);
    }
}