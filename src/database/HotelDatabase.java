package database;

import model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class HotelDatabase {

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

    public static Guest findGuest(String username) {

        for (Guest g : guests) {

            if (
                    g.getUsername()
                            .equalsIgnoreCase(username)
            ) {
                return g;
            }
        }

        return null;
    }

    public static Staff findStaff(String username) {

        for (Staff s : staffMembers) {

            if (
                    s.getUsername()
                            .equalsIgnoreCase(username)
            ) {
                return s;
            }
        }

        return null;
    }

    public static Room findRoomById(int id) {

        for (Room r : rooms) {

            if (r.getRoomId() == id) {
                return r;
            }
        }

        return null;
    }

    public static Reservation findReservationById(int id) {

        for (Reservation r : reservations) {

            if (
                    r.getReservationId() == id
            ) {
                return r;
            }
        }

        return null;
    }

    public static ArrayList<Reservation>
    findReservationsByGuest(Guest guest) {

        ArrayList<Reservation> result =
                new ArrayList<>();

        for (Reservation r : reservations) {

            if (r.getGuest().equals(guest)) {
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
                            .equalsIgnoreCase(name)
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
                            .equalsIgnoreCase(name)
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


public static void initializeDummyData() {

    guests.clear();
    rooms.clear();
    reservations.clear();
    invoices.clear();
    roomTypes.clear();
    amenities.clear();
    staffMembers.clear();

    Reservation.resetCounter();

    Random random = new Random();

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

    Amenity tv =
            new Amenity(
                    "TV",
                    AmenityType.ROOM,
                    300
            );

    Amenity minibar =
            new Amenity(
                    "MiniBar",
                    AmenityType.ROOM,
                    550
            );

    Amenity spa =
            new Amenity(
                    "Spa",
                    AmenityType.HOTEL,
                    1000
            );

    Amenity pool =
            new Amenity(
                    "Pool",
                    AmenityType.HOTEL,
                    870
            );

    Amenity gym =
            new Amenity(
                    "Gym",
                    AmenityType.HOTEL,
                    560
            );

    Amenity buffet =
            new Amenity(
                    "Lunch",
                    AmenityType.HOTEL,
                    300
            );

    Amenity parking =
            new Amenity(
                    "Parking",
                    AmenityType.HOTEL,
                    200
            );

    Amenity balcony =
            new Amenity(
                    "Balcony",
                    AmenityType.ROOM,
                    250
            );

    amenities.add(wifi);
    amenities.add(ac);
    amenities.add(tv);
    amenities.add(minibar);
    amenities.add(spa);
    amenities.add(pool);
    amenities.add(gym);
    amenities.add(buffet);
    amenities.add(parking);
    amenities.add(balcony);

    RoomType single =
            new RoomType(
                    "Single",
                    500
            );

    single.addAmenity(wifi);
    single.addAmenity(ac);

    RoomType doubleRoom =
            new RoomType(
                    "Double",
                    800
            );

    doubleRoom.addAmenity(wifi);
    doubleRoom.addAmenity(tv);

    RoomType suite =
            new RoomType(
                    "Suite",
                    1500
            );

    suite.addAmenity(wifi);
    suite.addAmenity(tv);
    suite.addAmenity(minibar);
    suite.addAmenity(spa);

    RoomType deluxe =
            new RoomType(
                    "Deluxe",
                    1200
            );

    deluxe.addAmenity(wifi);
    deluxe.addAmenity(pool);

    RoomType king =
            new RoomType(
                    "King",
                    1800
            );

    king.addAmenity(wifi);
    king.addAmenity(minibar);

    RoomType queen =
            new RoomType(
                    "Queen",
                    1600
            );

    queen.addAmenity(tv);
    queen.addAmenity(ac);

    RoomType family =
            new RoomType(
                    "Family",
                    2000
            );

    family.addAmenity(pool);
    family.addAmenity(buffet);

    RoomType economy =
            new RoomType(
                    "Economy",
                    400
            );

    economy.addAmenity(wifi);

    RoomType presidential =
            new RoomType(
                    "Presidential",
                    5000
            );

    presidential.addAmenity(wifi);
    presidential.addAmenity(tv);
    presidential.addAmenity(spa);
    presidential.addAmenity(pool);

    RoomType business =
            new RoomType(
                    "Business",
                    1300
            );

    business.addAmenity(wifi);
    business.addAmenity(parking);

    roomTypes.add(single);
    roomTypes.add(doubleRoom);
    roomTypes.add(suite);
    roomTypes.add(deluxe);
    roomTypes.add(king);
    roomTypes.add(queen);
    roomTypes.add(family);
    roomTypes.add(economy);
    roomTypes.add(presidential);
    roomTypes.add(business);

    int roomNumber = 101;

    for (RoomType type : roomTypes) {

        for (int i = 0; i < 10; i++) {

            Room room =
                    new Room(
                            roomNumber++,
                            type
                    );

            rooms.add(room);
        }
    }

    String[] names = {
            "Ahmed",
            "Mohamed",
            "Omar",
            "Ali",
            "Mariam",
            "Sara",
            "Nour",
            "Salma",
            "Laila",
            "Hassan",
            "Ibrahim",
            "Khaled",
            "Mostafa",
            "Karim",
            "Ziad",
            "Malak",
            "Farah",
            "Hana",
            "Adam"
    };

    String[] addresses = {
            "Cairo",
            "Abaseya",
            "Madinaty",
            "Tagamo3",
            "Maadi"
    };

    for (int i = 1; i <= 100; i++) {

        Gender gender;

        if (random.nextBoolean()) {

            gender = Gender.MALE;

        } else {

            gender = Gender.FEMALE;
        }

        String name =
                names[random.nextInt(
                        names.length
                )] + i;

        String password =
                "pass123";

        LocalDate birthDate =
                LocalDate.of(
                        1985 + random.nextInt(20),
                        1 + random.nextInt(12),
                        1 + random.nextInt(28)
                );

        int nationalId =
                100000 + random.nextInt(900000);

        String address =
                addresses[random.nextInt(
                        addresses.length
                )];

        Guest guest =
                new Guest(
                        name,
                        password,
                        birthDate,
                        nationalId,
                        address,
                        gender
                );

        guests.add(guest);
    }

    for (int i = 1; i <= 10; i++) {

        Admin admin =
                new Admin(
                        "admin" + i,
                        "admin123"
                );

        staffMembers.add(admin);
    }

    for (int i = 1; i <= 5; i++) {

        Receptionist receptionist =
                new Receptionist(
                        "rec" + i,
                        "rec123",
                        8
                );

        staffMembers.add(receptionist);
    }

    ReservationStatus[] statuses = {
            ReservationStatus.RESERVED,
            ReservationStatus.PENDING,
            ReservationStatus.CANCELLED,
            ReservationStatus.COMPLETED
    };

    Collections.shuffle(rooms);

    for (int i = 0; i < 50; i++) {

        Guest guest =
                guests.get(i);

        Room room =
                rooms.get(i);

        LocalDate checkIn =
                LocalDate.now()
                        .minusDays(
                                random.nextInt(60)
                        );

        LocalDate checkOut =
                checkIn.plusDays(
                        1 + random.nextInt(7)
                );

        Reservation reservation =
                new Reservation(
                        guest,
                        room,
                        checkIn,
                        checkOut
                );

        reservation.setStatus(
                statuses[
                        random.nextInt(
                                statuses.length
                        )
                        ]
        );

        reservations.add(reservation);
    }

    for (int i = 0; i < 70; i++) {

        Reservation reservation =
                reservations.get(
                        random.nextInt(
                                reservations.size()
                        )
                );

        PaymentMethod paymentMethod;

        if (random.nextBoolean()) {

            paymentMethod =
                    PaymentMethod.CASH;

        } else {

            paymentMethod =
                    PaymentMethod.CREDIT_CARD;
        }

        Invoice invoice =
                new Invoice(
                        reservation,
                        paymentMethod
                );

        invoices.add(invoice);
    }

    Guest showcaseGuest =
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

    guests.add(showcaseGuest);

    for (int i = 50; i < 70; i++) {

        Room room =
                rooms.get(i);

        LocalDate checkIn =
                LocalDate.now()
                        .minusDays(
                                random.nextInt(120)
                        );

        LocalDate checkOut =
                checkIn.plusDays(
                        2 + random.nextInt(8)
                );

        Reservation reservation =
                new Reservation(
                        showcaseGuest,
                        room,
                        checkIn,
                        checkOut
                );

        if (i % 4 == 0) {

            reservation.setStatus(
                    ReservationStatus.COMPLETED
            );

        } else if (i % 4 == 1) {

            reservation.setStatus(
                    ReservationStatus.RESERVED
            );

        } else if (i % 4 == 2) {

            reservation.setStatus(
                    ReservationStatus.PENDING
            );

        } else {

            reservation.setStatus(
                    ReservationStatus.CANCELLED
            );
        }

        reservations.add(reservation);

        Invoice invoice =
                new Invoice(
                        reservation,
                        random.nextBoolean()
                                ? PaymentMethod.CASH
                                : PaymentMethod.CREDIT_CARD
                );

        invoices.add(invoice);
    }

    Room room =
            new Room(
                    999,
                    single
            );

    Reservation reservation =
            new Reservation(
                    showcaseGuest,
                    room,
                    LocalDate.now(),
                    LocalDate.now()
                            .plusDays(14)
            );

    rooms.add(room);

    reservations.add(reservation);
}}