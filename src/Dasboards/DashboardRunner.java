package Dasboards;


public class DashboardRunner{
        public static void main(String[] args) {
            // This trick tells JavaFX to launch your specific dashboard class
            // even though we are starting from this 'Runner' class.
            RoomAmenities.launch(RoomAmenities.class , args);
        }
    }

