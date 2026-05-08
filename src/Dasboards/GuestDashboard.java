package Dasboards;

import database.HotelDatabase;
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
    public void start(Stage GuestDashboardStage) {
        HotelDatabase.initializeDummyData();
        if (this.guest == null) {
            this.guest = HotelDatabase.findGuest("kenzy");
        }

        if (!getActiveReservations().isEmpty()) {
            Reservation dummyRes = getActiveReservations().get(0);
            Invoice testInvoice = new Invoice(dummyRes, PaymentMethod.CREDIT_CARD);
            HotelDatabase.invoices.add(testInvoice);
        }

        BorderPane guestDashboardPane = new BorderPane();
        Scene guestDashboardScene = new Scene(guestDashboardPane, 1280, 800);

        String cssPath = "/style.css";
        var resource = getClass().getResource(cssPath);
        if (resource != null) {
            guestDashboardScene.getStylesheets().add(resource.toExternalForm());
        }

        // --- Left Sidebar ---
        VBox leftSwitcherText = new VBox();
        leftSwitcherText.getStyleClass().addAll("sidebar");
        leftSwitcherText.setSpacing(20);
        leftSwitcherText.setPadding(new Insets(20));
        leftSwitcherText.setStyle("-fx-background-color: #0F172A;");

        guestDashboardPane.setLeft(leftSwitcherText);
        String[] sidebarLabels = {"Dashboard", "View Rooms", "Reservations", "Profile"};
        String[] iconPaths = {"/home.png", "/bed.png", "/calendar.png", "/user2.png"};

        for (int i = 0; i < sidebarLabels.length; i++) {
            addIconText(leftSwitcherText, sidebarLabels[i], iconPaths[i]);
        }

        Region sidebarSpacer = new Region();
        VBox.setVgrow(sidebarSpacer, Priority.ALWAYS);
        leftSwitcherText.getChildren().add(sidebarSpacer);
        addIconText(leftSwitcherText, "Logout", "/exit.png");

        // Center Area
        VBox centerArea = new VBox();
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));
        centerArea.setSpacing(25);

        centerArea.setMaxWidth(1300);

        VBox centeringWrapper = new VBox(centerArea);
        centeringWrapper.setAlignment(Pos.TOP_CENTER);
        centeringWrapper.setStyle("-fx-background-color: #F8FAFC;"); // Match dashboard bg

        ScrollPane mainCenterScroll = new ScrollPane(centeringWrapper);
        mainCenterScroll.setFitToWidth(true);
        mainCenterScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        guestDashboardPane.setCenter(mainCenterScroll);

        // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label welcomeLabel = new Label("Welcome back, " + guest.getUsername() + "!");
        welcomeLabel.getStyleClass().add("title-label");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox topRightDetails = new HBox(15);
        topRightDetails.setAlignment(Pos.CENTER_RIGHT);

        ImageView bellIcon = new ImageView(new Image(getClass().getResourceAsStream("/notification.png")));
        bellIcon.setFitWidth(25);
        bellIcon.setFitHeight(25);

        VBox dateTimeBox = new VBox(2);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        Label dateLabel = new Label(LocalDate.now().format(dateFormatter));
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label timeLabel = new Label("Time: " + LocalDateTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);

        topRightDetails.getChildren().addAll(bellIcon, dateTimeBox);
        headerBox.getChildren().addAll(welcomeLabel, headerSpacer, topRightDetails);
        centerArea.getChildren().add(headerBox);

        // Stats Row
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getChildren().addAll(
                createStatCard("Current Balance", "$" + guest.getBalance(), "/wallet.png"),
                createStatCard("Active Reservations", String.valueOf(getActiveReservations().size()), "/calendar.png"),
                createStatCard("Total Invoices", String.valueOf(getInvoiceCnt()), "/invoice.png"),
                createStatCard("Loyalty Points", String.valueOf(calcLuckyPoints()), "/home.png")
        );
        centerArea.getChildren().add(statsBox);

        //  Bottom Layout
        HBox bottomLayout = new HBox(25);
        bottomLayout.setAlignment(Pos.TOP_CENTER);

        VBox upcomingSection = new VBox(15);
        HBox.setHgrow(upcomingSection, Priority.ALWAYS); // Let reservations take available space
        Label upcomingTitle = new Label("Upcoming Reservations");
        upcomingTitle.getStyleClass().add("section-title");

        upcomingSection.getChildren().addAll(upcomingTitle, createReservationList(upcomingSection));



        // Quick Actions
        VBox quickActionsCard = new VBox(20);
        quickActionsCard.getStyleClass().add("card");
        quickActionsCard.setMinWidth(350);
        quickActionsCard.setPrefHeight(350);
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

        // Recent Invoices
        VBox recentInvoicesSection = new VBox(15);
        HBox invoiceHeader = new HBox();
        Label riTitle = new Label("Recent Invoices");
        riTitle.getStyleClass().add("section-title");
        Region riSpacer = new Region();
        HBox.setHgrow(riSpacer, Priority.ALWAYS);
        Label viewAll = new Label("View All >");
        viewAll.setStyle("-fx-text-fill: #1E3A5F; -fx-cursor: hand; -fx-font-weight: bold;");
        invoiceHeader.getChildren().addAll(riTitle, riSpacer, viewAll);

        GridPane invoiceTable = new GridPane();
        invoiceTable.getStyleClass().add("card");
        invoiceTable.setPadding(new Insets(0));

        String[] headers = {"Invoice ID", "Room Type", "Stay Period", "Amount", "Method", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i]);
            h.setStyle("-fx-background-color: #0F172A; -fx-text-fill: white; -fx-padding: 12; -fx-font-weight: bold;");
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
                statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-padding: 10;");
                invoiceTable.add(statusLabel, 5, rowIdx);
                rowIdx++;
            }
            if (rowIdx > 4) break;
        }


        recentInvoicesSection.getChildren().addAll(invoiceHeader, invoiceTable);
        centerArea.getChildren().add(recentInvoicesSection);

        GuestDashboardStage.setScene(guestDashboardScene);
        GuestDashboardStage.setMaximized(true); // Full Screen
        GuestDashboardStage.setTitle("Guest Dashboard");
        GuestDashboardStage.show();

        // Clear initial focus highlight
        guestDashboardPane.requestFocus();
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
        card.setPrefSize(250, 120);
        card.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("small-label");

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);

        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        content.getChildren().addAll(icon, lblValue);
        card.getChildren().addAll(lblTitle, content);
        return card;
    }

    public HBox createReservationCard(Reservation res , VBox upcomingSection) {
        HBox card = new HBox(20);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);

        String roomType = res.getRoom().getRoomType().getName();
        String imgPath = "/" + roomType + ".jpg"; // Simplified matching
        if (roomType.equals("Double")) imgPath = "/Deluxe.jpg";

        ImageView roomImg = new ImageView(new Image(imgPath));
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
        status.setStyle("-fx-background-color: #E9EEF5; -fx-text-fill: #1E3A5F; -fx-padding: 4 10; -fx-background-radius: 8; -fx-font-weight: bold;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("danger-button");
        cancelBtn.setPrefWidth(100);

        cancelBtn.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cancel Reservation");
            alert.setHeaderText("Are you sure you want to cancel this booking?");
            alert.setContentText("Room: " + res.getRoom().getRoomType().getName());

            if (alert.showAndWait().get() == ButtonType.OK) {

                res.setStatus(ReservationStatus.CANCELLED);
                refreshReservations(upcomingSection);
                Pane parent = (Pane) card.getParent();
                if (parent != null) {
                    parent.getChildren().remove(card);
                }
                System.out.println("Reservation for " + res.getRoom().getRoomType().getName() + " cancelled.");
            }
        });

        actions.getChildren().addAll(status, cancelBtn);
        card.getChildren().addAll(roomImg, details, actions);
        return card;

    }

    private HBox createActionRow(String iconPath, String title, String sub) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 10; -fx-cursor: hand;");

        ImageView view = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
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
        return row;
    }

    private void addIconText(Pane pane, String string, String iconPath) {
        HBox h = new HBox(15);
        h.setAlignment(Pos.CENTER_LEFT);
        h.getStyleClass().add("sidebar-button");
        try {
            ImageView image = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            image.setFitWidth(22);
            image.setFitHeight(22);
            h.getChildren().add(image);
        } catch (Exception e) {}
        Label label = new Label(string);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        h.getChildren().add(label);
        pane.getChildren().add(h);
    }

    private ArrayList<Reservation> getActiveReservations() {
        ArrayList<Reservation> result = new ArrayList<>();
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuest().getUsername().equals(guest.getUsername()) && r.getStatus() == ReservationStatus.RESERVED) {
                result.add(r);
            }
        }
        return result;
    }

    private int getInvoiceCnt()
    {
        int cnt =0;
        for(Invoice i : HotelDatabase.invoices)
        {
         if(i.getReservation().getGuest() == guest)
         {
             cnt++;
         }
        }
        return cnt;
    }
    private double calcLuckyPoints()
    {
        double totalAmount=0;
        double luckyPoints;
        for(Invoice i : HotelDatabase.invoices)
        {
            if(i.getReservation().getGuest() == guest )
            {
                totalAmount+=i.getTotalAmount();
            }
        }
        luckyPoints=totalAmount/4;
        return luckyPoints;
    }
     ScrollPane createReservationList(VBox upcomingSection)
    {
        VBox reservationList = new VBox(15);
        ArrayList<Reservation> reservations = getActiveReservations();

        if (reservations.isEmpty()) {
            Label noRes = new Label("No upcoming reservations found.");
            noRes.getStyleClass().add("subtitle-label");
            reservationList.getChildren().add(noRes);
        } else {
            for (Reservation res : reservations) {
                reservationList.getChildren().add(createReservationCard(res,upcomingSection));
            }
        }
        ScrollPane resScroll = new ScrollPane(reservationList);
        resScroll.setFitToWidth(true);
        resScroll.setPrefHeight(350);
        resScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return resScroll;
    }
    private void refreshReservations(VBox upcomingSection) {
        if (upcomingSection.getChildren().size() > 1) {
            upcomingSection.getChildren().remove(1, upcomingSection.getChildren().size());
        }

        ScrollPane freshList = createReservationList(upcomingSection);
        upcomingSection.getChildren().add(freshList);

        System.out.println("UI Refreshed: Showing " + getActiveReservations().size() + " reservations.");
    }

}
