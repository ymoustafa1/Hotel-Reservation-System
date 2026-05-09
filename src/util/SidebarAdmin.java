package util;

import app.SceneManager;
import app.SessionManager;
import app.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Admin;
import Dasboards.AdminDashboard;
import Dasboards.AdminInvoiceDashboard;
import Dasboards.*;
import Dasboards.AdminReservationDashboard;

public class SidebarAdmin {

    public static VBox createSidebar(String activePage) {

        Admin admin = (Admin) SessionManager.getCurrentUser();

        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(240);

        String[] labels = {
                "Dashboard",
                "Manage Staff",
                "Browse Rooms",
                "Reservations",
                "Invoices",
                "Room Types & Amenities"
        };

        String[] icons = {
                "\uD83D\uDCCA",
                "\uD83D\uDC64",
                "\uD83D\uDEAA",
                "\uD83D\uDCC5",
                "\uD83D\uDCC4",
                "\u26F3"
        };

        for (int i = 0; i < labels.length; i++) {

            Button btn = createButton(labels[i], icons[i]);

            if (labels[i].equals(activePage)) {
                btn.getStyleClass().add("sidebar-button-active");
            }

            switch (labels[i]) {

                case "Dashboard" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new AdminDashboard(admin))
                        );

                case "Manage Staff" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new ManageStaffDashboard())
                        );

                case "Browse Rooms" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new AdminRoomBrowseView())
                        );

                case "Reservations" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new AdminReservationDashboard())
                        );

                case "Invoices" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new AdminInvoiceDashboard())
                        );

                case "Room Types & Amenities" ->
                        btn.setOnAction(e ->
                                SceneManager.switchToDashboard(new RoomAmenities())
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

        Button logoutBtn = createButton("Logout", "\uD83D\uDEAA");
        logoutBtn.setOnAction(e -> {
            SessionManager.logout();
            SceneManager.switchScene("/FXML/auth.fxml");
        });

        sidebar.getChildren().addAll(spacer, themeBtn, logoutBtn);

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
