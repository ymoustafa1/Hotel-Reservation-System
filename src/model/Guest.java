package model;
import java.time.*;
import database.*;
import java.util.*;


public class Guest extends AuthenticationService {
    //defining all data fields with private access modifiers
    private String username;
    private String password;
    private LocalDate dateOfBirth;
    private double balance;
    private String address;
    private Gender gender;

    public Guest()
    {}
    public Guest(String username,String password ,LocalDate dateOfBirth,double balance,String address, Gender gender)
    {
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
    public void register() throws Exception
    {

        AuthenticationService.isUsernameUnique(this.username);

            if(password.length()<6)
            {
                throw new Exception("Very short Password");
            }

            HotelDatabase.guests.add(this);


    }
    //uses authentication service  class login method to login
    public Object  login()
    {
        return AuthenticationService.login(this.username,this.password);
    }
    //method for viewing the available rooms only by calling the list of availableRooms stored in the database class
    public ArrayList<Room> viewAvailableRooms(LocalDate start , LocalDate end)
    {
        return HotelDatabase.availableRooms;
    }


    public void makeReservation(Room room , LocalDate start , LocalDate end) throws Exception
    {
        Reservation reservation = new Reservation(this ,room, start , end);
        //validates dates of reservation throws exception if dates not vaild
        if(!reservation.validateDates())
            throw new Exception("Dates are not vaild");
        //checks if room available throws exception if not available
        if(!room.isAvailable(start,end))
            throw new Exception("Room not available");

        HotelDatabase.reservations.add(reservation);
    }

    public void cancelReservation(Reservation res) throws Exception
    {
        //checks if same guest before cancelling
        if(!this.username.equals((res.getGuest()).getUsername())) {
            throw new Exception("Cannot Cancel Reservation");
        }
        res.cancel();
        HotelDatabase.reservations.remove(res);
    }
    //needs invoice from youssef postponed
    public void  checkout(Reservation res , PaymentMethod method )
    {

    }

    public void updateBalance(double amount) throws Exception
    {
        if(this.balance+amount<0)
        {
            throw new Exception("Insufficient Balance");
        }
        this.balance+=amount;
    }
}

