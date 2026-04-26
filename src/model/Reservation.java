package model;

import database.HotelDatabase;
import util.InvalidInputException;
import util.NotFoundException;
import util.ReservationOrderException;
import util.RoomRelatedException;

import java.time.LocalDate;
import java.util.ArrayList;

public class Reservation {
    //defining data fields
    private int reservationId;
    private Guest guest;
    private Room room;
    private Invoice invoice;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private ReservationStatus status;
    private ArrayList<Amenity> extraAmenities = new ArrayList<>();
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
        if (!HotelDatabase.validateDates(checkInDate,checkOutDate))
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

    private void  setReservationId(int rId) {this.reservationId = rId;}

    public ReservationStatus getStatus()
    {
        return status;
    }

    public void setStatus(ReservationStatus s) {this.status = s;}


    public void setInvoice(Invoice i) {
        this.invoice = i;
    }

    public Invoice getInvoice() {
        return invoice;
    }

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
    public void addExtraAmenity(Amenity a)
    {
        if (!extraAmenities.contains(a))
            extraAmenities.add(a);
    }

    public ArrayList<Amenity> getExtraAmenities()
    {
        return extraAmenities;
    }

    public static void resetCounter() {
        count = 1000;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("\nReservation ID: ").append(this.reservationId).append("\n");
        sb.append("Guest: ").append(guest.getUsername()).append("\n");
        sb.append("Room: ").append(room.getRoomId())
                .append(" (").append(room.getRoomType().getName()).append(")\n");

        sb.append("From: ").append(checkInDate)
                .append(" To: ").append(checkOutDate).append("\n");

        sb.append("Status: ").append(status).append("\n");

        // Extras
        if (extraAmenities != null && !extraAmenities.isEmpty())
        {
            sb.append("Extras: ");
            for (Amenity a : extraAmenities)
            {
                sb.append(a.getName()).append(", ");
            }
            sb.setLength(sb.length() - 2); // remove last comma
            sb.append("\n");
        }

        return sb.toString();
    }
}
