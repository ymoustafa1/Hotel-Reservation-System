package main;
import database.*;
import util.*;
import model.*;
import service.*;
import java.util.*;
import java.time.*;

public class Main {
    public static void main(String[] args) {
        HotelDatabase.initializeDummyData();
        Welcome();
    }

    static void Welcome(){
        Scanner input = new Scanner(System.in);
        int choice;
        while (true) {
            try {
                System.out.println("Initializing Data...\n 1-Login\n 2-Register\n 3-Exit");
                String line = input.nextLine();
                if (line == null || line.trim().isEmpty()) {
                    throw new InvalidInputException("Input cannot be empty");
                }
                choice = Integer.parseInt(line);
                if (choice < 1 || choice > 3) {
                    throw new InvalidInputException("Invalid choice. Enter 1, 2, or 3.");
                }
                if (choice == 1) {
                    Login();
                } else if (choice == 2) {
                    Register();
                } else if (choice == 3) {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-3).");
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    static LocalDate readDate(Scanner input) {
        while (true) {
            try {
                String line = input.nextLine().trim();
                return LocalDate.parse(line);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd:");
            }
        }
    }
    static void Register() {
        Scanner input = new Scanner(System.in);
        //input username
        String uname;
        while (true) {
            try {
                System.out.println("Enter your username:");
                uname = input.nextLine();
                if (uname == null || uname.isEmpty()) {
                    throw new InvalidInputException("Invalid username");
                }
                AuthenticationService.isUsernameUnique(uname);
                break;
            } catch (InvalidInputException | AlreadyExistsException e) {
                System.out.println(e.getMessage());
            }
        }
        //input password
        String pass;
        while (true) {
            try {
                System.out.println("Enter your password:");
                pass = input.nextLine();

                if (pass == null || pass.isEmpty()) {
                    throw new InvalidInputException("Invalid password");
                }

                if (pass.length() < 6) {
                    throw new AuthenticationException("Very short Password");
                }

                break;

            } catch (InvalidInputException | AuthenticationException e) {
                System.out.println(e.getMessage());
            }
        }
        //input balance
        double balance;
        while (true) {
            try {
                System.out.println("Enter Balance:");
                balance = Double.parseDouble(input.nextLine());

                if (balance < 0) {
                    throw new NegativeNumberException("Balance cannot be negative");
                }

                break;

            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            } catch (NegativeNumberException e) {
                System.out.println(e.getMessage());
            }
        }
        //input Gender
        Gender gender;
        while (true) {
            try {
                System.out.println("Enter your gender:");
                gender = Gender.valueOf(input.nextLine().toUpperCase());
                break;

            } catch (IllegalArgumentException e) {
                System.out.println("Invalid gender.");
            }
        }
        //input date of birth
        LocalDate dob;
        while (true) {
            try {
                System.out.println("Enter your date of birth (yyyy-mm-dd):");
                dob = readDate(input);
                if (dob.isAfter(LocalDate.now())) {
                    throw new InvalidInputException("Date of birth cannot be in the future");
                }

                break;

            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());

            } catch (Exception e) {
                System.out.println("Invalid date format. Use yyyy-mm-dd.");
            }
        }
        input.nextLine();
        //input address
        String address;
        while (true) {
            try {
                System.out.println("Enter your address:");
                address = input.nextLine().trim();

                if (address.isEmpty()) {
                    throw new InvalidInputException("Address cannot be empty");
                }

                break;

            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
        //generate guest and add to database
        try {
            Guest guest = new Guest(uname, pass, dob, balance, address, gender);
            guest.register();
            System.out.println(guest.getUsername() + " has been successfully registered.");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
    static void Login() {
        Scanner input = new Scanner(System.in);
        String uname;
        String pass;

        while (true) {
            try {
                System.out.println("Enter your username:");
                uname = input.nextLine();
                if (uname == null || uname.isEmpty()) {
                    throw new InvalidInputException("Invalid username");
                }
                System.out.println("Enter your password:");
                pass = input.nextLine();

                if (pass == null || pass.isEmpty()) {
                    throw new InvalidInputException("Invalid password");
                }
                Object user = AuthenticationService.login(uname, pass);
                if (user instanceof Guest)
                    guestMenu((Guest) user);
                else {
                    Staff s = (Staff) user;
                    if (s.getRole() == Role.ADMIN)
                        adminMenu((Admin) s);
                    else
                        receptionistMenu((Receptionist) s);
                }
                break;
            } catch (InvalidInputException | AuthenticationException e) {
                System.out.println(e.getMessage());
            }

        }

    }
    static  void guestMenu(Guest guest) {
        if (guest == null) {throw new InvalidInputException("Invalid guest");}
        Scanner input = new Scanner(System.in);
        System.out.println("Welcome " + guest.getUsername());
        upper:
        while (true) {
        System.out.println(" 1- View available rooms\n 2- Make Reservation\n 3- View my reservation\n 4- View balance\n 5-Log out");
        int choice = input.nextInt();
        if (choice<1 || choice > 5) {throw new InvalidInputException("Invalid choice. Enter 1-5.");}
            outer:
        while (true) {
            try {
                if (choice == 1) {
                    System.out.println("Enter Start date (yyyy-mm-dd):");
                    LocalDate start = readDate(input);
                    if (start==null || start.isBefore(LocalDate.now())) {throw new InvalidInputException("Invalid start date");}
                    System.out.println("Enter End date (yyyy-mm-dd):");
                    LocalDate end = readDate(input);
                    if (end==null || end.isBefore(LocalDate.now())) {throw new InvalidInputException("Invalid end date");}
                    System.out.println(guest.viewAvailableRooms(start, end));
                    break;

                }
                if (choice == 2) {
                    System.out.println("Enter Start date (yyyy-mm-dd):");
                    LocalDate start2 = readDate(input);
                    System.out.println("Enter End date (yyyy-mm-dd):");
                    LocalDate end2 = readDate(input);
                    if (!HotelDatabase.validateDates(start2, end2) || start2 == null || end2 == null) {
                        throw new InvalidInputException("Invalid dates.");
                    }
                    System.out.println("Enter room ID");
                    int roomId = input.nextInt();
                    if (roomId <= 0) {
                        throw new NegativeNumberException("Invalid room ID");
                    }

                    Room r = HotelDatabase.findRoomById(roomId);
                    if (r.isAvailable(start2, end2)) {
                        Reservation res = guest.makeReservation(r, start2, end2);

                        while (true) {
                            try {
                                System.out.println("Add extra amenities? (1 = Yes, 0 = No)");
                                int choice1 = Integer.parseInt(input.next().trim());
                                PaymentMethod p = null;
                                if (choice1 == 0) {
                                    System.out.println("\n\n\n1- CheckOut\n2- Cancel Reservation");
                                    int choice2 = input.nextInt();
                                    if (choice2 == 1) {
                                        System.out.println("Choose Payment Method\n 1-Cash\n 2-Credit_Card");
                                        int choice3 = input.nextInt();
                                        if (choice3 == 1) {
                                            p = PaymentMethod.CASH;
                                        } else if (choice3 == 2) {
                                            p = PaymentMethod.CREDIT_CARD;
                                        }
                                        if (p == null || res == null) {
                                            throw new InvalidInputException("Invalid PaymentMethod");
                                        }
                                        Invoice i = new Invoice(res, p);
                                        res.setInvoice(i);
                                        System.out.println(i.toString());
                                        guest.checkout(res, p);
                                        System.out.println("Checked Out Successfully");
                                        System.out.println(res.getReservationId());
                                        break outer;
                                    } else if (choice2 == 2) {
                                        guest.cancelReservation(res);
                                        break outer;
                                    }

                                } else if (choice1 == 1) {

                                    System.out.println("Enter amenity name:");
                                    String name = input.next().trim();

                                    Amenity amenity = HotelDatabase.findAmenity(name);

                                    if (amenity == null) {
                                        throw new NotFoundException("Amenity not found.");
                                    }

                                    if (r.getAmenities().contains(amenity) || res.getExtraAmenities().contains(amenity)) {
                                        throw new AlreadyExistsException("Amenity already added.");
                                    }

                                    res.addExtraAmenity(amenity);

                                    System.out.println("Added (will be included in invoice).");
                                } else {
                                    throw new InvalidInputException("Invalid choice.");
                                }
                            }catch (AlreadyExistsException | AuthenticationException | InsufficientBalanceException
                                    | InvalidInputException | NegativeNumberException
                                    | NotFoundException | ReservationOrderException | RoomRelatedException e)
                            {
                                System.out.println(e.getMessage());
                            }
                            catch (RuntimeException e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                        else {throw new NotFoundException("Room not available.");}

                }

                if (choice == 3) {
                    System.out.println(guest.viewMyReservations());
                    System.out.println("\n\n -----------------------\n");
                    System.out.println("Do you want to cancel a reservation? (1 = Yes, 0 = No)");
                    int choice4 = input.nextInt();

                    if (choice4 == 1)
                    {
                        // get all reservations of this guest
                        ArrayList<Reservation> list = HotelDatabase.findReservationsByGuest(guest);

                        if (list.isEmpty())
                        {
                            throw new NotFoundException("No reservations found.");
                        }

                        System.out.println("Enter Reservation ID to cancel:");
                        int id = input.nextInt();

                        Reservation res = HotelDatabase.findReservationById(id);

                        if (res == null)
                        {
                            throw new InvalidInputException("Invalid reservation ID.");
                        }

                            guest.cancelReservation(res);

                            System.out.println("Reservation cancelled.");

                    }
                    else if (choice4 == 0) {break;}
                }
                else if (choice == 4) {
                    System.out.println("Your balance: " + guest.getBalance());
                    System.out.println("Do you need to add extra balance? (1 = Yes, 0 = No)");
                    int choice5 = input.nextInt();
                    if (choice5 == 1)
                    {
                        System.out.println("Enter amount to add:");
                        int amount = input.nextInt();
                        if (amount <= 0)
                        {
                            throw new NegativeNumberException("Invalid amount.");
                        }
                        guest.setBalance(guest.getBalance() + amount);
                    }
                    else if (choice5 == 0) {break;}
                }
                else if (choice == 5) {break upper;}
            } catch (AlreadyExistsException | AuthenticationException | InsufficientBalanceException
                     | InvalidInputException | NegativeNumberException
                     | NotFoundException | ReservationOrderException | RoomRelatedException | NumberFormatException e)
            {
                System.out.println(e.getMessage());
            }
            catch (RuntimeException e){
                System.out.println(e.getMessage());
            }
        }}
    }
    static void adminMenu(Admin admin) {
        if (admin == null) throw new InvalidInputException("Invalid admin");

        Scanner input = new Scanner(System.in);
        System.out.println("Welcome Admin " + admin.getUsername());

        while (true)
        {
            System.out.println(
                    "1- Rooms (View / Manage Amenities)\n" +
                            "2- Add Room\n" +
                            "3- Remove Room\n" +
                            "4- Edit Room\n" +
                            "5- Add Receptionist\n" +
                            "6- View/Edit Amenities & RoomTypes\n" +
                            "7- Remove Staff\n" +
                            "8- Set Working Hours\n" +
                            "9- View Reservations\n" +
                            "10- View Invoices\n" +
                            "11- View Guests\n" +
                            "12- Logout"
            );

            try
            {
                String line = input.nextLine().trim();
                if (line.isEmpty()) throw new InvalidInputException("Input cannot be empty.");

                int choice = Integer.parseInt(line);
                if (choice < 1 || choice > 12) throw new InvalidInputException("Invalid choice.");

                if (choice == 1)
                {
                    if (HotelDatabase.rooms.isEmpty())
                        throw new NotFoundException("No rooms available.");

                    System.out.println(HotelDatabase.rooms);
                }

                else if (choice == 2)
                {
                    System.out.println("Enter Room ID:");
                    int id = Integer.parseInt(input.nextLine().trim());

                    System.out.println("Enter Room Type:");
                    String typeName = input.nextLine().trim();
                    if (typeName.isEmpty()) throw new InvalidInputException("Room type cannot be empty.");

                    RoomType type = HotelDatabase.findRoomType(typeName);
                    if (type == null) throw new NotFoundException("Room type not found.");

                    Room r = new Room(id, type);
                    admin.addRoom(r);
                    System.out.println("Room added.");
                    System.out.println(r);
                }

                else if (choice == 3)
                {
                    System.out.println("Enter Room ID:");
                    int id = Integer.parseInt(input.nextLine().trim());

                    admin.removeRoom(id);
                    System.out.println("Room removed.");
                }

                else if (choice == 4)
                {
                    if (HotelDatabase.rooms.isEmpty())
                        throw new NotFoundException("No rooms available.");

                    System.out.println(HotelDatabase.rooms);
                    System.out.println("Enter Room ID to edit:");
                    int roomId = Integer.parseInt(input.nextLine().trim());

                    Room room = HotelDatabase.findRoomById(roomId);
                    if (room == null) throw new NotFoundException("Room not found.");

                    System.out.println("Current info:\n" + room);

                    while (true)
                    {
                        System.out.println(
                                "4.1- Change Room Type\n" +
                                        "4.2- Change Room Price\n" +
                                        "4.3- Add Amenity\n" +
                                        "4.4- Remove Amenity\n" +
                                        "0- Back"
                        );

                        try
                        {
                            String sub = input.nextLine().trim();
                            if (sub.isEmpty()) throw new InvalidInputException("Input cannot be empty.");
                            if (sub.equals("0")) break;

                            if (sub.equals("4.1"))
                            {
                                System.out.println("Available room types: " + HotelDatabase.roomTypes);
                                System.out.println("Enter new Room Type:");
                                String typeName = input.nextLine().trim();
                                if (typeName.isEmpty()) throw new InvalidInputException("Room type cannot be empty.");

                                RoomType newType = HotelDatabase.findRoomType(typeName);
                                if (newType == null) throw new NotFoundException("Room type not found.");

                                room.setRoomType(newType);
                                room.syncAmenitiesFromRoomType();
                                room.setPrice(newType.getBasePrice());
                                System.out.println("Room type updated and amenities synced.");
                            }

                            else if (sub.equals("4.2"))
                            {
                                System.out.println("Current price: " + room.getPrice());
                                System.out.println("Enter new price:");
                                double newPrice = Double.parseDouble(input.nextLine().trim());
                                if (newPrice < 0) throw new NegativeNumberException("Price cannot be negative.");

                                room.setPrice(newPrice);
                                System.out.println("Price updated.");
                            }

                            else if (sub.equals("4.3"))
                            {
                                System.out.println("Available amenities: " + HotelDatabase.amenities);
                                System.out.println("Enter Amenity Name:");
                                String aName = input.nextLine().trim();
                                if (aName.isEmpty()) throw new InvalidInputException("Amenity name cannot be empty.");

                                Amenity a = HotelDatabase.findAmenity(aName);
                                if (a == null) throw new NotFoundException("Amenity not found.");

                                if (room.getAmenities().contains(a))
                                    throw new AlreadyExistsException("Amenity already exists in this room.");

                                room.addAmenity(a);
                                System.out.println("Amenity added.");
                            }

                            else if (sub.equals("4.4"))
                            {
                                if (room.getAmenities().isEmpty())
                                    throw new NotFoundException("This room has no amenities to remove.");

                                System.out.println("Current amenities: " + room.getAmenities());
                                System.out.println("Enter Amenity Name:");
                                String aName = input.nextLine().trim();
                                if (aName.isEmpty()) throw new InvalidInputException("Amenity name cannot be empty.");

                                Amenity a = HotelDatabase.findAmenity(aName);
                                if (a == null) throw new NotFoundException("Amenity not found.");

                                if (!room.getAmenities().contains(a))
                                    throw new NotFoundException("Amenity not found in this room.");

                                room.removeAmenity(a);
                                System.out.println("Amenity removed.");
                            }

                            else
                            {
                                throw new InvalidInputException("Invalid option. Enter 4.1, 4.2, 4.3, 4.4, or 0.");
                            }

                            System.out.println("Updated room info:\n" + room);
                        }
                        catch (AlreadyExistsException | InvalidInputException | NegativeNumberException
                               | NotFoundException | RoomRelatedException e)
                        {
                            System.out.println(e.getMessage());
                        }
                        catch (NumberFormatException e)
                        {
                            System.out.println("Please enter a valid number.");
                        }
                    }
                }

                else if (choice == 5)
                {
                    System.out.println("Enter Username:");
                    String u = input.nextLine().trim();
                    if (u.isEmpty()) throw new InvalidInputException("Username cannot be empty.");

                    AuthenticationService.isUsernameUnique(u);

                    System.out.println("Enter Password:");
                    String p = input.nextLine().trim();
                    if (p.isEmpty()) throw new InvalidInputException("Password cannot be empty.");
                    if (p.length() < 6) throw new InvalidInputException("Password too short. Minimum 6 characters.");

                    Receptionist rec = new Receptionist(u, p);
                    HotelDatabase.staffMembers.add(rec);
                    System.out.println("Receptionist added.");
                }

                else if (choice == 6)
                {
                    while (true)
                    {
                        System.out.println(
                                "1- View/Edit Amenities\n" +
                                        "2- View/Edit RoomTypes\n" +
                                        "0- Back"
                        );

                        try
                        {
                            String sub = input.nextLine().trim();
                            if (sub.isEmpty()) throw new InvalidInputException("Input cannot be empty.");
                            if (sub.equals("0")) break;

                            if (sub.equals("1"))
                            {
                                if (HotelDatabase.amenities.isEmpty())
                                    throw new NotFoundException("No amenities.");

                                System.out.println(HotelDatabase.amenities);
                                System.out.println("1- Add  2- Remove  3- Change Price  0- Back");
                                String op = input.nextLine().trim();

                                if (op.equals("0")) { }
                                else if (op.equals("1"))
                                {
                                    System.out.println("Enter Amenity Name:");
                                    String name = input.nextLine().trim();
                                    if (name.isEmpty()) throw new InvalidInputException("Name cannot be empty.");

                                    System.out.println("Enter Amenity Type:");
                                    String typeStr = input.nextLine().trim();
                                    if (typeStr.isEmpty()) throw new InvalidInputException("Type cannot be empty.");
                                    AmenityType type = AmenityType.valueOf(typeStr.toUpperCase());

                                    System.out.println("Enter Price:");
                                    double price = Double.parseDouble(input.nextLine().trim());
                                    if (price < 0) throw new NegativeNumberException("Price cannot be negative.");

                                    admin.addAmenity(new Amenity(name, type, price));
                                    System.out.println("Amenity added.");
                                }
                                else if (op.equals("2"))
                                {
                                    System.out.println("Enter Amenity Name:");
                                    String name = input.nextLine().trim();
                                    if (name.isEmpty()) throw new InvalidInputException("Name cannot be empty.");

                                    admin.deleteAmenity(name);
                                    System.out.println("Amenity removed.");
                                }
                                else if (op.equals("3"))
                                {
                                    System.out.println("Enter Amenity Name:");
                                    String name = input.nextLine().trim();
                                    if (name.isEmpty()) throw new InvalidInputException("Name cannot be empty.");

                                    Amenity a = HotelDatabase.findAmenity(name);
                                    if (a == null) throw new NotFoundException("Amenity not found.");

                                    System.out.println("Current price: " + a.getPrice());
                                    System.out.println("Enter new Price:");
                                    double newPrice = Double.parseDouble(input.nextLine().trim());
                                    if (newPrice < 0) throw new NegativeNumberException("Price cannot be negative.");

                                    a.setPrice(newPrice);
                                    System.out.println("Price updated.");
                                }
                                else throw new InvalidInputException("Invalid option.");
                            }

                            else if (sub.equals("2"))
                            {
                                if (HotelDatabase.roomTypes.isEmpty())
                                    throw new NotFoundException("No room types.");

                                System.out.println(HotelDatabase.roomTypes);
                                System.out.println("1- Add  2- Remove  3- Edit  0- Back");
                                String op = input.nextLine().trim();

                                if (op.equals("0")) { }
                                else if (op.equals("1"))
                                {
                                    System.out.println("Enter RoomType Name:");
                                    String name = input.nextLine().trim();
                                    if (name.isEmpty()) throw new InvalidInputException("Name cannot be empty.");

                                    System.out.println("Enter Base Price:");
                                    double price = Double.parseDouble(input.nextLine().trim());
                                    if (price < 0) throw new NegativeNumberException("Price cannot be negative.");

                                    RoomType rt = new RoomType(name, price);

                                    while (true)
                                    {
                                        System.out.println("Add amenity to type? (1=Yes, 0=Done)");
                                        String c = input.nextLine().trim();
                                        if (c.equals("0")) break;
                                        if (!c.equals("1")) throw new InvalidInputException("Enter 1 or 0.");

                                        System.out.println("Enter Amenity Name:");
                                        String aName = input.nextLine().trim();
                                        if (aName.isEmpty()) throw new InvalidInputException("Name cannot be empty.");

                                        Amenity a = HotelDatabase.findAmenity(aName);
                                        if (a == null) throw new NotFoundException("Amenity not found.");

                                        rt.addAmenity(a);
                                        System.out.println("Amenity added to type.");
                                    }

                                    admin.addRoomType(rt);
                                    System.out.println("RoomType added.");
                                }
                                else if (op.equals("2"))
                                {
                                    System.out.println("Enter RoomType Name:");
                                    String name = input.nextLine().trim();
                                    if (name.isEmpty()) throw new InvalidInputException("Name cannot be empty.");


                                    admin.deleteRoomType(name);
                                    System.out.println("RoomType removed.");
                                }
                                else if (op.equals("3"))
                                {
                                    System.out.println("Enter RoomType Name:");
                                    String name = input.nextLine().trim();
                                    if (name.isEmpty()) throw new InvalidInputException("Name cannot be empty.");

                                    RoomType rt = HotelDatabase.findRoomType(name);
                                    if (rt == null) throw new NotFoundException("RoomType not found.");

                                    System.out.println("1- Edit Name  2- Edit Price  3- Manage Amenities");
                                    String edit = input.nextLine().trim();

                                    if (edit.equals("1"))
                                    {
                                        System.out.println("Enter new Name:");
                                        String newName = input.nextLine().trim();
                                        if (newName.isEmpty()) throw new InvalidInputException("Name cannot be empty.");
                                        rt.setName(newName);
                                        System.out.println("Name updated.");
                                    }
                                    else if (edit.equals("2"))
                                    {
                                        System.out.println("Current base price: " + rt.getBasePrice());
                                        System.out.println("Enter new Price:");
                                        double newPrice = Double.parseDouble(input.nextLine().trim());
                                        if (newPrice < 0) throw new NegativeNumberException("Price cannot be negative.");
                                        rt.setBasePrice(newPrice);
                                        System.out.println("Base price updated.");
                                    }
                                    else if (edit.equals("3"))
                                    {
                                        while (true)
                                        {
                                            System.out.println("1- Add Amenity  2- Remove Amenity  0- Done");
                                            String c = input.nextLine().trim();
                                            if (c.equals("0")) break;
                                            if (!c.equals("1") && !c.equals("2"))
                                                throw new InvalidInputException("Enter 1, 2, or 0.");

                                            System.out.println("Enter Amenity Name:");
                                            String aName = input.nextLine().trim();
                                            if (aName.isEmpty()) throw new InvalidInputException("Name cannot be empty.");

                                            Amenity a = HotelDatabase.findAmenity(aName);
                                            if (a == null) throw new NotFoundException("Amenity not found.");

                                            if (c.equals("1")) rt.addAmenity(a);
                                            else rt.removeAmenity(a);
                                            System.out.println("Done.");
                                        }
                                    }
                                    else throw new InvalidInputException("Invalid edit option.");

                                    System.out.println("RoomType updated.");
                                }
                                else throw new InvalidInputException("Invalid option.");
                            }

                            else throw new InvalidInputException("Invalid option. Enter 1, 2, or 0.");
                        }
                        catch (AlreadyExistsException | InvalidInputException | NegativeNumberException
                               | NotFoundException | RoomRelatedException e)
                        {
                            System.out.println(e.getMessage());
                        }
                        catch (NumberFormatException e)
                        {
                            System.out.println("Please enter a valid number.");
                        }
                        catch (IllegalArgumentException e)
                        {
                            System.out.println("Invalid amenity type. Check available types.");
                        }
                    }
                }

                else if (choice == 7)
                {
                    System.out.println("Enter Staff Username:");
                    String name = input.nextLine().trim();
                    if (name.isEmpty()) throw new InvalidInputException("Username cannot be empty.");

                    Staff s = HotelDatabase.findStaff(name);
                    if (s == null) throw new NotFoundException("Staff not found.");

                    System.out.println("Enter Working Hours:");
                    int hrs = Integer.parseInt(input.nextLine().trim());
                    if (hrs <= 0) throw new NegativeNumberException("Working hours must be positive.");

                    admin.setStaffWorkingHours(s, hrs);
                    System.out.println("Working hours updated.");
                }

                else if (choice == 8)
                {
                    System.out.println("Enter Username:");
                    String name = input.nextLine().trim();
                    if (name.isEmpty()) throw new InvalidInputException("Username cannot be empty.");

                    Staff s = HotelDatabase.findStaff(name);
                    if (s == null) throw new NotFoundException("Staff not found.");

                    HotelDatabase.staffMembers.remove(s);
                    System.out.println("Staff removed.");
                }

                else if (choice == 9)
                {
                    if (HotelDatabase.reservations.isEmpty())
                        throw new NotFoundException("No reservations.");
                    System.out.println(HotelDatabase.reservations);
                }

                else if (choice == 10)
                {
                    if (HotelDatabase.invoices.isEmpty())
                        throw new NotFoundException("No invoices.");
                    System.out.println(HotelDatabase.invoices);
                }

                else if (choice == 11)
                {
                    if (HotelDatabase.guests.isEmpty())
                        throw new NotFoundException("No guests.");

                    System.out.println(HotelDatabase.guests);

                    System.out.println("Delete a guest? (1=Yes, 0=No)");
                    String delLine = input.nextLine().trim();
                    if (delLine.isEmpty()) throw new InvalidInputException("Input cannot be empty.");

                    int del = Integer.parseInt(delLine);
                    if (del != 0 && del != 1) throw new InvalidInputException("Enter 1 for Yes or 0 for No.");

                    if (del == 1)
                    {
                        System.out.println("Enter Guest Username:");
                        String name = input.nextLine().trim();
                        if (name.isEmpty()) throw new InvalidInputException("Username cannot be empty.");

                        Guest g = HotelDatabase.findGuest(name);
                        if (g == null) throw new NotFoundException("Guest not found.");

                        HotelDatabase.guests.remove(g);
                        System.out.println("Guest deleted.");
                    }
                }

                else if (choice == 12)
                {
                    System.out.println("Logging out...");
                    break;
                }
            }
            catch (AlreadyExistsException | AuthenticationException | InsufficientBalanceException
                   | InvalidInputException | NegativeNumberException
                   | NotFoundException | ReservationOrderException | RoomRelatedException e)
            {
                System.out.println(e.getMessage());
            }
            catch (NumberFormatException e)
            {
                System.out.println("Please enter a valid number.");
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Invalid value: " + e.getMessage());
            }
            catch (RuntimeException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
    static void receptionistMenu(Receptionist r) {
        if (r == null) throw new InvalidInputException("Invalid receptionist");

        Scanner input = new Scanner(System.in);
        System.out.println("Welcome Receptionist " + r.getUsername());

        while (true)
        {
            System.out.println(" 1- View Reservations\n 2- Find Guest\n 3- Walk-in Check In (Create Reservation)\n 4- Check Out\n 5- Logout");

            try
            {
                String line = input.nextLine();

                if (line == null || line.trim().isEmpty())
                    throw new InvalidInputException("Input cannot be empty.");

                int choice = Integer.parseInt(line.trim());

                if (choice < 1 || choice > 5)
                    throw new InvalidInputException("Invalid choice. Enter a number between 1 and 5.");

                if (choice == 1)
                {
                    ArrayList<Reservation> list = r.viewAllReservations();

                    if (list.isEmpty())
                        throw new NotFoundException("No reservations found.");

                    System.out.println(list);

                    System.out.println("\nAccept a pending reservation? (1=Yes, 0=No)");
                    String cLine = input.nextLine().trim();
                    int c = Integer.parseInt(cLine);

                    if (c != 0 && c != 1)
                        throw new InvalidInputException("Please enter 1 for Yes or 0 for No.");

                    if (c == 1)
                    {
                        System.out.println("Enter Reservation ID:");
                        int id = Integer.parseInt(input.nextLine().trim());

                        Reservation res = HotelDatabase.findReservationById(id);

                        if (res == null)
                            throw new NotFoundException("Reservation not found.");

                        if (res.getStatus() != ReservationStatus.PENDING)
                            throw new ReservationOrderException("Not a pending reservation.");

                        r.checkIn(res);
                        System.out.println("Reservation accepted (checked in).");
                    }
                }

                else if (choice == 2)
                {
                    System.out.println("Enter Guest Username:");
                    String name = input.nextLine().trim();

                    if (name.isEmpty())
                        throw new InvalidInputException("Username cannot be empty.");

                    Guest g = r.findGuest(name);

                    if (g == null)
                        throw new NotFoundException("Guest not found.");

                    System.out.println("Username: " + g.getUsername());
                    System.out.println("Balance: " + g.getBalance());
                    System.out.println("DOB: " + g.getDateOfBirth());
                    System.out.println("Address: " + g.getAddress());
                    System.out.println("Gender: " + g.getGender() + "\n");
                }

                else if (choice == 3)
                {
                    System.out.println("Enter Guest Username:");
                    String name = input.nextLine().trim();

                    if (name.isEmpty())
                        throw new InvalidInputException("Username cannot be empty.");

                    Guest g = r.findGuest(name);

                    if (g == null)
                        throw new NotFoundException("Guest not found.");

                    System.out.println("Enter Start date:");
                    LocalDate start = readDate(input);

                    System.out.println("Enter End date:");
                    LocalDate end = readDate(input);

                    if (!HotelDatabase.validateDates(start, end))
                        throw new InvalidInputException("Invalid dates. End date must be after start date.");

                    System.out.println("Enter Room ID:");
                    int roomId = Integer.parseInt(input.nextLine().trim());

                    Room room = HotelDatabase.findRoomById(roomId);

                    if (room == null)
                        throw new NotFoundException("Room not found.");

                    if (!room.isAvailable(start, end))
                        throw new RoomRelatedException("Room is not available for the selected dates.");

                    Reservation res = new Reservation(g, room, start, end);
                    HotelDatabase.reservations.add(res);

                    while (true)
                    {
                        System.out.println("Add extra amenities? (1 = Yes, 0 = No)");
                        String amenityChoiceLine = input.nextLine().trim();

                        if (amenityChoiceLine.isEmpty())
                        {
                            System.out.println("Input cannot be empty. Please enter 1 or 0.");
                            continue;
                        }

                        int choice1;
                        try
                        {
                            choice1 = Integer.parseInt(amenityChoiceLine);
                        }
                        catch (NumberFormatException e)
                        {
                            System.out.println("Invalid input. Please enter 1 or 0.");
                            continue;
                        }

                        if (choice1 == 0) break;

                        if (choice1 != 1)
                        {
                            System.out.println("Please enter 1 for Yes or 0 for No.");
                            continue;
                        }

                        System.out.println("Enter amenity name:");
                        String aName = input.nextLine().trim();

                        if (aName.isEmpty())
                        {
                            System.out.println("Amenity name cannot be empty.");
                            continue;
                        }

                        Amenity a = HotelDatabase.findAmenity(aName);

                        if (a == null)
                        {
                            System.out.println("Amenity not found.");
                            continue;
                        }

                        if (room.getAmenities().contains(a) || res.getExtraAmenities().contains(a))
                        {
                            System.out.println("Already included.");
                            continue;
                        }

                        res.addExtraAmenity(a);
                        System.out.println(a.getName() + " added.");
                    }

                    System.out.println("Choose Payment Method\n1-Cash\n2-Credit_Card");
                    String payLine = input.nextLine().trim();

                    if (payLine.isEmpty())
                        throw new InvalidInputException("Payment method cannot be empty.");

                    int pChoice = Integer.parseInt(payLine);

                    PaymentMethod method;
                    if (pChoice == 1)
                        method = PaymentMethod.CASH;
                    else if (pChoice == 2)
                        method = PaymentMethod.CREDIT_CARD;
                    else
                        throw new InvalidInputException("Invalid payment method. Enter 1 for Cash or 2 for Credit Card.");

                    Invoice inv = new Invoice(res, method);
                    res.setInvoice(inv);

                    inv.processPayment(g, method);

                    r.checkIn(res);

                    System.out.println(inv);
                    System.out.println("Walk-in reservation completed successfully.");
                    System.out.println("Reservation ID: " + res.getReservationId());
                }

                else if (choice == 4)
                {
                    System.out.println("Enter Reservation ID:");
                    int id = Integer.parseInt(input.nextLine().trim());

                    Reservation res = HotelDatabase.findReservationById(id);

                    if (res == null)
                        throw new NotFoundException("Reservation not found.");

                    r.checkOut(res);
                    System.out.println("Checked out successfully.");
                }

                else if (choice == 5)
                {
                    System.out.println("Logging out...");
                    break;
                }
            }
            catch (AlreadyExistsException | AuthenticationException | InsufficientBalanceException
                   | InvalidInputException | NegativeNumberException
                   | NotFoundException | ReservationOrderException | RoomRelatedException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
            catch (NumberFormatException e)
            {
                System.out.println("Error: Please enter a valid number.");
            }
            catch (InputMismatchException e)
            {
                System.out.println("Error: Unexpected input type.");
                input.nextLine();
            }
            catch (RuntimeException e)
            {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
    }
    }
