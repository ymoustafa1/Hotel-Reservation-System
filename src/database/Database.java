package database;
import java.util.*;
import java.time.*;
import model.*;

public class HotelDatabase {
    public static ArrayList<Guest> guests = new ArrayList<>();
    public static ArrayList<Room> rooms = new ArrayList<>();
    public static ArrayList<Reservation> reservations = new ArrayList<>();
    public static ArrayList<Invoice> invoices = new ArrayList<>();
    public static ArrayList<RoomType> roomTypes = new ArrayList<>();
    public static ArrayList<Amenity> amenities = new ArrayList<>();
    public static ArrayList<Staff> staffMembers = new ArrayList<>();
    public static ArrayList<Room> availableRooms = new ArrayList<>();

    private HotelDatabase() {}

    public static Guest findGuest(String username){
        for (Guest g: guests) {
            if (g.getUsername().equals(username)) {
                return g;
            }
        }
            return null;
    }

    public static Room findRoomById(int id){
        for (Room r: rooms) {
            if (id == r.getRoomId()) {
                return r;
            }
        }
        return null;
    }

    public ArrayList<Room> viewAvailableRooms(LocalDate start, LocalDate end)
    {
        //searching in the whole list of rooms stored in database for available rooms by calling the isAvailable method implemented in the class of Room
        for(Room r :HotelDatabase.rooms)
        {
            if(r.isAvailable(start,end))
            {
                availableRooms.add(r);
            }
        }
        return availableRooms;
    }

    public static Reservation findReservationById(int id){
        for (Reservation r: reservations) {
            if (id == r.getReservationId()) {
                return r;
            }
        }
        return null;
    }
    public static RoomType findRoomType(String name){
        for (RoomType r: roomTypes) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }

    public static Amenity findAmenity(String name){
        for (Amenity a: amenities){
            if (a.getName().equalsIgnoreCase(name)){
                return a;
            }
        }
        return null;
    }
}