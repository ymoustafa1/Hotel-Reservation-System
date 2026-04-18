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

    public Reservation(int id, Guest guest, Room room, LocalDate chin, LocalDate chout)
    {
        this.reservationId = id;
        this.guest = guest;
        this.room = room;
        this.checkInDate = chin;
        this.checkOutDate = chout;

        validateDates();

        if (room.isAvailable(chin, chout))
            this.status = ReservationStatus.PENDING;
        else
            throw new IllegalArgumentException("Room not available.");
    }
}
