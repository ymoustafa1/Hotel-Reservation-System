package Dasboards;

import app.SessionManager;
import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Guest;
import model.Receptionist;
import model.Reservation;
import model.ReservationStatus;
import util.SidebarReceptionist;

public class ReceptionistGuestSearchView extends Application {

    public ReceptionistGuestSearchView() {}

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.setLeft(SidebarReceptionist.createSidebar("Find Guest"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);

        // ── Page header ──────────────────────────────────────────────────
        VBox pageHeader = new VBox(4);
        Label pageTitle = new Label("Find Guest");
        pageTitle.getStyleClass().add("title-label");
        Label pageSub = new Label("Search for a registered guest by username.");
        pageSub.getStyleClass().add("subtitle-label");
        pageHeader.getChildren().addAll(pageTitle, pageSub);

        // ── Search card ──────────────────────────────────────────────────
        VBox searchCard = new VBox(16);
        searchCard.getStyleClass().add("card");
        searchCard.setPadding(new Insets(24));
        searchCard.setMaxWidth(600);

        Label searchTitle = new Label("Search Guest");
        searchTitle.getStyleClass().add("section-title");

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter guest username...");
        usernameField.setPrefWidth(320);
        HBox.setHgrow(usernameField, Priority.ALWAYS);

        Button searchBtn = new Button("🔍  Search");
        searchBtn.getStyleClass().add("button");

        searchRow.getChildren().addAll(usernameField, searchBtn);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 13;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        searchCard.getChildren().addAll(searchTitle, searchRow, errorLabel);

        // ── Result card (shown after search) ────────────────────────────
        VBox resultCard = new VBox(16);
        resultCard.getStyleClass().add("card");
        resultCard.setPadding(new Insets(24));
        resultCard.setVisible(false);
        resultCard.setManaged(false);

        Label resultTitle = new Label("Guest Information");
        resultTitle.getStyleClass().add("section-title");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(40);
        infoGrid.setVgap(12);

        // placeholders — filled on search
        Label[] values = new Label[6];
        String[] fields = {"Username", "Balance", "Date of Birth", "Address", "Gender", "Active Reservations"};
        for (int i = 0; i < fields.length; i++) {
            Label lbl = new Label(fields[i]);
            lbl.getStyleClass().add("section-title");
            values[i] = new Label("—");
            values[i].setStyle("-fx-font-size: 14; -fx-text-fill: #374151;");
            infoGrid.add(lbl,      0, i);
            infoGrid.add(values[i], 1, i);
        }

        // Reservation history sub-section
        Label resHistoryTitle = new Label("Reservation History");
        resHistoryTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 8 0 4 0;");

        VBox resHistoryBox = new VBox(8);

        resultCard.getChildren().addAll(resultTitle, new Separator(), infoGrid,
                new Separator(), resHistoryTitle, resHistoryBox);

        // ── Search action ────────────────────────────────────────────────
        Receptionist receptionist = (Receptionist) SessionManager.getCurrentUser();

        Runnable doSearch = () -> {
            String username = usernameField.getText().trim();
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            resultCard.setVisible(false);
            resultCard.setManaged(false);

            if (username.isEmpty()) {
                errorLabel.setText("Please enter a username.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            Guest g = receptionist.findGuest(username);
            if (g == null) {
                errorLabel.setText("No guest found with username: " + username);
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            // fill info
            values[0].setText(g.getUsername());
            values[1].setText("$" + String.format("%.2f", g.getBalance()));
            values[2].setText(g.getDateOfBirth() != null ? g.getDateOfBirth().toString() : "—");
            values[3].setText(g.getAddress() != null ? g.getAddress() : "—");
            values[4].setText(g.getGender() != null ? g.getGender().toString() : "—");

            long activeCount = HotelDatabase.reservations.stream()
                    .filter(r -> r.getGuest().getUsername().equals(g.getUsername())
                            && r.getStatus() == ReservationStatus.RESERVED)
                    .count();
            values[5].setText(String.valueOf(activeCount));

            // reservation history chips
            resHistoryBox.getChildren().clear();
            boolean hasAny = false;
            for (Reservation r : HotelDatabase.reservations) {
                if (!r.getGuest().getUsername().equals(g.getUsername())) continue;
                hasAny = true;

                HBox chip = new HBox(16);
                chip.setAlignment(Pos.CENTER_LEFT);
                chip.setPadding(new Insets(10, 16, 10, 16));
                chip.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 8;" +
                        "-fx-border-color: #E5E7EB; -fx-border-radius: 8;");

                Label resId = new Label("#" + r.getReservationId());
                resId.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

                String roomInfo = r.getRoom() != null
                        ? "Room #" + r.getRoom().getRoomId() + " — " + r.getRoom().getRoomType().getName()
                        : "No room";
                Label roomLbl = new Label(roomInfo);
                roomLbl.setStyle("-fx-font-size: 13; -fx-text-fill: #374151;");

                Label dates = new Label(r.getCheckInDate() + " → " + r.getCheckOutDate());
                dates.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");

                Label statusBadge = new Label(r.getStatus().toString());
                statusBadge.setPadding(new Insets(2, 8, 2, 8));
                statusBadge.setStyle("-fx-background-radius: 99; -fx-font-size: 11; -fx-font-weight: bold;"
                        + getStatusStyle(r.getStatus().toString()));

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                chip.getChildren().addAll(resId, roomLbl, dates, sp, statusBadge);
                resHistoryBox.getChildren().add(chip);
            }

            if (!hasAny) {
                Label noRes = new Label("No reservations found for this guest.");
                noRes.setStyle("-fx-text-fill: gray; -fx-font-size: 13;");
                resHistoryBox.getChildren().add(noRes);
            }

            resultCard.setVisible(true);
            resultCard.setManaged(true);
        };

        searchBtn.setOnAction(e -> doSearch.run());
        usernameField.setOnAction(e -> doSearch.run());

        centerArea.getChildren().addAll(pageHeader, searchCard, resultCard);

        stage.setScene(scene);
        stage.setTitle("Find Guest");
        stage.setMaximized(true);
        stage.show();
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

    public static void main(String[] args) { launch(args); }
}