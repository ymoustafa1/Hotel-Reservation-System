package util;
import model.*;
import java.util.*;

public interface Payable {
    public void processPayment(Guest g, PaymentMethod method);
    public double calculateTotal();
}
