package model;

import database.HotelDatabase;
import util.*;

public class Admin extends Staff {
    public Admin(String name, String pass) {
        //adding a new staff member of role admin
        super(name, pass, Role.ADMIN);
    }


    public Staff findStaff(String n) {
        return HotelDatabase.findStaff(n);
    }

    //finding staff member by searching for his/her ID from all staff members
    public Staff findStaffByID(int ID){
        for (Staff s : HotelDatabase.staffMembers) {
            if (s.getStaffID() == ID)  //using == as these are of primitive datatype
                return s;
        }
        throw new NotFoundException("Staff member not found");
    }

    public void addRoom(Room r){
        //validating room
        if (r == null)
            throw new InvalidInputException("Room cannot be null");
        for (Room s : HotelDatabase.rooms) {
            if (s.getRoomId() == r.getRoomId())
                throw new AlreadyExistsException("Room ID already exists");
        }
        HotelDatabase.rooms.add(r);
    }

    //method for removing a room by removing any object of room that has a specific id
    //if it was by Room the method would remove object reference not the room itself
    public void removeRoom(int ID){
        for (int i = 0; i < HotelDatabase.rooms.size(); i++) {
            Room r = HotelDatabase.rooms.get(i);

            if (r.getRoomId() == ID) {
                for (Reservation res : HotelDatabase.reservations) {
                    if (res.getRoom().getRoomId() == ID)
                        throw new RoomRelatedException("Room has reservations");
                }

                HotelDatabase.rooms.remove(i);
                return;
            }
        }
        throw new NotFoundException("Room not found");
    }

    public void updateRoom(int ID, Room upd){
        //assigning the existing room to the method that returns the room by searching for its id(stored in database)
        Room existing = HotelDatabase.findRoomById(ID);
        if (existing == null)
            throw new InvalidInputException("Room not found");
        existing.setPrice(upd.getPrice());  //setting the new price of the existing room to the new room's price
        existing.setRoomType(upd.getRoomType());
    }

    public void addRoomType(RoomType rt){
        //validating room type
        if (rt == null)
            throw new InvalidInputException("Room type cant be null");
        for (RoomType r : HotelDatabase.roomTypes) {
            if (r.getName().equalsIgnoreCase(rt.getName()))
                throw new AlreadyExistsException("Room type already exists");
        }
        HotelDatabase.roomTypes.add(rt);
    }

    public void deleteRoomType(String name){
        for (int i = 0; i < HotelDatabase.roomTypes.size(); i++) {
            RoomType m = HotelDatabase.roomTypes.get(i);
            //comparing name entered by user with names of room types stored in database(case insensitive)
            if (m.getName().equalsIgnoreCase(name)) {
                HotelDatabase.roomTypes.remove(i);
                return;
            }
        }
        throw new NotFoundException("There is no room type that matches this name!");
    }

    public void addAmenity(Amenity a){
        //validating amenity
        if (a == null)
            throw new InvalidInputException("Amenity can't be null");
        for (Amenity existing : HotelDatabase.amenities) {
            if (a.getName().equalsIgnoreCase(existing.getName()))
                throw new AlreadyExistsException("Amenity already exists");
        }
        HotelDatabase.amenities.add(a);
    }

    public void deleteAmenity(String name){
        for (int i = 0; i < HotelDatabase.amenities.size(); i++) {
            Amenity a = HotelDatabase.amenities.get(i);
            //same logic as deleteRoomType method
            if (a.getName().equalsIgnoreCase(name)) {
                HotelDatabase.amenities.remove(i);
                return;
            }
        }
        throw new NotFoundException("There is no amenity that matches that name!");
    }

    //method to update working hrs for a staff member from staff class(only accessible by an admin)
    public void setStaffWorkingHours(Staff s, int hrs) {
        if (s == null)
            throw new InvalidInputException("Staff cannot be null");

        s.setWorkingHours(hrs);
    }
}
