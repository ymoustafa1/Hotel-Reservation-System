package Dasboards;

import app.SceneManager;
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

import util.SidebarGuest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomBrowseView extends Application {

    private Guest guest;
    private FlowPane roomContainer;
    private LocalDate selectedStart;
    private LocalDate selectedEnd;

    public RoomBrowseView() {}

    public RoomBrowseView(Guest guest) {
        this.guest = guest;
    }

    @Override
    public void start(Stage stage) {

        if (guest == null) {
            guest = HotelDatabase.findGuest("youssef");
        }

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        root.setLeft(SidebarGuest.createSidebar("View Rooms"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setCenter(scroll);

        Label title = new Label("Browse Rooms");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("Select your dates to see available rooms.");
        subtitle.getStyleClass().add("subtitle-label");

        // ---------------- DATE PICKER ROW ----------------

        HBox dateRow = new HBox(15);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        DatePicker checkInPicker = new DatePicker();
        checkInPicker.setPromptText("Check In");
        checkInPicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        DatePicker checkOutPicker = new DatePicker();
        checkOutPicker.setPromptText("Check Out");
        checkOutPicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate minDate = checkInPicker.getValue() != null
                        ? checkInPicker.getValue().plusDays(1)
                        : LocalDate.now().plusDays(1);
                setDisable(empty || date.isBefore(minDate));
            }
        });



        Label dateError = new Label();
        dateError.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 13;");
        dateError.setVisible(false);
        dateError.setManaged(false);

        dateRow.getChildren().addAll(
                new Label("Check In:"), checkInPicker,
                new Label("Check Out:"), checkOutPicker

        );


        HBox filters = new HBox(15);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setVisible(false);
        filters.setManaged(false);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().add("All");

        for (RoomType rt : HotelDatabase.roomTypes) {

            typeCombo.getItems().add(
                    rt.getName()
            );
        }
        typeCombo.setValue("All");
        typeCombo.setPrefWidth(160);

        ComboBox<String> amenityCombo = new ComboBox<>();
        amenityCombo.getItems().add("All");

        for (Amenity amenity : HotelDatabase.amenities) {

            amenityCombo.getItems().add(
                    amenity.getName()
            );
        }
        amenityCombo.setValue("All");
        amenityCombo.setPrefWidth(160);

        TextField priceField = new TextField();
        priceField.setPromptText("Max Price");
        priceField.setPrefWidth(160);


        Button clearFilterBtn = new Button("Clear");
        clearFilterBtn.getStyleClass().add("secondary-button");

        filters.getChildren().addAll(
                new Label("Type:"), typeCombo,
                new Label("Amenity:"), amenityCombo,
                new Label("Max Price:"), priceField,
                 clearFilterBtn
        );

        // ---------------- ROOM CONTAINER ----------------

        roomContainer = new FlowPane();
        roomContainer.setAlignment(Pos.TOP_LEFT);
        roomContainer.setHgap(20);
        roomContainer.setVgap(20);
        roomContainer.setPadding(new Insets(10));
        roomContainer.setPrefWrapLength(1100);

        Label placeholderLabel = new Label("Choose your check-in and check-out dates to see available rooms.");
        placeholderLabel.getStyleClass().add("subtitle-label");
        roomContainer.getChildren().add(placeholderLabel);


        Runnable applyFilters = () -> {
            if (selectedStart == null || selectedEnd == null) return;

            roomContainer.getChildren().clear();

            String selType = typeCombo.getValue();
            String selAmenity = amenityCombo.getValue();
            String priceText = priceField.getText().trim();

            List<Room> available = new ArrayList<>();
            for (Room r : HotelDatabase.rooms) {
                if (!r.isAvailable(selectedStart, selectedEnd)) continue;

                if (!selType.equals("All") && !r.getRoomType().getName().equalsIgnoreCase(selType))
                    continue;

                if (!selAmenity.equals("All")) {
                    boolean hasAmenity = false;
                    for (Amenity a : r.getAmenities()) {
                        if (a.getName().equalsIgnoreCase(selAmenity)) { hasAmenity = true; break; }
                    }
                    if (!hasAmenity) continue;
                }

                if (!priceText.isEmpty()) {
                    try {
                        double maxPrice = Double.parseDouble(priceText);
                        if (r.getPrice() > maxPrice) continue;
                    } catch (NumberFormatException ignored) {}
                }

                available.add(r);
            }

            if (available.isEmpty()) {
                Label noRooms = new Label("No available rooms match your criteria.");
                noRooms.getStyleClass().add("subtitle-label");
                roomContainer.getChildren().add(noRooms);
            } else {
                for (Room r : available) {
                    roomContainer.getChildren().add(createRoomCard(r, stage));
                }
            }
        };
        Runnable validateAndRefresh = () -> {

            dateError.setVisible(false);
            dateError.setManaged(false);

            LocalDate start =
                    checkInPicker.getValue();

            LocalDate end =
                    checkOutPicker.getValue();

            if (start == null || end == null) {

                roomContainer.getChildren().clear();

                Label placeholder =
                        new Label(
                                "Choose your check-in and check-out dates to see available rooms."
                        );

                placeholder.getStyleClass()
                        .add("subtitle-label");

                roomContainer.getChildren()
                        .add(placeholder);

                filters.setVisible(false);
                filters.setManaged(false);

                return;
            }

            if (!start.isBefore(end)) {

                dateError.setText(
                        "Check-out must be after check-in."
                );

                dateError.setVisible(true);
                dateError.setManaged(true);

                return;
            }

            if (start.isBefore(LocalDate.now())) {

                dateError.setText(
                        "Check-in cannot be in the past."
                );

                dateError.setVisible(true);
                dateError.setManaged(true);

                return;
            }

            selectedStart = start;
            selectedEnd = end;

            filters.setVisible(true);
            filters.setManaged(true);

            applyFilters.run();
        };
        checkInPicker.valueProperty()
                .addListener(
                        (a,b,c) ->
                                validateAndRefresh.run()
                );

        checkOutPicker.valueProperty()
                .addListener(
                        (a,b,c) ->
                                validateAndRefresh.run()
                );

        typeCombo.valueProperty()
                .addListener(
                        (a,b,c) ->
                                applyFilters.run()
                );

        amenityCombo.valueProperty()
                .addListener(
                        (a,b,c) ->
                                applyFilters.run()
                );

        priceField.textProperty()
                .addListener(
                        (a,b,c) ->
                                applyFilters.run()
                );



        clearFilterBtn.setOnAction(e -> {
            typeCombo.setValue("All");
            amenityCombo.setValue("All");
            priceField.clear();
            applyFilters.run();
        });

        centerArea.getChildren().addAll(
                title, subtitle, dateRow, dateError, filters, roomContainer
        );

        stage.setScene(scene);
        stage.setTitle("Browse Rooms");
        stage.setMaximized(true);
        stage.show();
    }


    private VBox createRoomCard(Room room, Stage stage) {

        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.getStyleClass().add("card");

        String typeName = room.getRoomType().getName().toLowerCase();
        String roomTypeName =
                room.getRoomType()
                        .getName();

        String imagePath =
                "/" +
                        roomTypeName
                                .replaceAll("\\s+", "")
                                .toLowerCase()
                        +
                        ".jpg";
        Image image;

        try {

            image = new Image(
                    getClass()
                            .getResourceAsStream(
                                    imagePath
                            )
            );

            if (image.isError()) {

                throw new Exception();
            }

        } catch (Exception e) {

            image = new Image(
                    getClass()
                            .getResourceAsStream(
                                    "/placeholder.jpg"
                            )
            );
        }

        ImageView roomImage =
                new ImageView(image);

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

        Label roomName = new Label(room.getRoomType().getName() + " Room");
        roomName.getStyleClass().add("section-title");

        Label roomNumBadge = new Label("# " + room.getRoomId());
        roomNumBadge.setStyle(
                "-fx-background-color: #E9EEF5; -fx-text-fill: #1E3A5F;" +
                        "-fx-padding: 2 8; -fx-background-radius: 6;" +
                        "-fx-font-size: 12; -fx-font-weight: bold;"
        );

        nameBadgeRow.getChildren().addAll(roomName, roomNumBadge);

        StringBuilder amenityText = new StringBuilder();
        for (int i = 0; i < room.getAmenities().size(); i++) {
            amenityText.append(room.getAmenities().get(i).getName());
            if (i < room.getAmenities().size() - 1) amenityText.append(" • ");
        }

        Label amenities = new Label(amenityText.toString());
        amenities.getStyleClass().add("subtitle-label");

        Label roomPrice = new Label("$" + room.getPrice() + " / night");
        roomPrice.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #166534;");

        Button reserveBtn = new Button("Reserve");
        reserveBtn.getStyleClass().add("button");

        reserveBtn.setOnAction(e -> openReservationFlow(room, stage));

        HBox bottomSection = new HBox();
        bottomSection.setAlignment(Pos.CENTER_LEFT);

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        bottomSection.getChildren().addAll(roomPrice, push, reserveBtn);

        card.getChildren().addAll(roomImage, nameBadgeRow, amenities, bottomSection);

        return card;
    }

    // ---------------- RESERVATION FLOW ----------------

    private void openReservationFlow(Room room, Stage stage) {

        Reservation[] resHolder = {null};

        Stage popup = new Stage();
        popup.setTitle("Reserve - " + room.getRoomType().getName() + " Room");
        popup.setResizable(false);

        VBox popupRoot = new VBox(18);
        popupRoot.setPadding(new Insets(25));
        popupRoot.getStyleClass().add("card");
        popupRoot.setStyle("-fx-cursor: default;");

        Label popupTitle = new Label("Reserve " + room.getRoomType().getName() + " Room");
        popupTitle.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Label datesLabel = new Label(
                "📅 " + selectedStart + "  →  " + selectedEnd
        );
        datesLabel.getStyleClass().add("subtitle-label");

        Label priceLabel = new Label(
                "Price: $" + room.getPrice() + " / night"
        );
        priceLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #166534; -fx-font-weight: bold;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 13;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // ---------------- EXTRA AMENITIES ----------------

        Label amenityTitle = new Label("Extra Amenities");
        amenityTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        VBox amenityList = new VBox(8);

        List<Amenity> extraAmenities = new ArrayList<>();

        List<Amenity> allAmenities = HotelDatabase.amenities;

        for (Amenity amenity : allAmenities) {
            boolean alreadyOnRoom = room.getAmenities().stream()
                    .anyMatch(a -> a.getName().equalsIgnoreCase(amenity.getName()));

            CheckBox cb = new CheckBox(
                    amenity.getName() + "  (+$" + amenity.getPrice() + ")"
            );
            cb.setDisable(alreadyOnRoom);

            if (alreadyOnRoom) {
                cb.setText(amenity.getName() + "  (included)");
                cb.setSelected(true);
            }

            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    if (!extraAmenities.contains(amenity)) {
                        extraAmenities.add(amenity);
                    }
                } else {
                    extraAmenities.remove(amenity);
                }
            });

            amenityList.getChildren().add(cb);
        }

        // ---------------- PAYMENT METHOD ----------------

        Label paymentTitle = new Label("Payment Method");
        paymentTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        ComboBox<PaymentMethod> paymentCombo = new ComboBox<>();
        paymentCombo.getItems().addAll(PaymentMethod.values());
        paymentCombo.setPromptText("Select payment method");
        paymentCombo.setPrefWidth(260);

        // ---------------- CONFIRM BUTTON ----------------

        Button confirmBtn = new Button("Confirm Reservation");
        confirmBtn.getStyleClass().add("button");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("danger-button");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.getChildren().addAll(cancelBtn, confirmBtn);

        cancelBtn.setOnAction(e -> popup.close());

        confirmBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            if (paymentCombo.getValue() == null) {
                errorLabel.setText("Please select a payment method.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            try {
                Reservation res = guest.makeReservation(room, selectedStart, selectedEnd);
                resHolder[0] = res;

                for (Amenity a : extraAmenities) {
                    res.addExtraAmenity(a);
                }

                Invoice invoice = new Invoice(res, paymentCombo.getValue());
                res.setInvoice(invoice);
                guest.checkout(res, paymentCombo.getValue());

                popup.close();
                showInvoicePopup(invoice, stage);

            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        });

        ScrollPane popupScroll = new ScrollPane(popupRoot);
        popupScroll.setFitToWidth(true);
        popupScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        popupScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        popupRoot.getChildren().addAll(
                popupTitle,
                datesLabel,
                priceLabel,
                new Separator(),
                amenityTitle,
                amenityList,
                new Separator(),
                paymentTitle,
                paymentCombo,
                errorLabel,
                btnRow
        );

        Scene popupScene = new Scene(popupScroll, 440, 560);
        popupScene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        popup.setScene(popupScene);
        popup.show();
    }


    private void showInvoicePopup(Invoice invoice, Stage stage) {

        Stage popup = new Stage();
        popup.setTitle("Reservation Confirmed");
        popup.setResizable(false);

        VBox root = new VBox(18);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("card");

        Label title = new Label("✅ Reservation Confirmed!");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #166534;");

        GridPane details = new GridPane();
        details.setHgap(30);
        details.setVgap(12);

        addDetailRow(details, "Invoice ID", "INV-" + invoice.getInvoiceId(), 0);
        addDetailRow(details, "Room", invoice.getReservation().getRoom().getRoomType().getName() + " Room", 1);
        addDetailRow(details, "Check In", invoice.getReservation().getCheckInDate().toString(), 2);
        addDetailRow(details, "Check Out", invoice.getReservation().getCheckOutDate().toString(), 3);
        addDetailRow(details, "Payment", invoice.getPaymentMethod().toString(), 4);
        addDetailRow(details, "Total", "$" + String.format("%.2f", invoice.getTotalAmount()), 5);

        Button closeBtn = new Button("Done");
        closeBtn.getStyleClass().add("button");
        closeBtn.setOnAction(e -> {
            popup.close();
            SceneManager.switchToDashboard(new ReservationDashboard(guest));
        });

        root.getChildren().addAll(title, details, closeBtn);

        Scene scene = new Scene(root, 420, 360);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        popup.setScene(scene);
        popup.show();
    }

    private void addDetailRow(GridPane grid, String label, String value, int row) {
        Label l = new Label(label);
        l.getStyleClass().add("section-title");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14; -fx-text-fill: #374151;");

        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }
}