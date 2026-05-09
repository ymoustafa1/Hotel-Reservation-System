# Hotel-Reservation-System
# 🏨 Hotel Reservation System

## 📌 Overview

The **Hotel Reservation System** is a Java-based application designed to manage hotel bookings efficiently.
It allows users to browse rooms, make reservations, handle check-in and check-out processes, and generate invoices with integrated payment options.

This project demonstrates core concepts of **Object-Oriented Programming (OOP)**, **GUI development using JavaFX**, and basic **system design principles**.

---

## 🎯 Features

* 🛏️ Browse available rooms
* 📅 Book and manage reservations
* 🔑 Check-in and check-out system
* 🧾 Invoice generation
* 💳 Payment system:

  * Cash payments
  * Card payments with validation (16-digit numeric check)
* 📊 Reservation dashboard for tracking bookings

---

## 🛠️ Technologies Used

* **Java**
* **JavaFX**
* **IntelliJ IDEA**

---

## 🚀 How to Run the Project

1. Clone the repository:

   ```
   git clone https://github.com/ymoustafa1/Hotel-Reservation-System.git
   ```

2. Open the project in **IntelliJ IDEA**

3. Configure **JavaFX SDK** (if not already set up)

4. Run the main application class

---

## 📂 Project Structure

```
Hotel-Reservation-System/
│
├── src/                # Source code
│   ├── models/         # Data classes (Room, Booking, etc.)
│   ├── services/       # Business logic (Reservation, Payment, etc.)
│   ├── controllers/    # UI controllers
│   └── views/          # JavaFX UI files
│
├── dashboards/         # Application dashboards
├── resources/          # Assets (if any)
└── README.md
```

---

## 💳 Payment Validation

The system includes a simulated payment module:

* Card number must be **16 digits**
* Only **numeric input** is accepted
* Payment method is stored with each reservation

---

## 👥 Team Members

* Malek Elkordy 25P0249
* Youssef Mostafa 25P0326
* Ali Ismail 25P0218
* Kenzy Mohamed 25P0284
* Amira Ahmed 25P0319

---

## ⚠️ Notes

* Ensure JavaFX is properly configured before running
* Compiled files (`out/`, `.class`, `.iml`) are excluded using `.gitignore`
* This project is intended for educational purposes

---

## 📄 License

This project is developed for academic use only and is not intended for commercial deployment.
