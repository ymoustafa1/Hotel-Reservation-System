package Dasboards;

import app.SessionManager;
import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.*;
import util.SidebarReceptionist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistWalkInView extends Application {

    public ReceptionistWalkInView() {}

    @Override
    public void start(Stage stage) {

        Receptionist receptionist = (Receptionist) SessionManager.getCurrentUser();

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.setLeft(SidebarReceptionist.createSidebar("Walk-in Check In"));

        VBox centerArea = new VBox(25);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);

        VBox pageHeader = new VBox(4);
        Label pageTitle = new Label("Walk-in Check In");
        pageTitle.getStyleClass().add("title-label");
        Label pageSub = new Label("Create a reservation and immediately check in a walk-in guest.");
        pageSub.getStyleClass().add("subtitle-label");
        pageHeader.getChildren().addAll(pageTitle, pageSub);

        VBox step1Card = buildSectionCard("Step 1 — Guest, Dates & Room");

        GridPane step1Form = new GridPane();
        step1Form.setHgap(20); step1Form.setVgap(14);

        Label guestLbl = boldLabel("Guest Username:");
        TextField guestField = new TextField();
        guestField.setPromptText("Enter guest username");
        guestField.setPrefWidth(220);

        Button registerBtn = new Button("+ Register New Guest");
        registerBtn.getStyleClass().add("secondary-button");

        HBox guestRow = new HBox(10, guestField, registerBtn);
        guestRow.setAlignment(Pos.CENTER_LEFT);

        Label guestInfoLbl = new Label();
        guestInfoLbl.setStyle("-fx-font-size: 12;");

        Label startLbl = boldLabel("Check-in Date:");
        DatePicker startPicker = new DatePicker(LocalDate.now());

        Label endLbl = boldLabel("Check-out Date:");
        DatePicker endPicker = new DatePicker(LocalDate.now().plusDays(1));

        Label roomLbl = boldLabel("Room:");
        ComboBox<Room> roomCombo = new ComboBox<>();
        roomCombo.setPromptText("Select a room");
        roomCombo.setPrefWidth(280);
        roomCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Room r) {
                return r == null ? "" : "#" + r.getRoomId() + " — " + r.getRoomType().getName()
                                        + "  ($" + r.getRoomType().getBasePrice() + "/night)";
            }
            @Override public Room fromString(String s) { return null; }
        });

        Label roomInfoLbl = new Label();
        roomInfoLbl.setStyle("-fx-text-fill: #1D4ED8; -fx-font-size: 12;");
        roomInfoLbl.setWrapText(true);
        roomInfoLbl.setPrefWidth(340);

        VBox step2Card = buildSectionCard("Step 2 — Extra Amenities (Optional)");

        Label amenityNote = new Label("Only amenities NOT already included in the selected room are shown.");
        amenityNote.getStyleClass().add("subtitle-label");
        amenityNote.setWrapText(true);

        FlowPane amenityPane = new FlowPane();
        amenityPane.setHgap(12); amenityPane.setVgap(12);
        amenityPane.setPadding(new Insets(8, 0, 0, 0));

        List<CheckBox> amenityBoxes = new ArrayList<>();

        Runnable refreshAmenities = () -> {
            amenityPane.getChildren().clear();
            amenityBoxes.clear();
            Room sel = roomCombo.getValue();

            for (Amenity a : HotelDatabase.amenities) {
                if (sel != null && sel.getAmenities().contains(a)) continue;
                CheckBox cb = new CheckBox(a.getName() + "  (+$" + a.getPrice() + ")");
                cb.setUserData(a);
                amenityBoxes.add(cb);
                amenityPane.getChildren().add(cb);
            }

            if (amenityPane.getChildren().isEmpty()) {
                Label none = new Label(sel != null
                        ? "All amenities are already included in this room."
                        : "No extra amenities defined.");
                none.setStyle("-fx-text-fill: gray; -fx-font-size: 12;");
                amenityPane.getChildren().add(none);
            }
        };
        refreshAmenities.run();

        step2Card.getChildren().addAll(amenityNote, amenityPane);

        Runnable refreshRooms = () -> {
            LocalDate s = startPicker.getValue();
            LocalDate e = endPicker.getValue();
            roomCombo.getItems().clear();
            roomCombo.setValue(null);
            roomInfoLbl.setText("");
            if (s == null || e == null || !e.isAfter(s)) return;
            for (Room r : HotelDatabase.rooms) {
                if (r.isAvailable(s, e)) roomCombo.getItems().add(r);
            }
            if (roomCombo.getItems().isEmpty()) {
                roomInfoLbl.setText("No rooms available for these dates.");
                roomInfoLbl.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 12;");
            } else {
                roomInfoLbl.setText(roomCombo.getItems().size() + " room(s) available.");
                roomInfoLbl.setStyle("-fx-text-fill: #166534; -fx-font-size: 12;");
            }
        };
        refreshRooms.run();
        startPicker.valueProperty().addListener((a, b, c) -> refreshRooms.run());
        endPicker.valueProperty().addListener((a, b, c)   -> refreshRooms.run());

        roomCombo.valueProperty().addListener((a, b, sel) -> {
            if (sel == null) { roomInfoLbl.setText(""); refreshAmenities.run(); return; }
            List<Amenity> ams = sel.getAmenities();
            if (ams.isEmpty()) {
                roomInfoLbl.setText("No amenities included in this room.");
            } else {
                StringBuilder sb = new StringBuilder("Included: ");
                for (int i = 0; i < ams.size(); i++) {
                    sb.append(ams.get(i).getName());
                    if (i < ams.size() - 1) sb.append(", ");
                }
                roomInfoLbl.setText(sb.toString());
            }
            roomInfoLbl.setStyle("-fx-text-fill: #1D4ED8; -fx-font-size: 12;");
            refreshAmenities.run();
        });

        guestField.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validateGuestField(guestField, guestInfoLbl, receptionist);
        });
        guestField.setOnAction(e -> validateGuestField(guestField, guestInfoLbl, receptionist));

        registerBtn.setOnAction(e ->
                openRegisterGuestDialog(stage, guestField, guestInfoLbl, receptionist));

        step1Form.add(guestLbl,    0, 0); step1Form.add(guestRow,     1, 0);
        step1Form.add(new Label(), 0, 1); step1Form.add(guestInfoLbl, 1, 1);
        step1Form.add(startLbl,    0, 2); step1Form.add(startPicker,  1, 2);
        step1Form.add(endLbl,      0, 3); step1Form.add(endPicker,    1, 3);
        step1Form.add(roomLbl,     0, 4); step1Form.add(roomCombo,    1, 4);
        step1Form.add(new Label(), 0, 5); step1Form.add(roomInfoLbl,  1, 5);

        step1Card.getChildren().add(step1Form);

        VBox step3Card = buildSectionCard("Step 3 — Payment Method");

        ToggleGroup payGroup = new ToggleGroup();
        RadioButton cashBtn = new RadioButton("💵  Cash");
        cashBtn.setToggleGroup(payGroup); cashBtn.setSelected(true);
        cashBtn.setStyle("-fx-font-size: 14;");
        RadioButton cardBtn = new RadioButton("💳  Credit Card");
        cardBtn.setToggleGroup(payGroup);
        cardBtn.setStyle("-fx-font-size: 14;");

        step3Card.getChildren().add(new HBox(30, cashBtn, cardBtn));

        VBox summaryCard = buildSectionCard("Summary");
        Label summaryPlaceholder = new Label("Fill in the details above to see a summary.");
        summaryPlaceholder.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13;");
        summaryCard.getChildren().add(summaryPlaceholder);

        Runnable updateSummary = () -> {
            summaryCard.getChildren().subList(2, summaryCard.getChildren().size()).clear();
            Room selRoom = roomCombo.getValue();
            LocalDate s  = startPicker.getValue();
            LocalDate e  = endPicker.getValue();
            if (selRoom == null || s == null || e == null || !e.isAfter(s)) {
                summaryCard.getChildren().add(summaryPlaceholder);
                return;
            }
            long nights  = java.time.temporal.ChronoUnit.DAYS.between(s, e);
            double base  = selRoom.getRoomType().getBasePrice() * nights;
            double extras = amenityBoxes.stream()
                    .filter(CheckBox::isSelected)
                    .mapToDouble(cb -> ((Amenity) cb.getUserData()).getPrice())
                    .sum();

            GridPane sg = new GridPane();
            sg.setHgap(40); sg.setVgap(10);
            String gText = guestField.getText().trim().isEmpty() ? "—" : guestField.getText().trim();
            addSummaryRow(sg, "Guest",     gText, 0);
            addSummaryRow(sg, "Room",      "#" + selRoom.getRoomId() + " — " + selRoom.getRoomType().getName(), 1);
            addSummaryRow(sg, "Nights",    String.valueOf(nights), 2);
            addSummaryRow(sg, "Base Cost", "$" + String.format("%.2f", base), 3);
            addSummaryRow(sg, "Extras",    "$" + String.format("%.2f", extras), 4);
            addSummaryRow(sg, "Total",     "$" + String.format("%.2f", base + extras), 5);
            summaryCard.getChildren().add(sg);
        };

        roomCombo.valueProperty().addListener((a, b, c)   -> updateSummary.run());
        startPicker.valueProperty().addListener((a, b, c) -> updateSummary.run());
        endPicker.valueProperty().addListener((a, b, c)   -> updateSummary.run());
        guestField.textProperty().addListener((a, b, c)   -> updateSummary.run());
        amenityPane.getChildren().addListener(
                (javafx.collections.ListChangeListener<javafx.scene.Node>) ch -> {
                    for (CheckBox cb : amenityBoxes) {
                        cb.selectedProperty().addListener((a, b, c) -> updateSummary.run());
                    }
                });

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 13;");
        errorLabel.setVisible(false); errorLabel.setManaged(false);

        Button submitBtn = new Button("✔  Complete Walk-in Check In");
        submitBtn.getStyleClass().add("button");
        submitBtn.setStyle(submitBtn.getStyle() + "-fx-font-size: 15; -fx-padding: 12 28;");

        submitBtn.setOnAction(ev -> {
            errorLabel.setVisible(false); errorLabel.setManaged(false);

            String username = guestField.getText().trim();
            if (username.isEmpty()) { showErr(errorLabel, "Guest username is required."); return; }

            Guest guest = receptionist.findGuest(username);
            if (guest == null) { showErr(errorLabel, "Guest not found: " + username); return; }

            LocalDate start = startPicker.getValue();
            LocalDate end   = endPicker.getValue();
            if (start == null || end == null || !end.isAfter(start)) {
                showErr(errorLabel, "Check-out date must be after check-in date."); return;
            }

            Room room = roomCombo.getValue();
            if (room == null) { showErr(errorLabel, "Please select a room."); return; }
            if (!room.isAvailable(start, end)) {
                showErr(errorLabel, "Room is no longer available for the selected dates."); return;
            }

            Reservation res = new Reservation(guest, room, start, end);
            HotelDatabase.reservations.add(res);
            for (CheckBox cb : amenityBoxes) {
                if (!cb.isSelected()) continue;
                Amenity a = (Amenity) cb.getUserData();
                if (!room.getAmenities().contains(a) && !res.getExtraAmenities().contains(a))
                    res.addExtraAmenity(a);
            }

            PaymentMethod method = cashBtn.isSelected() ? PaymentMethod.CASH : PaymentMethod.CREDIT_CARD;
            Invoice inv = new Invoice(res, method);
            res.setInvoice(inv);

            try { inv.processPayment(guest, method); }
            catch (Exception ex) {
                HotelDatabase.reservations.remove(res);
                showErr(errorLabel, "Payment failed: " + ex.getMessage()); return;
            }

            receptionist.checkIn(res);
            showSuccessDialog(res, inv, stage);
        });

        HBox submitRow = new HBox(16, errorLabel, submitBtn);
        submitRow.setAlignment(Pos.CENTER_RIGHT);

        centerArea.getChildren().addAll(
                pageHeader, step1Card, step2Card, step3Card, summaryCard, submitRow);

        stage.setScene(scene);
        stage.setTitle("Walk-in Check In");
        stage.setMaximized(true);
        stage.show();
    }


    private void openRegisterGuestDialog(Stage owner, TextField guestField,
                                         Label guestInfoLbl, Receptionist receptionist) {
        Stage dialog = new Stage();
        dialog.setTitle("Register New Guest");
        dialog.setResizable(false);
        dialog.initOwner(owner);

        VBox root = new VBox(16);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("card");

        Label title = new Label("Register New Guest");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(20); form.setVgap(14);

        TextField usernameField = formField("Username");
        TextField passwordField = formField("Password");
        TextField balanceField  = formField("Initial Balance (e.g. 500)");
        TextField dobField      = formField("Date of Birth (YYYY-MM-DD)");
        TextField addressField  = formField("Address");

        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("MALE", "FEMALE");
        genderCombo.setPromptText("Select gender");
        genderCombo.setPrefWidth(240);

        form.add(boldLabel("Username:"), 0, 0); form.add(usernameField, 1, 0);
        form.add(boldLabel("Password:"), 0, 1); form.add(passwordField, 1, 1);
        form.add(boldLabel("Balance:"),  0, 2); form.add(balanceField,  1, 2);
        form.add(boldLabel("DOB:"),      0, 3); form.add(dobField,      1, 3);
        form.add(boldLabel("Address:"),  0, 4); form.add(addressField,  1, 4);
        form.add(boldLabel("Gender:"),   0, 5); form.add(genderCombo,   1, 5);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 13;");
        errorLabel.setVisible(false); errorLabel.setManaged(false);

        Button saveBtn   = new Button("Register Guest"); saveBtn.getStyleClass().add("button");
        Button cancelBtn = new Button("Cancel");         cancelBtn.getStyleClass().add("secondary-button");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            errorLabel.setVisible(false); errorLabel.setManaged(false);

            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (username.isEmpty()) { showErr(errorLabel, "Username is required."); return; }
            if (password.isEmpty()) { showErr(errorLabel, "Password is required."); return; }

            boolean dup = HotelDatabase.guests.stream()
                    .anyMatch(g -> g.getUsername().equalsIgnoreCase(username));
            if (dup) { showErr(errorLabel, "Username already exists."); return; }

            double balance = 0;
            if (!balanceField.getText().trim().isEmpty()) {
                try {
                    balance = Double.parseDouble(balanceField.getText().trim());
                    if (balance < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    showErr(errorLabel, "Enter a valid positive balance."); return;
                }
            }

            LocalDate dob = null;
            if (!dobField.getText().trim().isEmpty()) {
                try { dob = LocalDate.parse(dobField.getText().trim()); }
                catch (Exception ex) { showErr(errorLabel, "Date format must be YYYY-MM-DD."); return; }
            }

            Gender gender = null;
            if (genderCombo.getValue() != null) {
                try { gender = Gender.valueOf(genderCombo.getValue()); }
                catch (Exception ignored) {}
            }

            Guest newGuest = new Guest(username, password, dob, balance,
                    addressField.getText().trim(), gender);
            HotelDatabase.guests.add(newGuest);
            guestField.setText(username);
            guestInfoLbl.setText("✔ Registered & selected: " + username
                    + "  |  Balance: $" + String.format("%.2f", balance));
            guestInfoLbl.setStyle("-fx-text-fill: #166534; -fx-font-size: 12;");

            dialog.close();
        });

        HBox btns = new HBox(10, cancelBtn, saveBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, new Separator(), form, errorLabel, btns);

        Scene scene = new Scene(root, 480, 420);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }


    private void validateGuestField(TextField field, Label infoLbl, Receptionist receptionist) {
        String text = field.getText().trim();
        if (text.isEmpty()) { infoLbl.setText(""); return; }
        Guest g = receptionist.findGuest(text);
        if (g != null) {
            infoLbl.setText("✔ Found: " + g.getUsername()
                    + "  |  Balance: $" + String.format("%.2f", g.getBalance()));
            infoLbl.setStyle("-fx-text-fill: #166534; -fx-font-size: 12;");
        } else {
            infoLbl.setText("✘ Not found — use '+ Register New Guest' to create one.");
            infoLbl.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 12;");
        }
    }

    private void showSuccessDialog(Reservation res, Invoice inv, Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Check-in Successful");
        dialog.setResizable(false);
        dialog.initOwner(owner);

        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.getStyleClass().add("card");
        root.setAlignment(Pos.CENTER);

        Label icon  = new Label("✅"); icon.setStyle("-fx-font-size: 36;");
        Label title = new Label("Walk-in Check In Successful!");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #166534;");

        GridPane details = new GridPane();
        details.setHgap(40); details.setVgap(10);
        Room r = res.getRoom();
        addSummaryRow(details, "Reservation ID", String.valueOf(res.getReservationId()), 0);
        addSummaryRow(details, "Guest",          res.getGuest().getUsername(), 1);
        addSummaryRow(details, "Room",           r != null ? "#" + r.getRoomId() + " — " + r.getRoomType().getName() : "—", 2);
        addSummaryRow(details, "Check-in",       String.valueOf(res.getCheckInDate()), 3);
        addSummaryRow(details, "Check-out",      String.valueOf(res.getCheckOutDate()), 4);
        addSummaryRow(details, "Total Charged",  "$" + String.format("%.2f", inv.getTotalAmount()), 5);
        addSummaryRow(details, "Payment",        inv.getPaymentMethod().toString(), 6);

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("button");
        closeBtn.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(); btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.getChildren().add(closeBtn);

        root.getChildren().addAll(icon, title, new Separator(), details, btnRow);

        Scene scene = new Scene(root, 480, 400);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private VBox buildSectionCard(String titleText) {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20, 24, 20, 24));
        Label lbl = new Label(titleText);
        lbl.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
        card.getChildren().addAll(lbl, new Separator());
        return card;
    }

    private Label boldLabel(String text) {
        Label l = new Label(text); l.setStyle("-fx-font-weight: bold;"); return l;
    }

    private TextField formField(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefWidth(240); return tf;
    }

    private void addSummaryRow(GridPane g, String label, String value, int row) {
        Label l = new Label(label); l.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        Label v = new Label(value); v.setStyle("-fx-font-size: 13; -fx-text-fill: #374151;");
        g.add(l, 0, row); g.add(v, 1, row);
    }

    private void showErr(Label l, String msg) {
        l.setText(msg); l.setVisible(true); l.setManaged(true);
    }

    public static void main(String[] args) { launch(args); }
}