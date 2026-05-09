package model;

import database.HotelDatabase;
import util.*;

import java.time.LocalDate;
import java.util.ArrayList;

abstract public class Staff
{
    //defining data members
    private String username;
    private String password;
    private Role role;
    private int workingHrs;
    private int staffID;
    private static int counter=0;

    protected Staff() {}
    protected Staff(String username,String password,Role role)
    {
        if (username == null || username.isEmpty())
            throw new InvalidInputException("Invalid username");

        if (password == null || password.length() < 6)
            throw new InvalidInputException("Invalid password");

        if (role == null)
            throw new InvalidInputException("Role required");
        this.username=username;
        this.password=password;
        this.role=role;
        this.staffID=++counter;      //generating an ID for each new staff member
    }

    //Setters
    public void setPassword(String password) {this.password = password;}

    //validating number of working hours for a staff member before setting it
    public void setWorkingHours(int hrs)
    {
        if (hrs <= 0 || hrs > 24)
            throw new InvalidInputException("Invalid working hours");

        this.workingHrs = hrs;
    }



    //Getters
    public String getUsername() {return username;}

    public String getPassword() {return password;}

    public int getStaffID() {return staffID;}

    public Role getRole() {return role;}

    //method for viewing any room stored in database
    public ArrayList<Room> viewAllRooms(){return HotelDatabase.rooms;}

    //implementing a static method for the login process to be used by both subclasses
    public static Staff login(String username, String password){
        for (Staff s : HotelDatabase.staffMembers)
        {
            if (s.getUsername().equals(username) && s.getPassword().equals(password))
                return s;
        }
        throw new AuthenticationException("Invalid username or password");
    }

    //method for viewing all reservations stored in database
    public ArrayList<Reservation> viewAllReservations() {return HotelDatabase.reservations;}

    //method for finding guests by calling the already implemented method (findGuest) in the HotelDatabase class
    public Guest findGuest(String name) {
        return HotelDatabase.findGuest(name);
    }

    //method for viewing the available rooms only by calling the list of availableRooms stored in the database class
    public ArrayList<Room> viewAvailableRooms(LocalDate start, LocalDate end)
    {
        ArrayList<Room> availableRooms = new ArrayList<>();

        for (Room r : HotelDatabase.rooms)
        {
            if (r.isAvailable(start, end))
            {
                availableRooms.add(r);
            }
        }

        return availableRooms;
    }

    public String getWorkingHours() {
        return String.valueOf(workingHrs);
    }
}