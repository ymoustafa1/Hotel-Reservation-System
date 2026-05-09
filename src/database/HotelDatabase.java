package database;

import model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class HotelDatabase {



    private static final String URL =
            "jdbc:sqlite:hotel.db?busy_timeout=5000";


    public static ArrayList<Guest> guests =
            new ArrayList<>();

    public static ArrayList<Room> rooms =
            new ArrayList<>();

    public static ArrayList<Reservation> reservations =
            new ArrayList<>();

    public static ArrayList<Invoice> invoices =
            new ArrayList<>();

    public static ArrayList<RoomType> roomTypes =
            new ArrayList<>();

    public static ArrayList<Amenity> amenities =
            new ArrayList<>();

    public static ArrayList<Staff> staffMembers =
            new ArrayList<>();

    private HotelDatabase() {}



    public static Connection connect()
            throws SQLException {

        try {

            Class.forName(
                    "org.sqlite.JDBC"
            );

        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        }

        return DriverManager
                .getConnection(URL);
    }

    public static void initializeDatabase() {

        try (
                Connection conn = connect();
                Statement stmt = conn.createStatement()
        ) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS guests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE,
                    password TEXT,
                    birth_date TEXT,
                    national_id INTEGER,
                    address TEXT,
                    gender TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS staff (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE,
                    password TEXT,
                    role TEXT,
                    working_hours INTEGER
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS room_types (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE,
                    price REAL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_number INTEGER UNIQUE,
                    room_type_name TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    guest_username TEXT,
                    room_number INTEGER,
                    check_in TEXT,
                    check_out TEXT,
                    status TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS invoices (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reservation_id INTEGER,
                    payment_method TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS amenities (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE,
                    type TEXT,
                    price REAL
                )
            """);

            System.out.println(
                    "Database initialized."
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void insertSeedData() {

        try (
                Connection conn = connect();
                Statement stmt = conn.createStatement()
        ) {

            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM staff"
            );

            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



        amenities.clear();
        roomTypes.clear();
        rooms.clear();
        guests.clear();
        staffMembers.clear();
        reservations.clear();
        invoices.clear();



        Amenity wifi =
                new Amenity(
                        "WiFi",
                        AmenityType.ROOM,
                        100
                );

        Amenity ac =
                new Amenity(
                        "AC",
                        AmenityType.ROOM,
                        150
                );

        Amenity pool =
                new Amenity(
                        "Pool",
                        AmenityType.HOTEL,
                        800
                );

        amenities.add(wifi);
        amenities.add(ac);
        amenities.add(pool);



        RoomType single =
                new RoomType(
                        "Single",
                        500
                );

        RoomType deluxe =
                new RoomType(
                        "Deluxe",
                        1500
                );

        roomTypes.add(single);
        roomTypes.add(deluxe);



        Room r1 =
                new Room(101, single);

        Room r2 =
                new Room(102, single);

        Room r3 =
                new Room(201, deluxe);

        rooms.add(r1);
        rooms.add(r2);
        rooms.add(r3);



        Guest youssef =
                new Guest(
                        "youssef",
                        "pass123",
                        LocalDate.of(
                                2008,
                                4,
                                24
                        ),
                        777777,
                        "Madinaty",
                        Gender.MALE
                );

        Guest ahmed =
                new Guest(
                        "ahmed",
                        "pass123",
                        LocalDate.of(
                                2000,
                                5,
                                12
                        ),
                        123456,
                        "Cairo",
                        Gender.MALE
                );

        guests.add(youssef);
        guests.add(ahmed);



        Admin admin =
                new Admin(
                        "admin1",
                        "admin123"
                );

        Receptionist rec =
                new Receptionist(
                        "rec1",
                        "rec123",
                        8
                );

        staffMembers.add(admin);
        staffMembers.add(rec);



        Reservation reservation =
                new Reservation(
                        youssef,
                        r1,
                        LocalDate.now(),
                        LocalDate.now()
                                .plusDays(5)
                );

        reservation.setStatus(
                ReservationStatus.RESERVED
        );

        reservations.add(reservation);


        Invoice invoice =
                new Invoice(
                        reservation,
                        PaymentMethod.CASH
                );

        invoices.add(invoice);



        saveAllData();

        System.out.println(
                "Seed data inserted."
        );
    }
    public static void saveAllData() {

        for (Amenity a : amenities) {
            insertAmenity(a);
        }

        for (RoomType rt : roomTypes) {
            insertRoomType(rt);
        }

        for (Room r : rooms) {
            insertRoom(r);
        }

        for (Guest g : guests) {
            insertGuest(g);
        }

        for (Staff s : staffMembers) {

            if (s instanceof Receptionist) {

                Receptionist r =
                        (Receptionist) s;

                insertStaff(
                        r,
                        Integer.valueOf(r.getWorkingHours())
                );

            } else {

                insertStaff(s, 0);
            }
        }

        for (Reservation r : reservations) {
            insertReservation(r);
        }

        for (Invoice i : invoices) {
            insertInvoice(i);
        }
    }


    public static void loadData() {

        loadAmenities();
        loadRoomTypes();
        loadRooms();
        loadGuests();
        loadStaff();
        loadReservations();
    }


    public static void insertGuest(Guest guest) {

        String sql = """
            INSERT INTO guests
            (username, password,
             birth_date, national_id,
             address, gender)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (
                Connection conn = connect();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setString(
                    1,
                    guest.getUsername()
            );

            pstmt.setString(
                    2,
                    guest.getPassword()
            );

            pstmt.setString(
                    3,
                    guest.getDateOfBirth()
                            .toString()
            );


            pstmt.setString(
                    5,
                    guest.getAddress()
            );

            pstmt.setString(
                    6,
                    guest.getGender()
                            .name()
            );

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertRoom(Room room) {

        String sql = """
            INSERT INTO rooms
            (room_number, room_type_name)
            VALUES (?, ?)
        """;

        try (
                Connection conn = connect();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setInt(
                    1,
                    room.getRoomId()
            );

            pstmt.setString(
                    2,
                    room.getRoomType()
                            .getName()
            );

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertRoomType(
            RoomType roomType
    ) {

        String sql = """
            INSERT INTO room_types
            (name, price)
            VALUES (?, ?)
        """;

        try (
                Connection conn = connect();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setString(
                    1,
                    roomType.getName()
            );

            pstmt.setDouble(
                    2,
                    roomType.getBasePrice()
            );

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertAmenity(
            Amenity amenity
    ) {

        String sql = """
            INSERT INTO amenities
            (name, type, price)
            VALUES (?, ?, ?)
        """;

        try (
                Connection conn = connect();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setString(
                    1,
                    amenity.getName()
            );

            pstmt.setString(
                    2,
                    amenity.getType()
                            .name()
            );

            pstmt.setDouble(
                    3,
                    amenity.getPrice()
            );

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertReservation(
            Reservation reservation
    ) {

        String sql = """
            INSERT INTO reservations
            (guest_username,
             room_number,
             check_in,
             check_out,
             status)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (
                Connection conn = connect();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setString(
                    1,
                    reservation.getGuest()
                            .getUsername()
            );

            pstmt.setInt(
                    2,
                    reservation.getRoom()
                            .getRoomId()
            );

            pstmt.setString(
                    3,
                    reservation.getCheckInDate()
                            .toString()
            );

            pstmt.setString(
                    4,
                    reservation.getCheckOutDate()
                            .toString()
            );

            pstmt.setString(
                    5,
                    reservation.getStatus()
                            .name()
            );

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertInvoice(
            Invoice invoice
    ) {

        String sql = """
            INSERT INTO invoices
            (reservation_id,
             payment_method)
            VALUES (?, ?)
        """;

        try (
                Connection conn = connect();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setInt(
                    1,
                    invoice.getReservation()
                            .getReservationId()
            );

            pstmt.setString(
                    2,
                    invoice.getPaymentMethod()
                            .name()
            );

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertStaff(
            Staff staff,
            int hours
    ) {

        String role;

        if (staff instanceof Admin) {
            role = "ADMIN";
        } else {
            role = "RECEPTIONIST";
        }

        String sql = """
            INSERT INTO staff
            (username,
             password,
             role,
             working_hours)
            VALUES (?, ?, ?, ?)
        """;

        try (
                Connection conn = connect();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setString(
                    1,
                    staff.getUsername()
            );

            pstmt.setString(
                    2,
                    staff.getPassword()
            );

            pstmt.setString(
                    3,
                    role
            );

            pstmt.setInt(
                    4,
                    hours
            );

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void loadGuests() {

        guests.clear();

        String sql =
                "SELECT * FROM guests";

        try (
                Connection conn = connect();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            ResultSet rs =
                    pstmt.executeQuery();

            while (rs.next()) {

                Guest guest =
                        new Guest(
                                rs.getString(
                                        "username"
                                ),
                                rs.getString(
                                        "password"
                                ),
                                LocalDate.parse(
                                        rs.getString(
                                                "birth_date"
                                        )
                                ),
                                rs.getInt(
                                        "national_id"
                                ),
                                rs.getString(
                                        "address"
                                ),
                                Gender.valueOf(
                                        rs.getString(
                                                "gender"
                                        )
                                )
                        );

                guests.add(guest);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadRoomTypes() {}

    public static void loadRooms() {}

    public static void loadReservations() {}

    public static void loadAmenities() {}

    public static void loadStaff() {}



    public static Guest findGuest(
            String username
    ) {

        for (Guest g : guests) {

            if (
                    g.getUsername()
                            .equalsIgnoreCase(
                                    username
                            )
            ) {
                return g;
            }
        }

        return null;
    }

    public static Staff findStaff(
            String username
    ) {

        for (Staff s : staffMembers) {

            if (
                    s.getUsername()
                            .equalsIgnoreCase(
                                    username
                            )
            ) {
                return s;
            }
        }

        return null;
    }

    public static Room findRoomById(
            int id
    ) {

        for (Room r : rooms) {

            if (r.getRoomId() == id) {
                return r;
            }
        }

        return null;
    }

    public static Reservation findReservationById(
            int id
    ) {

        for (Reservation r : reservations) {

            if (
                    r.getReservationId()
                            == id
            ) {
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

    public static RoomType findRoomType(
            String name
    ) {

        for (RoomType r : roomTypes) {

            if (
                    r.getName()
                            .equalsIgnoreCase(
                                    name
                            )
            ) {
                return r;
            }
        }

        return null;
    }

    public static Amenity findAmenity(
            String name
    ) {

        for (Amenity a : amenities) {

            if (
                    a.getName()
                            .equalsIgnoreCase(
                                    name
                            )
            ) {
                return a;
            }
        }

        return null;
    }



    public static boolean validateDates(
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {

        return checkInDate.isBefore(
                checkOutDate
        );
    }
}