package Dasboards;

import app.SceneManager;
import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Guest;
import model.Reservation;
import model.ReservationStatus;
import util.SidebarGuest;

import java.util.ArrayList;

public class ReservationDashboard extends Application {

    private Guest guest;

    private Label upcomingCount;
    private Label pastCount;
    private Label cancelledCount;
    private Label totalCount;

    public ReservationDashboard() {}

    public ReservationDashboard(Guest guest) {
        this.guest = guest;
    }

    @Override
    public void start(Stage stage) {

        if (guest == null) {
            guest = HotelDatabase.findGuest("youssef");
        }

        upcomingCount = new Label();
        pastCount = new Label();
        cancelledCount = new Label();
        totalCount = new Label();

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        root.setLeft(SidebarGuest.createSidebar("Reservations"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scroll);

        Label title = new Label("My Reservations");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("View and manage all your reservations.");
        subtitle.getStyleClass().add("subtitle-label");

        HBox statsCards = new HBox(20);
        statsCards.getChildren().addAll(
                createStatCard("Upcoming", upcomingCount),
                createStatCard("Past", pastCount),
                createStatCard("Cancelled", cancelledCount),
                createStatCard("Total", totalCount)
        );

        updateStatistics();

        HBox filters = new HBox(15);

        TextField searchField = new TextField();
        searchField.setPromptText("Search reservation");
        searchField.setPrefWidth(280);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("All", "Reserved", "Pending", "Cancelled", "Completed");
        statusCombo.setValue("All");

        DatePicker startDate = new DatePicker();
        startDate.setPromptText("Check In");

        DatePicker endDate = new DatePicker();
        endDate.setPromptText("Check Out");

        Button clearBtn = new Button("Clear Filters");
        clearBtn.getStyleClass().add("secondary-button");

        filters.getChildren().addAll(searchField, statusCombo, startDate, endDate, clearBtn);

        VBox reservationContainer = new VBox(15);

        Runnable refreshReservations = () -> {
            reservationContainer.getChildren().clear();

            for (Reservation res : getGuestReservations()) {
                boolean matches = true;

                String search = searchField.getText().toLowerCase();
                if (!search.isBlank()
                        && !String.valueOf(res.getReservationId()).contains(search)
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
                            createReservationCard(res, reservationContainer)
                    );
                }
            }
        };

        refreshReservations.run();

        searchField.textProperty().addListener((a, b, c) -> refreshReservations.run());
        statusCombo.valueProperty().addListener((a, b, c) -> refreshReservations.run());
        startDate.valueProperty().addListener((a, b, c) -> refreshReservations.run());
        endDate.valueProperty().addListener((a, b, c) -> refreshReservations.run());

        clearBtn.setOnAction(e -> {
            searchField.clear();
            statusCombo.setValue("All");
            startDate.setValue(null);
            endDate.setValue(null);
            refreshReservations.run();
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

    public Parent createContent() {
        if (guest == null) {
            guest = HotelDatabase.findGuest("youssef");
        }

        upcomingCount = new Label();
        pastCount = new Label();
        cancelledCount = new Label();
        totalCount = new Label();

        BorderPane root = new BorderPane();
        root.setLeft(SidebarGuest.createSidebar("Reservations"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scroll);

        Label title = new Label("My Reservations");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("View and manage all your reservations.");
        subtitle.getStyleClass().add("subtitle-label");

        HBox statsCards = new HBox(20);
        statsCards.getChildren().addAll(
                createStatCard("Upcoming", upcomingCount),
                createStatCard("Past", pastCount),
                createStatCard("Cancelled", cancelledCount),
                createStatCard("Total", totalCount)
        );

        updateStatistics();

        HBox filters = new HBox(15);

        TextField searchField = new TextField();
        searchField.setPromptText("Search reservation");
        searchField.setPrefWidth(280);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("All", "Reserved", "Pending", "Cancelled", "Completed");
        statusCombo.setValue("All");

        DatePicker startDate = new DatePicker();
        startDate.setPromptText("Check In");

        DatePicker endDate = new DatePicker();
        endDate.setPromptText("Check Out");

        Button clearBtn = new Button("Clear Filters");
        clearBtn.getStyleClass().add("secondary-button");

        filters.getChildren().addAll(searchField, statusCombo, startDate, endDate, clearBtn);

        VBox reservationContainer = new VBox(15);

        Runnable refreshReservations = () -> {
            reservationContainer.getChildren().clear();
            for (Reservation res : getGuestReservations()) {
                boolean matches = true;

                String search = searchField.getText().toLowerCase();
                if (!search.isBlank()
                        && !String.valueOf(res.getReservationId()).contains(search)
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
                            createReservationCard(res, reservationContainer)
                    );
                }
            }
        };

        refreshReservations.run();

        searchField.textProperty().addListener((a, b, c) -> refreshReservations.run());
        statusCombo.valueProperty().addListener((a, b, c) -> refreshReservations.run());
        startDate.valueProperty().addListener((a, b, c) -> refreshReservations.run());
        endDate.valueProperty().addListener((a, b, c) -> refreshReservations.run());

        clearBtn.setOnAction(e -> {
            searchField.clear();
            statusCombo.setValue("All");
            startDate.setValue(null);
            endDate.setValue(null);
            refreshReservations.run();
        });

        ScrollPane reservationScroll = new ScrollPane(reservationContainer);
        reservationScroll.setFitToWidth(true);
        reservationScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        reservationScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        centerArea.getChildren().addAll(title, subtitle, statsCards, filters, reservationScroll);

        return root;
    }

    private HBox createReservationCard(Reservation res, VBox reservationContainer) {
        HBox card = new HBox(30);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: default;");

        VBox left = new VBox(8);

        Label id = new Label("Reservation #" + res.getReservationId());
        id.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label room = new Label("Room: " + res.getRoom().getRoomType().getName());

        Label dates = new Label(
                "Check In: " + res.getCheckInDate()
                        + " | Check Out: " + res.getCheckOutDate()
        );

        left.getChildren().addAll(id, room, dates);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label status = new Label(res.getStatus().toString());
        applyStatusStyle(status, res.getStatus());

        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("button");

        viewBtn.setOnAction(e -> {
            Stage popup = new Stage();

            VBox popupRoot = new VBox(18);
            popupRoot.setPadding(new Insets(25));
            popupRoot.getStyleClass().add("card");

            Label popupTitle = new Label("Reservation Details");
            popupTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

            Label details = new Label(
                    "Reservation ID: " + res.getReservationId()
                            + "\n\nRoom: " + res.getRoom().getRoomType().getName()
                            + "\n\nCheck In: " + res.getCheckInDate()
                            + "\n\nCheck Out: " + res.getCheckOutDate()
                            + "\n\nStatus: " + res.getStatus()
            );

            Button closeBtn = new Button("Close");
            closeBtn.getStyleClass().add("button");
            closeBtn.setOnAction(ev -> popup.close());

            popupRoot.getChildren().addAll(popupTitle, details, closeBtn);

            Scene popupScene = new Scene(popupRoot, 400, 320);
            popupScene.getStylesheets().add(
                    getClass().getResource("/style.css").toExternalForm()
            );

            popup.setScene(popupScene);
            popup.show();
        });

        right.getChildren().addAll(status, viewBtn);

        if (res.getStatus() == ReservationStatus.RESERVED
                || res.getStatus() == ReservationStatus.PENDING) {

            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("danger-button");

            cancelBtn.setOnAction(e -> {
                Stage popup = new Stage();

                VBox popupRoot = new VBox(18);
                popupRoot.setPadding(new Insets(25));
                popupRoot.getStyleClass().add("card");

                Label popupTitle = new Label("Cancel Reservation?");
                popupTitle.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

                Label text = new Label("This reservation will be cancelled.");

                HBox buttons = new HBox(10);
                buttons.setAlignment(Pos.CENTER_RIGHT);

                Button noBtn = new Button("Keep");
                noBtn.getStyleClass().add("secondary-button");

                Button yesBtn = new Button("Cancel Reservation");
                yesBtn.getStyleClass().add("danger-button");

                buttons.getChildren().addAll(noBtn, yesBtn);
                popupRoot.getChildren().addAll(popupTitle, text, buttons);

                Scene popupScene = new Scene(popupRoot, 360, 180);
                popupScene.getStylesheets().add(
                        getClass().getResource("/style.css").toExternalForm()
                );

                popup.setScene(popupScene);
                popup.show();

                noBtn.setOnAction(ev -> popup.close());

                yesBtn.setOnAction(ev -> {
                    res.setStatus(ReservationStatus.CANCELLED);
                    applyStatusStyle(status, ReservationStatus.CANCELLED);
                    right.getChildren().remove(cancelBtn);
                    updateStatistics();
                    popup.close();
                });
            });

            right.getChildren().add(cancelBtn);
        }

        card.getChildren().addAll(left, spacer, right);

        return card;
    }

    private void applyStatusStyle(Label status, ReservationStatus rs) {
        switch (rs) {
            case RESERVED -> status.setStyle(
                    "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;" +
                            "-fx-padding: 6 14 6 14; -fx-background-radius: 10; -fx-font-weight: bold;"
            );
            case PENDING -> status.setStyle(
                    "-fx-background-color: #FEF9C3; -fx-text-fill: #854D0E;" +
                            "-fx-padding: 6 14 6 14; -fx-background-radius: 10; -fx-font-weight: bold;"
            );
            case CANCELLED -> status.setStyle(
                    "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;" +
                            "-fx-padding: 6 14 6 14; -fx-background-radius: 10; -fx-font-weight: bold;"
            );
            case COMPLETED -> status.setStyle(
                    "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;" +
                            "-fx-padding: 6 14 6 14; -fx-background-radius: 10; -fx-font-weight: bold;"
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
        int upcoming = 0;
        int past = 0;
        int cancelled = 0;
        int total = getGuestReservations().size();

        for (Reservation r : getGuestReservations()) {
            if (r.getStatus() == ReservationStatus.RESERVED) upcoming++;
            if (r.getStatus() == ReservationStatus.COMPLETED) past++;
            if (r.getStatus() == ReservationStatus.CANCELLED) cancelled++;
        }

        upcomingCount.setText(String.valueOf(upcoming));
        pastCount.setText(String.valueOf(past));
        cancelledCount.setText(String.valueOf(cancelled));
        totalCount.setText(String.valueOf(total));
    }

    private ArrayList<Reservation> getGuestReservations() {
        ArrayList<Reservation> result = new ArrayList<>();
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuest().getUsername().equals(guest.getUsername())) {
                result.add(r);
            }
        }
        return result;
    }
}