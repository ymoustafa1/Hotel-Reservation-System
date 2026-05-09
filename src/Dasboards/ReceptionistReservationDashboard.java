package Dasboards;

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
import model.*;
import util.SidebarReceptionist;

public class ReceptionistReservationDashboard extends Application {

    private TableView<Reservation> table;
    private Receptionist receptionist;

    public ReceptionistReservationDashboard() {}

    @Override
    public void start(Stage stage) {
        receptionist = (Receptionist) SessionManager.getCurrentUser();

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.setLeft(SidebarReceptionist.createSidebar("Reservations"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);

        VBox pageHeader = new VBox(4);
        Label pageTitle = new Label("Reservations");
        pageTitle.getStyleClass().add("title-label");
        Label pageSub = new Label("View, accept, check-in, and check-out reservations.");
        pageSub.getStyleClass().add("subtitle-label");
        pageHeader.getChildren().addAll(pageTitle, pageSub);

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "PENDING", "RESERVED", "COMPLETED", "CANCELLED");
        statusFilter.setValue("All");
        statusFilter.setPrefWidth(150);

        TextField searchField = new TextField();
        searchField.setPromptText("Search guest name...");
        searchField.setPrefWidth(220);

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("secondary-button");

        filterBar.getChildren().addAll(
                new Label("Status:"), statusFilter,
                new Label("Search:"), searchField,
                clearBtn
        );

        VBox tableCard = new VBox(0);
        tableCard.getStyleClass().add("card");
        tableCard.setPadding(new Insets(0));

        HBox cardHeader = new HBox();
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(18, 20, 18, 20));

        Label cardTitle = new Label("All Reservations");
        cardTitle.getStyleClass().add("section-title");
        cardHeader.getChildren().add(cardTitle);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(560);

        TableColumn<Reservation, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getReservationId())));

        TableColumn<Reservation, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getGuest().getUsername()));

        TableColumn<Reservation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(c -> {
            Room r = c.getValue().getRoom();
            return new SimpleStringProperty(r != null ? "#" + r.getRoomId() + " — " + r.getRoomType().getName() : "—");
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

        TableColumn<Reservation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                Reservation res = getTableView().getItems().get(getIndex());
                HBox actions = new HBox(8);
                actions.setAlignment(Pos.CENTER_LEFT);

                if (res.getStatus() == ReservationStatus.PENDING) {
                    Button acceptBtn = actionBtn("✔ Check-in", "#166534", "#DCFCE7");
                    acceptBtn.setOnAction(e -> {
                        try {
                            receptionist.checkIn(res);
                            refreshTable(statusFilter.getValue(), searchField.getText());
                        } catch (Exception ex) {
                            showAlert("Error", ex.getMessage());
                        }
                    });
                    actions.getChildren().add(acceptBtn);
                }

                if (res.getStatus() == ReservationStatus.RESERVED) {
                    Button checkOutBtn = actionBtn("✖ Check-out", "#991B1B", "#FEE2E2");
                    checkOutBtn.setOnAction(e -> {
                        try {
                            receptionist.checkOut(res);
                            refreshTable(statusFilter.getValue(), searchField.getText());
                        } catch (Exception ex) {
                            showAlert("Error", ex.getMessage());
                        }
                    });
                    actions.getChildren().add(checkOutBtn);
                }

                Button detailBtn = actionBtn("👁 Detail", "#1D4ED8", "#DBEAFE");
                detailBtn.setOnAction(e -> openDetailDialog(res));
                actions.getChildren().add(detailBtn);

                setGraphic(actions);
            }
        });

        table.getColumns().addAll(idCol, guestCol, roomCol, checkInCol, checkOutCol, statusCol, actionsCol);
        refreshTable("All", "");

        Runnable applyFilter = () -> refreshTable(statusFilter.getValue(), searchField.getText());
        statusFilter.valueProperty().addListener((a, b, c) -> applyFilter.run());
        searchField.textProperty().addListener((a, b, c) -> applyFilter.run());
        clearBtn.setOnAction(e -> { statusFilter.setValue("All"); searchField.clear(); });

        tableCard.getChildren().addAll(cardHeader, new Separator(), table);
        centerArea.getChildren().addAll(pageHeader, filterBar, tableCard);

        stage.setScene(scene);
        stage.setTitle("Reservations");
        stage.setMaximized(true);
        stage.show();
    }

    private void refreshTable(String statusFilter, String search) {
        table.getItems().clear();
        for (Reservation r : HotelDatabase.reservations) {
            if (!statusFilter.equals("All") && !r.getStatus().toString().equalsIgnoreCase(statusFilter))
                continue;
            if (search != null && !search.isBlank()
                    && !r.getGuest().getUsername().toLowerCase().contains(search.toLowerCase()))
                continue;
            table.getItems().add(r);
        }
    }

    private void openDetailDialog(Reservation res) {
        Stage dialog = new Stage();
        dialog.setTitle("Reservation #" + res.getReservationId());
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("card");

        Label title = new Label("Reservation #" + res.getReservationId() + " — " + res.getGuest().getUsername());
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setHgap(30); details.setVgap(12);

        Room r = res.getRoom();
        addRow(details, "Guest",      res.getGuest().getUsername(), 0);
        addRow(details, "Room",       r != null ? "#" + r.getRoomId() + " — " + r.getRoomType().getName() : "—", 1);
        addRow(details, "Check-in",   String.valueOf(res.getCheckInDate()), 2);
        addRow(details, "Check-out",  String.valueOf(res.getCheckOutDate()), 3);
        addRow(details, "Status",     res.getStatus().toString(), 4);

        StringBuilder amenities = new StringBuilder();
        if (r != null) {
            for (int i = 0; i < r.getAmenities().size(); i++) {
                amenities.append(r.getAmenities().get(i).getName());
                if (i < r.getAmenities().size() - 1) amenities.append(", ");
            }
        }
        addRow(details, "Room Amenities", amenities.length() > 0 ? amenities.toString() : "None", 5);

        StringBuilder extras = new StringBuilder();
        for (int i = 0; i < res.getExtraAmenities().size(); i++) {
            extras.append(res.getExtraAmenities().get(i).getName());
            if (i < res.getExtraAmenities().size() - 1) extras.append(", ");
        }
        addRow(details, "Extra Amenities", extras.length() > 0 ? extras.toString() : "None", 6);

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("button");
        closeBtn.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox();
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.getChildren().add(closeBtn);

        root.getChildren().addAll(title, new Separator(), details, btnRow);

        Scene scene = new Scene(root, 480, 380);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private void addRow(GridPane grid, String label, String value, int row) {
        Label l = new Label(label);
        l.getStyleClass().add("section-title");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14; -fx-text-fill: #374151;");
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private Button actionBtn(String text, String color, String bg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + color + ";" +
                "-fx-background-radius:6;-fx-font-size:12;-fx-cursor:hand;" +
                "-fx-padding:4 10;-fx-border-color:" + color + ";" +
                "-fx-border-radius:6;-fx-border-width:1;");
        return b;
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
            case "reserved"  -> "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;";
            case "pending"   -> "-fx-background-color: #FEF9C3; -fx-text-fill: #854D0E;";
            case "cancelled" -> "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;";
            case "completed" -> "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;";
            default          -> "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;";
        };
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}