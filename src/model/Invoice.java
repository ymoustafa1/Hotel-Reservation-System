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


    public Invoice(int invoiceId, Reservation reservation, PaymentMethod paymentMethod) {
        this.invoiceId = invoiceId;
        this.reservation = reservation;
        this.paymentMethod = paymentMethod;
        this.totalAmount = calculateTotal();
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

        double price= reservation.getRoom().getPrice();
        double total = price * days;
        this.totalAmount = total;
        return total;
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
}



