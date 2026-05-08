package Dasboards;

import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.Amenity;
import model.Room;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

public class RoomBrowseView extends Application {

    // CLASS VARIABLE
    private FlowPane roomContainer;

    @Override
    public void start(Stage stage) {

        HotelDatabase.initializeDummyData();

        Scene scene = createScene();

        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

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

        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setMaxWidth(240);

        sidebar.setPadding(new Insets(25, 20, 25, 20));
        sidebar.setSpacing(15);

        sidebar.getStyleClass().add("sidebar");

        VBox logoBox = new VBox(5);

        Label logo = new Label("Hotel");

        logo.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 48;" +
                        "-fx-font-weight: bold;"
        );

        Label logoSubtitle = new Label("Management System");

        logoSubtitle.setStyle(
                "-fx-text-fill: #CBD5E1;" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: 500;"
        );

        logoBox.getChildren().addAll(
                logo,
                logoSubtitle
        );

        logoBox.setPadding(new Insets(30, 0, 40, 0));

        Button dashboardBtn =
                createSidebarButton("Dashboard");

        Button browseBtn =
                createActiveSidebarButton("Browse Rooms");

        Button reservationBtn =
                createSidebarButton("Reservations");

        Button invoiceBtn =
                createSidebarButton("Invoices");

        Button profileBtn =
                createSidebarButton("Profile");

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

        Button logoutBtn =
                createSidebarButton("Logout");

        dashboardBtn.setOnAction(e -> {
            System.out.println("Dashboard clicked");
        });

        reservationBtn.setOnAction(e -> {
            System.out.println("Reservations clicked");
        });

        invoiceBtn.setOnAction(e -> {
            System.out.println("Invoices clicked");
        });

        profileBtn.setOnAction(e -> {
            System.out.println("Profile clicked");
        });

        logoutBtn.setOnAction(e -> {

            Alert alert = new Alert(
                    Alert.AlertType.INFORMATION
            );

            alert.setHeaderText("Logout");

            alert.setContentText(
                    "Logged out successfully."
            );

            alert.showAndWait();
        });

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

        mainContent.getStyleClass()
                .add("dashboard-pane");

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

        // CLASS VARIABLE INITIALIZATION
        roomContainer = new FlowPane();

        roomContainer.setAlignment(Pos.TOP_LEFT);

        roomContainer.setHgap(20);
        roomContainer.setVgap(20);

        roomContainer.setPadding(new Insets(10));

        roomContainer.setPrefWrapLength(1100);

        // DISPLAY ALL ROOMS
        displayRooms(loadRooms());

        ScrollPane scrollPane =
                new ScrollPane(roomContainer);

        scrollPane.setFitToWidth(true);

        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        mainContent.getChildren().addAll(
                title,
                subtitle,
                filters,
                scrollPane
        );

        return mainContent;
    }

    // LOAD ROOMS

    private List<Room> loadRooms() {

        return HotelDatabase.rooms;
    }

    // DISPLAY ROOMS

    private void displayRooms(List<Room> rooms) {

        roomContainer.getChildren().clear();

        for (Room room : rooms) {

            roomContainer.getChildren().add(
                    createcard(room)
            );
        }
    }

    // SHOW ROOM DETAILS

    private void showRoomDetails(Room room) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Room Details");

        alert.setHeaderText(
                room.getRoomType().getName()
        );

        alert.setContentText(
                "Room ID: " + room.getRoomId() +
                        "\nPrice: $" + room.getPrice() +
                        "\nAmenities: " + room.getAmenities()
        );

        alert.showAndWait();
    }

    // ALL FILTERS

    // FILTER BY TYPE

    private boolean filterByType(
            Room room,
            String selectedType
    ) {

        if (selectedType.equals("All")) {

            return true;
        }

        return room.getRoomType()
                .getName()
                .equalsIgnoreCase(selectedType);
    }

// FILTER BY PRICE

    private boolean filterByPrice(
            Room room,
            String priceText
    ) {

        if (priceText.isEmpty()) {

            return true;
        }

        double maxPrice =
                Double.parseDouble(priceText);

        return room.getPrice() <= maxPrice;
    }

