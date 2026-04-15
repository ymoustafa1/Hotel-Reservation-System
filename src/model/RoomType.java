package model;

public class RoomType{
    private String name;
    private double basePrice;

    public RoomType(){
    }
    public RoomType(String name, double basePrice)throws Exception {
        if (name == null || name.isEmpty()){
            throw new Exception ("Room type can't be empty");
        }
        if (basePrice<0){
            throw new Exception ("base price can't be negative");
        }
        this.name = name;
        this.basePrice = basePrice;
    }
    public double getBasePrice(){
        return basePrice;
    }
    public String getName(){
        return name;
    }
    public void setBasePrice(double basePrice){
        this.basePrice = basePrice ;
    }
    public void setName(String name){
        this.name= name ;
    }

}