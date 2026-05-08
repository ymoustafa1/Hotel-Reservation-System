package Dasboards;

import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Amenity;
import model.RoomType;

import java.time.LocalDate;

public class RoomAmenities extends Application {

    @Override
    public void start(Stage stage) {

        BorderPane root =
                new BorderPane();

        root.getStyleClass()
                .add("dashboard-pane");

        // LEFT SIDEBAR

        VBox sidebar =
                createSidebar();

        // MAIN CONTENT

        ScrollPane scrollPane =
                new ScrollPane();

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        VBox content =
                createMainContent();

        scrollPane.setContent(content);

        root.setLeft(sidebar);

        root.setCenter(scrollPane);

        Scene scene =
                new Scene(root, 1550, 900);

        scene.getStylesheets().add(
                getClass()
                        .getResource("/style.css")
                        .toExternalForm()
        );

        stage.setTitle("Room Amenities");

        stage.setMaximized(true);

        stage.setScene(scene);

        stage.show();
    }

    // SIDEBAR

    private VBox createSidebar() {

        VBox sidebar =
                new VBox(25);

        sidebar.setPadding(
                new Insets(40, 20, 40, 20)
        );

        sidebar.setPrefWidth(320);

        sidebar.setStyle(
                "-fx-background-color: linear-gradient(to bottom,#142850,#1F3A68,#2B50EC);"
        );

        Label hotelTitle =
                new Label("Hotel");

        hotelTitle.setStyle(
                "-fx-font-size: 52;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label systemLabel =
                new Label("Management System");

        systemLabel.setStyle(
                "-fx-font-size: 24;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);"
        );

        VBox topSection =
                new VBox(8);

        topSection.getChildren().addAll(
                hotelTitle,
                systemLabel
        );

        VBox menu =
                new VBox(18);

        Button dashboardBtn =
                createSidebarButton("Dashboard");

        Button browseBtn =
                createSidebarButton("Browse Rooms");

        Button amenitiesBtn =
                createSidebarButton("Room Amenities");

        Button invoicesBtn =
                createSidebarButton("Invoices");

        Button profileBtn =
                createSidebarButton("Profile");

        Button logoutBtn =
                createSidebarButton("Logout");

        amenitiesBtn.setStyle(
                "-fx-background-color: #3158ff;" +
                        "-fx-background-radius: 18;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 18 20 18 20;"
        );

        menu.getChildren().addAll(
                dashboardBtn,
                browseBtn,
                amenitiesBtn,
                invoicesBtn,
                profileBtn
        );

        Region spacer =
                new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        sidebar.getChildren().addAll(
                topSection,
                menu,
                spacer,
                logoutBtn
        );

        return sidebar;
    }

    // MAIN CONTENT

    private VBox createMainContent() {

        VBox content =
                new VBox(30);

        content.setPadding(
                new Insets(35)
        );

        content.setStyle(
                "-fx-background-color: #ECEEF3;"
        );

        // TITLE

        Label title =
                new Label(
                        "Room Types & Amenities"
                );

        title.setStyle(
                "-fx-font-size: 44;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #16386B;"
        );

        Label subtitle =
                new Label(
                        "Manage hotel room configurations."
                );

        subtitle.setStyle(
                "-fx-font-size: 20;" +
                        "-fx-text-fill: #6b7280;"
        );

        // TOP SECTION

        HBox topSection =
                new HBox(30);

        VBox leftContent =
                new VBox(30);

        leftContent.setPrefWidth(1100);

        // ROOM IMAGE CARD

        VBox imageCard =
                new VBox();

        imageCard.setPadding(
                new Insets(20)
        );

        imageCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08),12,0,0,4);"
        );

        ImageView roomImage =
                new ImageView(
                        new Image(
                                getClass()
                                        .getResourceAsStream("/Deluxe.jpg")
                        )
                );

        roomImage.setFitWidth(760);

        roomImage.setFitHeight(300);

        roomImage.setPreserveRatio(false);

        roomImage.setSmooth(true);

        roomImage.setStyle(
                "-fx-background-radius: 20;"
        );

        imageCard.getChildren().add(
                roomImage
        );

        // ROOM TYPES SECTION

        VBox roomTypesSection =
                createRoomTypesSection();

        // AMENITIES SECTION

        VBox amenitiesSection =
                createAmenitiesSection();

        leftContent.getChildren().addAll(
                title,
                subtitle,
                imageCard,
                roomTypesSection,
                amenitiesSection
        );

        VBox bookingPanel =
                createBookingPanel();

        topSection.getChildren().addAll(
                leftContent,
                bookingPanel
        );

        content.getChildren().add(
                topSection
        );

        return content;
    }

    // ROOM TYPES SECTION

    private VBox createRoomTypesSection() {

        VBox section =
                new VBox(20);

        section.setPadding(
                new Insets(25)
        );

        section.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 25;"
        );

        Label title =
                new Label("Room Types");

        title.setStyle(
                "-fx-font-size: 32;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #16386B;"
        );

        FlowPane roomTypesPane =
                new FlowPane();

        roomTypesPane.setHgap(25);

        roomTypesPane.setVgap(25);

        roomTypesPane.setPrefWrapLength(1100);

        roomTypesPane.setAlignment(Pos.TOP_LEFT);

        for (RoomType roomType :
                HotelDatabase.roomTypes) {

            roomTypesPane.getChildren().add(
                    createRoomTypeCard(roomType)
            );
        }

        section.getChildren().addAll(
                title,
                roomTypesPane
        );

        return section;
    }

    // ROOM TYPE CARD

    private VBox createRoomTypeCard(RoomType roomType) {

        VBox card =
                new VBox(12);

        card.setPrefWidth(240);

        card.setPadding(
                new Insets(18)
        );

        card.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 20;"
        );

        String imagePath =
                "/Single.jpg";

        if (roomType.getName()
                .equalsIgnoreCase("Double")) {

            imagePath = "/Double.jpg";
        }

        else if (roomType.getName()
                .equalsIgnoreCase("Suite")) {

            imagePath = "/Suite.jpg";
        }

        ImageView image =
                new ImageView(
                        new Image(
                                getClass()
                                        .getResourceAsStream(imagePath)
                        )
                );

        image.setFitWidth(200);

        image.setFitHeight(110);

        image.setPreserveRatio(false);

        Label name =
                new Label(roomType.getName());

        name.setStyle(
                "-fx-font-size: 24;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #16386B;"
        );

        Label price =
                new Label(
                        "Base Price: $" +
                                roomType.getBasePrice()
                );

        price.setStyle(
                "-fx-font-size: 18;"
        );

        Label amenities =
                new Label(
                        roomType.getAmenities().size() +
                                " Amenities Included"
                );

        amenities.setStyle(
                "-fx-font-size: 17;" +
                        "-fx-text-fill: #6B7280;"
        );

        Button editBtn =
                createPrimaryButton("Edit");

        editBtn.setOnAction(e -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setHeaderText(
                    roomType.getName()
            );

            alert.setContentText(
                    "Room Type Updated Successfully"
            );

            alert.showAndWait();
        });

        card.getChildren().addAll(
                image,
                name,
                price,
                amenities,
                editBtn
        );

        return card;
    }

    // AMENITIES SECTION

    private VBox createAmenitiesSection() {

        VBox section =
                new VBox(20);

        section.setPadding(
                new Insets(25)
        );

        section.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 25;"
        );

        Label title =
                new Label("Amenities");

        title.setStyle(
                "-fx-font-size: 32;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #16386B;"
        );

        FlowPane amenitiesPane =
                new FlowPane();

        amenitiesPane.setHgap(18);

        amenitiesPane.setVgap(18);

        amenitiesPane.setPrefWrapLength(1100);

        amenitiesPane.setAlignment(Pos.TOP_LEFT);

        for (Amenity amenity :
                HotelDatabase.amenities) {

            amenitiesPane.getChildren().add(
                    createAmenityCard(amenity)
            );
        }

        section.getChildren().addAll(
                title,
                amenitiesPane
        );

        return section;
    }

    // AMENITY CARD

    private VBox createAmenityCard(Amenity amenity) {

        VBox card =
                new VBox(10);

        card.setPrefWidth(170);

        card.setPadding(
                new Insets(16)
        );

        card.setAlignment(Pos.CENTER);

        card.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 20;"
        );

        String imagePath =
                "/bed.png";

        if (amenity.getName()
                .equalsIgnoreCase("WiFi")) {

            imagePath = "/Wifi.png";
        }

        else if (amenity.getName()
                .equalsIgnoreCase("TV")) {

            imagePath = "/Tv.png";
        }

        else if (amenity.getName()
                .equalsIgnoreCase("Pool")) {

            imagePath = "/Pool.png";
        }

        else if (amenity.getName()
                .equalsIgnoreCase("Gym")) {

            imagePath = "/gym.png";
        }

        else if (amenity.getName()
                .equalsIgnoreCase("Spa")) {

            imagePath = "/spa.png";
        }

        ImageView icon =
                new ImageView(
                        new Image(
                                getClass()
                                        .getResourceAsStream(imagePath)
                        )
                );

        icon.setFitWidth(40);

        icon.setFitHeight(40);

        Label name =
                new Label(amenity.getName());

        name.setStyle(
                "-fx-font-size: 22;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #16386B;"
        );

        Label type =
                new Label(
                        amenity.getType()
                                .toString()
                );

        type.setStyle(
                "-fx-font-size: 15;" +
                        "-fx-text-fill: #6B7280;"
        );

        Label price =
                new Label(
                        "$" + amenity.getPrice()
                );

        price.setStyle(
                "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #3158ff;"
        );

        Button editBtn =
                createPrimaryButton("Edit");

        editBtn.setOnAction(e -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setHeaderText(
                    amenity.getName()
            );

            alert.setContentText(
                    "Amenity Updated Successfully"
            );

            alert.showAndWait();
        });

        card.getChildren().addAll(
                icon,
                name,
                type,
                price,
                editBtn
        );

        return card;
    }

    // BOOKING PANEL

    private VBox createBookingPanel() {

        VBox panel =
                new VBox(22);

        panel.setPrefWidth(320);

        panel.setPadding(
                new Insets(28)
        );

        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08),12,0,0,4);"
        );

        Label title =
                new Label("Book This Room");

        title.setStyle(
                "-fx-font-size: 28;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #16386B;"
        );

        DatePicker checkIn =
                new DatePicker(
                        LocalDate.now()
                );

        DatePicker checkOut =
                new DatePicker(
                        LocalDate.now().plusDays(4)
                );

        ComboBox<String> guests =
                new ComboBox<>();

        guests.getItems().addAll(
                "1 Adult",
                "2 Adults",
                "3 Adults",
                "4 Adults"
        );

        guests.setValue("2 Adults");

        Label total =
                new Label("Total: $660");

        total.setStyle(
                "-fx-font-size: 34;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #3158ff;"
        );

        Button bookBtn =
                createPrimaryButton("Book Now");

        Button wishlistBtn =
                createPrimaryButton("Add To Wishlist");

        bookBtn.setOnAction(e -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setHeaderText(
                    "Booking Confirmed"
            );

            alert.setContentText(
                    "Room booked successfully."
            );

            alert.showAndWait();
        });

        wishlistBtn.setOnAction(e -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setHeaderText(
                    "Wishlist"
            );

            alert.setContentText(
                    "Added to wishlist."
            );

            alert.showAndWait();
        });

        panel.getChildren().addAll(
                title,

                new Label("Check In"),
                checkIn,

                new Label("Check Out"),
                checkOut,

                new Label("Guests"),
                guests,

                total,
                bookBtn,
                wishlistBtn
        );

        return panel;
    }

    // PRIMARY BUTTON

    private Button createPrimaryButton(String text) {

        Button button =
                new Button(text);

        button.setPrefWidth(180);

        button.setPrefHeight(50);

        button.setStyle(
                "-fx-background-color: #16386B;" +
                        "-fx-background-radius: 16;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        // HOVER

        button.setOnMouseEntered(e -> {

            button.setStyle(
                    "-fx-background-color: #3158ff;" +
                            "-fx-background-radius: 16;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 18;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );
        });

        // EXIT

        button.setOnMouseExited(e -> {

            button.setStyle(
                    "-fx-background-color: #16386B;" +
                            "-fx-background-radius: 16;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 18;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );
        });

        // PRESS

        button.setOnMousePressed(e -> {

            button.setScaleX(0.96);

            button.setScaleY(0.96);
        });

        // RELEASE

        button.setOnMouseReleased(e -> {

            button.setScaleX(1);

            button.setScaleY(1);
        });

        return button;
    }

    // SIDEBAR BUTTON

    private Button createSidebarButton(String text) {

        Button button =
                new Button(text);

        button.setPrefWidth(250);

        button.setAlignment(Pos.CENTER_LEFT);

        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 18 20 18 20;" +
                        "-fx-background-radius: 18;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> {

            button.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.12);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 20;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 18 20 18 20;" +
                            "-fx-background-radius: 18;" +
                            "-fx-cursor: hand;"
            );
        });

        button.setOnMouseExited(e -> {

            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 20;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 18 20 18 20;" +
                            "-fx-background-radius: 18;" +
                            "-fx-cursor: hand;"
            );
        });

        button.setOnMousePressed(e -> {

            button.setScaleX(0.97);

            button.setScaleY(0.97);
        });

        button.setOnMouseReleased(e -> {

            button.setScaleX(1);

            button.setScaleY(1);
        });

        return button;
    }

    public static void main(String[] args) {

        launch(args);
    }
}