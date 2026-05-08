package database;
import java.util.*;
import java.time.*;
import model.*;


public class HotelDatabase
{
    public static ArrayList<Guest> guests = new ArrayList<>();
    public static ArrayList<Room> rooms = new ArrayList<>();
    public static ArrayList<Reservation> reservations = new ArrayList<>();
    public static ArrayList<Invoice> invoices = new ArrayList<>();
    public static ArrayList<RoomType> roomTypes = new ArrayList<>();
    public static ArrayList<Amenity> amenities = new ArrayList<>();
    public static ArrayList<Staff> staffMembers = new ArrayList<>();

    private HotelDatabase() {}

    public static Guest findGuest(String username){
        for (Guest g: guests) {
            if (g.getUsername().equalsIgnoreCase(username)) {
                return g;
            }
        }
            return null;
    }

    public static Staff findStaff(String username){
        for (Staff s: staffMembers) {
            if (s.getUsername().equalsIgnoreCase(username)) {
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

    public static Reservation findReservationById(int id){
        for (Reservation r: reservations) {
            if (id == r.getReservationId()) {
                return r;
            }
        }
        return null;
    }

    public static ArrayList<Reservation> findReservationsByGuest(Guest g)
    {
        ArrayList<Reservation> result = new ArrayList<>();

        for (Reservation r : reservations)
        {
            if (r.getGuest().equals(g))
            {
                result.add(r);
            }
        }

        return result;
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

    public static boolean validateDates(LocalDate checkInDate, LocalDate checkOutDate)
    {
        return checkInDate.isBefore(checkOutDate);
    }



    public static void initializeDummyData() {

        guests.clear();
        rooms.clear();
        reservations.clear();
        invoices.clear();
        roomTypes.clear();
        amenities.clear();
        staffMembers.clear();
        Reservation.resetCounter();

        Amenity wifi    = new Amenity("WiFi",    AmenityType.ROOM,  100);
        Amenity ac      = new Amenity("AC",      AmenityType.ROOM,  150);
        Amenity tv      = new Amenity("TV",      AmenityType.ROOM,  300);
        Amenity minibar = new Amenity("MiniBar", AmenityType.ROOM,  550);
        Amenity spa     = new Amenity("Spa",     AmenityType.HOTEL, 1000);
        Amenity pool    = new Amenity("Pool",    AmenityType.HOTEL, 870);
        Amenity gym     = new Amenity("Gym",     AmenityType.HOTEL, 560);
        Amenity buffet  = new Amenity("Lunch",   AmenityType.HOTEL, 300);

        amenities.add(wifi);
        amenities.add(ac);
        amenities.add(tv);
        amenities.add(minibar);
        amenities.add(spa);
        amenities.add(pool);
        amenities.add(gym);
        amenities.add(buffet);

        RoomType single = new RoomType("Single", 500);
        single.addAmenity(wifi);
        single.addAmenity(ac);
        single.addAmenity(buffet);

        RoomType doubleRoom = new RoomType("Double", 800);
        doubleRoom.addAmenity(wifi);
        doubleRoom.addAmenity(tv);
        doubleRoom.addAmenity(ac);
        doubleRoom.addAmenity(buffet);
        doubleRoom.addAmenity(spa);

        RoomType suite = new RoomType("Suite", 1500);
        suite.addAmenity(wifi);
        suite.addAmenity(ac);
        suite.addAmenity(tv);
        suite.addAmenity(minibar);
        suite.addAmenity(spa);
        suite.addAmenity(gym);
        suite.addAmenity(buffet);
        suite.addAmenity(pool);

        roomTypes.add(single);
        roomTypes.add(doubleRoom);
        roomTypes.add(suite);

        Room r1 = new Room(101, single);
        Room r2 = new Room(102, doubleRoom);
        Room r3 = new Room(201, suite);

        rooms.add(r1);
        rooms.add(r2);
        rooms.add(r3);

        // GUESTS
        Guest g1 = new Guest("youssef", "1", LocalDate.of(2008, 4, 24), 7000, "Cairo", Gender.MALE);
        Guest g2 = new Guest("kenzy", "12345678", LocalDate.of(2007, 6, 1), 7900, "Cairo", Gender.FEMALE);

        guests.add(g1);
        guests.add(g2);

        // STAFF
        Staff s1 = new Admin("admin1", "admin123");
        Staff s2 = new Receptionist("rec1", "rec123");
        staffMembers.add(s1);
        staffMembers.add(s2);

        // RESERVATIONS
        Reservation res1 = new Reservation(g2, r1, LocalDate.now(), LocalDate.now().plusDays(3));
        Reservation res2 = new Reservation(g1, r2, LocalDate.now(), LocalDate.now().plusDays(3));
        res1.setStatus(ReservationStatus.RESERVED);
        res2.setStatus(ReservationStatus.RESERVED);
        reservations.add(res1);
        reservations.add(res2);

        // INVOICES
        Invoice invoice = new Invoice(res1,PaymentMethod.CASH);
        Invoice invoice1 = new Invoice(res1,PaymentMethod.CREDIT_CARD);
    }
}