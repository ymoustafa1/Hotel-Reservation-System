package Dasboards;

import app.SceneManager;
import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Gender;
import model.Guest;
import util.ErrorHandler;
import util.SidebarGuest;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserProfileDashboard extends Application {

    private Guest guest;

    public UserProfileDashboard() {}

    public UserProfileDashboard(Guest guest) {
        this.guest = guest;
    }

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        root.setLeft(SidebarGuest.createSidebar("Profile"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scroll);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerTexts = new VBox(4);

        Label title = new Label("User Profile");
        title.getStyleClass().add("title-label");

        Label sub = new Label("Manage your account information");
        sub.getStyleClass().add("subtitle-label");

        headerTexts.getChildren().addAll(title, sub);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        VBox dateBox = new VBox(2);

        Label date = new Label(
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        );

        Label time = new Label(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
        time.setStyle("-fx-text-fill: gray;");

        dateBox.getChildren().addAll(date, time);
        header.getChildren().addAll(headerTexts, headerSpacer, dateBox);
        centerArea.getChildren().add(header);

        VBox profileCard = new VBox(25);
        profileCard.getStyleClass().add("card");
        profileCard.setStyle("-fx-cursor: default;");
        profileCard.setPadding(new Insets(30));

        HBox topSection = new HBox(30);
        topSection.setAlignment(Pos.CENTER_LEFT);

        Image avatarImage;
        if (guest.getImagePath() != null) {
            avatarImage = new Image(guest.getImagePath());
        } else {
            avatarImage = new Image(getClass().getResourceAsStream("/user2.png"));
        }

        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitWidth(120);
        avatar.setFitHeight(120);

        VBox userTexts = new VBox(5);

        Label uname = new Label(guest.getUsername());
        uname.setStyle("-fx-font-size: 28; -fx-font-weight: bold;");

        Label role = new Label("Guest");
        role.setStyle("-fx-text-fill: #6B7280;");

        userTexts.getChildren().addAll(uname, role);
        topSection.getChildren().addAll(avatar, userTexts);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(40);
        infoGrid.setVgap(22);

        addInfoRow(infoGrid, "Username", guest.getUsername(), 0);
        addInfoRow(infoGrid, "Gender", guest.getGender().toString(), 1);
        addInfoRow(infoGrid, "Address", guest.getAddress(), 2);
        addInfoRow(infoGrid, "Date of Birth", guest.getDateOfBirth().toString(), 3);

        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Edit Profile");
        editBtn.getStyleClass().add("button");

        actionRow.getChildren().add(editBtn);

        profileCard.getChildren().addAll(topSection, infoGrid, actionRow);
        centerArea.getChildren().add(profileCard);

        VBox balanceCard = new VBox(15);
        balanceCard.getStyleClass().add("card");
        balanceCard.setStyle("-fx-cursor: default;");
        balanceCard.setPadding(new Insets(30));

        Label balanceTitle = new Label("Current Balance");
        balanceTitle.getStyleClass().add("section-title");

        Label balance = new Label("$" + String.format("%.2f", guest.getBalance()));
        balance.setStyle("-fx-font-size: 42; -fx-font-weight: bold; -fx-text-fill: #166534;");

        VBox balanceActions = new VBox(12);

        Label addBalanceLabel = new Label("Add Balance");
        addBalanceLabel.getStyleClass().add("section-title");

        TextField addBalanceField = new TextField();
        addBalanceField.setPromptText("Enter amount");

        Button addBalanceBtn = new Button("Add Funds");
        addBalanceBtn.getStyleClass().add("button");

        addBalanceBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(addBalanceField.getText());
                if (amount <= 0) {
                    throw new IllegalArgumentException("Amount must be greater than 0.");
                }
                guest.setBalance(guest.getBalance() + amount);
                balance.setText("$" + String.format("%.2f", guest.getBalance()));
                addBalanceField.clear();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        balanceActions.getChildren().addAll(addBalanceLabel, addBalanceField, addBalanceBtn);
        balanceCard.getChildren().addAll(balanceTitle, balance, balanceActions);
        centerArea.getChildren().add(balanceCard);

        // ── Support Chat Card ────────────────────────────────────────────────
        VBox supportCard = new VBox(15);
        supportCard.getStyleClass().add("card");
        supportCard.setStyle("-fx-cursor: default;");
        supportCard.setPadding(new Insets(30));

        Label supportTitle = new Label("Need Help?");
        supportTitle.getStyleClass().add("section-title");

        Label supportSub = new Label("Chat with our support team anytime.");
        supportSub.getStyleClass().add("subtitle-label");

        Button supportBtn = new Button("Open Chat Support");
        supportBtn.getStyleClass().add("button");

        // ▶ Wire button to GuestChatDashboard
        supportBtn.setOnAction(e -> {
            GuestChatDashboard chatDashboard = new GuestChatDashboard(guest);
            Stage chatStage = new Stage();
            try {
                chatDashboard.start(chatStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        supportCard.getChildren().addAll(supportTitle, supportSub, supportBtn);
        centerArea.getChildren().add(supportCard);

        // ── Edit Profile Popup ───────────────────────────────────────────────
        editBtn.setOnAction(e -> {
            Stage popup = new Stage();

            VBox popupRoot = new VBox(16);
            popupRoot.setPadding(new Insets(25));
            popupRoot.getStyleClass().add("card");

            VBox errorBox = new VBox();
            errorBox.setVisible(false);
            errorBox.setManaged(false);
            errorBox.getStyleClass().add("error-box");

            Label errorLabel = new Label();
            errorLabel.getStyleClass().add("error-label");
            errorBox.getChildren().add(errorLabel);

            TextField usernameField = new TextField(guest.getUsername());
            TextField addressField = new TextField(guest.getAddress());

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("New Password");

            TextField visiblePasswordField = new TextField();
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setPromptText("New Password");

            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirm Password");

            TextField visibleConfirmField = new TextField();
            visibleConfirmField.setManaged(false);
            visibleConfirmField.setVisible(false);
            visibleConfirmField.setPromptText("Confirm Password");

            Button togglePassBtn = new Button("👁");
            togglePassBtn.getStyleClass().add("secondary-button");

            Button toggleConfirmBtn = new Button("👁");
            toggleConfirmBtn.getStyleClass().add("secondary-button");

            togglePassBtn.setOnAction(e2 -> {
                if (passwordField.isVisible()) {
                    visiblePasswordField.setText(passwordField.getText());
                    passwordField.setVisible(false);
                    passwordField.setManaged(false);
                    visiblePasswordField.setVisible(true);
                    visiblePasswordField.setManaged(true);
                    togglePassBtn.setText("✖");
                } else {
                    passwordField.setText(visiblePasswordField.getText());
                    visiblePasswordField.setVisible(false);
                    visiblePasswordField.setManaged(false);
                    passwordField.setVisible(true);
                    passwordField.setManaged(true);
                    togglePassBtn.setText("👁");
                }
            });

            toggleConfirmBtn.setOnAction(e2 -> {
                if (confirmPasswordField.isVisible()) {
                    visibleConfirmField.setText(confirmPasswordField.getText());
                    confirmPasswordField.setVisible(false);
                    confirmPasswordField.setManaged(false);
                    visibleConfirmField.setVisible(true);
                    visibleConfirmField.setManaged(true);
                    toggleConfirmBtn.setText("✖");
                } else {
                    confirmPasswordField.setText(visibleConfirmField.getText());
                    visibleConfirmField.setVisible(false);
                    visibleConfirmField.setManaged(false);
                    confirmPasswordField.setVisible(true);
                    confirmPasswordField.setManaged(true);
                    toggleConfirmBtn.setText("👁");
                }
            });

            DatePicker dobPicker = new DatePicker(guest.getDateOfBirth());
            dobPicker.setEditable(false);

            ComboBox<Gender> genderBox = new ComboBox<>();
            genderBox.getItems().addAll(Gender.values());
            genderBox.setValue(guest.getGender());

            Button uploadBtn = new Button("Upload Image");
            uploadBtn.getStyleClass().add("button");

            Label imageLabel = new Label(
                    guest.getImagePath() != null ? "Image Selected" : "No Image"
            );

            uploadBtn.setOnAction(ev -> {
                FileChooser fc = new FileChooser();
                File file = fc.showOpenDialog(popup);
                if (file != null) {
                    guest.setImagePath(file.toURI().toString());
                    imageLabel.setText("Image Uploaded");
                }
            });

            Button saveBtn = new Button("Save Changes");
            saveBtn.getStyleClass().add("button");

            saveBtn.setOnAction(ev -> {
                try {
                    ErrorHandler.hideError(errorBox);

                    if (usernameField.getText().isBlank()
                            || addressField.getText().isBlank()
                            || dobPicker.getValue() == null
                            || genderBox.getValue() == null) {
                        throw new IllegalArgumentException("Please fill all fields.");
                    }

                    if (!dobPicker.getValue().isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("Date of birth must be before today.");
                    }

                    String newPassword = passwordField.isVisible()
                            ? passwordField.getText()
                            : visiblePasswordField.getText();

                    String confirmPassword = confirmPasswordField.isVisible()
                            ? confirmPasswordField.getText()
                            : visibleConfirmField.getText();

                    if (!newPassword.isBlank()) {
                        if (newPassword.length() < 6) {
                            throw new IllegalArgumentException("Password must be at least 6 characters.");
                        }
                        if (!newPassword.equals(confirmPassword)) {
                            throw new IllegalArgumentException("Passwords do not match.");
                        }
                        guest.setPassword(newPassword);
                    }

                    guest.setUsername(usernameField.getText());
                    guest.setAddress(addressField.getText());
                    guest.setGender(genderBox.getValue());
                    guest.setDateOfBirth(dobPicker.getValue());

                    popup.close();
                    start(stage);

                } catch (Exception ex) {
                    ErrorHandler.showError(errorBox, errorLabel, ex.getMessage());
                }
            });

            popupRoot.getChildren().addAll(
                    errorBox,
                    new Label("Username"), usernameField,
                    new Label("Address"), addressField,
                    new Label("New Password"),
                    new HBox(10, passwordField, visiblePasswordField, togglePassBtn),
                    new Label("Confirm Password"),
                    new HBox(10, confirmPasswordField, visibleConfirmField, toggleConfirmBtn),
                    new Label("Date of Birth"), dobPicker,
                    new Label("Gender"), genderBox,
                    uploadBtn, imageLabel,
                    saveBtn
            );

            ScrollPane popupScroll = new ScrollPane(popupRoot);
            popupScroll.setFitToWidth(true);
            popupScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            popupScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

            Scene popupScene = new Scene(popupScroll, 450, 700);
            popupScene.getStylesheets().add(
                    getClass().getResource("/style.css").toExternalForm()
            );

            popup.setScene(popupScene);
            popup.setResizable(false);
            popup.show();
        });

        stage.setScene(scene);
        stage.setTitle("User Profile");
        stage.setMaximized(true);
        stage.show();
    }

    private void addInfoRow(GridPane grid, String title, String value, int row) {
        Label label = new Label(title);
        label.getStyleClass().add("section-title");

        Label data = new Label(value);
        data.setStyle("-fx-font-size: 15; -fx-text-fill: #374151;");

        grid.add(label, 0, row);
        grid.add(data, 1, row);
    }
}