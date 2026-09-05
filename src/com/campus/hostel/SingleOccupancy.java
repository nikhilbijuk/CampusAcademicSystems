package com.campus.hostel;

public class SingleOccupancy extends BaseRoom {
    private static final long serialVersionUID = 1L;
    public SingleOccupancy(int roomNumber, double baseRate) { super(roomNumber, baseRate); }
    @Override
    public double calculateMonthlyTariff() { return getBaseRate() * 1.2; }
}
