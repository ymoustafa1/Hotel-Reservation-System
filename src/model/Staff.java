package model;

import database.HotelDatabase;

import javax.management.relation.Role;
import java.time.LocalDate;
import java.util.ArrayList;

abstract public class Staff
{
    //defining data members
    private String username;
    private String password;
    private LocalDate dateOfBirth;
    private Role role;
    private int workingHrs;

    //method for viewing any room stored in database
    public ArrayList<Room> viewAllRooms() {
        return HotelDatabase.rooms;
    }

    //implementing a method for the login process
    public Boolean login(String name, String pass) {
        for (Staff staff : HotelDatabase.staffMembers) {
            //comparing username storedd in database with that entered by the user(in the parameters)
            if (staff.username.equals(name) && staff.password.equals(pass))
                return true;
        }
        return false;
    }

    //method for viewing all reservations stored in database
    public ArrayList<Reservation> viewAllReservations() {
        return HotelDatabase.reservations
    }

    //method for finding guests by calling the already implemented method (findGuest) in the HotelDatabase class
    public Guest findGuest(String name) {
        return HotelDatabase.findGuest(name);
    }

    //method for viewing the available rooms only by calling the list of availableRooms stored in the database class
    public ArrayList<Room> viewAvailableRooms(LocalDate start, LocalDate end) {
        return HotelDatabase.availableRooms;
    }

    //method for validating and updating number of working hours for a staff member
    public void setWorkingHours(int hrs) {
        //validating
        if (hrs <= 0 || hrs > 24) {
            throw new IllegalArgumentException("Invalid working hours");
        }
        //setting after validating
        this.workingHrs = hrs;
    }
}