// FILTER BY AMENITIES

    private boolean filterByAmenities(
            Room room,
            String selectedAmenity
    ) {

        if (selectedAmenity.equals("All")) {

            return true;
        }

        for (Amenity amenity :
                room.getAmenities()) {

            if (amenity.getName()
                    .equalsIgnoreCase(selectedAmenity)) {

                return true;
            }
        }

        return false;
    }

    private void applyAllFilters(
            String selectedType,
            String selectedAmenity,
            String priceText
    ) {

        List<Room> filteredRooms =
                new ArrayList<>();

        for (Room room : loadRooms()) {

            boolean matchesType =
                    filterByType(room, selectedType);

            boolean matchesAmenity =
                    filterByAmenities(
                            room,
                            selectedAmenity
                    );

            boolean matchesPrice =
                    filterByPrice(room, priceText);

            if (matchesType &&
                    matchesAmenity &&
                    matchesPrice) {

                filteredRooms.add(room);
            }
        }

        displayRooms(filteredRooms);
    }

    // FILTERS

    private HBox createFilters() {

        HBox filters = new HBox(15);

        filters.setAlignment(Pos.CENTER_LEFT);

        filters.getStyleClass()
                .add("filter-bar");

        ComboBox<String> typeCombo =
                new ComboBox<>();

        typeCombo.getItems().addAll(
                "All",
                "Single",
                "Double",
                "Suite"
        );

        typeCombo.setValue("All");

        typeCombo.setPrefWidth(180);

        ComboBox<String> amenityCombo =
                new ComboBox<>();

        amenityCombo.getItems().addAll(
                "All",
                "WiFi",
                "TV",
                "MiniBar",
                "AC"
        );

        amenityCombo.setValue("All");

        amenityCombo.setPrefWidth(180);

        TextField priceField =
                new TextField();

        priceField.setPromptText("Max Price");

        priceField.setPrefWidth(220);

        Button filterBtn =
                createPrimaryButton("Apply Filter");

        // BUTTON ACTION

        filterBtn.setOnAction(e -> {

            applyAllFilters(
                    typeCombo.getValue(),
                    amenityCombo.getValue(),
                    priceField.getText()
            );
        });

        filters.getChildren().addAll(
                typeCombo,
                amenityCombo,
                priceField,
                filterBtn
        );

        return filters;
    }

    // ROOM CARD

    private VBox createcard(Room room) {

        VBox card = new VBox(10);

        card.setPrefWidth(320);

        card.getStyleClass()
                .add("room-card");

        String imagePath = "/Single.jpg";

        if (room.getRoomType()
                .getName()
                .equalsIgnoreCase("Double")) {

            imagePath = "/Double.jpg";
        }

        else if (room.getRoomType()
                .getName()
                .equalsIgnoreCase("Suite")) {

            imagePath = "/Suite.jpg";
        }

        Image image;

        try {

            image = new Image(
                    getClass().getResource(imagePath).toExternalForm()
            );

        }

        catch (Exception e) {

            image = new Image(
                    getClass().getResource("/Single.jpg").toExternalForm()
            );
        }

        ImageView roomImage =
                new ImageView(image);

        roomImage.setFitWidth(290);
        roomImage.setFitHeight(170);

        roomImage.setPreserveRatio(false);

        roomImage.setSmooth(true);

        Label roomName = new Label(
                room.getRoomType().getName() + " Room"
        );

        roomName.getStyleClass()
                .add("room-title");

        Label roomType = new Label(
                room.getRoomType().getName()
        );

        StringBuilder amenityText =
                new StringBuilder();

        for (int i = 0;
             i < room.getAmenities().size();
             i++) {

            amenityText.append(
                    room.getAmenities()
                            .get(i)
                            .getName()
            );

            if (i < room.getAmenities().size() - 1) {

                amenityText.append(" • ");
            }
        }

        Label amenities = new Label(
                amenityText.toString()
        );

        Label capacity =
                new Label("2 Guests");

        roomType.getStyleClass()
                .add("room-meta");

        amenities.getStyleClass()
                .add("room-meta");

        capacity.getStyleClass()
                .add("room-meta");

        Label roomPrice = new Label(
                "$" + room.getPrice() + " / night"
        );

        roomPrice.getStyleClass()
                .add("room-price");

        Button reserveBtn =
                createPrimaryButton("Reserve");

        reserveBtn.setOnAction(e -> {

            Alert alert = new Alert(
                    Alert.AlertType.INFORMATION
            );

            alert.setTitle("Reservation");

            alert.setHeaderText(
                    "Reservation Successful"
            );

            alert.setContentText(
                    "You reserved Room " +
                            room.getRoomId()
            );

            alert.showAndWait();
        });

        HBox bottomSection = new HBox();

        bottomSection.setAlignment(
                Pos.CENTER_LEFT
        );

        Region push = new Region();

        HBox.setHgrow(push, Priority.ALWAYS);

        bottomSection.getChildren().addAll(
                roomPrice,
                push,
                reserveBtn
        );

        card.getChildren().addAll(
                roomImage,
                roomName,
                roomType,
                amenities,
                capacity,
                bottomSection
        );

        return card;
    }

    // BUTTON HELPERS

    private Button createSidebarButton(
            String text
    ) {

        Button btn = new Button(text);

        btn.setPrefWidth(Double.MAX_VALUE);

        btn.getStyleClass().add("sidebar-button");

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

    private Button createActiveSidebarButton(
            String text
    ) {

        Button btn = new Button(text);

        btn.setPrefWidth(Double.MAX_VALUE);

        btn.getStyleClass().add("sidebar-button-active");

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


    private Button createPrimaryButton(
            String text
    ) {

        Button btn = new Button(text);
        btn.getStyleClass().add("primary-button");

        return btn;
    }

    public static void main(String[] args) {

        launch(args);
    }
}