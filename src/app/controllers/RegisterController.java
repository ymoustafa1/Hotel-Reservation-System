package app.controllers;

import app.SceneManager;
import database.HotelDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Gender;
import model.Guest;
import service.AuthenticationService;
import util.AuthenticationException;
import util.ErrorHandler;

import java.time.LocalDate;

public class RegisterController {

    @FXML
    private VBox errorBox;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField balanceField;

    @FXML
    private TextField addressField;

    @FXML
    private ComboBox<Gender> genderBox;

    private boolean passwordVisible = false;

    @FXML
    public void initialize() {

        datePicker.setEditable(false);

        genderBox.getItems().addAll(
                Gender.values()
        );


        usernameField.setOnAction(e -> {

            if (passwordVisible) {

                visiblePasswordField.requestFocus();

            } else {

                passwordField.requestFocus();
            }
        });

        passwordField.setOnAction(e ->
                datePicker.requestFocus()
        );

        visiblePasswordField.setOnAction(e ->
                datePicker.requestFocus()
        );

        datePicker.getEditor().setOnAction(e ->
                balanceField.requestFocus()
        );

        balanceField.setOnAction(e ->
                addressField.requestFocus()
        );

        addressField.setOnAction(e ->
                genderBox.requestFocus()
        );

        genderBox.setOnAction(e ->
                handleCreateAccount()
        );
    }
    @FXML
    private void handleCreateAccount() {

        ErrorHandler.hideError(errorBox);

        try {

            String username = usernameField
                    .getText()
                    .trim()
                    .toLowerCase();
            AuthenticationService.isUsernameUnique(username);

            String password = passwordVisible
                    ? visiblePasswordField.getText()
                    : passwordField.getText();
            LocalDate dateOfBirth = datePicker.getValue();

            String address = addressField.getText().trim();

            Gender gender = genderBox.getValue();

            if (username.isBlank()
                    || password.isBlank()
                    || address.isBlank()
                    || gender == null
                    || dateOfBirth == null) {

                throw new IllegalArgumentException(
                        "Please fill all fields."
                );
            }

            if (password.length() < 6) {

                throw new IllegalArgumentException(
                        "Password must be at least 6 characters."
                );
            }
            if (dateOfBirth == null) {

                throw new IllegalArgumentException(
                        "Please select a valid date of birth."
                );
            }
            if (!dateOfBirth.isBefore(LocalDate.now())) {

                throw new IllegalArgumentException(
                        "Date of birth must be before today."
                );
            }
            if (dateOfBirth.isAfter(LocalDate.now().minusYears(18))) {

                throw new IllegalArgumentException(
                        "Guest must be at least 18 years old."
                );
            }

            double balance;

            try {

                balance = Double.parseDouble(
                        balanceField.getText().trim()
                );

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Balance must be a valid number."
                );
            }

            Guest guest = new Guest(
                    username,
                    password,
                    dateOfBirth,
                    balance,
                    address,
                    gender
            );

            HotelDatabase.guests.add(guest);
            SceneManager.switchScene("/FXML/auth.fxml");

        } catch (Exception e) {

            ErrorHandler.showError(
                    errorBox,
                    errorLabel,
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleBackToLogin() {

        SceneManager.switchScene("/FXML/auth.fxml");
    }

    @FXML
    private void togglePasswordVisibility() {

        if (passwordVisible) {

            passwordField.setText(
                    visiblePasswordField.getText()
            );

            passwordField.setVisible(true);
            passwordField.setManaged(true);

            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            togglePasswordButton.setText("👁");

            passwordVisible = false;

        } else {

            visiblePasswordField.setText(
                    passwordField.getText()
            );

            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            togglePasswordButton.setText("✖");

            passwordVisible = true;
        }
    }
}