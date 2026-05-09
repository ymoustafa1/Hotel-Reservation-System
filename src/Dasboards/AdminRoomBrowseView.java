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
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.*;
import util.SidebarAdmin;

import java.util.ArrayList;
import java.util.List;

public class AdminRoomBrowseView extends Application {

    private FlowPane roomContainer;
    private Runnable applyFilters;

    public AdminRoomBrowseView() {}

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        root.setLeft(SidebarAdmin.createSidebar("Browse Rooms"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scroll);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Browse Rooms");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label("View, filter, and manage all hotel rooms.");
        subtitle.getStyleClass().add("subtitle-label");
        titleBox.getChildren().addAll(title, subtitle);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button addRoomBtn = new Button("+ Add Room");
        addRoomBtn.getStyleClass().add("button");
        addRoomBtn.setOnAction(e -> openAddRoomDialog(stage));

        titleRow.getChildren().addAll(titleBox, titleSpacer, addRoomBtn);

        HBox filters = new HBox(15);
        filters.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().add("All");
        for (RoomType rt : HotelDatabase.roomTypes) {
            typeCombo.getItems().add(rt.getName());
        }
        typeCombo.setValue("All");
        typeCombo.setPrefWidth(160);

        ComboBox<String> amenityCombo = new ComboBox<>();
        amenityCombo.getItems().add("All");
        for (Amenity a : HotelDatabase.amenities) {
            amenityCombo.getItems().add(a.getName());
        }
        amenityCombo.setValue("All");
        amenityCombo.setPrefWidth(160);

        TextField priceField = new TextField();
        priceField.setPromptText("Max Price");
        priceField.setPrefWidth(130);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("All", "Available", "Occupied");
        statusCombo.setValue("All");
        statusCombo.setPrefWidth(130);

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("secondary-button");

        filters.getChildren().addAll(
                new Label("Type:"), typeCombo,
                new Label("Amenity:"), amenityCombo,
                new Label("Max Price:"), priceField,
                new Label("Status:"), statusCombo,
                clearBtn
        );

        roomContainer = new FlowPane();
        roomContainer.setAlignment(Pos.TOP_LEFT);
        roomContainer.setHgap(20);
        roomContainer.setVgap(20);
        roomContainer.setPadding(new Insets(10));
        roomContainer.setPrefWrapLength(1100);

        applyFilters = () -> {
            roomContainer.getChildren().clear();

            String selType    = typeCombo.getValue();
            String selAmenity = amenityCombo.getValue();
            String priceText  = priceField.getText().trim();
            String selStatus  = statusCombo.getValue();

            List<Room> filtered = new ArrayList<>();

            for (Room r : HotelDatabase.rooms) {

                if (!selType.equals("All") && !r.getRoomType().getName().equalsIgnoreCase(selType))
                    continue;

                if (!selAmenity.equals("All")) {
                    boolean has = false;
                    for (Amenity a : r.getAmenities()) {
                        if (a.getName().equalsIgnoreCase(selAmenity)) { has = true; break; }
                    }
                    if (!has) continue;
                }

                if (!priceText.isEmpty()) {
                    try {
                        double maxPrice = Double.parseDouble(priceText);
                        if (r.getPrice() > maxPrice) continue;
                    } catch (NumberFormatException ignored) {}
                }

                if (!selStatus.equals("All")) {
                    boolean occupied = HotelDatabase.reservations.stream()
                            .anyMatch(res -> res.getRoom() != null
                                    && res.getRoom().getRoomId() == r.getRoomId()
                                    && res.getStatus() == ReservationStatus.RESERVED);
                    if (selStatus.equals("Occupied") && !occupied) continue;
                    if (selStatus.equals("Available") && occupied) continue;
                }

                filtered.add(r);
            }

            if (filtered.isEmpty()) {
                Label noRooms = new Label("No rooms match your criteria.");
                noRooms.getStyleClass().add("subtitle-label");
                roomContainer.getChildren().add(noRooms);
            } else {
                for (Room r : filtered) {
                    roomContainer.getChildren().add(createRoomCard(r, stage));
                }
            }
        };

        applyFilters.run();

        typeCombo.valueProperty().addListener((a, b, c) -> applyFilters.run());
        amenityCombo.valueProperty().addListener((a, b, c) -> applyFilters.run());
        priceField.textProperty().addListener((a, b, c) -> applyFilters.run());
        statusCombo.valueProperty().addListener((a, b, c) -> applyFilters.run());

        clearBtn.setOnAction(e -> {
            typeCombo.setValue("All");
            amenityCombo.setValue("All");
            priceField.clear();
            statusCombo.setValue("All");
        });

        centerArea.getChildren().addAll(titleRow, filters, roomContainer);

        stage.setScene(scene);
        stage.setTitle("Browse Rooms");
        stage.setMaximized(true);
        stage.show();
    }

