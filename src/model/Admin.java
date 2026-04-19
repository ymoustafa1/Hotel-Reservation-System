package model;

import database.HotelDatabase;

import java.sql.SQLException;
import java.util.*;
import java.util.ArrayList;

public class Admin extends Staff
{
    public Admin(){}
    public Admin(String name,String pass)
    {
        //adding a new staff member of role admin
        super(name,pass,Role.ADMIN);
    }

    //overriding method in staff class to view all rooms
    @Override
    public ArrayList<Room> viewAllRooms()
    {
        return super.viewAllRooms();
    }

    public Staff findStaff(String n)
    {
        return HotelDatabase.findStaff(n);
    }

    //finding staff member by searching for his/her ID from all staff members
    public Staff findStaffByID(int ID)
    {
        for(Staff s:HotelDatabase.staffMembers)
        {
            if(s.getStaffID()==ID)  //using == as these are of primitive datatype
                return s;
        }
        throw new IllegalArgumentException("Staff member not found");
    }

    public void addRoom(Room r)
    {
        if(r==null)
            throw new IllegalArgumentException("Room cannot be null");
        //validating room id
        for(Room s:HotelDatabase.rooms)
        {
            if(s.getRoomId()==r.getRoomId())
                throw new IllegalArgumentException("Room ID already exists");
        }
        HotelDatabase.rooms.add(r);
    }
    //method for removing a room by removing any object of room that has a specific id
    //if it was by Room the method would remove object reference not the room itself
    public void removeRoom(int ID)
    {
        for(Room r:HotelDatabase.rooms)
        {
            if(r.getRoomId()==ID)
            {
                //checking if room is reserved before removing it
                for (Reservation res : HotelDatabase.reservations) {
                    if (res.getRoom().getRoomId() == r.getRoomId())        //getRoom() is a method that gets the reserved room....to be done by amira
                        throw new IllegalArgumentException("Room has reservations");
                }
                HotelDatabase.rooms.remove(r);
            }
        }
        throw new IllegalArgumentException("Room not found");
    }

    public void updateRoom(int ID,Room upd)
    {
        //assigning the existing room to the method that returns the room by searching for its id(stored in database)
        Room existing=HotelDatabase.findRoomById(ID);
        existing.setPrice(upd.getPrice());  //setting the new price of the existing room to the new room's price
        existing.setRoomType(upd.getRoomType());    //...
    }
    
}
