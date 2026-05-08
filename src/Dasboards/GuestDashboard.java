package Dasboards;

import app.SceneManager;
import database.HotelDatabase;
//import Dasboards.ViewRoomsDashboard;
import Dasboards.ReservationDashboard;
//import Dasboards.InvoicesDashboard;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.*;
import util.SidebarGuest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class GuestDashboard extends Application {

    private Guest guest;

    public GuestDashboard() {}

    public GuestDashboard(Guest guest) {
        this.guest = guest;
    }

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1400, 850);

        String cssPath = "/style.css";
        var resource = getClass().getResource(cssPath);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }

        root.setLeft(SidebarGuest.createSidebar("Dashboard"));

        VBox centerArea = new VBox();
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));
        centerArea.setSpacing(25);
        centerArea.setFillWidth(true);

        ScrollPane mainCenterScroll = new ScrollPane(centerArea);
        mainCenterScroll.setFitToWidth(true);
        mainCenterScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainCenterScroll.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent;"
        );

        root.setCenter(mainCenterScroll);

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        String username = guest != null ? guest.getUsername() : "Guest";
        Label welcomeLabel = new Label("Welcome back, " + username + "!");
        welcomeLabel.getStyleClass().add("title-label");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        VBox dateTimeBox = new VBox(2);

        Label dateLabel = new Label(
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        );
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label timeLabel = new Label(
                "Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
        timeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);
        headerBox.getChildren().addAll(welcomeLabel, headerSpacer, dateTimeBox);
        centerArea.getChildren().add(headerBox);

        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.getChildren().addAll(
                createStatCard("Current Balance", "$" + guest.getBalance(), "/wallet.png"),
                createStatCard("Active Reservations", String.valueOf(getActiveReservations().size()), "/calendar.png"),
                createStatCard("Total Invoices", String.valueOf(getInvoiceCnt()), "/invoice.png"),
                createStatCard("Loyalty Points", String.valueOf((int) calcLuckyPoints()), "/home.png")
        );
        centerArea.getChildren().add(statsBox);

        HBox bottomLayout = new HBox(25);
        bottomLayout.setAlignment(Pos.TOP_CENTER);

        VBox upcomingSection = new VBox(15);
        HBox.setHgrow(upcomingSection, Priority.ALWAYS);

        Label upcomingTitle = new Label("Upcoming Reservations");
        upcomingTitle.getStyleClass().add("section-title");

        upcomingSection.getChildren().addAll(upcomingTitle, createReservationList(upcomingSection));

        VBox quickActionsCard = new VBox(20);
        quickActionsCard.getStyleClass().add("card");
        quickActionsCard.setMinWidth(350);
        quickActionsCard.setPrefHeight(350);
        quickActionsCard.setStyle("-fx-cursor: default;");

        Label qaTitle = new Label("Quick Actions");
        qaTitle.getStyleClass().add("section-title");

        VBox actionsList = new VBox(12);
        actionsList.getChildren().addAll(
                createActionRow("/bed.png", "Browse Available Rooms", "Find your perfect stay"),
                createActionRow("/calendar.png", "Make a Reservation", "Book a new room"),
                createActionRow("/invoice.png", "View My Invoices", "Check your invoices")
        );

        quickActionsCard.getChildren().addAll(qaTitle, actionsList);
        bottomLayout.getChildren().addAll(upcomingSection, quickActionsCard);
        centerArea.getChildren().add(bottomLayout);

        VBox recentInvoicesSection = new VBox(15);

        HBox invoiceHeader = new HBox();
        Label riTitle = new Label("Recent Invoices");
        riTitle.getStyleClass().add("section-title");

        Region riSpacer = new Region();
        HBox.setHgrow(riSpacer, Priority.ALWAYS);

        Label viewAll = new Label("View All");
        viewAll.getStyleClass().add("view-all-link");
        viewAll.setOnMouseClicked(e -> SceneManager.switchToDashboard(new InvoicesDashboard(guest)));

        invoiceHeader.getChildren().addAll(riTitle, riSpacer, viewAll);

        GridPane invoiceTable = new GridPane();
        invoiceTable.getStyleClass().addAll("card", "default-cursor");
        invoiceTable.setPadding(new Insets(0));

        String[] headers = {"Invoice ID", "Room Type", "Stay Period", "Amount", "Method", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i]);
            h.setStyle(
                    "-fx-background-color: #0F172A; -fx-text-fill: white;" +
                            "-fx-padding: 12; -fx-font-weight: bold;"
            );
            h.setMaxWidth(Double.MAX_VALUE);
            invoiceTable.add(h, i, 0);

            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / headers.length);
            invoiceTable.getColumnConstraints().add(cc);
        }

        int rowIdx = 1;
        for (Invoice inv : HotelDatabase.invoices) {
            if (inv.getReservation().getGuest().getUsername().equals(guest.getUsername())) {
                invoiceTable.add(createTableCell("INV-" + inv.getInvoiceId()), 0, rowIdx);
                invoiceTable.add(createTableCell(inv.getReservation().getRoom().getRoomType().getName()), 1, rowIdx);
                invoiceTable.add(createTableCell(inv.getReservation().getCheckInDate().toString()), 2, rowIdx);
                invoiceTable.add(createTableCell("$" + inv.getTotalAmount()), 3, rowIdx);
                invoiceTable.add(createTableCell(inv.getPaymentMethod().toString()), 4, rowIdx);

                Label statusLabel = new Label("Paid");
                statusLabel.setStyle(
                        "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;" +
                                "-fx-background-radius: 10; -fx-padding: 6 12; -fx-font-weight: bold;"
                );
                invoiceTable.add(statusLabel, 5, rowIdx);
                rowIdx++;
            }
            if (rowIdx > 4) break;
        }

        recentInvoicesSection.getChildren().addAll(invoiceHeader, invoiceTable);
        centerArea.getChildren().add(recentInvoicesSection);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Guest Dashboard");
        stage.show();

        root.requestFocus();
    }

    private Label createTableCell(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-padding: 12; -fx-border-color: #F3F4F6; -fx-border-width: 0 0 1 0;");
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private VBox createStatCard(String title, String value, String iconPath) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("stat-card", "card");
        card.setPrefWidth(280);
        card.setMinHeight(120);
        card.setStyle("-fx-cursor: default;");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("small-label");

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);

        ImageView icon = new ImageView(
                new Image(getClass().getResourceAsStream(iconPath))
        );
        icon.setFitWidth(40);
        icon.setFitHeight(40);

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        content.getChildren().addAll(icon, lblValue);
        card.getChildren().addAll(lblTitle, content);

        return card;
    }

    public HBox createReservationCard(Reservation res, VBox upcomingSection) {
        HBox card = new HBox(20);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-cursor: default;");

        String roomType = res.getRoom().getRoomType().getName();
        String imgPath = "/" + roomType + ".jpg";
        if (roomType.equals("Double")) {
            imgPath = "/Double.jpg";
        }

        Image image;
        try {
            image = new Image(getClass().getResourceAsStream(imgPath));
        } catch (Exception e) {
            image = new Image(getClass().getResourceAsStream("/placeholder.jpg"));
        }

        ImageView roomImg = new ImageView(image);
        roomImg.setFitWidth(160);
        roomImg.setFitHeight(100);

        Rectangle clip = new Rectangle(160, 100);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        roomImg.setClip(clip);

        VBox details = new VBox(5);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label name = new Label(roomType + " Room");
        name.getStyleClass().add("section-title");

        Label date = new Label("📅 " + res.getCheckInDate() + " - " + res.getCheckOutDate());
        date.getStyleClass().add("subtitle-label");

        details.getChildren().addAll(name, date);

        VBox actions = new VBox(10);
        actions.setAlignment(Pos.TOP_RIGHT);

        Label status = new Label("Confirmed");
        status.setStyle(
                "-fx-background-color: #E9EEF5; -fx-text-fill: #1E3A5F;" +
                        "-fx-padding: 4 10; -fx-background-radius: 8; -fx-font-weight: bold;"
        );

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("danger-button");
        cancelBtn.setPrefWidth(100);

        cancelBtn.setOnAction(e -> {
            VBox confirmCard = new VBox(15);
            confirmCard.getStyleClass().add("confirmation-card");

            Label confirmTitle = new Label("Cancel Reservation?");
            confirmTitle.getStyleClass().add("confirmation-title");

            Label confirmText = new Label(
                    "Are you sure you want to cancel your reservation for "
                            + res.getRoom().getRoomType().getName() + " Room?"
            );
            confirmText.getStyleClass().add("confirmation-text");

            HBox confirmButtons = new HBox(10);
            confirmButtons.setAlignment(Pos.CENTER_RIGHT);

            Button keepBtn = new Button("Keep Reservation");
            keepBtn.getStyleClass().add("auth-button");

            Button confirmBtn = new Button("Confirm Cancel");
            confirmBtn.getStyleClass().add("danger-button");

            confirmButtons.getChildren().addAll(keepBtn, confirmBtn);
            confirmCard.getChildren().addAll(confirmTitle, confirmText, confirmButtons);

            if (!upcomingSection.getChildren().contains(confirmCard)) {
                upcomingSection.getChildren().add(1, confirmCard);
            }

            keepBtn.setOnAction(ev -> upcomingSection.getChildren().remove(confirmCard));

            confirmBtn.setOnAction(ev -> {
                res.setStatus(ReservationStatus.CANCELLED);
                upcomingSection.getChildren().remove(confirmCard);
                refreshReservations(upcomingSection);
            });
        });

        actions.getChildren().addAll(status, cancelBtn);
        card.getChildren().addAll(roomImg, details, actions);

        return card;
    }

    private HBox createActionRow(String iconPath, String title, String sub) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.getStyleClass().add("action-row");

        ImageView view = new ImageView(
                new Image(getClass().getResourceAsStream(iconPath))
        );
        view.setFitWidth(30);
        view.setFitHeight(30);

        VBox texts = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label s = new Label(sub);
        s.getStyleClass().add("small-label");

        texts.getChildren().addAll(t, s);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label(">");

        row.getChildren().addAll(view, texts, spacer, arrow);

        switch (title) {
            case "Browse Available Rooms" ->
                    row.setOnMouseClicked(e ->
                            SceneManager.switchToDashboard(new RoomBrowseView(guest))
                    );
            case "Make a Reservation" ->
                    row.setOnMouseClicked(e ->
                            SceneManager.switchToDashboard(new ReservationDashboard(guest))
                    );
            case "View My Invoices" ->
                    row.setOnMouseClicked(e ->
                            SceneManager.switchToDashboard(new InvoicesDashboard(guest))
                    );
        }

        return row;
    }

    private ArrayList<Reservation> getActiveReservations() {
        ArrayList<Reservation> result = new ArrayList<>();
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuest().getUsername().equals(guest.getUsername())
                    && r.getStatus() == ReservationStatus.RESERVED) {
                result.add(r);
            }
        }
        return result;
    }

    public int getInvoiceCnt() {
        int cnt = 0;
        for (Invoice i : HotelDatabase.invoices) {
            if (i.getReservation().getGuest() == guest) {
                cnt++;
            }
        }
        return cnt;
    }

    private double calcLuckyPoints() {
        double totalPaid = 0;
        for (Invoice i : HotelDatabase.invoices) {
            if (i.getReservation().getGuest() == guest) {
                totalPaid += i.getTotalAmount();
            }
        }
        return totalPaid / 10;
    }

    ScrollPane createReservationList(VBox upcomingSection) {
        VBox reservationList = new VBox(15);
        ArrayList<Reservation> reservations = getActiveReservations();

        if (reservations.isEmpty()) {
            Label noRes = new Label("No upcoming reservations found.");
            noRes.getStyleClass().add("subtitle-label");
            reservationList.getChildren().add(noRes);
        } else {
            for (Reservation res : reservations) {
                reservationList.getChildren().add(createReservationCard(res, upcomingSection));
            }
        }

        ScrollPane resScroll = new ScrollPane(reservationList);
        resScroll.setFitToWidth(true);
        resScroll.setPrefHeight(350);
        resScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        resScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return resScroll;
    }

    private void refreshReservations(VBox upcomingSection) {
        if (upcomingSection.getChildren().size() > 1) {
            upcomingSection.getChildren().remove(1, upcomingSection.getChildren().size());
        }
        upcomingSection.getChildren().add(createReservationList(upcomingSection));
    }
}