package model;
import java.util.*;
import java.time.*;
import database.*;

public class Amenity {
    //defining all data fields with private access modifiers
    private String name;
    private AmenityType type;
    private double price;

    public Amenity(){}
    public Amenity(String name,AmenityType Type, double price){
        this.name = name;
        this.type= Type;
        this.price = price;
    }
    //data methods getters
    public String getName(){
        return name;
    }
    public AmenityType getType(){
        return type;
    }
    public double getPrice(){return price;}
    //data methods setters
    public void setName(String name){
        this.name = name;
    }
    public void setType(AmenityType Type){
        this.type = Type;
    }
    public void setPrice(double Price){this.price = Price;}

}