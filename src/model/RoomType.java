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
    private ArrayList<Amenity> amenities;
    private String imagePath;


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
        this.amenities = new ArrayList<>();
    }
    //data methods getters
    public double getBasePrice(){
        return basePrice;
    }
    public String getName(){
        return name;
    }
    public ArrayList<Amenity> getAmenities() {return amenities;}

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
    public void setAmenities(ArrayList<Amenity> amenities){this.amenities = amenities;}

    //adds amenity to the room and prevents duplicates
    public void addAmenity(Amenity a) {
        if (!amenities.contains(a)) {
            amenities.add(a);
        }
    }
    //removes specified amenities from the room
    public void removeAmenity(Amenity a) {
        amenities.remove(a);
    }

    @Override
    public String toString(){
        return name;
    }
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}