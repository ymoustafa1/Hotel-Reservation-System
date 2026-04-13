import java.util.*
import java.time.LocalDate;

public class Room {
    private int roomId;
    private RoomType roomType;
    private ArrayList<Amenity> amenities;
    private double price;

    public Room(int roomId, RoomType roomType, double price) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.price = price;
        this.amenities = new ArrayList<>();
    }

    public void addAmenity(Amenity a) {
        if (!amenities.contains(a)) {
            amenities.add(a);
        }
    }

    public void removeAmenity(Amenity a) {
        amenities.remove(a);
    }

    public double getPrice() {
        return price;
    }

    public int getRoomId() {
        return roomId;
    }

    public boolean isAvailable(LocalDate start, LocalDate end) {
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getRoom().getRoomId() == this.roomId) {
                if (!(end.isBefore(r.getCheckInDate()) || start.isAfter(r.getCheckOutDate()))) {
                    return false;
                }
            }
        }
        return true;
    }
}