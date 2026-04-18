package model;
import java.time.*;
import database.*;
import java.util.*;

public class RoomType{
    //defining all data fields with private access modifiers
    private String name;
    private double basePrice;

    public RoomType(){
    }
    public RoomType(String name, double basePrice) {
        //validating the user's input
        if (name == null || name.isEmpty()){
            throw new IllegalArgumentException ("Room type can't be empty");
        }
        //validation: base price must be greater than 0
        if (basePrice<0){
            throw new IllegalArgumentException ("base price can't be negative");
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
        this.basePrice = basePrice ;
    }
    public void setName(String name){
        this.name= name ;
    }

}