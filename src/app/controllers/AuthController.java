package app.controllers;

import app.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController {

    @FXML
    private TextField loginEmailField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private TextField registerUsernameField;

    @FXML
    private TextField registerEmailField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    public void handleLogin() {

        SceneManager.switchScene("/FXML/dashboard.fxml");
    }

    @FXML
    public void handleRegister() {

        SceneManager.switchScene("/FXML/dashboard.fxml");
    }
}