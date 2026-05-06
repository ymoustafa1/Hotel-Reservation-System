package Dasboards;

import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
        //testing the invoice table
        Reservation dummyRes = getActiveReservations().get(0); // Get her dummy reservation
        Invoice testInvoice = new Invoice(dummyRes, PaymentMethod.CREDIT_CARD);
        HotelDatabase.invoices.add(testInvoice); // Manually inject it to test the table

        BorderPane guestDashboardPane = new BorderPane();
        Scene guestDashboardScene = new Scene(guestDashboardPane, 1200, 700);

        String cssPath = "/style.css";
        var resource = getClass().getResource(cssPath);
        if (resource != null) {
            guestDashboardScene.getStylesheets().add(resource.toExternalForm());
        }

        // Left Sidebar
        VBox leftSwitcherText = new VBox();
        leftSwitcherText.getStyleClass().addAll("sidebar");
        leftSwitcherText.setSpacing(20);
        leftSwitcherText.setPadding(new Insets(20));
        leftSwitcherText.setStyle("-fx-background-color: #0F172A;");

        guestDashboardPane.setLeft(leftSwitcherText);
        String[] sidebarLabels = {"Dashboard", "View Rooms", "Reservations", "Settings"};
        String[] iconPaths = {"/home.png", "/bed.png", "/calendar.png", "/setting.png"};

        for (int i = 0; i < sidebarLabels.length; i++) {
            addIconText(leftSwitcherText, sidebarLabels[i], iconPaths[i]);
        }

        // Center Area (Main Content)
        VBox centerArea = new VBox();
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(25));
        centerArea.setSpacing(20);

        ScrollPane mainCenterScroll = new ScrollPane(centerArea);
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

        ImageView bellIcon = new ImageView(new Image(getClass().getResourceAsStream("/home.png"))); // Replace with bell icon if available
        bellIcon.setFitWidth(25);
        bellIcon.setFitHeight(25);

        VBox dateTimeBox = new VBox(2);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        Label dateLabel = new Label(LocalDate.now().format(dateFormatter));
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label timeLabel = new Label("Time: "+ LocalDateTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);

        topRightDetails.getChildren().addAll(bellIcon, dateTimeBox);

        headerBox.getChildren().addAll(welcomeLabel, headerSpacer, topRightDetails);
        centerArea.getChildren().add(headerBox);

        HBox statsBox = new HBox(20);
        statsBox.getChildren().addAll(
                createStatCard("Current Balance", "$" + guest.getBalance(), "/wallet.png"),
                createStatCard("Active Reservations", String.valueOf(getActiveReservations().size()), "/calendar.png"),
                createStatCard("Total Invoices", "5", "/invoice.jpg"),
                createStatCard("Loyalty Points", "1,240", "/home.png") // Added Loyalty Points
        );
        centerArea.getChildren().add(statsBox);

        // --- Bottom Layout (Reservations + Quick Actions) ---
        HBox bottomLayout = new HBox(25);

        // Upcoming Reservations Container
        VBox upcomingSection = new VBox(15);
        upcomingSection.setPrefWidth(600);
        Label upcomingTitle = new Label("Upcoming Reservations");
        upcomingTitle.getStyleClass().add("section-title");

        VBox reservationList = new VBox(15);
        ArrayList<Reservation> reservations = getActiveReservations();

        if (reservations.isEmpty()) {
            Label noRes = new Label("No upcoming reservations found.");
            noRes.getStyleClass().add("subtitle-label");
            reservationList.getChildren().add(noRes);
        } else {
            for (Reservation res : reservations) {
                reservationList.getChildren().add(createReservationCard(res));
            }
        }

        ScrollPane scrollPane = new ScrollPane(reservationList);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300); // Adjusted to make room for table

        upcomingSection.getChildren().addAll(upcomingTitle, scrollPane);

        // Quick Actions Container
        VBox quickActionsCard = new VBox(20);
        quickActionsCard.getStyleClass().add("card");
        quickActionsCard.setPrefSize(320,200);
        quickActionsCard.setMinWidth(320);
        quickActionsCard.setPrefHeight(300);
        quickActionsCard.setMaxHeight(300);
        Label qaTitle = new Label("Quick Actions");
        qaTitle.getStyleClass().add("section-title");

        VBox actionsList = new VBox(12);
        actionsList.getChildren().addAll(
                createActionRow("/bed.png", "Browse Available Rooms", "Find your perfect stay"),
                createActionRow("/calendar.png", "Make a Reservation", "Book a new room"),
                createActionRow("/invoice.jpg", "View My Invoices", "Check your invoices")
        );

        quickActionsCard.getChildren().addAll(qaTitle, actionsList);

        bottomLayout.getChildren().addAll(upcomingSection, quickActionsCard);
        centerArea.getChildren().add(bottomLayout);

        //  Recent Invoices Section
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

        // Table Header
        String[] headers = {"Invoice ID", "Room Type", "Stay Period", "Amount", "Method", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i]);
            h.setStyle("-fx-background-color: #0F172A; -fx-text-fill: white; -fx-padding: 10; -fx-font-weight: bold;");
            h.setMaxWidth(Double.MAX_VALUE);
            invoiceTable.add(h, i, 0);
        }

        // Add Data Rows from HotelDatabase.invoices
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
            if (rowIdx > 4) break; // Limit to 3-4 recent rows
        }

        recentInvoicesSection.getChildren().addAll(invoiceHeader, invoiceTable);
        centerArea.getChildren().add(recentInvoicesSection);

        GuestDashboardStage.setScene(guestDashboardScene);
        GuestDashboardStage.setResizable(false);
        GuestDashboardStage.setTitle("Guest Dashboard");
        GuestDashboardStage.show();
    }

    private Label createTableCell(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-padding: 10; -fx-border-color: #F3F4F6; -fx-border-width: 0 0 1 0;");
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private VBox createStatCard(String title, String value, String iconPath) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("stat-card", "card");
        card.setPrefSize(210, 110);
        card.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("small-label");

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);


        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        content.getChildren().addAll(icon, lblValue);
        card.getChildren().addAll(lblTitle, content);
        return card;
    }

    public HBox createReservationCard(Reservation res) {
        HBox card = new HBox(20);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        ImageView roomImg = new ImageView();

        // Image
        String roomType = res.getRoom().getRoomType().getName();
        if (roomType.equals("Double")) {

            Image Double = new Image("/Deluxe.jpg");

            ImageView Doubleview = new ImageView(Double);

            Doubleview.setPreserveRatio(true);

            Doubleview.setFitWidth(100);

            Doubleview.setFitHeight(100);

            Doubleview.setFitWidth(140);
            Doubleview.setFitHeight(90);
            Rectangle clip = new Rectangle(140, 90);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            Doubleview.setClip(clip);
            roomImg = Doubleview;
        }

        if (roomType.equals("Single")) {

            Image Single = new Image("/Single.jpg");

            ImageView Singleview = new ImageView(Single);

            Singleview.setPreserveRatio(true);

            Singleview.setFitWidth(100);

            Singleview.setFitHeight(100);
            Rectangle clip = new Rectangle(140, 90);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            Singleview.setClip(clip);
            roomImg= Singleview;
        }

        if (roomType.equals("Suite")) {

            Image Suite = new Image("/Suite.jpg");

            ImageView Suiteview = new ImageView(Suite);

            Suiteview.setPreserveRatio(true);

            Suiteview.setFitWidth(100);

            Suiteview.setFitHeight(100);

            Suiteview.setFitWidth(140);
            Suiteview.setFitHeight(90);
            Rectangle clip = new Rectangle(140, 90);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            Suiteview.setClip(clip);
            roomImg=Suiteview;
        }




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

        actions.getChildren().addAll(status, cancelBtn);
        card.getChildren().addAll(roomImg, details, actions);
        return card;
    }

    private HBox createActionRow(String iconPath, String title, String sub) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 10; -fx-cursor: hand;");

        ImageView view = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        view.setFitWidth(30);
        view.setFitHeight(30);
        row.getChildren().add(view);


        VBox texts = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label s = new Label(sub);
        s.getStyleClass().add("small-label");
        texts.getChildren().addAll(t, s);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label arrow = new Label(">");
        row.getChildren().addAll(texts, spacer, arrow);
        return row;
    }

    void addIconText(Pane pane, String string, String iconPath) {
        HBox h = new HBox(15);
        h.setAlignment(Pos.CENTER_LEFT);
        h.getStyleClass().add("sidebar-button");
        try {
            ImageView image = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            image.setFitWidth(20);
            image.setFitHeight(20);
            h.getChildren().add(image);
        } catch (Exception e) {}
        Label label = new Label(string);
        label.setStyle("-fx-text-fill: white;");
        h.getChildren().add(label);
        pane.getChildren().add(h);
    }

    public ArrayList<Reservation> getActiveReservations() {
        ArrayList<Reservation> result = new ArrayList<>();
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuest().getUsername().equals(guest.getUsername()) && r.getStatus() == ReservationStatus.RESERVED) {
                result.add(r);
            }
        }
        return result;
    }
}
//    private void displayReservations(List<Reservation> res)
//    {
//        int i=4;
//        int j=0;
//        int cnt=0;
//        for(Reservation r : res )
//        {
//            guestDashboardPane.add(new Label(cnt+". "+r.toString()),j,i);
//            i++;
//            cnt++;
//        }
//    }
//    private void updateBalance(double balance)
//    {
////        guest.updateBalance(balance); // This updates the logic/data
////        balanceLabel.setText("Balance: " + guest.getBalance()); // This updates the UI
//    }









