package app;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SceneManager {

    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getStage() {
        return primaryStage;
    }

    private static void showLoadingThen(Runnable onFinished) {
        ProgressIndicator loader = new ProgressIndicator();
        StackPane loadingPane = new StackPane(loader);
        loadingPane.setStyle("-fx-background-color: white;");

        Scene loadingScene = new Scene(
                loadingPane,
                primaryStage.getWidth(),
                primaryStage.getHeight()
        );

        primaryStage.setScene(loadingScene);

        PauseTransition pause = new PauseTransition(Duration.millis(400));
        pause.setOnFinished(e -> onFinished.run());
        pause.play();
    }

    public static void switchScene(String fxmlPath) {
        showLoadingThen(() -> {
            try {
                FXMLLoader loaderFXML = new FXMLLoader(
                        SceneManager.class.getResource(fxmlPath)
                );
                Parent root = loaderFXML.load();
                Scene newScene = new Scene(root);
                newScene.getStylesheets().add(
                        SceneManager.class.getResource("/style.css").toExternalForm()
                );
                primaryStage.setScene(newScene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void switchDashboard(Parent root) {
        showLoadingThen(() -> {
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    SceneManager.class.getResource("/style.css").toExternalForm()
            );
            primaryStage.setScene(scene);
        });
    }

    public static void switchToDashboard(javafx.application.Application dashboard) {
        showLoadingThen(() -> {
            try {
                dashboard.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static Parent loadFXML(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(path));
        return loader.load();
    }
}