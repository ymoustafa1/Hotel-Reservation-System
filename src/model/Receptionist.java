package model;

import database.HotelDatabase;

import java.time.LocalDate;
import java.util.ArrayList;

public class Receptionist extends Staff
{
    public Receptionist(String name,String pass)
    {
        //adding a new staff member of role receptionist
        super(name,pass,Role.RECEPTIONIST);
    }

    //methods for checking in and out by validating then calling already implemented methods in Reservation class
    public void checkIn(Reservation r)
    {
        if(r==null)
            throw new IllegalArgumentException("Reservation cant be null");
        r.reserve();
    }

    public void checkOut(Reservation r)
    {
        if(r==null)
            throw new IllegalArgumentException("Reservation cant be null");
        r.complete();
    }
}
