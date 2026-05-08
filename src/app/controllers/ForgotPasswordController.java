package app.controllers;

import app.SceneManager;
import database.HotelDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Guest;
import util.ErrorHandler;

public class ForgotPasswordController {

    @FXML
    private VBox errorBox;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private VBox passwordSection;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Button togglePasswordButton;
    @FXML
    private Button findAccountButton;

    private Guest foundGuest;

    private boolean passwordVisible = false;

    @FXML
    private void handleFindUser() {

        ErrorHandler.hideError(errorBox);

        try {

            String username = usernameField.getText().trim();

            if (username.isBlank()) {

                throw new IllegalArgumentException(
                        "Please enter a username."
                );
            }

            foundGuest = null;

            for (Guest guest : HotelDatabase.guests) {

                if (guest.getUsername().equals(username)) {

                    foundGuest = guest;
                    break;
                }
            }

            if (foundGuest == null) {

                throw new IllegalArgumentException(
                        "Username not found."
                );
            }

            passwordSection.setVisible(true);
            passwordSection.setManaged(true);
            findAccountButton.setVisible(false);
            findAccountButton.setManaged(false);

        } catch (Exception e) {

            ErrorHandler.showError(
                    errorBox,
                    errorLabel,
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleSetPassword() {

        ErrorHandler.hideError(errorBox);

        try {

            String password = passwordVisible
                    ? visiblePasswordField.getText()
                    : passwordField.getText();

            if (password.isBlank()) {

                throw new IllegalArgumentException(
                        "Please enter a password."
                );
            }

            if (password.length() < 6) {

                throw new IllegalArgumentException(
                        "Password must be at least 6 characters."
                );
            }

            foundGuest.setPassword(password);

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

    @FXML
    private void handleBackToLogin() {

        SceneManager.switchScene("/FXML/auth.fxml");
    }
}