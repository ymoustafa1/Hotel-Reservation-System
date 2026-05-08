package Dasboards;

import database.HotelDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Guest;
import model.Reservation;
import model.ReservationStatus;

import java.util.ArrayList;
import java.time.LocalDate;

public class ReservationDashboard extends Application {

    private Guest guest;
    private Label upcomingCount;
    private Label pastCount;
    private Label cancelledCount;
    private Label totalCount;

    public ReservationDashboard(){}

    public ReservationDashboard(Guest guest){
        this.guest = guest;
    }

    @Override
    public void start(Stage stage) {

        HotelDatabase.initializeDummyData();

        upcomingCount = new Label();
        pastCount = new Label();
        cancelledCount = new Label();
        totalCount = new Label();

        if(guest == null){
            guest = HotelDatabase.findGuest("kenzy");
        }

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root,1400,800);

        // CSS
        String cssPath = "/style.css";

        var resource = getClass().getResource(cssPath);

        if(resource != null){
            scene.getStylesheets().add(resource.toExternalForm());
        }

        // LEFT SIDEBAR
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // CENTER CONTENT
        VBox centerArea = new VBox(25);
        centerArea.setPadding(new Insets(30));
        centerArea.setStyle("-fx-background-color: #F5F7FA;");

        // TITLE
        Label title = new Label("My Reservations");

        title.setStyle("-fx-font-size: 38;" + "-fx-font-weight: bold;" + "-fx-text-fill: #041E42;");

        Label subtitle = new Label("View and manage all your reservations.");

        subtitle.setStyle("-fx-text-fill: #64748B;" + "-fx-font-size: 15;");

        // STATS CARDS
        HBox statsCards = new HBox(20);
        statsCards.getChildren().addAll(
                createStatCard("Upcoming",upcomingCount),
                createStatCard("Past", pastCount),
                createStatCard("Cancelled",cancelledCount),
                createStatCard("Total",totalCount)
        );
        updateStatistics();

