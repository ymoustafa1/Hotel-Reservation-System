package util;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ErrorHandler {

    public static void showError(
            VBox errorBox,
            Label errorLabel,
            String message
    ) {

        errorLabel.setText(message);

        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    public static void hideError(
            VBox errorBox
    ) {

        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }
}