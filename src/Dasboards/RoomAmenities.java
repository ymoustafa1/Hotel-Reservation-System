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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import util.SidebarAdmin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class RoomAmenities extends Application {

    private VBox     roomTypesTableBody;
    private FlowPane roomAmenitiesPane;
    private FlowPane hotelAmenitiesPane;

    // Resources folder — resolved once so we can copy images into it
    private Path resourcesDir;

    public RoomAmenities() {}


    @Override
    public void start(Stage stage) {

        // resolve the resources root (where /style.css lives)
        try {
            var cssUrl = getClass().getResource("/style.css");
            if (cssUrl != null) {
                resourcesDir = Paths.get(cssUrl.toURI()).getParent();
            }
        } catch (Exception ignored) {}

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        root.setLeft(SidebarAdmin.createSidebar("Room Types & Amenities"));

        VBox centerArea = new VBox(30);
        centerArea.getStyleClass().add("dashboard-pane");
        centerArea.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(centerArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);

        VBox pageHeader = new VBox(4);
        Label pageTitle = new Label("Room Types & Amenities Management");
        pageTitle.getStyleClass().add("title-label");
        Label pageSub = new Label("Manage room categories and hotel amenities.");
        pageSub.getStyleClass().add("subtitle-label");
        pageHeader.getChildren().addAll(pageTitle, pageSub);

        VBox roomTypesSection = buildRoomTypesSection(stage);

        roomAmenitiesPane = new FlowPane();
        VBox roomAmenitiesSection = buildAmenitiesSection(
                "Room Amenities", AmenityType.ROOM, stage, roomAmenitiesPane);

        hotelAmenitiesPane = new FlowPane();
        VBox hotelAmenitiesSection = buildAmenitiesSection(
                "Hotel Amenities", AmenityType.HOTEL, stage, hotelAmenitiesPane);

        centerArea.getChildren().addAll(
                pageHeader, roomTypesSection, roomAmenitiesSection, hotelAmenitiesSection);

        stage.setScene(scene);
        stage.setTitle("Room Types & Amenities");
        stage.setMaximized(true);
        stage.show();
    }



    private VBox buildRoomTypesSection(Stage owner) {
        VBox section = new VBox(0);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(0));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 18, 20));

        Label title = new Label("Room Types");
        title.getStyleClass().add("section-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Room Type");
        addBtn.getStyleClass().add("button");
        addBtn.setOnAction(e -> openAddRoomTypeDialog(owner));

        header.getChildren().addAll(title, sp, addBtn);

        GridPane tableHeader = buildRoomTypeHeaderRow();
        roomTypesTableBody = new VBox(0);
        refreshRoomTypesTable(owner);

        section.getChildren().addAll(header, new Separator(), tableHeader, roomTypesTableBody);
        return section;
    }

    private GridPane buildRoomTypeHeaderRow() {
        GridPane g = new GridPane();
        applyRTCols(g);
        String[] cols = {"Type Name", "Base Price", "Amenities", "Actions"};
        for (int i = 0; i < cols.length; i++) {
            Label l = new Label(cols[i]);
            l.setStyle("-fx-background-color:#F9FAFB;-fx-text-fill:#6B7280;" +
                    "-fx-padding:12 16;-fx-font-weight:bold;-fx-font-size:12;");
            l.setMaxWidth(Double.MAX_VALUE);
            g.add(l, i, 0);
        }
        return g;
    }

    private void applyRTCols(GridPane g) {
        g.getColumnConstraints().clear();
        for (double pct : new double[]{25, 15, 45, 15}) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(pct);
            g.getColumnConstraints().add(cc);
        }
    }

    private void refreshRoomTypesTable(Stage owner) {
        roomTypesTableBody.getChildren().clear();
        int[] idx = {0};
        for (RoomType rt : HotelDatabase.roomTypes) {
            String bg   = idx[0] % 2 == 0 ? "white" : "#F9FAFB";
            String cell = "-fx-background-color:" + bg +
                    ";-fx-padding:14 16;-fx-border-color:#F3F4F6;-fx-border-width:0 0 1 0;";

            GridPane row = new GridPane();
            applyRTCols(row);

            row.add(styledCell(rt.getName(), cell + "-fx-font-weight:bold;"), 0, 0);
            row.add(styledCell("$" + rt.getBasePrice(), cell), 1, 0);

            StringBuilder sb = new StringBuilder();
            List<Amenity> ams = rt.getAmenities();
            for (int i = 0; i < ams.size(); i++) {
                sb.append(ams.get(i).getName());
                if (i < ams.size() - 1) sb.append(", ");
            }
            Label amenLbl = styledCell(sb.length() > 0 ? sb.toString() : "—", cell);
            amenLbl.setWrapText(true);
            row.add(amenLbl, 2, 0);

            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_LEFT);
            actions.setStyle(cell);

            Button editBtn = iconBtn("✏", "#2563EB", "#DBEAFE");
            Button delBtn  = iconBtn("🗑", "#DC2626", "#FEE2E2");

            editBtn.setOnAction(e -> openEditRoomTypeDialog(rt, owner));
            delBtn.setOnAction(e -> {
                HotelDatabase.roomTypes.remove(rt);
                refreshRoomTypesTable(owner);
            });

            actions.getChildren().addAll(editBtn, delBtn);
            row.add(actions, 3, 0);
            roomTypesTableBody.getChildren().add(row);
            idx[0]++;
        }
        if (HotelDatabase.roomTypes.isEmpty()) {
            Label empty = new Label("No room types defined.");
            empty.setStyle("-fx-padding:20;-fx-text-fill:gray;");
            roomTypesTableBody.getChildren().add(empty);
        }
    }


    private VBox buildAmenitiesSection(String sectionTitle, AmenityType type,
                                       Stage owner, FlowPane pane) {
        VBox section = new VBox(0);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(0));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 18, 20));

        Label title = new Label(sectionTitle);
        title.getStyleClass().add("section-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Amenity");
        addBtn.getStyleClass().add("button");
        addBtn.setOnAction(e -> openAddAmenityDialog(type, pane, owner));

        header.getChildren().addAll(title, sp, addBtn);

        pane.setHgap(16);
        pane.setVgap(16);
        pane.setPadding(new Insets(20));
        pane.setAlignment(Pos.TOP_LEFT);
        fillAmenityPane(pane, type, owner);

        section.getChildren().addAll(header, new Separator(), pane);
        return section;
    }

    private void fillAmenityPane(FlowPane pane, AmenityType type, Stage owner) {
        pane.getChildren().clear();
        for (Amenity a : HotelDatabase.amenities) {
            if (a.getType() == type)
                pane.getChildren().add(createAmenityChip(a, pane, type, owner));
        }
        if (pane.getChildren().isEmpty()) {
            Label empty = new Label("No amenities yet.");
            empty.setStyle("-fx-text-fill:gray;");
            pane.getChildren().add(empty);
        }
    }

    private HBox createAmenityChip(Amenity amenity, FlowPane pane,
                                   AmenityType type, Stage owner) {
        HBox chip = new HBox(10);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(10, 16, 10, 12));
        chip.setStyle(
                "-fx-background-color:white;-fx-background-radius:8;" +
                        "-fx-border-color:#E5E7EB;-fx-border-radius:8;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),4,0,0,2);"
        );

        ImageView icon = amenityIcon(amenity, 28);
        Label nameLbl = new Label(amenity.getName());
        nameLbl.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-text-fill:#111827;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button editBtn = iconBtn("✏", "#2563EB", "#DBEAFE");
        Button delBtn  = iconBtn("🗑", "#DC2626", "#FEE2E2");

        editBtn.setOnAction(e -> openEditAmenityDialog(amenity, pane, type, owner));
        delBtn.setOnAction(e -> {
            HotelDatabase.amenities.remove(amenity);
            // also remove from any room types that reference it
            for (RoomType rt : HotelDatabase.roomTypes)
                rt.getAmenities().remove(amenity);
            fillAmenityPane(pane, type, owner);
            refreshRoomTypesTable(owner);
        });

        chip.getChildren().addAll(icon, nameLbl, sp, editBtn, delBtn);
        return chip;
    }



    private void openAddRoomTypeDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Add Room Type");
        dialog.initOwner(owner);
        dialog.setResizable(true);

        VBox root = dialogRoot();

        Label ttl = dialogTitle("Add Room Type");
        root.getChildren().addAll(ttl, new Separator());

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(14);

        TextField nameField  = formField("e.g. Deluxe");
        TextField priceField = formField("e.g. 200");

        // image picker
        File[] chosen = {null};
        Label imgStatus = new Label("No image selected");
        imgStatus.setStyle("-fx-text-fill:gray;-fx-font-size:12;");
        HBox imgRow = imagePickerRow(dialog, chosen, imgStatus);

        form.add(boldLbl("Name:"),       0, 0); form.add(nameField,  1, 0);
        form.add(boldLbl("Base Price:"), 0, 1); form.add(priceField, 1, 1);
        form.add(boldLbl("Image:"),      0, 2); form.add(imgRow,     1, 2);

        // amenity checklist
        Label amenHeader = new Label("Amenities:");
        amenHeader.setStyle("-fx-font-weight:bold;");

        VBox checkList = new VBox(8);
        checkList.setPadding(new Insets(0, 0, 0, 4));
        List<CheckBox> boxes = new ArrayList<>();
        for (Amenity a : HotelDatabase.amenities) {
            CheckBox cb = new CheckBox(a.getName() + "  ($" + a.getPrice() + ")");
            cb.setUserData(a);
            boxes.add(cb);
            checkList.getChildren().add(cb);
        }
        if (HotelDatabase.amenities.isEmpty()) {
            checkList.getChildren().add(new Label("No amenities available."));
        }

        ScrollPane checkScroll = new ScrollPane(checkList);
        checkScroll.setFitToWidth(true);
        checkScroll.setPrefHeight(140);
        checkScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        checkScroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");

        Label error = errorLabel();

        Button save   = new Button("Add Room Type"); save.getStyleClass().add("button");
        Button cancel = new Button("Cancel");        cancel.getStyleClass().add("secondary-button");
        cancel.setOnAction(e -> dialog.close());

        save.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showErr(error, "Name is required."); return; }
            double price;
            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) { showErr(error, "Enter a valid positive price."); return; }
            boolean dup = HotelDatabase.roomTypes.stream()
                    .anyMatch(rt -> rt.getName().equalsIgnoreCase(name));
            if (dup) { showErr(error, "That name already exists."); return; }

            RoomType rt = new RoomType(name, price);

            // add checked amenities
            for (CheckBox cb : boxes) {
                if (cb.isSelected()) rt.addAmenity((Amenity) cb.getUserData());
            }

            // copy image into resources
            rt.setImagePath(
                    copyImageForType(
                            chosen[0],
                            name
                    )
            );

            HotelDatabase.roomTypes.add(rt);
            dialog.close();
            refreshRoomTypesTable(owner);
        });

        HBox btns = new HBox(10, cancel, save);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(form, amenHeader, checkScroll, error, btns);
        showDialog(dialog, root, 500, 480, owner);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EDIT ROOM TYPE  — with amenity checklist
    // ══════════════════════════════════════════════════════════════════════

    private void openEditRoomTypeDialog(RoomType rt, Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Room Type");
        dialog.initOwner(owner);
        dialog.setResizable(true);

        VBox root = dialogRoot();
        root.getChildren().addAll(dialogTitle("Edit — " + rt.getName()), new Separator());

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(14);

        TextField nameField  = formField(""); nameField.setText(rt.getName());
        TextField priceField = formField(""); priceField.setText(String.valueOf(rt.getBasePrice()));

        File[] chosen = {null};
        Label imgStatus = new Label("No new image");
        imgStatus.setStyle("-fx-text-fill:gray;-fx-font-size:12;");
        HBox imgRow = imagePickerRow(dialog, chosen, imgStatus);

        form.add(boldLbl("Name:"),       0, 0); form.add(nameField,  1, 0);
        form.add(boldLbl("Base Price:"), 0, 1); form.add(priceField, 1, 1);
        form.add(boldLbl("Image:"),      0, 2); form.add(imgRow,     1, 2);

        // amenity checklist — pre-tick what the room type already has
        Label amenHeader = new Label("Amenities:");
        amenHeader.setStyle("-fx-font-weight:bold;");

        VBox checkList = new VBox(8);
        checkList.setPadding(new Insets(0, 0, 0, 4));
        List<CheckBox> boxes = new ArrayList<>();
        for (Amenity a : HotelDatabase.amenities) {
            CheckBox cb = new CheckBox(a.getName() + "  ($" + a.getPrice() + ")");
            cb.setUserData(a);
            cb.setSelected(rt.getAmenities().contains(a));
            boxes.add(cb);
            checkList.getChildren().add(cb);
        }
        if (HotelDatabase.amenities.isEmpty()) {
            checkList.getChildren().add(new Label("No amenities available."));
        }

        ScrollPane checkScroll = new ScrollPane(checkList);
        checkScroll.setFitToWidth(true);
        checkScroll.setPrefHeight(140);
        checkScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        checkScroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");

        Label error = errorLabel();

        Button save   = new Button("Save Changes"); save.getStyleClass().add("button");
        Button cancel = new Button("Cancel");       cancel.getStyleClass().add("secondary-button");
        cancel.setOnAction(e -> dialog.close());

        save.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showErr(error, "Name is required."); return; }
            double price;
            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) { showErr(error, "Enter a valid positive price."); return; }

            rt.setName(name);
            rt.setBasePrice(price);

            // rebuild amenity list from checkboxes
            rt.getAmenities().clear();
            for (CheckBox cb : boxes) {
                if (cb.isSelected()) rt.addAmenity((Amenity) cb.getUserData());
            }

            // copy new image if chosen
            if (chosen[0] != null) {

                rt.setImagePath(
                        copyImageForType(
                                chosen[0],
                                name
                        )
                );
            }
            dialog.close();
            refreshRoomTypesTable(owner);
        });

        HBox btns = new HBox(10, cancel, save);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(form, amenHeader, checkScroll, error, btns);
        showDialog(dialog, root, 500, 480, owner);
    }


    private void openAddAmenityDialog(AmenityType type, FlowPane pane, Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Add Amenity");
        dialog.initOwner(owner);
        dialog.setResizable(false);

        VBox root = dialogRoot();
        root.getChildren().addAll(dialogTitle("Add Amenity"), new Separator());

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(14);

        TextField nameField  = formField("e.g. Spa");
        TextField priceField = formField("e.g. 50");

        File[] chosen = {null};
        Label imgStatus = new Label("No image selected");
        imgStatus.setStyle("-fx-text-fill:gray;-fx-font-size:12;");
        HBox imgRow = imagePickerRow(dialog, chosen, imgStatus);

        form.add(boldLbl("Name:"),  0, 0); form.add(nameField,  1, 0);
        form.add(boldLbl("Price:"), 0, 1); form.add(priceField, 1, 1);
        form.add(boldLbl("Image:"), 0, 2); form.add(imgRow,     1, 2);

        Label error = errorLabel();

        Button save   = new Button("Add Amenity"); save.getStyleClass().add("button");
        Button cancel = new Button("Cancel");      cancel.getStyleClass().add("secondary-button");
        cancel.setOnAction(e -> dialog.close());

        save.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showErr(error, "Name is required."); return; }
            double price = 0;
            String pt = priceField.getText().trim();
            if (!pt.isEmpty()) {
                try { price = Double.parseDouble(pt); if (price < 0) throw new NumberFormatException(); }
                catch (NumberFormatException ex) { showErr(error, "Enter a valid positive price."); return; }
            }
            boolean dup = HotelDatabase.amenities.stream()
                    .anyMatch(a -> a.getName().equalsIgnoreCase(name));
            if (dup) { showErr(error, "That amenity name already exists."); return; }

            // copy image and record path
            String imgPath = null;
            if (chosen[0] != null) imgPath = copyImageForAmenity(chosen[0], name);

            Amenity newAmenity = new Amenity(name, type, price);
            if (imgPath != null) newAmenity.setImagePath(imgPath);

            HotelDatabase.amenities.add(newAmenity);
            dialog.close();
            fillAmenityPane(pane, type, owner);
        });

        HBox btns = new HBox(10, cancel, save);
        btns.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().addAll(form, error, btns);
        showDialog(dialog, root, 460, 300, owner);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EDIT AMENITY
    // ══════════════════════════════════════════════════════════════════════

    private void openEditAmenityDialog(Amenity amenity, FlowPane pane,
                                       AmenityType type, Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Amenity");
        dialog.initOwner(owner);
        dialog.setResizable(false);

        VBox root = dialogRoot();
        root.getChildren().addAll(dialogTitle("Edit — " + amenity.getName()), new Separator());

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(14);

        TextField nameField  = formField(""); nameField.setText(amenity.getName());
        TextField priceField = formField(""); priceField.setText(String.valueOf(amenity.getPrice()));

        File[] chosen = {null};
        String currentImg = amenity.getImagePath();
        Label imgStatus = new Label(currentImg != null ? Paths.get(currentImg).getFileName().toString() : "No image");
        imgStatus.setStyle("-fx-text-fill:gray;-fx-font-size:12;");
        HBox imgRow = imagePickerRow(dialog, chosen, imgStatus);

        form.add(boldLbl("Name:"),  0, 0); form.add(nameField,  1, 0);
        form.add(boldLbl("Price:"), 0, 1); form.add(priceField, 1, 1);
        form.add(boldLbl("Image:"), 0, 2); form.add(imgRow,     1, 2);

        Label error = errorLabel();

        Button save   = new Button("Save Changes"); save.getStyleClass().add("button");
        Button cancel = new Button("Cancel");       cancel.getStyleClass().add("secondary-button");
        cancel.setOnAction(e -> dialog.close());

        save.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showErr(error, "Name is required."); return; }
            double price = amenity.getPrice();
            String pt = priceField.getText().trim();
            if (!pt.isEmpty()) {
                try { price = Double.parseDouble(pt); if (price < 0) throw new NumberFormatException(); }
                catch (NumberFormatException ex) { showErr(error, "Enter a valid positive price."); return; }
            }
            amenity.setName(name);
            amenity.setPrice(price);
            if (chosen[0] != null) {
                String imgPath = copyImageForAmenity(chosen[0], name);
                if (imgPath != null) amenity.setImagePath(imgPath);
            }
            dialog.close();
            fillAmenityPane(pane, type, owner);
        });

        HBox btns = new HBox(10, cancel, save);
        btns.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().addAll(form, error, btns);
        showDialog(dialog, root, 460, 300, owner);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  IMAGE HELPERS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Copies the chosen file into the resources directory as <name>.png/jpg,
     * returns the absolute path string (used by amenity.setImagePath).
     */
    private String copyImageForAmenity(File src, String amenityName) {
        if (resourcesDir == null) return null;
        try {
            String ext = extension(src.getName());
            Path dest = resourcesDir.resolve(amenityName + ext);
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toAbsolutePath().toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    private String copyImageForType(File src, String typeName) {
        if (resourcesDir == null) return null;
        try {
            String ext = extension(src.getName());
            Path dest = resourcesDir.resolve(
                    typeName.replaceAll("\\s+", "").toLowerCase() + ext);
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toAbsolutePath().toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".png";
    }

    /**
     * Load amenity icon: prefer the stored absolute path, then fall back to
     * classpath lookup, then /bed.png.
     */
    private ImageView amenityIcon(Amenity amenity, double size) {
        Image img = null;

        // 1. stored absolute path from a previous upload
        String stored = amenity.getImagePath();
        if (stored != null) {
            try {
                File f = new File(stored);
                if (f.exists()) img = new Image(f.toURI().toString());
            } catch (Exception ignored) {}
        }

        // 2. classpath by name (built-in assets)
        if (img == null || img.isError()) {
            for (String path : new String[]{
                    "/" + amenity.getName() + ".png",
                    "/" + amenity.getName().toLowerCase() + ".png",
                    "/" + amenity.getName() + ".jpg",
                    "/" + amenity.getName().toLowerCase() + ".jpg"
            }) {
                try {
                    var stream = getClass().getResourceAsStream(path);
                    if (stream != null) {
                        img = new Image(stream);
                        if (!img.isError()) break;
                    }
                } catch (Exception ignored) {}
            }
        }

        // 3. generic fallback
        if (img == null || img.isError()) {
            try { img = new Image(getClass().getResourceAsStream("/bed.png")); }
            catch (Exception ignored) {}
        }

        ImageView iv = new ImageView();
        if (img != null && !img.isError()) iv.setImage(img);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        return iv;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SMALL UI HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private Button iconBtn(String icon, String color, String bg) {
        Button b = new Button(icon);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + color + ";" +
                "-fx-background-radius:6;-fx-font-size:12;-fx-cursor:hand;" +
                "-fx-padding:4 8;-fx-border-color:" + color + ";" +
                "-fx-border-radius:6;-fx-border-width:1;");
        return b;
    }

    private Label styledCell(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private HBox imagePickerRow(Stage owner, File[] holder, Label status) {
        Button btn = new Button("Choose Image");
        btn.getStyleClass().add("secondary-button");
        btn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Image");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files","*.png","*.jpg","*.jpeg","*.gif"));
            File f = fc.showOpenDialog(owner);
            if (f != null) { holder[0] = f; status.setText(f.getName()); }
        });
        HBox row = new HBox(10, btn, status);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox dialogRoot() {
        VBox v = new VBox(16);
        v.setPadding(new Insets(25));
        v.getStyleClass().add("card");
        return v;
    }

    private Label dialogTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:18;-fx-font-weight:bold;");
        return l;
    }

    private TextField formField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(240);
        return tf;
    }

    private Label boldLbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight:bold;");
        return l;
    }

    private Label errorLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill:#991B1B;-fx-font-size:13;");
        l.setVisible(false);
        l.setManaged(false);
        return l;
    }

    private void showErr(Label l, String msg) {
        l.setText(msg);
        l.setVisible(true);
        l.setManaged(true);
    }

    private void showDialog(Stage dialog, VBox root, int w, int h, Stage owner) {
        Scene scene = new Scene(root, w, h);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    public static void main(String[] args) { launch(args); }
}