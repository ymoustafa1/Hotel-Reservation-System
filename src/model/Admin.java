package model;

import database.HotelDatabase;

import java.sql.SQLException;

public class Admin extends Staff
{
    public Admin(){}
    public Admin(String name,String pass)
    {
        super(name,pass,Role.ADMIN);        //adding a new staff member of role admin
    }
    public void addRoom(Room r) throws Exception
    {
        if(r==null)
            throw new IllegalArgumentException("Room cannot be null");
        //checking if new room id already exists before adding the new room (Validation)
        for(Room s:HotelDatabase.rooms)
        {
            if(s.getRoomId()==r.getRoomId())
                throw new IllegalArgumentException("Room ID already exists");
        }
        HotelDatabase.rooms.add(r);
    }
    //method for removing a room by removing any object of room that has a specific id
    //if it was by Room the method would remove object reference not the room itself
    public void removeRoom(int ID) throws Exception
    {
        for(Room r:HotelDatabase.rooms)
        {
            if(r.getRoomId()==ID)
            {
                //checking if room is reserved before removing it
                for (Reservation res : HotelDatabase.reservations) {
                    if (res.getRoom().getRoomId() == roomId)        //getRoom() is a method that gets the reserved room....to be done by amira
                        throw new IllegalArgumentException("Room has reservations");
                }
                HotelDatabase.rooms.remove(r);
            }
        }
        throw new IllegalArgumentException("Room not found");
    }

}
