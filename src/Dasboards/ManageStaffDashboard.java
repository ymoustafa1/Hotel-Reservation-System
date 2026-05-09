package Dasboards;

import app.SceneManager;
import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Receptionist;
import model.Staff;
import util.SidebarAdmin;

public class ManageStaffDashboard extends Application {

    public ManageStaffDashboard() {}

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        root.setLeft(SidebarAdmin.createSidebar("Manage Staff"));

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
        Label title = new Label("Manage Staff");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label("View, add, and remove receptionist accounts.");
        subtitle.getStyleClass().add("subtitle-label");
        titleBox.getChildren().addAll(title, subtitle);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button addStaffBtn = new Button("+ Add Receptionist");
        addStaffBtn.getStyleClass().add("button");

        titleRow.getChildren().addAll(titleBox, titleSpacer, addStaffBtn);

        VBox tableSection = new VBox(0);
        tableSection.getStyleClass().addAll("card");
        tableSection.setPadding(new Insets(0));

        GridPane tableHeader = buildTableHeader();
        tableSection.getChildren().add(tableHeader);

        VBox tableBody = new VBox(0);
        refreshTableBody(tableBody, tableSection);

        tableSection.getChildren().add(tableBody);

        centerArea.getChildren().addAll(titleRow, tableSection);

        addStaffBtn.setOnAction(e -> openAddDialog(stage, tableBody, tableSection));

