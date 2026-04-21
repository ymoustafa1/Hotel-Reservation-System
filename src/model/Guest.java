package model;
import java.time.*;
import database.*;
import service.AuthenticationService;
import util.AuthenticationException;
import util.InsufficientBalanceException;
import util.InvalidInputException;
import util.NegativeNumberException;

import java.util.*;


public class Guest  {
    //defining all data fields with private access modifiers
    private String username;
    private String password;
    private LocalDate dateOfBirth;
    private double balance;
    private String address;
    private Gender gender;
    public static int CurrentInvoiceID=1000;
    public Guest()
    {}
    public Guest(String username,String password ,LocalDate dateOfBirth,double balance,String address, Gender gender)
    {
        if (username == null || username.isEmpty())
            throw new InvalidInputException("Invalid username");

        if (password == null || password.length() < 6)
            throw new InvalidInputException("Invalid password");

        if (balance < 0)
            throw new NegativeNumberException("Balance cannot be negative");
        this.username=username;
        this.password=password;
        this.dateOfBirth=dateOfBirth;
        this.balance=balance;
        this.address=address;
        this.gender=gender;
    }
//data methods getters
    public double getBalance() {
        return balance;
    }

    public Gender getGender() {
        return gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getAddress() {
        return address;
    }

//data methods setters
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    //register method throws exception in the case of duplicated username else adds guest to Hotel Database
    public void register()
    {

        AuthenticationService.isUsernameUnique(this.username);

            if(password.length()<6)
            {
                throw new AuthenticationException("Very short Password");
            }

            HotelDatabase.guests.add(this);


    }
    //uses authentication service  class login method to login
    public Object  login()
    {
        return AuthenticationService.login(this.username,this.password);
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


    public void makeReservation(Room room , LocalDate start , LocalDate end)
    {
        if (room == null || start == null || end == null) {
            throw new InvalidInputException("Null values not allowed");
        }
        Reservation reservation = new Reservation(this ,room, start , end);
        HotelDatabase.reservations.add(reservation);
    }

    public ArrayList<Reservation> viewMyReservations()
    {
        ArrayList<Reservation> result = new ArrayList<>();

        for (Reservation r : HotelDatabase.reservations)
        {
            if (r.getGuest().getUsername().equals(this.username))
            {
                result.add(r);
            }
        }

        return result;
    }

    public void cancelReservation(Reservation res)
    {
        //checks if same guest before cancelling
        if(!this.username.equals((res.getGuest()).getUsername())) {
            throw new AuthenticationException("Cannot Cancel Reservation");
        }
        res.cancel();
        HotelDatabase.reservations.remove(res);
    }
    public void  checkout(Reservation res , PaymentMethod method )
    {
     Invoice currentInvoice = new Invoice(CurrentInvoiceID++ , res , method);
     currentInvoice.processPayment(this, method);
    }
    // a value that will be deducted from Guest balance
    public void updateBalance(double amount)
    {
        if(this.balance-amount<0)
        {
            throw new InsufficientBalanceException("Insufficient Balance");
        }
        this.balance-=amount;
    }
}

