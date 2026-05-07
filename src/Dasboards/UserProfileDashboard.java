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
import model.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
        BorderPane userProfilePane= new BorderPane();
        Scene userProfileScene = new Scene(userProfilePane,1000,700);
        userProfilePane.requestFocus();

        String cssPath = "/style.css";
        var resource = getClass().getResource(cssPath);
        if (resource != null) {
            userProfileScene.getStylesheets().add(resource.toExternalForm());
        }
        //left sidebar
        VBox leftSwitcherText = new VBox();
        leftSwitcherText.getStyleClass().addAll("sidebar");
        leftSwitcherText.setSpacing(20);
        leftSwitcherText.setPadding(new Insets(20));
        leftSwitcherText.setStyle("-fx-background-color: #0F172A;");

        userProfilePane.setLeft(leftSwitcherText);
        String[] sidebarLabels = {"Dashboard", "View Rooms", "Reservations", "Settings","Invoices","Profile"};
        String[] iconPaths = {"/home.png", "/bed.png", "/calendar.png", "/setting.png","/invoice.png","/user2.png"};

        for (int i = 0; i < sidebarLabels.length; i++) {
            addIconText(leftSwitcherText, sidebarLabels[i], iconPaths[i]);
        }
        Region r1 = new Region();
        r1.setPrefHeight(400);
        leftSwitcherText.getChildren().add(r1);
        addIconText (leftSwitcherText , "Logout","/exit.png");

        //center area

        VBox centerArea = new VBox();
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(25));
        centerArea.setSpacing(10);

       // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        VBox welcomeLabels = new VBox();
        Label welcomeLabel = new Label("User Profile");
        welcomeLabel.getStyleClass().add("title-label");
        Label subWelcomeLabel = new Label("Manage your Account Details and Prefrences");
        subWelcomeLabel.getStyleClass().add("subtitle-label");
        welcomeLabels.getChildren().addAll(welcomeLabel,subWelcomeLabel);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox topRightDetails = new HBox(15);
        topRightDetails.setAlignment(Pos.CENTER_RIGHT);

        ImageView bellIcon = new ImageView(new Image(getClass().getResourceAsStream("/notification.png")));
        bellIcon.setFitWidth(25);
        bellIcon.setFitHeight(25);

        VBox dateTimeBox = new VBox(4);
        dateTimeBox.setPadding(new Insets(4));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        Label dateLabel = new Label(LocalDate.now().format(dateFormatter));
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label timeLabel = new Label("Time: "+ LocalDateTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);

        topRightDetails.getChildren().addAll(bellIcon, dateTimeBox);

        headerBox.getChildren().addAll(welcomeLabels, headerSpacer, topRightDetails);
        centerArea.getChildren().add(headerBox);
        userProfilePane.setCenter(centerArea);

        // middle area
        HBox middle = new HBox(25);

       //Profile Information Card
        HBox profielInfo = new HBox();
        profielInfo.getStyleClass().add("card");
        profielInfo.setPrefSize(800,300);
        profielInfo.setSpacing(60);
        profielInfo.setPadding(new Insets(15));
        VBox Left = new VBox();
        Left.setSpacing(20);
        Left.setPadding(new Insets(0));
        addIconTextBlack(Left , "Profile Information","/user2.png");
        Image changePhotoImage = new Image("/user-avatar.png");
        ImageView changePhotoView = new ImageView(changePhotoImage);
        changePhotoView.setPreserveRatio(true);
        changePhotoView.setFitWidth(150);
        changePhotoView.setFitHeight(150);
        Left.setAlignment(Pos.TOP_CENTER);
        Label changePhotoLabel = new Label("Change Photo");
        changePhotoLabel.getStyleClass().add("subtitle-label");
        Left.getChildren().addAll(changePhotoView,changePhotoLabel);
        profielInfo.getChildren().add(Left);
        middle.getChildren().add(profielInfo);
        Region r2 = new Region();
        r2.setPrefHeight(0);
        centerArea.getChildren().addAll(r2,middle);

        GridPane userInfoGrid = new GridPane();
        userInfoGrid.setHgap(20);
        userInfoGrid.setVgap(15);
        userInfoGrid.setAlignment(Pos.TOP_LEFT);

        String[] infoLabels = {"First Name", "Gender", "Address", "DOB"};

        for (int i = 0; i < infoLabels.length; i++) {
            addInfoToGrid(infoLabels[i], userInfoGrid, i);
        }
        VBox userInfoBox = new VBox();
        userInfoBox.setSpacing(20);
        HBox buttonRegion = new HBox();
        Region r4 = new Region();
        r4.setPrefWidth(350);
        Button save = new Button("Save");
        save.getStyleClass().add("button");
        buttonRegion.getChildren().addAll(r4,save);
        userInfoBox.getChildren().addAll(userInfoGrid,buttonRegion);
        profielInfo.getChildren().add(userInfoBox);
        save.setOnMouseClicked(e -> {
            TextField nameField = (TextField) allFields.get(0);
            guest.setUsername(nameField.getText());

            ComboBox<String> genderBox = (ComboBox<String>) allFields.get(1);
            String selectedGender = genderBox.getValue();
            guest.setGender(Gender.valueOf(selectedGender.toUpperCase()));

            TextField addressField = (TextField) allFields.get(2);
            guest.setAddress(addressField.getText());

            TextField dobField = (TextField) allFields.get(3);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            guest.setDateOfBirth(LocalDate.parse(dobField.getText(), formatter));

            System.out.println("Guest Information Updated Successfully!");
        });

        
        UserProfileStage.setScene(userProfileScene);
        UserProfileStage.setResizable(false);
        UserProfileStage.setTitle("User Profile");
        UserProfileStage.show();


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
    void addIconTextBlack(Pane pane, String string, String iconPath) {
        HBox h = new HBox(15);
        h.setAlignment(Pos.CENTER_LEFT);
        h.getStyleClass().add("section-title");

        var resourceStream = getClass().getResourceAsStream(iconPath);
        if (resourceStream != null) {
            ImageView image = new ImageView(new Image(resourceStream));
            image.setFitWidth(20);
            image.setFitHeight(20);
            h.getChildren().add(image);
        } else {
            System.out.println("Missing Icon: " + iconPath);
        }

        Label label = new Label(string);
//        label.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
        h.getChildren().add(label);
        pane.getChildren().add(h);
    }

    // Change the method signature to accept GridPane and row index
    void addInfoToGrid(String label, GridPane grid, int row) {
        Label nameLabel = new Label(label + ":");
        nameLabel.getStyleClass().add("section-title");
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female");
        genderBox.setValue(guest.getGender().toString());
        TextField field = new TextField();
        field.getStyleClass().add("text-field");
        field.setPrefWidth(250);

        // Logic to fill the field
        if (label.contains("Gender")) {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll("Male", "Female");
            combo.getStyleClass().add("combo-box");
            combo.setPrefSize(300,10);
            combo.setValue(guest.getGender().toString());
            allFields.add(combo);
            grid.add(nameLabel, 0, row);
            grid.add(combo, 1, row);
        } else if (label.contains("First Name")) {
            field.setText(guest.getUsername());
            allFields.add(field);
            grid.add(nameLabel, 0, row);
            grid.add(field, 1, row);
        } else if (label.contains("Address")) {
            field.setText(guest.getAddress());
            allFields.add(field);
            grid.add(nameLabel, 0, row);
            grid.add(field, 1, row);
        } else if (label.contains("DOB")) {
            LocalDate DOB = guest.getDateOfBirth();

            if (DOB != null) {
                field.setText(DOB.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                allFields.add(field);
                grid.add(nameLabel, 0, row);
                grid.add(field, 1, row);
            }
        }

    }

}
