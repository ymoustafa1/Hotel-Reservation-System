package Dasboards;
import database.HotelDatabase;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.*;

public class PaymentAndCheckout extends Application {
    private Guest guest;

    public PaymentAndCheckout() {
    }

    public PaymentAndCheckout(Guest guest) {
        this.guest = guest;
    }

    @Override
    public void start(Stage stage) {
        HotelDatabase.initializeDummyData();
        if (guest == null)
            guest = HotelDatabase.findGuest("kenzy");

        // Ensure data exists
        if (HotelDatabase.invoices.isEmpty() && !HotelDatabase.reservations.isEmpty()) {
            Reservation r = HotelDatabase.reservations.get(0);
            HotelDatabase.invoices.add(new Invoice(r, PaymentMethod.CASH));
        }

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1200, 700);

        var css = getClass().getResource("/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        root.setLeft(createSidebar());
        root.setCenter(createMain());

        stage.setScene(scene);
        stage.setTitle("Payment & Checkout");
        stage.setMaximized(true);
        stage.show();
    }

    // ================= ICON =================
    private ImageView icon(String path, int size) {
        var stream = getClass().getResourceAsStream(path);
        if (stream == null) return new ImageView();
        ImageView img = new ImageView(new Image(stream));
        img.setFitWidth(size);
        img.setFitHeight(size);
        return img;
    }

    // ================= SIDEBAR =================
    private VBox createSidebar() {

        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #0F172A;");

        sidebar.getChildren().addAll(
                sidebarItem("Invoice Management", "/invoice.png"),
                sidebarItem("Payment & Checkout", "/wallet.png"),
                sidebarItem("Receptionist Panel", "/user.png")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().add(spacer);
        sidebar.getChildren().add(sidebarItem("Logout", "/exit.png"));

        return sidebar;
    }

    private HBox sidebarItem(String text, String iconPath) {

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("sidebar-button");

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white;");

        box.getChildren().addAll(icon(iconPath, 18), label);

        return box;
    }

    // ================= MAIN =================
    private VBox createMain() {

        VBox main = new VBox(20);
        main.getStyleClass().add("dashboard-pane");
        main.setPadding(new Insets(25));

        main.getChildren().addAll(
                header(),
                stats(),
                filters(),
                paymentsSection(),
                checkoutSection()
        );
        return main;
    }

    private VBox checkoutSection() {

        VBox box = new VBox(15);
        box.getStyleClass().add("card");

        Label title = new Label("Checkout");
        title.getStyleClass().add("section-title");

        Label info = new Label("Complete your stay after finishing all payments.");
        info.getStyleClass().add("subtitle-label");

        Button checkoutBtn = new Button("Complete Checkout");
        checkoutBtn.getStyleClass().add("warning-button");

        checkoutBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent e) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Checkout");
                alert.setHeaderText(null);
                alert.setContentText("Checkout completed successfully!");
                alert.show();
            }
        });

        box.getChildren().addAll(title, info, checkoutBtn);

        return box;
    }
    // ================= HEADER =================
    private VBox header() {

        VBox box = new VBox(5);

        Label title = new Label("Payment & Checkout");
        title.getStyleClass().add("title-label");

        Label sub = new Label("Manage payments and complete checkout.");
        sub.getStyleClass().add("subtitle-label");

        box.getChildren().addAll(title, sub);

        return box;
    }

    // ================= STATS =================
    private HBox stats() {

        int total = HotelDatabase.invoices.size();

        HBox row = new HBox(20);

        row.getChildren().addAll(
                statCard("Total Invoices", String.valueOf(total), "/invoice.png"),
                statCard("Paid", String.valueOf(total), "/wallet.png"),
                statCard("Pending", "0", "/calendar.png"),
                statCard("Checkout Ready", String.valueOf(total), "/user.png")
        );

        return row;
    }

    private VBox statCard(String title, String value, String iconPath) {

        VBox card = new VBox(10);
        card.getStyleClass().addAll("card");
        card.setPrefSize(200, 110);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label(title);
        t.getStyleClass().add("small-label");

        top.getChildren().addAll(icon(iconPath, 22), t);

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        card.getChildren().addAll(top, v);

        return card;
    }

    // ================= FILTERS =================
    private HBox filters() {

        HBox filters = new HBox(15);
        filters.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("Search invoice...");

        ComboBox<String> status = new ComboBox<String>();
        status.getItems().addAll("All", "Paid", "Pending");
        status.setValue("All");

        DatePicker from = new DatePicker();
        DatePicker to = new DatePicker();

        Button clear = new Button("Clear");
        clear.getStyleClass().add("button");

        clear.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent e) {
                search.clear();
                status.setValue("All");
                from.setValue(null);
                to.setValue(null);
            }
        });

        filters.getChildren().addAll(search, status, from, to, clear);

        return filters;
    }

    // ================= TABLE =================
    private VBox paymentsSection() {

        VBox container = new VBox(15);
        container.getStyleClass().add("card");

        Label title = new Label("Pending Payments");
        title.getStyleClass().add("section-title");

        VBox list = new VBox(10);

        double totalDue = 0;

        for (Invoice inv : HotelDatabase.invoices) {

            HBox row = new HBox(20);
            row.setAlignment(Pos.CENTER_LEFT);

            Label id = new Label("INV-" + inv.getInvoiceId());
            Label amount = new Label("$" + inv.getTotalAmount());

            totalDue += inv.getTotalAmount();

            Button payBtn = new Button("Pay Now");
            payBtn.getStyleClass().add("success-button");

            payBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent e) {

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Payment Completed!");
                    alert.show();

                    payBtn.setDisable(true);
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(id, amount, spacer, payBtn);

            list.getChildren().add(row);
        }

        // ===== SUMMARY =====
        Label summary = new Label("Total Due: $" + totalDue);
        summary.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        container.getChildren().addAll(title, list, summary);

        return container;
    }
}
