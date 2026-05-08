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
import javafx.stage.Stage;
import model.Gender;
import model.Guest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class UserProfileDashboard extends Application {
    private Guest guest;
    private ArrayList<Control> allFields = new ArrayList<>();

    public UserProfileDashboard() {}

    public UserProfileDashboard(Guest guest) {
        this.guest = guest;
    }

    @Override
    public void start(Stage UserProfileStage) {
        HotelDatabase.initializeDummyData();
        if (this.guest == null) {
            this.guest = HotelDatabase.findGuest("kenzy");
        }
        BorderPane userProfilePane = new BorderPane();
        Scene userProfileScene = new Scene(userProfilePane, 1280, 800);
        userProfilePane.requestFocus();

        String cssPath = "/style.css";
        var resource = getClass().getResource(cssPath);
        if (resource != null) {
            userProfileScene.getStylesheets().add(resource.toExternalForm());
        }

        // --- Left Sidebar ---
        VBox leftSwitcherText = new VBox();
        leftSwitcherText.getStyleClass().addAll("sidebar");
        leftSwitcherText.setSpacing(20);
        leftSwitcherText.setPadding(new Insets(20));
        leftSwitcherText.setStyle("-fx-background-color: #0F172A;");

        userProfilePane.setLeft(leftSwitcherText);
        String[] sidebarLabels = {"Dashboard", "View Rooms", "Reservations", "Settings", "Invoices", "Profile"};
        String[] iconPaths = {"/home.png", "/bed.png", "/calendar.png", "/setting.png", "/invoice.png", "/user2.png"};

        for (int i = 0; i < sidebarLabels.length; i++) {
            addIconText(leftSwitcherText, sidebarLabels[i], iconPaths[i]);
        }

        Region sidebarSpacer = new Region();
        VBox.setVgrow(sidebarSpacer, Priority.ALWAYS);
        leftSwitcherText.getChildren().add(sidebarSpacer);
        addIconText(leftSwitcherText, "Logout", "/exit.png");

        // --- Center Area (Scrollable) ---
        VBox centerArea = new VBox();
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));
        centerArea.setSpacing(25);
        centerArea.setMaxWidth(1100);

        VBox centeringWrapper = new VBox(centerArea);
        centeringWrapper.setAlignment(Pos.TOP_CENTER);
        centeringWrapper.setStyle("-fx-background-color: #F8FAFC;");

        ScrollPane mainScroll = new ScrollPane(centeringWrapper);
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        userProfilePane.setCenter(mainScroll);

        // --- Header ---
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        VBox welcomeLabels = new VBox();
        Label welcomeLabel = new Label("User Profile");
        welcomeLabel.getStyleClass().add("title-label");
        Label subWelcomeLabel = new Label("Manage your Account Details and Preferences");
        subWelcomeLabel.getStyleClass().add("subtitle-label");
        welcomeLabels.getChildren().addAll(welcomeLabel, subWelcomeLabel);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox topRightDetails = new HBox(15);
        topRightDetails.setAlignment(Pos.CENTER_RIGHT);

        ImageView bellIcon = new ImageView(new Image(getClass().getResourceAsStream("/notification.png")));
        bellIcon.setFitWidth(25);
        bellIcon.setFitHeight(25);

        VBox dateTimeBox = new VBox(4);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        Label dateLabel = new Label(LocalDate.now().format(dateFormatter));
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label timeLabel = new Label("Time: " + LocalDateTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);

        topRightDetails.getChildren().addAll(bellIcon, dateTimeBox);
        headerBox.getChildren().addAll(welcomeLabels, headerSpacer, topRightDetails);
        centerArea.getChildren().add(headerBox);

        // --- Profile Information Card ---
        HBox profileInfoCard = new HBox();
        profileInfoCard.getStyleClass().add("card");
        profileInfoCard.setPadding(new Insets(30));
        profileInfoCard.setSpacing(20);
        profileInfoCard.setAlignment(Pos.CENTER_LEFT);
        profileInfoCard.setMaxWidth(1100);

        VBox photoSection = new VBox(20);
        photoSection.setAlignment(Pos.TOP_CENTER);
        addIconTextBlack(photoSection, "Profile Picture", "/user2.png");

        ImageView avatarView = new ImageView(new Image("/user2.png"));
        avatarView.setFitWidth(150);
        avatarView.setFitHeight(150);
        avatarView.setPreserveRatio(true);

        Label changePhotoLabel = new Label("Change Photo");
        changePhotoLabel.getStyleClass().add("subtitle-label");
        changePhotoLabel.setStyle("-fx-cursor: hand; -fx-text-fill: #1E3A5F;");
        photoSection.getChildren().addAll(avatarView, changePhotoLabel);

        VBox formSection = new VBox(20);
        HBox.setHgrow(formSection, Priority.ALWAYS);

        GridPane userInfoGrid = new GridPane();
        userInfoGrid.setHgap(30);
        userInfoGrid.setVgap(20);

        String[] infoLabels = {"First Name", "Gender", "Address", "DOB"};
        for (int i = 0; i < infoLabels.length; i++) {
            addInfoToGrid(infoLabels[i], userInfoGrid, i);
        }

        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("button");
        saveBtn.setPrefWidth(150);
        actionRow.getChildren().add(saveBtn);

        formSection.getChildren().addAll(userInfoGrid, actionRow);
        profileInfoCard.getChildren().addAll(photoSection, formSection);
        centerArea.getChildren().add(profileInfoCard);

        saveBtn.setOnMouseClicked(e -> {
            try {
                guest.setUsername(((TextField) allFields.get(0)).getText());
                guest.setGender(Gender.valueOf(((ComboBox<String>) allFields.get(1)).getValue().toUpperCase()));
                guest.setAddress(((TextField) allFields.get(2)).getText());
                String dobText = ((TextField) allFields.get(3)).getText();
                guest.setDateOfBirth(LocalDate.parse(dobText, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully!").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid data. Please use DD/MM/YYYY for dates.").show();
            }
        });

        // --- Account Balance Section ---
        VBox balanceArea = new VBox(20);
        balanceArea.getStyleClass().add("card");
        balanceArea.setPadding(new Insets(30));
        balanceArea.setAlignment(Pos.CENTER_LEFT);
        balanceArea.setMaxWidth(1100);
        addIconTextBlack(balanceArea, "Account Balance", "/business.png");

        VBox currentBalanceBox = new VBox(5);
        currentBalanceBox.setAlignment(Pos.CENTER);
        currentBalanceBox.setPadding(new Insets(20));
        currentBalanceBox.setStyle("-fx-background-color: #F0FDF4; -fx-background-radius: 15; -fx-border-color: #DCFCE7;");

        Label currentBalanceText = new Label("Current Balance");
        currentBalanceText.setStyle("-fx-text-fill: #15803D; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label amountLabel = new Label("$" + String.format("%.2f", guest.getBalance()));
        amountLabel.setStyle("-fx-text-fill: #166534; -fx-font-size: 42px; -fx-font-weight: 900;");

        Label availableLabel = new Label("Available to use");
        availableLabel.setStyle("-fx-text-fill: #15803D; -fx-font-size: 14px; -fx-opacity: 0.8;");

        currentBalanceBox.getChildren().addAll(currentBalanceText, amountLabel, availableLabel);
        balanceArea.getChildren().add(currentBalanceBox);

        Label addBalanceLabel = new Label("Quick Top-up");
        addBalanceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        balanceArea.getChildren().add(addBalanceLabel);

        HBox options = new HBox(15);
        String[] balanceAmountOptions = {"50", "100", "200", "500"};
        for (String amt : balanceAmountOptions) {
            createBalanceOptions("$" + amt, options, amountLabel);
        }
        balanceArea.getChildren().add(options);

        HBox manualEntryRow = new HBox(15);
        manualEntryRow.setAlignment(Pos.CENTER_LEFT);
        TextField customAmountField = new TextField();
        customAmountField.setPromptText("Enter custom amount");
        customAmountField.setPrefWidth(250);
        Button updateBalanceBtn = new Button("Update Balance");
        updateBalanceBtn.getStyleClass().add("button");

        updateBalanceBtn.setOnMouseClicked(e -> {
            try {
                double val = Double.parseDouble(customAmountField.getText());
                if (val > 0) {
                    guest.setBalance(guest.getBalance() + val);
                    amountLabel.setText("$" + String.format("%.2f", guest.getBalance()));
                    customAmountField.clear();
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid amount entered.").show();
            }
        });
        manualEntryRow.getChildren().addAll(new Label("$"), customAmountField, updateBalanceBtn);
        balanceArea.getChildren().add(manualEntryRow);
        centerArea.getChildren().add(balanceArea);

        // --- Customer Support Section ---
        VBox supportArea = new VBox(15);
        supportArea.getStyleClass().add("card");
        supportArea.setPadding(new Insets(30));
        supportArea.setMaxWidth(1100);
        addIconTextBlack(supportArea, "Need Help?", "/notification.png");

        HBox supportContent = new HBox(20);
        supportContent.setAlignment(Pos.CENTER_LEFT);
        VBox supportTexts = new VBox(5);
        Label supportTitle = new Label("24/7 Customer Support");
        supportTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label supportSub = new Label("Our team is here to assist you with any inquiries or issues.");
        supportSub.getStyleClass().add("subtitle-label");
        supportTexts.getChildren().addAll(supportTitle, supportSub);

        Region supportSpacer = new Region();
        HBox.setHgrow(supportSpacer, Priority.ALWAYS);
        Button contactBtn = new Button("Contact Support");
        contactBtn.getStyleClass().add("button");
        contactBtn.setStyle("-fx-background-color: #0F172A;");

        supportContent.getChildren().addAll(supportTexts, supportSpacer, contactBtn);
        supportArea.getChildren().add(supportContent);
        centerArea.getChildren().add(supportArea);

        UserProfileStage.setScene(userProfileScene);
        UserProfileStage.setMaximized(true);
        UserProfileStage.setTitle("User Profile Dashboard");
        UserProfileStage.show();
    }

    void createBalanceOptions(String amount, Pane pane, Label amountLabel) {
        Button option = new Button(amount);
        option.getStyleClass().add("button");
        option.setPrefWidth(100);
        option.setOnMouseClicked(e -> {
            double addAmount = Double.parseDouble(amount.replace("$", ""));
            guest.setBalance(guest.getBalance() + addAmount);
            amountLabel.setText("$" + String.format("%.2f", guest.getBalance()));
        });
        pane.getChildren().add(option);
    }

    void addInfoToGrid(String label, GridPane grid, int row) {
        Label nameLabel = new Label(label + ":");
        nameLabel.getStyleClass().add("section-title");
        grid.add(nameLabel, 0, row);

        if (label.contains("Gender")) {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll("Male", "Female");
            combo.getStyleClass().add("combo-box");
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.setValue(guest.getGender().toString());
            allFields.add(combo);
            grid.add(combo, 1, row);
        } else {
            TextField field = new TextField();
            field.getStyleClass().add("text-field");
            field.setPrefWidth(350);
            if (label.contains("First Name")) field.setText(guest.getUsername());
            else if (label.contains("Address")) field.setText(guest.getAddress());
            else if (label.contains("DOB")) {
                LocalDate dob = guest.getDateOfBirth();
                field.setText(dob != null ? dob.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            }
            allFields.add(field);
            grid.add(field, 1, row);
        }
    }

    void addIconText(Pane pane, String string, String iconPath) {
        HBox h =  new HBox(15);
        h.setPadding(new Insets(0));
        h.setAlignment(Pos.CENTER_LEFT);
        h.getStyleClass().add("sidebar-button");
        try {
            ImageView image = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            image.setFitWidth(20); image.setFitHeight(20);
            h.getChildren().add(image);
        } catch (Exception e) {}
        Label label = new Label(string);
        label.setStyle("-fx-text-fill: white;");
        h.getChildren().add(label);
        pane.getChildren().add(h);
    }

    void addIconTextBlack(Pane pane, String string, String iconPath) {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        h.getStyleClass().add("section-title");
        try {
            ImageView image = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            image.setFitWidth(18); image.setFitHeight(18);
            h.getChildren().add(image);
        } catch (Exception e) {}
        Label label = new Label(string);
        h.getChildren().add(label);
        pane.getChildren().add(h);
    }
}