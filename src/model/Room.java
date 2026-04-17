package model;
import java.time.*;
import database.*;
import java.util.*;
public class Room {
    private int roomId;
    private RoomType roomType;
    private ArrayList<Amenity> amenities;
    private double price;

    public Room(int roomId, RoomType roomType) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.price = roomType.getBasePrice();
        this.amenities = new ArrayList<>();
    }

    public void addAmenity(Amenity a) {
        if (!amenities.contains(a)) {
            amenities.add(a);
        }
    }

    public void removeAmenity(Amenity a) {
        amenities.remove(a);
    }

    public double getPrice() {
        return price;
    }

    public int getRoomId() {
        return roomId;
    }
    public void setPrice (double price){
        this.price= price;
    }
    public void setRoomId(int roomId){
        this.roomId= roomId;
    }
    public boolean isAvailable(LocalDate start, LocalDate end) {
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getRoom().getRoomId() == this.roomId) {
                if (!(end.isBefore(r.getCheckInDate()) || start.isAfter(r.getCheckOutDate()))) {
                    return false;
                }
            }
        }
        return true;
    }
}