    private VBox createRoomCard(Room room, Stage owner) {

        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor: hand;");

        String roomTypeName = room.getRoomType().getName();
        Image image = null;

        String storedPath = room.getRoomType().getImagePath();
        if (storedPath != null) {
            try {
                java.io.File f = new java.io.File(storedPath);
                if (f.exists()) image = new Image(f.toURI().toString());
            } catch (Exception ignored) {}
        }

        if (image == null || image.isError()) {
            for (String candidate : new String[]{
                    "/" + roomTypeName.replaceAll("\\s+", "").toLowerCase() + ".jpg",
                    "/" + roomTypeName.replaceAll("\\s+", "").toLowerCase() + ".png",
                    "/" + roomTypeName + ".jpg",
                    "/" + roomTypeName + ".png"
            }) {
                try {
                    var stream = getClass().getResourceAsStream(candidate);
                    if (stream != null) { image = new Image(stream); if (!image.isError()) break; }
                } catch (Exception ignored) {}
            }
        }

        if (image == null || image.isError()) {
            try { image = new Image(getClass().getResourceAsStream("/placeholder.jpg")); }
            catch (Exception ignored) {}
        }

        ImageView roomImage = new ImageView(image);
        roomImage.setFitWidth(290);
        roomImage.setFitHeight(170);
        roomImage.setPreserveRatio(false);
        roomImage.setSmooth(true);

        Rectangle clip = new Rectangle(290, 170);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        roomImage.setClip(clip);

        HBox nameBadgeRow = new HBox(8);
        nameBadgeRow.setAlignment(Pos.CENTER_LEFT);

        Label roomName = new Label(roomTypeName + " Room");
        roomName.getStyleClass().add("section-title");

        Label roomNumBadge = new Label("# " + room.getRoomId());
        roomNumBadge.setStyle(
                "-fx-background-color: #E9EEF5; -fx-text-fill: #1E3A5F;" +
                        "-fx-padding: 2 8; -fx-background-radius: 6;" +
                        "-fx-font-size: 12; -fx-font-weight: bold;"
        );

        boolean occupied = HotelDatabase.reservations.stream()
                .anyMatch(res -> res.getRoom() != null
                        && res.getRoom().getRoomId() == room.getRoomId()
                        && res.getStatus() == ReservationStatus.RESERVED);

        Label statusBadge = new Label(occupied ? "Occupied" : "Available");
        statusBadge.setStyle(occupied
                ? "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;" +
                  "-fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 12; -fx-font-weight: bold;"
                : "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;" +
                  "-fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 12; -fx-font-weight: bold;"
        );

        nameBadgeRow.getChildren().addAll(roomName, roomNumBadge, statusBadge);

        StringBuilder amenityText = new StringBuilder();
        for (int i = 0; i < room.getAmenities().size(); i++) {
            amenityText.append(room.getAmenities().get(i).getName());
            if (i < room.getAmenities().size() - 1) amenityText.append(" • ");
        }

        Label amenities = new Label(amenityText.toString());
        amenities.getStyleClass().add("subtitle-label");
        amenities.setWrapText(true);

        Label roomPrice = new Label("$" + room.getRoomType().getBasePrice() + " / night");
        roomPrice.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #166534;");

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setStyle(deleteBtn.getStyle() + "-fx-cursor: hand;");

        Label confirmLabel = new Label("Delete this room?");
        confirmLabel.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 12;");
        confirmLabel.setVisible(false);
        confirmLabel.setManaged(false);

        Button confirmDelete = new Button("Confirm");
        confirmDelete.getStyleClass().add("danger-button");
        confirmDelete.setVisible(false);
        confirmDelete.setManaged(false);

        Button cancelDelete = new Button("No");
        cancelDelete.getStyleClass().add("secondary-button");
        cancelDelete.setVisible(false);
        cancelDelete.setManaged(false);

        deleteBtn.setOnAction(e -> {
            e.consume();
            confirmLabel.setVisible(true);
            confirmLabel.setManaged(true);
            confirmDelete.setVisible(true);
            confirmDelete.setManaged(true);
            cancelDelete.setVisible(true);
            cancelDelete.setManaged(true);
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        });

        cancelDelete.setOnAction(e -> {
            e.consume();
            confirmLabel.setVisible(false);
            confirmLabel.setManaged(false);
            confirmDelete.setVisible(false);
            confirmDelete.setManaged(false);
            cancelDelete.setVisible(false);
            cancelDelete.setManaged(false);
            deleteBtn.setVisible(true);
            deleteBtn.setManaged(true);
        });

        confirmDelete.setOnAction(e -> {
            e.consume();
            HotelDatabase.rooms.remove(room);
            applyFilters.run();
        });

        HBox confirmRow = new HBox(8);
        confirmRow.setAlignment(Pos.CENTER_LEFT);
        confirmRow.getChildren().addAll(confirmLabel, confirmDelete, cancelDelete);

        HBox bottomSection = new HBox();
        bottomSection.setAlignment(Pos.CENTER_LEFT);

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        bottomSection.getChildren().addAll(roomPrice, push, deleteBtn);

        card.getChildren().addAll(roomImage, nameBadgeRow, amenities, confirmRow, bottomSection);

        card.setOnMouseClicked(e -> openRoomDetailDialog(room, owner));

        return card;
    }

