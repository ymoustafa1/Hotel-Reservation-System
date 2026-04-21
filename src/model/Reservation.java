package model;

import database.HotelDatabase;
import util.InvalidInputException;
import util.NotFoundException;
import util.ReservationOrderException;
import util.RoomRelatedException;

import java.time.LocalDate;

public class Reservation {
    //defining data fields
    private static int reservationId;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private ReservationStatus status;
    private static int count=1000;

    //constructor
    public Reservation(Guest g, Room r, LocalDate in, LocalDate out)
    {
        if (g == null || r == null || in == null || out == null) {
            throw new InvalidInputException("Null values not allowed");
        }
        //assigning values
        this.guest = g;
        this.room = r;
        this.checkInDate = in;
        this.checkOutDate = out;
        this.reservationId = count++;

        //validating dates to ensure reservation dates are logically correct
        if (!validateDates())
        {
            throw new InvalidInputException("Invalid dates.");
        }
        //defining room availability
        if (room.isAvailable(in, out))
        {
            this.status = ReservationStatus.PENDING;
        }
        else
            throw new NotFoundException("Room not available.");
    }

    public boolean validateDates()
    {
            return checkInDate.isBefore(checkOutDate);
    }

    //getters for data members
    public Room getRoom()
    {
        return room;
    }

    public void setRoom(Room r) {this.room = r;}

    public LocalDate getCheckInDate()
    {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {this.checkInDate = checkInDate;}

    public LocalDate getCheckOutDate()
    {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {this.checkOutDate = checkOutDate;}

    public Guest getGuest()
    {
        return guest;
    }

    public void setGuest(Guest g) {this.guest = g;}

    public int getReservationId()
    {
        return reservationId;
    }

    public void  setReservationId(int rId) {this.reservationId = rId;}

    public ReservationStatus getStatus()
    {
        return status;
    }

    public void setStatus(ReservationStatus s) {this.status = s;}

    //logic methods for reservation status
    public void cancel()
    {
        if (status == ReservationStatus.CANCELLED)
            throw new RoomRelatedException("Already cancelled");
        if(status==ReservationStatus.COMPLETED)
            throw new ReservationOrderException("Cannot cancel a completed reservation");
        status = ReservationStatus.CANCELLED;
    }

    public void reserve()
    {
        if(status!=ReservationStatus.PENDING)
            throw new ReservationOrderException("Reservation must be pending before getting reserved");
        status = ReservationStatus.RESERVED;
    }

    public void complete()
    {
        if(status!=ReservationStatus.RESERVED)
            throw new ReservationOrderException("Cannot complete a reservation without being reserved first");
        status = ReservationStatus.COMPLETED;
    }
}
