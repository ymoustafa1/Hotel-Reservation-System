package Dasboards;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class RoomBrowseView extends Application {

    @Override
    public void start(Stage stage) {

        Scene scene = createScene();

        stage.setTitle("Hotel Management System");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public Scene createScene() {

        BorderPane root = new BorderPane();

        root.setLeft(createSidebar());
        root.setCenter(createMainContent());

        return new Scene(root, 1400, 800);
    }

    // SIDEBAR

    private VBox createSidebar() {

        VBox sidebar = new VBox();

        sidebar.setPrefWidth(260);
        sidebar.setMinWidth(260);
        sidebar.setMaxWidth(260);

        sidebar.setPadding(new Insets(25, 20, 25, 20));
        sidebar.setSpacing(15);

        sidebar.setStyle("-fx-background-color: #041E42;");

        VBox logoBox = new VBox(2);

        Label logo = new Label("Hotel");
        logo.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 32;" +
                        "-fx-font-weight: bold;"
        );

        Label logoSubtitle = new Label("Management System");
        logoSubtitle.setStyle(
                "-fx-text-fill: #CBD5E1;" +
                        "-fx-font-size: 13;"
        );

        logoBox.getChildren().addAll(logo, logoSubtitle);
        logoBox.setPadding(new Insets(10, 0, 30, 0));

        Button dashboardBtn = createSidebarButton("Dashboard");
        Button browseBtn = createActiveSidebarButton("Browse Rooms");
        Button reservationBtn = createSidebarButton("Reservations");
        Button invoiceBtn = createSidebarButton("Invoices");
        Button profileBtn = createSidebarButton("Profile");

        VBox navLinks = new VBox(10);

        navLinks.getChildren().addAll(
                dashboardBtn,
                browseBtn,
                reservationBtn,
                invoiceBtn,
                profileBtn
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createSidebarButton("Logout");

        sidebar.getChildren().addAll(
                logoBox,
                navLinks,
                spacer,
                logoutBtn
        );

        return sidebar;
    }

    // MAIN CONTENT

    private VBox createMainContent() {

        VBox mainContent = new VBox(20);

        mainContent.setPadding(new Insets(30));

        mainContent.setStyle(
                "-fx-background-color: #F5F7FA;"
        );

        Label title = new Label("Browse Rooms");

        title.setStyle(
                "-fx-font-size: 42;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #041E42;"
        );

        Label subtitle = new Label(
                "Find the perfect room for your stay."
        );

        subtitle.setStyle(
                "-fx-font-size: 16;" +
                        "-fx-text-fill: #555555;"
        );

        HBox filters = createFilters();

        FlowPane roomContainer = new FlowPane();

        roomContainer.setHgap(20);
        roomContainer.setVgap(20);

        roomContainer.setPadding(new Insets(10));

        roomContainer.setPrefWrapLength(1100);

        roomContainer.getChildren().addAll(
                createRoomCard(),
                createRoomCard(),
                createRoomCard()
        );

        ScrollPane scrollPane = new ScrollPane(roomContainer);

        scrollPane.setFitToWidth(true);

        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        mainContent.getChildren().addAll(
                title,
                subtitle,
                filters,
                scrollPane
        );

        return mainContent;
    }

    // =========================
    // FILTERS
    // =========================

    private HBox createFilters() {

        HBox filters = new HBox(15);

        filters.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> typeCombo = new ComboBox<>();

        typeCombo.getItems().addAll(
                "Single",
                "Double",
                "Suite"
        );

        typeCombo.setPromptText("Room Type");

        ComboBox<String> amenityCombo = new ComboBox<>();

        amenityCombo.getItems().addAll(
                "WiFi",
                "TV",
                "Mini Bar",
                "AC"
        );

        amenityCombo.setPromptText("Amenity");

        TextField priceField = new TextField();

        priceField.setPromptText("Max Price");

        Button filterBtn = createPrimaryButton("Apply Filter");

        filters.getChildren().addAll(
                typeCombo,
                amenityCombo,
                priceField,
                filterBtn
        );

        return filters;
    }

    // ROOM CARD

    private VBox createRoomCard() {

        VBox roomCard = new VBox(15);

        roomCard.setPrefWidth(320);

        roomCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 18;" +
                        "-fx-padding: 15;"
        );

        Rectangle imagePlaceholder = new Rectangle(290, 170);

        imagePlaceholder.setArcWidth(20);
        imagePlaceholder.setArcHeight(20);

        imagePlaceholder.setStyle(
                "-fx-fill: #D1D5DB;"
        );

        Label roomName = new Label("Deluxe King Room");

        roomName.setStyle(
                "-fx-font-size: 24;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #041E42;"
        );

        Label roomType = new Label("Double Room");

        Label amenities = new Label(
                "WiFi • TV • AC • Mini Bar"
        );

        Label capacity = new Label("2 Guests");

        Label roomPrice = new Label("$140 / night");

        roomPrice.setStyle(
                "-fx-font-size: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1D4ED8;"
        );

        Button reserveBtn = createPrimaryButton("Reserve");

        HBox bottomSection = new HBox();

        bottomSection.setAlignment(Pos.CENTER_LEFT);

        Region push = new Region();

        HBox.setHgrow(push, Priority.ALWAYS);

        bottomSection.getChildren().addAll(
                roomPrice,
                push,
                reserveBtn
        );

        roomCard.getChildren().addAll(
                imagePlaceholder,
                roomName,
                roomType,
                amenities,
                capacity,
                bottomSection
        );

        return roomCard;
    }

    // BUTTON HELPERS

    private Button createSidebarButton(String text) {

        Button btn = new Button(text);

        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setMinHeight(50);

        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16;" +
                        "-fx-alignment: center-left;" +
                        "-fx-padding: 12 20 12 20;"
        );

        return btn;
    }

    private Button createActiveSidebarButton(String text) {

        Button btn = new Button(text);

        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setMinHeight(50);

        btn.setStyle(
                "-fx-background-color: #1D4ED8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16;" +
                        "-fx-background-radius: 10;" +
                        "-fx-alignment: center-left;" +
                        "-fx-padding: 12 20 12 20;"
        );

        return btn;
    }

    private Button createPrimaryButton(String text) {

        Button btn = new Button(text);

        btn.setStyle(
                "-fx-background-color: #041E42;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 20 10 20;"
        );

        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}