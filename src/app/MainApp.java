package app;

import database.HotelDatabase;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setStage(stage);
        stage.getIcons().add(
                new Image(
                        getClass()
                                .getResourceAsStream(
                                        "/logo.png"
                                )
                )
        );
        initializeApp(stage);    }


    private void initializeApp(Stage stage) {
        HotelDatabase.initializeDatabase();

        HotelDatabase.insertSeedData();

        HotelDatabase.loadData();
        SceneManager.setStage(stage);
        stage.setTitle("Hotel Reservation System");
        stage.setMaximized(true);
        stage.setWidth(1400);
        stage.setHeight(900);
        SceneManager.switchScene("/FXML/auth.fxml");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}