    private void openRoomDetailDialog(Room room, Stage owner) {

        Stage dialog = new Stage();
        dialog.setTitle("Room #" + room.getRoomId() + " Details");
        dialog.setResizable(false);
        dialog.initOwner(owner);

        VBox root = new VBox(16);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("card");

        Label title = new Label(room.getRoomType().getName() + " Room  —  #" + room.getRoomId());
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setHgap(30);
        details.setVgap(12);

        boolean occupied = HotelDatabase.reservations.stream()
                .anyMatch(res -> res.getRoom() != null
                        && res.getRoom().getRoomId() == room.getRoomId()
                        && res.getStatus() == ReservationStatus.RESERVED);

        String currentGuest = HotelDatabase.reservations.stream()
                .filter(res -> res.getRoom() != null
                        && res.getRoom().getRoomId() == room.getRoomId()
                        && res.getStatus() == ReservationStatus.RESERVED)
                .map(res -> res.getGuest().getUsername())
                .findFirst().orElse("—");

        String checkOut = HotelDatabase.reservations.stream()
                .filter(res -> res.getRoom() != null
                        && res.getRoom().getRoomId() == room.getRoomId()
                        && res.getStatus() == ReservationStatus.RESERVED)
                .map(res -> res.getCheckOutDate().toString())
                .findFirst().orElse("—");

        StringBuilder amenityText = new StringBuilder();
        List<Amenity> amenities = room.getAmenities();
        for (int i = 0; i < amenities.size(); i++) {
            amenityText.append(amenities.get(i).getName());
            if (i < amenities.size() - 1) amenityText.append(", ");
        }

        addDetailRow(details, "Room ID",    String.valueOf(room.getRoomId()), 0);
        addDetailRow(details, "Type",       room.getRoomType().getName(), 1);
        addDetailRow(details, "Base Price", "$" + room.getRoomType().getBasePrice() + " / night", 2);
        addDetailRow(details, "Price",      "$" + room.getPrice() + " / night", 3);
        addDetailRow(details, "Status",     occupied ? "Occupied" : "Available", 4);
        addDetailRow(details, "Guest",      currentGuest, 5);
        addDetailRow(details, "Check-out",  checkOut, 6);
        addDetailRow(details, "Amenities",  amenityText.length() > 0 ? amenityText.toString() : "None", 7);

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("button");
        closeBtn.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox();
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.getChildren().add(closeBtn);

        root.getChildren().addAll(title, new Separator(), details, btnRow);

        Scene scene = new Scene(root, 460, 420);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        dialog.setScene(scene);
        dialog.show();
    }

