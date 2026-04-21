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
    public Amenity(String name, AmenityType type, double price) throws IllegalArgumentException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Invalid name");

        if (type == null)
            throw new IllegalArgumentException("Type required");

        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative");

        this.name = name;
        this.type = type;
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
    public void setName(String name) throws IllegalArgumentException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Invalid name");
        this.name = name;
    }
    public void setType(AmenityType Type) throws IllegalArgumentException {
        if (type == null)
            throw new IllegalArgumentException("Type required");
        this.type = Type;
    }
    public void setPrice(double Price) throws  IllegalArgumentException
    {
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.price = Price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Amenity)) return false;

        Amenity a = (Amenity) o;
        return this.name.equalsIgnoreCase(a.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

}