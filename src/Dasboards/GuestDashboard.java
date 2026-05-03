package Dasboards;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.image.*;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Gender;
import model.Guest;
import model.Reservation;

import java.time.LocalDate;
import java.util.List;

public class GuestDashboard extends Application {
    private Guest guest;
    private Label balanceLabel = new Label();
    public GuestDashboard(){}
    public GuestDashboard(Guest guest)
    {
        this.guest=guest;
    }
    GridPane guestDashboardPane = new GridPane();
    Scene guestDashboardScene = new Scene(guestDashboardPane,700,700);


    public  void start (Stage GuestDashboardStage)
    {

        Image profileIcon = new Image(getClass().getResourceAsStream("/profile_icon.jpg"));
        ImageView ProfileIcon = new ImageView(profileIcon);
        ProfileIcon.setPreserveRatio(true);
        ProfileIcon.setFitHeight(250);
        ProfileIcon.setFitWidth(250);
        TextField UpdateBalance = new TextField();
        Button Update = new Button("Update");
        guestDashboardPane.add(new Label("Enter Amount : "),2,2);
        guestDashboardPane.add(UpdateBalance,3,2);
        guestDashboardPane.add(Update,4,2);

            Update.setOnAction(e->
            {
                try
                {
                    updateBalance(Double.parseDouble(UpdateBalance.getText()));
                }
                catch (NumberFormatException ex)
                {
                    System.out.println(" Wrong Format NumberFormatException occurred");
                }
            }
            );


        guestDashboardPane.setPadding(new Insets(5));
        guestDashboardPane.setVgap(10);
        guestDashboardPane.setHgap(10);
        guestDashboardPane.add(ProfileIcon,0,0);
        if (this.guest == null) {
            this.guest = new Guest("Test Username","TestPassword",LocalDate.now(),123.4,"Test Address", Gender.MALE);
        }
        balanceLabel.setText("Balance: " + guest.getBalance());
        guestDashboardPane.add(balanceLabel, 3, 0);
        Button loadGuestData = new Button("Load Data");
        guestDashboardPane.add(loadGuestData,0,2);
        loadGuestData.setOnAction(e->
        { loadGuestData(this.guest);
            guestDashboardPane.getChildren().remove(loadGuestData);
        }
        );
        displayReservations(guest.viewMyReservations());
        GuestDashboardStage.setScene(guestDashboardScene);
        GuestDashboardStage.setResizable(true);
        GuestDashboardStage.setTitle("Guest Dashboard");
        GuestDashboardStage.show();
    }

    private void loadGuestData(Guest guest)
    {   guestDashboardPane.add(new Label("User: "+guest.getUsername()),1,0);
        guestDashboardPane.add(new Label("Gender: "+ guest.gendertoString()),2,0);
        guestDashboardPane.add(new Label("Active Reservations"),0,3);
    }
    private void displayReservations(List<Reservation> res)
    {
        int i=4;
        int j=0;
        int cnt=0;
        for(Reservation r : res )
        {
            guestDashboardPane.add(new Label(cnt+". "+r.toString()),j,i);
            i++;
            cnt++;
        }
    }
    private void updateBalance(double balance)
    {
        guest.updateBalance(balance); // This updates the logic/data
        balanceLabel.setText("Balance: " + guest.getBalance()); // This updates the UI
    }

}
