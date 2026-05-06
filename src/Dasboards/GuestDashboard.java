package Dasboards;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Gender;
import model.Guest;
import model.Reservation;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GuestDashboard extends Application {
    private Guest guest;
//    private Label balanceLabel = new Label();
    public GuestDashboard(){}
    public GuestDashboard(Guest guest)
    {
        this.guest=guest;
    }



    public  void start (Stage GuestDashboardStage)
    {

        if (this.guest == null) {
            this.guest = new Guest("Test Username","TestPassword",LocalDate.now(),123.4,"Test Address", Gender.MALE);
        }
        BorderPane guestDashboardPane = new BorderPane();
        Scene guestDashboardScene = new Scene(guestDashboardPane,1000,600);
        String css = this.getClass().getResource("/style.css").toExternalForm();
        guestDashboardScene.getStylesheets().add(css);
        //Left sidebar Area
        VBox leftSwitcherText = new VBox();
        StackPane leftSwitcher = new StackPane();
        Rectangle leftR = new Rectangle(200,600);
        leftR.setFill(Color.DARKBLUE);
        leftSwitcher.getChildren().addAll(leftR,leftSwitcherText);
        guestDashboardPane.setLeft(leftSwitcher);
        String[] sidebarLabels ={"Dashboard","View Rooms","Reservations" ,"Settings"};
        ArrayList<ImageView> GuestDashboardImages = new ArrayList<>();
        String[] iconPaths = {"home.png", "bed.png", "calendar.png", "setting.png"};
        for (String path : iconPaths) {
            String resourcePath = "/" + path;
            Image img = new Image(getClass().getResourceAsStream(resourcePath));
            GuestDashboardImages.add(new ImageView(img));
        }
        int k=0;
        for(String s : sidebarLabels)
        {
         addIconText(leftSwitcherText,s,GuestDashboardImages.get(k));
         k++;
        }
        leftSwitcherText.setSpacing(20);
        leftSwitcherText.setPadding(new Insets(30));
        leftSwitcherText.getStyleClass().add("sidebar");


       //Center Area
        VBox centerArea = new VBox();
        StackPane  header = new StackPane();
        centerArea.setPadding(new Insets(15));
        Rectangle r3 = new Rectangle(25, 90,750,80);
        r3.setArcWidth(15);
        r3.setArcHeight(25);
        r3.setFill(Color.DARKBLUE);
        header.getChildren().addAll(r3,new Label(" Welcome back, "+guest.getUsername()+"!"));
        centerArea.getChildren().add(header);
        guestDashboardPane.setCenter(centerArea);
        header.getStyleClass().add("header");
        HBox middle = new HBox();
        middle.setPadding(new Insets(15));
        middle.setSpacing(15);
        VBox guestProfileCard = new VBox();
        VBox balanceCard = new VBox();
        VBox actionsCard = new VBox();
        guestProfileCard.getStyleClass().add("card");
        balanceCard.getStyleClass().add("card");
        guestProfileCard.setPrefSize(250,400);
        guestProfileCard.setAlignment(Pos.TOP_CENTER);
        guestProfileCard.setSpacing(20);
        Image man= new Image(getClass().getResourceAsStream("/man.png"));
        ImageView manview= new ImageView(man);
        Image woman= new Image(getClass().getResourceAsStream("/woman.png"));
        ImageView womanview = new ImageView(woman);
        if(guest.getGender()== Gender.MALE)
        {
            manview.setFitHeight(100);
            manview.setFitHeight(100);
            manview.setPreserveRatio(true);
            guestProfileCard.getChildren().add(manview);
        }
        else {
            womanview.setFitHeight(100);
            womanview.setFitHeight(100);
            womanview.setPreserveRatio(true);
            guestProfileCard.getChildren().add(womanview);
        }

        loadGuestData(guestProfileCard,guest);

        balanceCard.setPrefSize(100,200);
        middle.getChildren().addAll(guestProfileCard, balanceCard,actionsCard);
        centerArea.getChildren().add(middle);


        //running Stage
        GuestDashboardStage.setScene(guestDashboardScene);
        GuestDashboardStage.setResizable(false);
        GuestDashboardStage.setTitle("Guest Dashboard");
        GuestDashboardStage.show();

    }
    void addIconText(Pane pane, String string, ImageView image) {
        image.setPreserveRatio(true);
        image.setFitWidth(30);
        image.setFitHeight(30);

        Label label = new Label(string);
        HBox h = new HBox(15, image, label);
        h.setAlignment(Pos.CENTER_LEFT);

        pane.getChildren().add(h);
    }


    private void loadGuestData( Pane pane, Guest guest)
    {   pane.getChildren().add(new Label("User: "+ guest.getUsername()));
        pane.getChildren().add(new Label("Gender: "+ guest.gendertoString()));
        pane.getChildren().add(new Label("Date of Birth: " +guest.getDateOfBirth()));
        pane.getChildren().add(new Label("Address: "+ guest.getAddress()));
    }
//    private void displayReservations(List<Reservation> res)
//    {
////        int i=4;
////        int j=0;
////        int cnt=0;
////        for(Reservation r : res )
////        {
////            guestDashboardPane.add(new Label(cnt+". "+r.toString()),j,i);
////            i++;
////            cnt++;
////        }
//    }
//    private void updateBalance(double balance)
//    {
////        guest.updateBalance(balance); // This updates the logic/data
////        balanceLabel.setText("Balance: " + guest.getBalance()); // This updates the UI
//    }

}







//Image profileIcon = new Image(getClass().getResourceAsStream("/profile_icon.jpg"));
//ImageView ProfileIcon = new ImageView(profileIcon);
//        ProfileIcon.setPreserveRatio(true);
//        ProfileIcon.setFitHeight(250);
//        ProfileIcon.setFitWidth(250);
//TextField UpdateBalance = new TextField();
//Button Update = new Button("Update");
//        guestDashboardPane.add(new Label("Enter Amount : "),2,2);
//        guestDashboardPane.add(UpdateBalance,3,2);
//        guestDashboardPane.add(Update,4,2);
//
//            Update.setOnAction(e->
//        {
//        try
//        {
//updateBalance(Double.parseDouble(UpdateBalance.getText()));
//        }
//        catch (NumberFormatException ex)
//        {
//        System.out.println(" Wrong Format NumberFormatException occurred");
//                }
//                        }
//                        );
//
//
//                        guestDashboardPane.setPadding(new Insets(5));
//        guestDashboardPane.setVgap(10);
//        guestDashboardPane.setHgap(10);
//        guestDashboardPane.add(ProfileIcon,0,0);

//        balanceLabel.setText("Balance: " + guest.getBalance());
//        guestDashboardPane.add(balanceLabel, 3, 0);
//Button loadGuestData = new Button("Load Data");
//        guestDashboardPane.add(loadGuestData,0,2);
//        loadGuestData.setOnAction(e->
//        { loadGuestData(this.guest);
//            guestDashboardPane.getChildren().remove(loadGuestData);
//        }
//                );
