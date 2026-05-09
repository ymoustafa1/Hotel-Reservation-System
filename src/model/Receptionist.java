package model;

import database.HotelDatabase;
import util.InvalidInputException;

import java.time.LocalDate;
import java.util.ArrayList;

public class Receptionist extends Staff
{
    private int workingHours;
    public Receptionist(String name,String pass,int workingHours)
    {
        //adding a new staff member of role receptionist
        super(name,pass,Role.RECEPTIONIST);
        this.workingHours = workingHours;
    }

    //methods for checking in and out by validating then calling already implemented methods in Reservation class
    public void checkIn(Reservation r)
    {
        if(r==null)
            throw new InvalidInputException("Reservation cant be null");
        r.reserve();
    }

    public void checkOut(Reservation r)
    {
        if(r==null)
            throw new InvalidInputException("Reservation cant be null");
        r.complete();
    }

    @Override
    public String getWorkingHours() {
        return String.valueOf(workingHours);
    }
}
