package service;

import database.HotelDatabase;
import model.*;
import util.*;


public class AuthenticationService {


    public static  Object  login (String username , String password )
    {
        for(Guest g : HotelDatabase.guests)
        {
            if(
                g.getUsername()
                        .trim()
                        .toLowerCase()
                        .equals(
                                username.trim().toLowerCase()
                        )
         &&  password.equals(g.getPassword())) {
                return g;
            }
        }
        for( Staff s : HotelDatabase.staffMembers)
        {
            if(username.equals(s.getUsername()) &&  password.equals(s.getPassword())) {
                return s;
            }
        }
        throw new AuthenticationException("Invalid username or password");
    }

    public  static   void isUsernameUnique(String username)
    {
        if((HotelDatabase.findGuest(username))!=null)
        {
            throw new AlreadyExistsException("username already exists");
        }
        for(Staff s : HotelDatabase.staffMembers)
        {
            if(username.equals(s.getUsername())){
                throw new AlreadyExistsException("username already exists");
            }
        }
    }
}
