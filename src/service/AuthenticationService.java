package service;

import database.HotelDatabase;
import model.*;


public class AuthenticationService {


    public static  Object  login (String username , String password )
    {
        for(Guest g : HotelDatabase.guests)
        {
            if(username.equals(g.getUsername()) &&  password.equals(g.getPassword())) {
                return g;
            }
        }
        for( Staff s : HotelDatabase.staffMembers)
        {
            if(username.equals(s.getUsername()) &&  password.equals(s.getPassword())) {
                return s;
            }
        }
        return null;
    }

    public  static   void isUsernameUnique(String username) throws IllegalArgumentException
    {
        if((HotelDatabase.findGuest(username))!=null)
        {
            throw new IllegalArgumentException("username already exists");
        }
        for(Staff s : HotelDatabase.staffMembers)
        {
            if(username.equals(s.getUsername())){
                throw new IllegalArgumentException("username already exists");
            }
        }
    }
}
