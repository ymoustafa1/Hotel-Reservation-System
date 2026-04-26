package model;

import java.time.*;
import database.*;
import java.util.*;
import util.*;


public class Invoice implements Payable{
    private int invoiceId;
    private Reservation reservation;
    private double totalAmount;
    private PaymentMethod paymentMethod;
    private LocalDate paymentDate;
    private static int count=1000;


    public Invoice(Reservation reservation, PaymentMethod paymentMethod) {
        this.invoiceId = count++;
        this.reservation = reservation;
        this.paymentMethod = paymentMethod;
        this.totalAmount = calculateTotal();
        this.paymentDate = LocalDate.now();
    }

    // Getters & Setters

    public int getInvoiceId() {
        return invoiceId;
    }
    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }
    public Reservation getReservation() {
        return reservation;
    }
    public void setReservation(Reservation reservation){
        this.reservation = reservation;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    // calculate total based on days reserved
    public double calculateTotal(){
        if (reservation == null) {
            throw new InvalidInputException("Reservation cannot be null");
        }

        LocalDate start = reservation.getCheckInDate();
        LocalDate end = reservation.getCheckOutDate();

        long days = end.toEpochDay() - start.toEpochDay();

        if (days <= 0) {
            throw new NegativeNumberException("Invalid reservation duration");
        }

        // base room price
        double base = reservation.getRoom().getPrice();

        // extras from reservation
        double extras = 0;
        for (Amenity a : reservation.getExtraAmenities())
        {
            extras += a.getPrice();
        }

        double total = (base + extras) * days;

        this.totalAmount = total;
        return total;
    }
    public double calculateExtrasPerDay()
    {
        double extras = 0;

        for (Amenity a : reservation.getExtraAmenities())
        {
            extras += a.getPrice();
        }

        return extras;
    }

    //change/accept payment from guest
    public void processPayment(Guest g, PaymentMethod method){

        if (reservation == null) {
            throw new InvalidInputException("Reservation cannot be null");
        }

        if (g == null) {
            throw new InvalidInputException("Guest cannot be null");
        }

        if (totalAmount == 0) {
            calculateTotal();
        }
        switch (paymentMethod) {


            case CASH:
                // Assume cash is always successful
                paymentDate = LocalDate.now();
                HotelDatabase.invoices.add(this);
                break;

            case CREDIT_CARD:
                if (g.getBalance() >= totalAmount) {
                    g.setBalance(g.getBalance() - totalAmount);
                    paymentDate = LocalDate.now();
                    HotelDatabase.invoices.add(this);
                } else {
                    throw new InsufficientBalanceException("Insufficient balance");
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid paymentMethod");
        }
    }
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("      INVOICE      \n");
        sb.append("Invoice ID: ").append(invoiceId).append("\n");
        sb.append("Guest: ").append(reservation.getGuest().getUsername()).append("\n");
        sb.append("Room ID: ").append(reservation.getRoom().getRoomId()).append("\n");
        sb.append("From: ").append(reservation.getCheckInDate())
                .append(" To: ").append(reservation.getCheckOutDate()).append("\n");

        long days = reservation.getCheckOutDate().toEpochDay()
                - reservation.getCheckInDate().toEpochDay();

        double base = reservation.getRoom().getPrice();
        double extrasPerDay = calculateExtrasPerDay();

        sb.append("\n    Pricing    \n");
        sb.append("Base: ").append(base)
                .append(" × ").append(days)
                .append(" = ").append(base * days).append("\n");

        if (!reservation.getExtraAmenities().isEmpty())
        {
            sb.append("Extras:\n");

            for (Amenity a : reservation.getExtraAmenities())
            {
                sb.append("- ").append(a.getName())
                        .append(" (+").append(a.getPrice()).append(" per day)\n");
            }

            sb.append("Extras total: ")
                    .append(extrasPerDay).append(" × ").append(days)
                    .append(" = ").append(extrasPerDay * days).append("\n");
        }

        // 🔥 ONLY ONE SOURCE OF TRUTH
        sb.append("\nTOTAL: ").append(calculateTotal()).append("\n");

        sb.append("\nPayment Method: ").append(paymentMethod);
        sb.append("\nPayment Date: ").append(paymentDate);

        sb.append("\n=====================");

        return sb.toString();
    }
}



