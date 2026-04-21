package model;
import java.time.*;
import database.*;
import util.InvalidInputException;
import util.NegativeNumberException;

import java.util.*;

public class RoomType{
    //defining all data fields with private access modifiers
    private String name;
    private double basePrice;

    public RoomType(){
    }
    public RoomType(String name, double basePrice){
        //validating the user's input
        if (name == null || name.isEmpty()){
            throw new InvalidInputException("Room type can't be null");
        }
        //validation: base price must be greater than 0
        if (basePrice<0){
            throw new NegativeNumberException("base price can't be negative");
        }
        this.name = name;
        this.basePrice = basePrice;
    }
    //data methods getters
    public double getBasePrice(){
        return basePrice;
    }
    public String getName(){
        return name;
    }
    //data methods setters
    public void setBasePrice(double basePrice){
        if (basePrice < 0)
            throw new NegativeNumberException("base price can't be negative");
        this.basePrice = basePrice ;
    }
    public void setName(String name){
        if (name == null || name.isEmpty())
            throw new InvalidInputException("Room type can't be null");
        this.name= name ;
    }

}