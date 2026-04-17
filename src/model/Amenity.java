package model;
import java.util.*;
import java.time.*;
import database.*;

public class Amenity {
    //defining all data fields with private access modifiers
    private String name;
    private AmenityType Type;

    public Amenity(){
    }
    public Amenity(String name,AmenityType Type ){
        this.name = name;
        this.Type= Type;
    }
    //data methods getters
    public String getName(){
        return name;
    }
    public AmenityType getType(){
        return Type;
    }
    //data methods setters
    public void setName(String name){
        this.name = name;
    }
    public void setType(AmenityType Type){
        this.Type = Type;
    }

}