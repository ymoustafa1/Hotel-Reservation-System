package model;

public class RoomType{
    private String name;
    private double basePrice;

    public RoomType(){

    }

    public RoomType(String name, double basePrice)throws Exception {
        this.name = name;
        this.basePrice = basePrice;
        if (name == null || name.isEmpty()){
            

        }    }

    public double getBasePrice(){
        return basePrice;
    }

    public String getName(){
        return name;
    }
}