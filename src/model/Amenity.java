package model;
import java.util.*;
import java.time.*;
import database.*;
import util.InvalidInputException;
import util.NegativeNumberException;

public class Amenity {
    //defining all data fields with private access modifiers
    private String name;
    private AmenityType type;
    private double price;

    public Amenity(){}
    public Amenity(String name, AmenityType type, double price){
        if (name == null || name.isEmpty())
            throw new InvalidInputException("Invalid name");

        if (type == null)
            throw new InvalidInputException("Type required");

        if (price < 0)
            throw new NegativeNumberException("Price cannot be negative");

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
    public void setName(String name){
        if (name == null || name.isEmpty())
            throw new InvalidInputException("Invalid name");
        this.name = name;
    }
    public void setType(AmenityType Type) {
        if (type == null)
            throw new InvalidInputException("Type required");
        this.type = Type;
    }
    public void setPrice(double Price) {
        if (price < 0)
            throw new NegativeNumberException("Price cannot be negative");
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

    @Override
    public String toString() {
        return name;
    }
}