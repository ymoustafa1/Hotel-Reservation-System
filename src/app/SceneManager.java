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

    public static void switchScene(String fxmlPath) {

        StackPane loadingRoot = new StackPane();

        ProgressIndicator loader =
                new ProgressIndicator();

        loadingRoot.getChildren().add(loader);

        Scene loadingScene = new Scene(
                loadingRoot,
                primaryStage.getWidth(),
                primaryStage.getHeight()
        );

        loadingScene.getStylesheets().add(
                SceneManager.class
                        .getResource("/style.css")
                        .toExternalForm()
        );

        primaryStage.setScene(loadingScene);

        PauseTransition pause =
                new PauseTransition(Duration.seconds(0.5));

        pause.setOnFinished(event -> {

            try {

                Parent root = loadFXML(fxmlPath);

                Scene scene = new Scene(
                        root,
                        primaryStage.getWidth(),
                        primaryStage.getHeight()
                );

                scene.getStylesheets().add(
                        SceneManager.class
                                .getResource("/style.css")
                                .toExternalForm()
                );

                primaryStage.setScene(scene);

            } catch (IOException e) {

                e.printStackTrace();
            }
        });

        pause.play();
    }

    public static Parent loadFXML(String path) throws IOException {

        FXMLLoader loader =
                new FXMLLoader(SceneManager.class.getResource(path));

        return loader.load();
    }
}