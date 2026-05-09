package util;

import Dasboards.GuestChatDashboard;
import Dasboards.ReceptionistChatDashboard;
import app.SceneManager;
import app.SessionManager;
import app.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Guest;

public class SidebarGuest {

    public static VBox createSidebar(String activePage) {

        Guest guest = (Guest) SessionManager.getCurrentUser();

        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(240);

        String[] labels = {
                "Dashboard",
                "View Rooms",
                "Reservations",
                "Invoices",
                "Profile"
        };

        String[] icons = {
                "/home.png",
                "/bed.png",
                "/calendar.png",
                "/invoice.png",
                "/user2.png"
        };

        for (int i = 0; i < labels.length; i++) {
            Button btn = createButton(labels[i], icons[i]);

            if (labels[i].equals(activePage)) {
                btn.getStyleClass().add("sidebar-button-active");
            }

            switch (labels[i]) {
                case "Dashboard" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(
                                        new Dasboards.GuestDashboard(guest)
                                )
                        );
                case "View Rooms" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(
                                        new Dasboards.RoomBrowseView(guest)
                                )
                        );
                case "Reservations" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(
                                        new Dasboards.ReservationDashboard(guest)
                                )
                        );
                case "Invoices" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(
                                        new Dasboards.InvoicesDashboard(guest)
                                )
                        );
                case "Profile" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(
                                        new Dasboards.UserProfileDashboard(guest)
                                )
                        );
            }

            sidebar.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button themeBtn = new Button(ThemeManager.getInstance().getToggleLabel());
        themeBtn.getStyleClass().add("theme-toggle-button");
        themeBtn.setPrefWidth(190);
        themeBtn.setOnAction(e -> {
            ThemeManager.getInstance().toggleTheme();
            themeBtn.setText(ThemeManager.getInstance().getToggleLabel());
        });

        Button logoutBtn = createButton("Logout", "/exit.png");
        logoutBtn.setOnAction(e -> {
            SessionManager.logout();
            GuestChatDashboard.closeWindow();
            ReceptionistChatDashboard.closeWindow();
            SceneManager.switchScene("/FXML/auth.fxml");
        });

        sidebar.getChildren().addAll(spacer, themeBtn, logoutBtn);

        return sidebar;
    }

    private static Button createButton(String text, String iconPath) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-button");
        btn.setPrefWidth(190);
        btn.setMinHeight(48);
        btn.setAlignment(Pos.CENTER_LEFT);

        try {
            ImageView icon = new ImageView(
                    new Image(SidebarGuest.class.getResourceAsStream(iconPath))
            );
            icon.setFitWidth(18);
            icon.setFitHeight(18);
            btn.setGraphic(icon);
        } catch (Exception ignored) {}

        return btn;
    }
}
