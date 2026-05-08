package Dasboards;

import app.SceneManager;
import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Guest;
import model.Invoice;
import util.SidebarGuest;

import java.util.ArrayList;

public class InvoicesDashboard extends Application {

    private Guest guest;

    private Label totalCount;
    private Label totalPaidLabel;
    private Label totalAmountLabel;

    public InvoicesDashboard() {}

    public InvoicesDashboard(Guest guest) {
        this.guest = guest;
    }

    @Override
    public void start(Stage stage) {

        if (guest == null) {
            guest = HotelDatabase.findGuest("youssef");
        }

        totalCount = new Label();
        totalPaidLabel = new Label();
        totalAmountLabel = new Label();

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        root.setLeft(SidebarGuest.createSidebar("Invoices"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scroll);

        Label title = new Label("My Invoices");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("View and track all your payment invoices.");
        subtitle.getStyleClass().add("subtitle-label");

        HBox statsCards = new HBox(20);
        statsCards.getChildren().addAll(
                createStatCard("Total Invoices", totalCount),
                createStatCard("Total Paid", totalPaidLabel),
                createStatCard("Total Spent", totalAmountLabel)
        );

        updateStatistics();

        HBox filters = new HBox(15);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by invoice ID or room");
        searchField.setPrefWidth(280);

        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("All", "CASH", "CREDIT_CARD", "DEBIT_CARD", "ONLINE");
        methodCombo.setValue("All");

        DatePicker startDate = new DatePicker();
        startDate.setPromptText("From Date");

        DatePicker endDate = new DatePicker();
        endDate.setPromptText("To Date");

        Button clearBtn = new Button("Clear Filters");
        clearBtn.getStyleClass().add("secondary-button");

        filters.getChildren().addAll(searchField, methodCombo, startDate, endDate, clearBtn);

        VBox invoiceContainer = new VBox(15);

        Runnable refreshInvoices = () -> {
            invoiceContainer.getChildren().clear();

            for (Invoice inv : getGuestInvoices()) {
                boolean matches = true;

                String search = searchField.getText().toLowerCase();
                if (!search.isBlank()
                        && !String.valueOf(inv.getInvoiceId()).contains(search)
                        && !inv.getReservation().getRoom().getRoomType().getName().toLowerCase().contains(search)) {
                    matches = false;
                }

                if (methodCombo.getValue() != null && !methodCombo.getValue().equals("All")) {
                    if (!inv.getPaymentMethod().toString().equalsIgnoreCase(methodCombo.getValue())) {
                        matches = false;
                    }
                }

                if (startDate.getValue() != null
                        && inv.getReservation().getCheckInDate().isBefore(startDate.getValue())) {
                    matches = false;
                }

                if (endDate.getValue() != null
                        && inv.getReservation().getCheckInDate().isAfter(endDate.getValue())) {
                    matches = false;
                }

                if (matches) {
                    invoiceContainer.getChildren().add(createInvoiceCard(inv));
                }
            }

            if (invoiceContainer.getChildren().isEmpty()) {
                Label empty = new Label("No invoices found.");
                empty.getStyleClass().add("subtitle-label");
                invoiceContainer.getChildren().add(empty);
            }
        };

        refreshInvoices.run();

        searchField.textProperty().addListener((a, b, c) -> refreshInvoices.run());
        methodCombo.valueProperty().addListener((a, b, c) -> refreshInvoices.run());
        startDate.valueProperty().addListener((a, b, c) -> refreshInvoices.run());
        endDate.valueProperty().addListener((a, b, c) -> refreshInvoices.run());

        clearBtn.setOnAction(e -> {
            searchField.clear();
            methodCombo.setValue("All");
            startDate.setValue(null);
            endDate.setValue(null);
            refreshInvoices.run();
        });

        ScrollPane invoiceScroll = new ScrollPane(invoiceContainer);
        invoiceScroll.setFitToWidth(true);
        invoiceScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        invoiceScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        centerArea.getChildren().addAll(title, subtitle, statsCards, filters, invoiceScroll);

        stage.setScene(scene);
        stage.setTitle("Invoices");
        stage.setMaximized(true);
        stage.show();
    }

    private HBox createInvoiceCard(Invoice inv) {
        HBox card = new HBox(30);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: default;");

        VBox left = new VBox(8);

        Label id = new Label("INV-" + inv.getInvoiceId());
        id.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label room = new Label(
                "Room: " + inv.getReservation().getRoom().getRoomType().getName()
        );

        Label period = new Label(
                "Check In: " + inv.getReservation().getCheckInDate()
                        + " | Check Out: " + inv.getReservation().getCheckOutDate()
        );

        Label method = new Label("Payment: " + inv.getPaymentMethod().toString());
        method.getStyleClass().add("subtitle-label");

        left.getChildren().addAll(id, room, period, method);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label amount = new Label("$" + String.format("%.2f", inv.getTotalAmount()));
        amount.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #166534;");

        Label statusLabel = new Label("Paid");
        statusLabel.setStyle(
                "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;" +
                        "-fx-padding: 6 14; -fx-background-radius: 10; -fx-font-weight: bold;"
        );

        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().add("button");

        viewBtn.setOnAction(e -> {
            Stage popup = new Stage();

            VBox popupRoot = new VBox(18);
            popupRoot.setPadding(new Insets(25));
            popupRoot.getStyleClass().add("card");

            Label popupTitle = new Label("Invoice Details");
            popupTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

            GridPane details = new GridPane();
            details.setHgap(30);
            details.setVgap(14);

            addDetailRow(details, "Invoice ID", "INV-" + inv.getInvoiceId(), 0);
            addDetailRow(details, "Room Type", inv.getReservation().getRoom().getRoomType().getName(), 1);
            addDetailRow(details, "Check In", inv.getReservation().getCheckInDate().toString(), 2);
            addDetailRow(details, "Check Out", inv.getReservation().getCheckOutDate().toString(), 3);
            addDetailRow(details, "Payment Method", inv.getPaymentMethod().toString(), 4);
            addDetailRow(details, "Total Amount", "$" + String.format("%.2f", inv.getTotalAmount()), 5);
            addDetailRow(details, "Status", "Paid", 6);

            Button closeBtn = new Button("Close");
            closeBtn.getStyleClass().add("button");
            closeBtn.setOnAction(ev -> popup.close());

            popupRoot.getChildren().addAll(popupTitle, details, closeBtn);

            Scene popupScene = new Scene(popupRoot, 420, 400);
            popupScene.getStylesheets().add(
                    getClass().getResource("/style.css").toExternalForm()
            );

            popup.setScene(popupScene);
            popup.setTitle("Invoice #" + inv.getInvoiceId());
            popup.setResizable(false);
            popup.show();
        });

        right.getChildren().addAll(amount, statusLabel, viewBtn);

        card.getChildren().addAll(left, spacer, right);

        return card;
    }

    private void addDetailRow(GridPane grid, String label, String value, int row) {
        Label l = new Label(label);
        l.getStyleClass().add("section-title");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14; -fx-text-fill: #374151;");

        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private VBox createStatCard(String title, Label value) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-cursor: default;");

        Label t = new Label(title);
        t.getStyleClass().add("subtitle-label");

        value.setStyle("-fx-font-size: 30; -fx-font-weight: bold;");

        card.getChildren().addAll(t, value);

        return card;
    }

    private void updateStatistics() {
        ArrayList<Invoice> invoices = getGuestInvoices();

        int total = invoices.size();
        double totalAmount = 0;

        for (Invoice inv : invoices) {
            totalAmount += inv.getTotalAmount();
        }

        totalCount.setText(String.valueOf(total));
        totalPaidLabel.setText(String.valueOf(total));
        totalAmountLabel.setText("$" + String.format("%.2f", totalAmount));
    }

    private ArrayList<Invoice> getGuestInvoices() {
        ArrayList<Invoice> result = new ArrayList<>();
        for (Invoice inv : HotelDatabase.invoices) {
            if (inv.getReservation().getGuest().getUsername().equals(guest.getUsername())) {
                result.add(inv);
            }
        }
        return result;
    }
}