    private void addDetailRow(GridPane grid, String label, String value, int row) {
        Label l = new Label(label);
        l.getStyleClass().add("section-title");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14; -fx-text-fill: #374151;");

        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private void openAddRoomDialog(Stage owner) {

        Stage dialog = new Stage();
        dialog.setTitle("Add New Room");
        dialog.setResizable(false);
        dialog.initOwner(owner);

        VBox root = new VBox(16);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("card");

        Label title = new Label("Add New Room");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(14);

        Label typeLabel = new Label("Room Type:");
        typeLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<RoomType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(HotelDatabase.roomTypes);
        typeCombo.setPromptText("Select room type");
        typeCombo.setPrefWidth(240);
        typeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(RoomType rt) { return rt == null ? "" : rt.getName(); }
            @Override public RoomType fromString(String s) { return null; }
        });

        Label priceLabel = new Label("Base Price:");
        priceLabel.setStyle("-fx-font-weight: bold;");

        Label priceDisplay = new Label("—");
        priceDisplay.setStyle("-fx-font-size: 14; -fx-text-fill: #166634; -fx-font-weight: bold;");

        Label amenityLabel = new Label("Amenities:");
        amenityLabel.setStyle("-fx-font-weight: bold;");

        Label amenityDisplay = new Label("Select a room type first.");
        amenityDisplay.getStyleClass().add("subtitle-label");
        amenityDisplay.setWrapText(true);
        amenityDisplay.setPrefWidth(240);

        typeCombo.valueProperty().addListener((a, b, selectedType) -> {
            if (selectedType == null) {
                priceDisplay.setText("—");
                amenityDisplay.setText("Select a room type first.");
                return;
            }
            priceDisplay.setText("$" + selectedType.getBasePrice() + " / night");
            List<Amenity> amenities = selectedType.getAmenities();
            if (amenities == null || amenities.isEmpty()) {
                amenityDisplay.setText("No amenities for this type.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < amenities.size(); i++) {
                    sb.append(amenities.get(i).getName());
                    if (i < amenities.size() - 1) sb.append(" • ");
                }
                amenityDisplay.setText(sb.toString());
            }
        });

        form.add(typeLabel,    0, 0); form.add(typeCombo,      1, 0);
        form.add(priceLabel,   0, 1); form.add(priceDisplay,   1, 1);
        form.add(amenityLabel, 0, 2); form.add(amenityDisplay, 1, 2);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 13;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button saveBtn = new Button("Add Room");
        saveBtn.getStyleClass().add("button");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.getChildren().addAll(cancelBtn, saveBtn);

        saveBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            RoomType selectedType = typeCombo.getValue();

            if (selectedType == null) {
                errorLabel.setText("Please select a room type.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            int newId = HotelDatabase.rooms.stream()
                    .mapToInt(Room::getRoomId).max().orElse(0) + 1;

            Room newRoom = new Room(newId, selectedType);
            for (Amenity a : selectedType.getAmenities()) {
                newRoom.addAmenity(a);
            }

            HotelDatabase.rooms.add(newRoom);
            HotelDatabase.insertRoom(newRoom);
            dialog.close();
            applyFilters.run();
        });

        root.getChildren().addAll(title, form, errorLabel, btnRow);

        Scene scene = new Scene(root, 500, 280);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        dialog.setScene(scene);
        dialog.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}