        stage.setScene(scene);
        stage.setTitle("Manage Staff");
        stage.setMaximized(true);
        stage.show();
    }


    private GridPane buildTableHeader() {
        GridPane header = new GridPane();
        applyColumnConstraints(header);

        String[] cols = {"#", "Username", "Role", "Working Hours", "Actions"};
        double[] widths = {5, 35, 20, 20, 20};

        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setStyle(
                    "-fx-background-color: #0F172A; -fx-text-fill: white;" +
                            "-fx-padding: 14 12; -fx-font-weight: bold; -fx-font-size: 13;"
            );
            h.setMaxWidth(Double.MAX_VALUE);
            header.add(h, i, 0);

            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(widths[i]);
            header.getColumnConstraints().add(cc);
        }
        return header;
    }

    private void applyColumnConstraints(GridPane grid) {
    }

    private void refreshTableBody(VBox tableBody, VBox tableSection) {
        tableBody.getChildren().clear();

        int[] idx = {1};
        boolean[] found = {false};

        for (Staff s : HotelDatabase.staffMembers) {
            if (!(s instanceof Receptionist rec)) continue;
            found[0] = true;

            GridPane row = new GridPane();
            String rowBg = idx[0] % 2 == 0 ? "#F9FAFB" : "white";

            String cellStyle = "-fx-background-color: " + rowBg + ";" +
                    "-fx-padding: 14 12; -fx-border-color: #F3F4F6;" +
                    "-fx-border-width: 0 0 1 0;";

            Label numLbl   = styledCell(String.valueOf(idx[0]), cellStyle);
            Label userLbl  = styledCell(rec.getUsername(), cellStyle);
            Label roleLbl  = styledCell("Receptionist", cellStyle);
            Label hoursLbl = styledCell(rec.getWorkingHours() + " hrs", cellStyle);

            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_LEFT);
            actions.setStyle(cellStyle);

            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("danger-button");

            Label confirmLbl = new Label("Delete?");
            confirmLbl.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 12;");
            confirmLbl.setVisible(false); confirmLbl.setManaged(false);

            Button confirmBtn = new Button("Yes");
            confirmBtn.getStyleClass().add("danger-button");
            confirmBtn.setVisible(false); confirmBtn.setManaged(false);

            Button cancelBtn = new Button("No");
            cancelBtn.getStyleClass().add("secondary-button");
            cancelBtn.setVisible(false); cancelBtn.setManaged(false);

            deleteBtn.setOnAction(e -> {
                deleteBtn.setVisible(false); deleteBtn.setManaged(false);
                confirmLbl.setVisible(true); confirmLbl.setManaged(true);
                confirmBtn.setVisible(true); confirmBtn.setManaged(true);
                cancelBtn.setVisible(true);  cancelBtn.setManaged(true);
            });

            cancelBtn.setOnAction(e -> {
                deleteBtn.setVisible(true); deleteBtn.setManaged(true);
                confirmLbl.setVisible(false); confirmLbl.setManaged(false);
                confirmBtn.setVisible(false); confirmBtn.setManaged(false);
                cancelBtn.setVisible(false);  cancelBtn.setManaged(false);
            });

            confirmBtn.setOnAction(e -> {
                HotelDatabase.staffMembers.remove(rec);
                refreshTableBody(tableBody, tableSection);
            });

            actions.getChildren().addAll(deleteBtn, confirmLbl, confirmBtn, cancelBtn);

            double[] widths = {5, 35, 20, 20, 20};
            for (int i = 0; i < widths.length; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(widths[i]);
                row.getColumnConstraints().add(cc);
            }

            row.add(numLbl,   0, 0);
            row.add(userLbl,  1, 0);
            row.add(roleLbl,  2, 0);
            row.add(hoursLbl, 3, 0);
            row.add(actions,  4, 0);

            tableBody.getChildren().add(row);
            idx[0]++;
        }

        if (!found[0]) {
            Label empty = new Label("No receptionists found.");
            empty.setStyle("-fx-padding: 20; -fx-text-fill: gray;");
            tableBody.getChildren().add(empty);
        }
    }

    private Label styledCell(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }


    private void openAddDialog(Stage owner, VBox tableBody, VBox tableSection) {

        Stage dialog = new Stage();
        dialog.setTitle("Add Receptionist");
        dialog.setResizable(false);
        dialog.initOwner(owner);

        VBox root = new VBox(16);
        root.setPadding(new Insets(25));
        root.getStyleClass().add("card");

        Label title = new Label("Add New Receptionist");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(14);

        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-weight: bold;");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");
        userField.setPrefWidth(240);

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-weight: bold;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Min. 6 characters");
        passField.setPrefWidth(240);

        Label hoursLabel = new Label("Working Hours:");
        hoursLabel.setStyle("-fx-font-weight: bold;");
        TextField hoursField = new TextField();
        hoursField.setPromptText("e.g. 8");
        hoursField.setPrefWidth(240);

        form.add(userLabel,  0, 0); form.add(userField,  1, 0);
        form.add(passLabel,  0, 1); form.add(passField,  1, 1);
        form.add(hoursLabel, 0, 2); form.add(hoursField, 1, 2);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 13;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button saveBtn = new Button("Add Receptionist");
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

            String username = userField.getText().trim();
            String password = passField.getText().trim();
            String hoursText = hoursField.getText().trim();

            if (username.isEmpty()) {
                showError(errorLabel, "Username cannot be empty.");
                return;
            }
            if (password.length() < 6) {
                showError(errorLabel, "Password must be at least 6 characters.");
                return;
            }

            boolean taken = HotelDatabase.staffMembers.stream()
                    .anyMatch(s -> s.getUsername().equalsIgnoreCase(username));
            if (taken) {
                showError(errorLabel, "Username is already taken.");
                return;
            }

            int hours = 0;
            if (!hoursText.isEmpty()) {
                try {
                    hours = Integer.parseInt(hoursText);
                    if (hours <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    showError(errorLabel, "Working hours must be a positive number.");
                    return;
                }
            }

            Receptionist rec = new Receptionist(username, password, hours);
            if (hours > 0) rec.setWorkingHours(hours);
            HotelDatabase.staffMembers.add(rec);

            dialog.close();
            refreshTableBody(tableBody, tableSection);
        });

        root.getChildren().addAll(title, new Separator(), form, errorLabel, btnRow);

        Scene scene = new Scene(root, 460, 320);
        var css = getClass().getResource("/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        dialog.setScene(scene);
        dialog.show();
    }

    private void showError(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    public static void main(String[] args) { launch(args); }
}