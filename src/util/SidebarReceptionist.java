package util;

import Dasboards.*;
import app.SceneManager;
import app.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Receptionist;

public class SidebarReceptionist {

    public static VBox createSidebar(String activePage) {

        Receptionist receptionist = (Receptionist) SessionManager.getCurrentUser();

        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(240);

        String[] labels = {
                "Dashboard",
                "Reservations",
                "Find Guest",
                "Walk-in Check In"
        };

        String[] icons = {
                "\uD83D\uDCCA",
                "\uD83D\uDCC5",
                "\uD83D\uDC64",
                "\uD83D\uDEAA"
        };

        for (int i = 0; i < labels.length; i++) {

            Button btn = createButton(labels[i], icons[i]);

            if (labels[i].equals(activePage)) {
                btn.getStyleClass().add("sidebar-button-active");
            }

            switch (labels[i]) {

                case "Dashboard" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new ReceptionistDashboard(receptionist))
                        );

                case "Reservations" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new ReceptionistReservationDashboard())
                        );

                case "Find Guest" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new ReceptionistGuestSearchView())
                        );

                case "Walk-in Check In" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new ReceptionistWalkInView())
                        );
            }

            sidebar.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createButton("Logout", "\uD83D\uDEAA");
        logoutBtn.setOnAction(e -> {
            SessionManager.logout();
            GuestChatDashboard.closeWindow();
            ReceptionistChatDashboard.closeWindow();
            SceneManager.switchScene("/FXML/auth.fxml");
        });

        sidebar.getChildren().addAll(spacer, logoutBtn);

        return sidebar;
    }

    private static Button createButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("sidebar-button");
        btn.setPrefWidth(190);
        btn.setMinHeight(48);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }
}