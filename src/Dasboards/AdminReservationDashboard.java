package Dasboards;

import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Reservation;
import model.ReservationStatus;
import util.SidebarAdmin;

import java.util.ArrayList;

public class AdminReservationDashboard extends Application {

    private Label confirmedCount;
    private Label pendingCount;
    private Label cancelledCount;
    private Label totalCount;
    private Runnable refresh;


    public AdminReservationDashboard() {}

    @Override
    public void start(Stage stage) {

        confirmedCount = new Label();
        pendingCount   = new Label();
        cancelledCount = new Label();
        totalCount     = new Label();

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        root.setLeft(SidebarAdmin.createSidebar("Reservations"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scroll);

        Label title = new Label("Reservations");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("View and manage all hotel reservations.");
        subtitle.getStyleClass().add("subtitle-label");

        HBox statsCards = new HBox(20);
        statsCards.getChildren().addAll(
                createStatCard("Confirmed", confirmedCount),
                createStatCard("Pending",   pendingCount),
                createStatCard("Cancelled", cancelledCount),
                createStatCard("Total",     totalCount)
        );

        updateStatistics();

        HBox filters = new HBox(15);
        filters.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by guest or room");
        searchField.setPrefWidth(260);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("All", "Reserved", "Pending", "Cancelled", "Completed");
        statusCombo.setValue("All");

        DatePicker startDate = new DatePicker();
        startDate.setPromptText("Check In From");

        DatePicker endDate = new DatePicker();
        endDate.setPromptText("Check Out To");

        Button clearBtn = new Button("Clear Filters");
        clearBtn.getStyleClass().add("secondary-button");

        filters.getChildren().addAll(searchField, statusCombo, startDate, endDate, clearBtn);

        VBox reservationContainer = new VBox(15);

         refresh = () -> {
            reservationContainer.getChildren().clear();

            for (Reservation res : getAllReservations()) {
                boolean matches = true;

                String search = searchField.getText().toLowerCase();
                if (!search.isBlank()
                        && !String.valueOf(res.getReservationId()).contains(search)
                        && !res.getGuest().getUsername().toLowerCase().contains(search)
                        && !res.getRoom().getRoomType().getName().toLowerCase().contains(search)) {
                    matches = false;
                }

                if (statusCombo.getValue() != null && !statusCombo.getValue().equals("All")) {
                    if (!res.getStatus().toString().equalsIgnoreCase(statusCombo.getValue())) {
                        matches = false;
                    }
                }

                if (startDate.getValue() != null
                        && res.getCheckInDate().isBefore(startDate.getValue())) {
                    matches = false;
                }

                if (endDate.getValue() != null
                        && res.getCheckOutDate().isAfter(endDate.getValue())) {
                    matches = false;
                }

                if (matches) {
                    reservationContainer.getChildren().add(
                            createReservationCard(res)
                    );
                }
            }

            if (reservationContainer.getChildren().isEmpty()) {
                Label empty = new Label("No reservations match your criteria.");
                empty.getStyleClass().add("subtitle-label");
                reservationContainer.getChildren().add(empty);
            }
        };

        refresh.run();

        searchField.textProperty().addListener((a, b, c) -> refresh.run());
        statusCombo.valueProperty().addListener((a, b, c) -> refresh.run());
        startDate.valueProperty().addListener((a, b, c) -> refresh.run());
        endDate.valueProperty().addListener((a, b, c) -> refresh.run());

        clearBtn.setOnAction(e -> {
            searchField.clear();
            statusCombo.setValue("All");
            startDate.setValue(null);
            endDate.setValue(null);
        });

        ScrollPane reservationScroll = new ScrollPane(reservationContainer);
        reservationScroll.setFitToWidth(true);
        reservationScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        reservationScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        centerArea.getChildren().addAll(title, subtitle, statsCards, filters, reservationScroll);

        stage.setScene(scene);
        stage.setTitle("Reservations");
        stage.setMaximized(true);
        stage.show();
    }

    private HBox createReservationCard(Reservation res) {

        HBox card = new HBox(30);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: default;");

        VBox left = new VBox(8);

        Label id = new Label("Reservation #" + res.getReservationId());
        id.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label guest = new Label("Guest: " + res.getGuest().getUsername());
        guest.getStyleClass().add("subtitle-label");

        Label room = new Label("Room: " + res.getRoom().getRoomType().getName()
                + "  (#" + res.getRoom().getRoomId() + ")");

        Label dates = new Label(
                "Check In: " + res.getCheckInDate()
                        + "  |  Check Out: " + res.getCheckOutDate()
        );

        left.getChildren().addAll(id, guest, room, dates);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label statusLabel = new Label(res.getStatus().toString());
        applyStatusStyle(statusLabel, res.getStatus());

        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("button");
        viewBtn.setOnAction(e -> openDetailDialog(res));

        right.getChildren().addAll(statusLabel, viewBtn);

        if (res.getStatus() == ReservationStatus.RESERVED
                || res.getStatus() == ReservationStatus.PENDING) {

            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("danger-button");
            cancelBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Cancel reservation #" + res.getReservationId() + "?",
                        ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText(null);
                confirm.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.YES) {
                        res.setStatus(ReservationStatus.CANCELLED);
                        updateStatistics();
                        refresh.run();
                    }
                });
            });

            right.getChildren().add(cancelBtn);
        }

        card.getChildren().addAll(left, spacer, right);

        return card;
    }

    private void openDetailDialog(Reservation res) {

        Stage popup = new Stage();
        popup.setTitle("Reservation #" + res.getReservationId());
        popup.setResizable(false);

        VBox root = new VBox(18);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("card");

        Label title = new Label("Reservation Details");
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setHgap(30);
        details.setVgap(14);

        addDetailRow(details, "Reservation ID", String.valueOf(res.getReservationId()), 0);
        addDetailRow(details, "Guest",          res.getGuest().getUsername(), 1);
        addDetailRow(details, "Room",           res.getRoom().getRoomType().getName()
                + " (#" + res.getRoom().getRoomId() + ")", 2);
        addDetailRow(details, "Check In",       res.getCheckInDate().toString(), 3);
        addDetailRow(details, "Check Out",      res.getCheckOutDate().toString(), 4);
        addDetailRow(details, "Status",         res.getStatus().toString(), 5);

        Label changeStatusLabel = new Label("Change Status:");
        changeStatusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        ComboBox<ReservationStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(ReservationStatus.values());
        statusCombo.setValue(res.getStatus());
        statusCombo.setPrefWidth(200);

        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().add("button");
        applyBtn.setOnAction(e -> {
            res.setStatus(statusCombo.getValue());
            updateStatistics();
            refresh.run();
            popup.close();
        });

        HBox statusRow = new HBox(12);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(statusCombo, applyBtn);

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("secondary-button");
        closeBtn.setOnAction(e -> popup.close());

        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.getChildren().add(closeBtn);

        root.getChildren().addAll(title, details, changeStatusLabel, statusRow, btnRow);

        Scene scene = new Scene(root, 460, 440);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        popup.setScene(scene);
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

    private void applyStatusStyle(Label label, ReservationStatus rs) {
        switch (rs) {
            case RESERVED -> label.setStyle(
                    "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;" +
                            "-fx-padding: 6 14; -fx-background-radius: 10; -fx-font-weight: bold;"
            );
            case PENDING -> label.setStyle(
                    "-fx-background-color: #FEF9C3; -fx-text-fill: #854D0E;" +
                            "-fx-padding: 6 14; -fx-background-radius: 10; -fx-font-weight: bold;"
            );
            case CANCELLED -> label.setStyle(
                    "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;" +
                            "-fx-padding: 6 14; -fx-background-radius: 10; -fx-font-weight: bold;"
            );
            case COMPLETED -> label.setStyle(
                    "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;" +
                            "-fx-padding: 6 14; -fx-background-radius: 10; -fx-font-weight: bold;"
            );
        }
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
        int confirmed = 0, pending = 0, cancelled = 0;
        int total = HotelDatabase.reservations.size();

        for (Reservation r : HotelDatabase.reservations) {
            if (r.getStatus() == ReservationStatus.RESERVED)  confirmed++;
            if (r.getStatus() == ReservationStatus.PENDING)   pending++;
            if (r.getStatus() == ReservationStatus.CANCELLED) cancelled++;
        }

        confirmedCount.setText(String.valueOf(confirmed));
        pendingCount.setText(String.valueOf(pending));
        cancelledCount.setText(String.valueOf(cancelled));
        totalCount.setText(String.valueOf(total));
    }

    private ArrayList<Reservation> getAllReservations() {
        return new ArrayList<>(HotelDatabase.reservations);
    }

    public static void main(String[] args) {
        launch(args);
    }
}