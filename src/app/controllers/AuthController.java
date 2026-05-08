package app.controllers;

import Dasboards.*;
import database.*;
import app.SceneManager;
import app.SessionManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;
import service.AuthenticationService;
import util.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import service.AuthenticationService;

import java.io.IOException;

public class AuthController {

    @FXML
    private TextField loginEmailField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private Button loginButton;
    @FXML
    private VBox errorBox;
    @FXML
    private Label errorLabel;
    private boolean passwordVisible = false;
    @FXML
    public void initialize() {

        loginEmailField.setOnAction(e -> {

            loginPasswordField.requestFocus();
        });

        loginPasswordField.setOnAction(e -> {

            loginButton.fire();
        });
    }
    @FXML
    private void handleLogin() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);

        loginButton.getStyleClass().remove("danger-button");
        String username = loginEmailField.getText();

        String password = passwordVisible
                ? visiblePasswordField.getText()
                : loginPasswordField.getText();

        try {

            Object user = AuthenticationService.login(username, password);

            SessionManager.setCurrentUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            loginButton.setDisable(true);

            PauseTransition pause =
                    new PauseTransition(
                            Duration.millis(500)
                    );

            pause.setOnFinished(event -> {

                try {

                    if (user instanceof Admin) {

                        new AdminDashboard().start(stage);

                    } else if (user instanceof Receptionist) {

//            new ReceptionistDashboard().start(stage);

                    } else if (user instanceof Guest) {

                        new GuestDashboard(
                                (Guest) user
                        ).start(stage);
                    }

                    stage.show();

                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            });

            pause.play();
        } catch (AuthenticationException e) {

            errorLabel.setText(e.getMessage());

            errorBox.setVisible(true);
            errorBox.setManaged(true);

            loginButton.getStyleClass().remove("danger-button");
            loginButton.getStyleClass().add("danger-button");
        }
    }
    @FXML
    public void handleRegister() {

        SceneManager.switchScene("/FXML/register.fxml");
    }

    @FXML
    public void togglePasswordVisibility() {

        if (passwordVisible) {

            loginPasswordField.setText(
                    visiblePasswordField.getText()
            );

            loginPasswordField.setVisible(true);
            loginPasswordField.setManaged(true);

            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            togglePasswordButton.setText("👁");

            passwordVisible = false;
        }

        else {

            visiblePasswordField.setText(
                    loginPasswordField.getText()
            );

            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            loginPasswordField.setVisible(false);
            loginPasswordField.setManaged(false);

            togglePasswordButton.setText("✖");

            passwordVisible = true;
        }
    }
    @FXML
    private void handleForgotPassword() {

        SceneManager.switchScene("/FXML/forgot-password.fxml");
    }
}