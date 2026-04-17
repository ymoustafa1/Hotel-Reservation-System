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

    public static Staff findStaff(String username){
        for (Staff s: staffMembers) {
            if (s.getUsername().equals(username)) {
                return s;
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
        /*searching in the whole list of rooms stored in database for available
        rooms by calling the isAvailable method implemented in the class of Room*/
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



    public static void initializeDummyData() {

        //Clear Old Data
        guests.clear();
        rooms.clear();
        reservations.clear();
        invoices.clear();
        roomTypes.clear();
        amenities.clear();
        staffMembers.clear();
        availableRooms.clear();

        //ROOM TYPES
        RoomType single = new RoomType("Single", 500);
        RoomType doubleRoom = new RoomType("Double", 800);
        RoomType suite = new RoomType("Suite", 1500);

        roomTypes.add(single);
        roomTypes.add(doubleRoom);
        roomTypes.add(suite);

        //AMENITIES
        Amenity wifi = new Amenity("WiFi");
        Amenity ac = new Amenity("AC");
        Amenity tv = new Amenity("TV");
        Amenity minibar = new Amenity("MiniBar");

        amenities.add(wifi);
        amenities.add(ac);
        amenities.add(tv);
        amenities.add(minibar);

        //ROOMS
        Room r1 = new Room(101, single);
        r1.addAmenity(wifi);
        r1.addAmenity(ac);

        Room r2 = new Room(102, doubleRoom);
        r2.addAmenity(wifi);
        r2.addAmenity(tv);

        Room r3 = new Room(201, suite);
        r3.addAmenity(wifi);
        r3.addAmenity(ac);
        r3.addAmenity(tv);
        r3.addAmenity(minibar);

        rooms.add(r1);
        rooms.add(r2);
        rooms.add(r3);

        //GUESTS
        Guest g1 = new Guest("youssef", "12345678", LocalDate.of(2008, 4, 24),7000, "Cairo", Gender.MALE);
        Guest g2 = new Guest("kenzy", "12345678", LocalDate.of(2007, 6, 1), 7900, "Cairo", Gender.FEMALE);

        guests.add(g1);
        guests.add(g2);

        //STAFF
        Staff s1 = new Admin("admin1", "admin123");
        staffMembers.add(s1);

        //RESERVATIONS


        //INVOICES

    }
}