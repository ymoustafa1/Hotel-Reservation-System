package model;

import database.HotelDatabase;

import java.time.LocalDate;

public class Reservation {
    //defining data fields
    private int reservationId;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private ReservationStatus status;

    //constructor
    public Reservation(Guest g, Room r, LocalDate in, LocalDate out)
    {
        //assigning values
        this.guest = guest;
        this.room = room;
        this.checkInDate = in;
        this.checkOutDate = out;

        //validating dates to ensure reservation dates are logically correct
        if (!validateDates())
        {
            throw new IllegalArgumentException("Invalid dates.");
        }
        //defining room availability
        if (room.isAvailable(in, out))
        {
            this.status = ReservationStatus.PENDING;
        }
        else
            throw new IllegalArgumentException("Room not available.");
    }
    //getters for data members
    public boolean validateDates()
    {
            return checkInDate.isBefore(checkOutDate);
    }

    public Room getRoom()
    {
        return room;
    }
    public LocalDate getCheckInDate()
    {
        return checkInDate;
    }
    public LocalDate getCheckOutDate()
    {
        return checkOutDate;
    }
    public Guest getGuest()
    {
        return guest;
    }
    public int getReservationId()
    {
        return reservationId;
    }
    public ReservationStatus getStatus()
    {
        return status;
    }
    //logic methods for reservation status
    public void cancel()
    {
        status = ReservationStatus.CANCELLED;
    }
    public void reserve()
    {
        status = ReservationStatus.RESERVED;
    }
    public void complete()
    {
        status = ReservationStatus.COMPLETED;
    }
}