//Image profileIcon = new Image(getClass().getResourceAsStream("/profile_icon.jpg"));
//ImageView ProfileIcon = new ImageView(profileIcon);
//        ProfileIcon.setPreserveRatio(true);
//        ProfileIcon.setFitHeight(250);
//        ProfileIcon.setFitWidth(250);
//TextField UpdateBalance = new TextField();
//Button Update = new Button("Update");
//        guestDashboardPane.add(new Label("Enter Amount : "),2,2);
//        guestDashboardPane.add(UpdateBalance,3,2);
//        guestDashboardPane.add(Update,4,2);
//
//            Update.setOnAction(e->
//        {
//        try
//        {
//updateBalance(Double.parseDouble(UpdateBalance.getText()));
//        }
//        catch (NumberFormatException ex)
//        {
//        System.out.println(" Wrong Format NumberFormatException occurred");
//                }
//                        }
//                        );
//
//
//                        guestDashboardPane.setPadding(new Insets(5));
//        guestDashboardPane.setVgap(10);
//        guestDashboardPane.setHgap(10);
//        guestDashboardPane.add(ProfileIcon,0,0);

//        balanceLabel.setText("Balance: " + guest.getBalance());
//        guestDashboardPane.add(balanceLabel, 3, 0);
//Button loadGuestData = new Button("Load Data");
//        guestDashboardPane.add(loadGuestData,0,2);
//        loadGuestData.setOnAction(e->
//        { loadGuestData(this.guest);
//            guestDashboardPane.getChildren().remove(loadGuestData);
//        }
//                );

//String avatarPath = (guest.getGender() == Gender.MALE) ? "/man.png" : "/woman.png";
//Image avatarImg = new Image(getClass().getResourceAsStream(avatarPath));
//ImageView avatarView = new ImageView(avatarImg);
//            avatarView.setFitHeight(120);
//            avatarView.setPreserveRatio(true);
//            guestProfileCard.getChildren().add(avatarView);

//        loadGuestData(guestProfileCard, guest);