        // FILTERS
        HBox filters = new HBox(15);
        TextField searchField = new TextField();
        searchField.setPromptText("Search reservation");
        searchField.setPrefWidth(300);
        searchField.setOnAction(e -> {
            System.out.println(
                    "Searching for: " + searchField.getText());
        });

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
                "All",
                "Reserved",
                "Cancelled",
                "Completed"
        );

        statusCombo.setPromptText("Status");

        DatePicker startDate = new DatePicker();
        startDate.setPromptText("Check In date");

        DatePicker endDate = new DatePicker();
        endDate.setPromptText("Check Out date");

        Button clearBtn = createViewButton("Clear Filters");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            statusCombo.setValue(null);
            startDate.setValue(null);
            endDate.setValue(null);
        });

        Button createReservationBtn =
                createCreateButton("Create Reservation");
        createReservationBtn.setOnAction(e -> {
            CreateReservationForm form = new CreateReservationForm();
            form.showForm();
        });

        filters.getChildren().addAll(
                searchField,
                statusCombo,
                startDate,
                endDate,
                clearBtn,
                createReservationBtn
        );

        // RESERVATIONS LIST
        VBox reservationContainer = new VBox(15);
        ArrayList<Reservation> reservations = getGuestReservations();
        for(Reservation res : reservations){
            reservationContainer.getChildren().add(createReservationCard(res));
        }
        ScrollPane scrollPane = new ScrollPane(reservationContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        centerArea.getChildren().addAll(
                title,
                subtitle,
                statsCards,
                filters,
                scrollPane
        );
        root.setCenter(centerArea);
        stage.setScene(scene);
        stage.setTitle("Reservations");
        stage.setMaximized(true);
        stage.show();
    }

    // SIDEBAR
    private VBox createSidebar(){
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(25));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #041E42;");
        Label logo = new Label("Hotel");
        logo.setStyle("-fx-text-fill: white;" + "-fx-font-size: 30;" + "-fx-font-weight: bold;");

        Button dashboardBtn = createSidebarButton("Dashboard");

        Button roomsBtn = createSidebarButton("Browse Rooms");

        Button reservationBtn = createActiveSidebarButton("Reservations");

        Button invoicesBtn = createSidebarButton("Invoices");

        Button profileBtn = createSidebarButton("Profile");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createSidebarButton("Logout");

        sidebar.getChildren().addAll(
                logo,
                dashboardBtn,
                roomsBtn,
                reservationBtn,
                invoicesBtn,
                profileBtn,
                spacer,
                logoutBtn
        );
        return sidebar;
    }

    // RESERVATION CARD
    private HBox createReservationCard(Reservation res){
        HBox card = new HBox(30);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 18;" + "-fx-border-radius: 18;");
        VBox left = new VBox(8);
        Label id = new Label(
                "Reservation ID: " + res.getReservationId());
        id.setStyle("-fx-font-size: 16;" + "-fx-font-weight: bold;");
        Label room = new Label(
                "Room: " + res.getRoom().getRoomType().getName());
        Label dates = new Label(
                "Check In: "
                        + res.getCheckInDate()
                        + " | Check Out: "
                        + res.getCheckOutDate()
        );
        Label guests = new Label(
                "Guests: " + res.getGuest());
        left.getChildren().addAll(
                id,
                room,
                dates,
                guests
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox right = new VBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label status = new Label(
                res.getStatus().toString());
        status.setStyle("-fx-background-color: #DCFCE7;" + "-fx-text-fill: #166534;" + "-fx-padding: 6 14 6 14;" + "-fx-background-radius: 10;" + "-fx-font-weight: bold;");

        Button viewBtn =
                createViewButton("View");
            viewBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reservation Details");
            alert.setHeaderText("Reservation Information");
            alert.setContentText(
                    "Reservation ID: "
                            + res.getReservationId()
                            + "\nRoom: "
                            + res.getRoom().getRoomType().getName()
                            + "\nCheck In: "
                            + res.getCheckInDate()
                            + "\nCheck Out: "
                            + res.getCheckOutDate()
                            + "\nGuests: "
                            + res.getGuest()
                            + "\nStatus: "
                            + res.getStatus()
            );
            alert.show();
        });

        Button cancelBtn =
                createCancelButton("Cancel");
        cancelBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Cancel Reservation");
                confirm.setHeaderText("Are you sure?");
                confirm.setContentText("This reservation will be cancelled.");
                ButtonType result = confirm.showAndWait().orElse(ButtonType.CANCEL);
                if(result == ButtonType.OK){
                    res.setStatus(ReservationStatus.CANCELLED
                    );
                    updateStatistics();
                    status.setText("CANCELLED");
                    status.setStyle("-fx-background-color: #FEE2E2;" + "-fx-text-fill: #991B1B;" + "-fx-padding: 6 14 6 14;" + "-fx-background-radius: 10;" + "-fx-font-weight: bold;");
                    Alert done = new Alert(Alert.AlertType.INFORMATION);
                    done.setContentText("Reservation Cancelled Successfully!");
                    done.show();
                }
            });
        right.getChildren().addAll(
                status,
                viewBtn,
                cancelBtn
        );
        card.getChildren().addAll(
                left,
                spacer,
                right
        );
        return card;
    }

    // STAT CARD
    private VBox createStatCard(
            String title,
            Label valueLabel
    )
    {
        VBox card = new VBox(10);
        card.setPrefSize(220,120);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 18;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle( "-fx-text-fill: #64748B;");
        valueLabel.setStyle("-fx-font-size: 30;" + "-fx-font-weight: bold;");
        card.getChildren().addAll(
                titleLabel,
                valueLabel
        );
        return card;
    }

    private void updateStatistics(){
        int upcoming = 0;
        int past = 0;
        int cancelled = 0;
        int total = getGuestReservations().size();
        for(Reservation r :
                getGuestReservations()){
            if(r.getStatus()
                    == ReservationStatus.RESERVED){
                upcoming++;
            }
            if(r.getStatus()
                    == ReservationStatus.COMPLETED){
                past++;
            }
            if(r.getStatus()
                    == ReservationStatus.CANCELLED){
                cancelled++;
            }
        }
        upcomingCount.setText(String.valueOf(upcoming));
        pastCount.setText(String.valueOf(past));
        cancelledCount.setText(String.valueOf(cancelled));
        totalCount.setText(String.valueOf(total));
    }

    // BUTTONS
    private Button createViewButton(String text){
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #041E42;" + "-fx-text-fill: white;" + "-fx-background-radius: 10;" + "-fx-font-size: 14;");
        return btn;
    }

    private Button createCancelButton(String text){
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #DC2626;" + "-fx-text-fill: white;" + "-fx-background-radius: 10;" + "-fx-font-size: 14;");
        return btn;
    }

    private Button createCreateButton(String text){
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #16A34A;" + "-fx-text-fill: white;" + "-fx-background-radius: 10;" + "-fx-font-size: 14;");
        return btn;
    }

    private Button createSidebarButton(String text){
        Button btn = new Button(text);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setMinHeight(45);
        btn.setStyle("-fx-background-color: transparent;" + "-fx-text-fill: white;" + "-fx-font-size: 15;" + "-fx-alignment: center-left;");
        return btn;
    }

    private Button createActiveSidebarButton(String text){
        Button btn = new Button(text);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setMinHeight(45);
        btn.setStyle("-fx-background-color: #1D4ED8;" + "-fx-text-fill: white;" + "-fx-font-size: 15;" + "-fx-background-radius: 10;" + "-fx-alignment: center-left;");
        return btn;
    }

    private boolean validateReservationInputs(
            LocalDate checkIn,
            LocalDate checkOut
    )
    {
        if(checkIn == null || checkOut == null){
            showError("Dates cannot be empty.");
            return false;
        }
        if(checkOut.isBefore(checkIn)){
            showError("Check-out date must be after check-in.");
            return false;
        }
        return true;
    }

    private void showError(String message){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            alert.show();
    }

    // GET RESERVATIONS
    private ArrayList<Reservation>
    getGuestReservations(){
        ArrayList<Reservation> result = new ArrayList<>();
        for(Reservation r : HotelDatabase.reservations){
            if(r.getGuest().getUsername().equals(guest.getUsername())){
                result.add(r);
            }
        }
        return result;
    }
    public static void main(String[] args) {
        launch(args);
    }
}