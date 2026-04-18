package model;
import java.time.*;
import database.*;
import java.util.*;
public class Room {
    //defining all data fields with private access modifiers
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
    //adds amenity to the room and prevents duplicates
    public void addAmenity(Amenity a) {
        if (!amenities.contains(a)) {
            amenities.add(a);
        }
    }
    //removes specified amenities from the room
    public void removeAmenity(Amenity a) {
        amenities.remove(a);
    }
    //data methods getters
    public double getPrice() {
        return price;
    }
    public int getRoomId() {
        return roomId;
    }
    //data methods setters
    public void setPrice (double price){
        this.price= price;
    }
    public void setRoomId(int roomId){
        this.roomId= roomId;
    }
    //checks reservation for specific room in specific dates
    public boolean isAvailable(LocalDate start, LocalDate end) {
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getRoom().getRoomId() == this.roomId) {
                //returns false if dates are already reserved
                if (!(end.isBefore(r.getCheckInDate()) || start.isAfter(r.getCheckOutDate()))) {
                    return false;
                }
            }
        }
        return true;
    }
}