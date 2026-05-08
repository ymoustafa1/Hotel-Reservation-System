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
import javafx.util.Callback;
import model.Guest;
import model.Invoice;

import java.util.ArrayList;

    public class InvoiceDashboard extends Application
    {
        private Guest guest;
        public InvoiceDashboard() {}
        public InvoiceDashboard(Guest guest) { this.guest = guest; }
        @Override
        public void start(Stage stage)
        {
            HotelDatabase.initializeDummyData();
            if (guest == null)
                guest = HotelDatabase.findGuest("kenzy");
            // ===== FORCE INVOICES FOR TESTING =====
            if (HotelDatabase.invoices.isEmpty())
            {
                if (!HotelDatabase.reservations.isEmpty())
                {
                    model.Reservation res = HotelDatabase.reservations.get(0);
                    HotelDatabase.invoices.add(new model.Invoice(res, model.PaymentMethod.CASH));
                    HotelDatabase.invoices.add(new model.Invoice(res, model.PaymentMethod.CREDIT_CARD));
                    HotelDatabase.invoices.add(new model.Invoice(res, model.PaymentMethod.CASH));
                }
            }
            BorderPane root = new BorderPane();
            Scene scene = new Scene(root, 1200, 700);

            // CSS
            var css = getClass().getResource("/style.css");
            if (css != null)
                scene.getStylesheets().add(css.toExternalForm());
            root.setLeft(createSidebar());
            root.setCenter(createMain());
            stage.setScene(scene);
            stage.setTitle("Invoice Dashboard");
            stage.setMaximized(true);
            stage.show();
        }

        // ================= SAFE ICON =================
        private ImageView icon(String path, int size)
        {
            try {
                var stream = getClass().getResourceAsStream(path);
                if (stream == null) return new ImageView();
                ImageView img = new ImageView(new Image(stream));
                img.setFitWidth(size);
                img.setFitHeight(size);
                return img;
            } catch (Exception e) {
                return new ImageView();
            }
        }

        // ================= SIDEBAR =================
        private VBox createSidebar()
        {
            VBox sidebar = new VBox();
            sidebar.getStyleClass().add("sidebar");
            sidebar.setSpacing(15);
            sidebar.setPadding(new Insets(20));
            sidebar.setStyle("-fx-background-color: #0F172A;");
            sidebar.getChildren().addAll(sidebarItem("Invoice Management", "/invoice.png"), sidebarItem("Payment & Checkout", "/wallet.png"), sidebarItem("Receptionist Panel", "/user.png"));
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            sidebar.getChildren().add(spacer);
            sidebar.getChildren().add(sidebarItem("Logout", "/exit.png"));
            return sidebar;
        }

        private HBox sidebarItem(String text, String iconPath)
        {
            HBox box = new HBox(10);
            box.setAlignment(Pos.CENTER_LEFT);
            box.getStyleClass().add("sidebar-button");
            Label label = new Label(text);
            label.setStyle("-fx-text-fill: white;");
            box.getChildren().addAll(icon(iconPath, 18), label);
            return box;
        }

        // ================= MAIN =================
        private VBox createMain()
        {
            VBox main = new VBox(20);
            main.getStyleClass().add("dashboard-pane");
            main.setPadding(new Insets(25));
            main.getChildren().addAll(header(), stats(), filters(), table());
            return main;
        }

        // ================= HEADER =================
        private VBox header()
        {
            VBox box = new VBox(5);
            Label title = new Label("Invoice Management");
            title.getStyleClass().add("title-label");
            Label sub = new Label("Create, view and manage all hotel invoices.");
            sub.getStyleClass().add("subtitle-label");
            box.getChildren().addAll(title, sub);
            return box;
        }

        // ================= STATS =================
        private HBox stats()
        {
            HBox row = new HBox(20);
            int total = HotelDatabase.invoices.size();
            row.getChildren().addAll(stat("Total Invoices", total), stat("Paid", total), stat("Pending", 0), stat("Cancelled", 0));
            return row;
        }

        private VBox stat(String title, int value)
        {
            VBox card = new VBox(10);
            card.getStyleClass().addAll("card", "stat-card");
            card.setPrefSize(200, 110);
            Label t = new Label(title);
            t.getStyleClass().add("small-label");
            Label v = new Label(String.valueOf(value));
            v.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");
            card.getChildren().addAll(t, v);
            return card;
        }

        // ================= FILTERS =================
        private HBox filters()
        {
            HBox filters = new HBox(15);
            filters.setAlignment(Pos.CENTER_LEFT);
            TextField search = new TextField();
            search.setPromptText("Search invoice...");
            search.setPrefWidth(200);
            ComboBox<String> status = new ComboBox<>();
            status.getItems().addAll("All", "Paid", "Pending");
            status.setValue("All");
            DatePicker from = new DatePicker();
            DatePicker to = new DatePicker();
            Button clear = new Button("Clear");
            clear.getStyleClass().add("button");
            clear.setOnAction(e ->
            {
                search.clear();
                status.setValue("All");
                from.setValue(null);
                to.setValue(null);
            });
            filters.getChildren().addAll(search, status, from, to, clear);
            return filters;
        }

        // ================= TABLE =================
        private VBox table()
        {
            VBox container = new VBox(10);
            container.getStyleClass().add("card");
            Label title = new Label("Invoices");
            title.getStyleClass().add("section-title");
            TableView<Invoice> table = new TableView<>();
            container.setFillWidth(true);
            VBox.setVgrow(table, Priority.ALWAYS);
            // ID
            TableColumn<Invoice, String> idCol = new TableColumn<>("Invoice ID");
            idCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Invoice, String>, javafx.beans.value.ObservableValue<String>>() {
                @Override
                public javafx.beans.value.ObservableValue<String> call(TableColumn.CellDataFeatures<Invoice, String> data)
                {return new SimpleStringProperty("INV-" + data.getValue().getInvoiceId());}
            });

            // ROOM
            TableColumn<Invoice, String> roomCol = new TableColumn<>("Room");
            roomCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Invoice, String>, javafx.beans.value.ObservableValue<String>>()
            {
                @Override
                public javafx.beans.value.ObservableValue<String> call(TableColumn.CellDataFeatures<Invoice, String> data)
                {return new SimpleStringProperty(data.getValue().getReservation().getRoom().getRoomType().getName());}
            });

            // CHECK IN
            TableColumn<Invoice, String> checkInCol = new TableColumn<>("Check In");
            checkInCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getReservation().getCheckInDate().toString()));

            // CHECK OUT
            TableColumn<Invoice, String> checkOutCol = new TableColumn<>("Check Out");
            checkOutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getReservation().getCheckOutDate().toString()));

            // AMOUNT
            TableColumn<Invoice, String> amountCol = new TableColumn<>("Amount");
            amountCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Invoice, String>, javafx.beans.value.ObservableValue<String>>()
            {
                @Override
                public javafx.beans.value.ObservableValue<String> call(TableColumn.CellDataFeatures<Invoice, String> data)
                {return new SimpleStringProperty("$" + data.getValue().getTotalAmount());}
            });

            // PAYMENT
            TableColumn<Invoice, String> methodCol = new TableColumn<>("Payment");
            methodCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Invoice, String>, javafx.beans.value.ObservableValue<String>>()
            {
                @Override
                public javafx.beans.value.ObservableValue<String> call(TableColumn.CellDataFeatures<Invoice, String> data)
                {return new SimpleStringProperty(data.getValue().getPaymentMethod().toString());}
            });

            // STATUS
            TableColumn<Invoice, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Invoice, String>, javafx.beans.value.ObservableValue<String>>()
            {
                @Override
                public javafx.beans.value.ObservableValue<String> call(TableColumn.CellDataFeatures<Invoice, String> data)
                {return new SimpleStringProperty("Paid");}
            });

            // ACTION
            TableColumn<Invoice, String> actionCol = new TableColumn<>("Action");
            actionCol.setCellFactory(new Callback<TableColumn<Invoice, String>, TableCell<Invoice, String>>()
            {
                @Override
                public TableCell<Invoice, String> call(TableColumn<Invoice, String> param)
                {
                    return new TableCell<>()
                    {
                        private final Button viewBtn = new Button("View");
                        {
                            viewBtn.getStyleClass().add("button");
                            viewBtn.setOnAction(e -> {
                                Invoice inv = getTableView().getItems().get(getIndex());
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setHeaderText("Invoice Details");
                                alert.setContentText("Invoice ID: " + inv.getInvoiceId() + "\nAmount: $" + inv.getTotalAmount() + "\nPayment: " + inv.getPaymentMethod());
                                alert.show();
                            });
                        }

                        @Override
                        protected void updateItem(String item, boolean empty)
                        {
                            if (empty)
                                setGraphic(null);
                            else
                                setGraphic(viewBtn);
                        }
                    };
                }
            });
            table.getColumns().addAll(idCol, roomCol, checkInCol, checkOutCol, amountCol, methodCol, statusCol, actionCol);

            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefWidth(Double.MAX_VALUE);

            // DATA
            ArrayList<Invoice> list = new ArrayList<>();
            for (Invoice inv : HotelDatabase.invoices)
            {
                list.add(inv);
            }
            table.getItems().addAll(list);
            container.getChildren().addAll(title, table);
            return container;
        }
    }
