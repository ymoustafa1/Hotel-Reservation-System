package model;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CreateReservationForm {

    public void showForm(){
        Stage stage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        Label title = new Label("Create Reservation");
        title.setStyle("-fx-font-size: 24;" + "-fx-font-weight: bold;");
        ComboBox<String> roomBox = new ComboBox<>();
        roomBox.getItems().addAll(
                "Single Room",
                "Deluxe Room",
                "Suite",
                "Family Room"
        );

        roomBox.setPromptText("Select Room");
        DatePicker startDate = new DatePicker();
        startDate.setPromptText("Check In date");
        DatePicker endDate = new DatePicker();
        endDate.setPromptText("Check Out date");
        TextField guestsField = new TextField();

        guestsField.setPromptText("Number of Guests");

        Button createBtn = new Button("Create Reservation");

        createBtn.setStyle("-fx-background-color: #16A34A;" + "-fx-text-fill: white;");

        createBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Reservation Created Successfully!");
            alert.show();
            stage.close();
        });

        root.getChildren().addAll(
                title,
                roomBox,
                startDate,
                endDate,
                guestsField,
                createBtn
        );

        Scene scene = new Scene(root,400,400);
        stage.setScene(scene);
        stage.setTitle("Create Reservation");
        stage.show();
    }
}