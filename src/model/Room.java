package model;
import java.time.*;
import database.*;
import util.InvalidInputException;
import util.NegativeNumberException;

import java.util.*;
public class Room {
    //defining all data fields with private access modifiers
    private int roomId;
    private RoomType roomType;
    private ArrayList<Amenity> amenities;
    private double price;

    public Room(int roomId, RoomType roomType){
        if (roomType == null)
            throw new InvalidInputException("RoomType cannot be null");
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
    public RoomType getRoomType(){return roomType;}
    public double getPrice() {return price;}
    public int getRoomId() {
        return roomId;
    }
    public ArrayList<Amenity> getAmenities() {return amenities;}
    //data methods setters
    public void setRoomType(RoomType roomType){
        if (roomType == null)
        throw new InvalidInputException("RoomType cannot be null");
        this.roomType = roomType;
    }
    public void setPrice (double price){
        if (price < 0)
            throw new NegativeNumberException("Price cannot be negative");
        this.price= price;
    }
    public void setRoomId(int roomId){
        this.roomId= roomId;
    }
    public void setAmenities(ArrayList<Amenity> amenities){this.amenities = amenities;}
    //checks reservation for specific room in specific dates
    public boolean isAvailable(LocalDate start, LocalDate end){
        if (start == null || end == null)
            throw new InvalidInputException("Dates cannot be null");
        if (!start.isBefore(end))
            throw new InvalidInputException("Invalid date range");
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