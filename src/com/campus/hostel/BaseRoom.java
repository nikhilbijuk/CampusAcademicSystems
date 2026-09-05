package com.campus.hostel;

import java.io.Serializable;

public abstract class BaseRoom implements Serializable {
    private static final long serialVersionUID = 1L;
    private int roomNumber;
    private double baseRate;

    public BaseRoom(int roomNumber, double baseRate) {
        this.roomNumber = roomNumber;
        this.baseRate = baseRate;
    }

    public int getRoomNumber() { return roomNumber; }
    public double getBaseRate() { return baseRate; }
    public abstract double calculateMonthlyTariff();
}
