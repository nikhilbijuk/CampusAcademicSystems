package com.campus.hostel;

public class ACSuite extends BaseRoom {
    private static final long serialVersionUID = 1L;
    private double acTax = 1500.0;
    public ACSuite(int roomNumber, double baseRate) { super(roomNumber, baseRate); }
    @Override
    public double calculateMonthlyTariff() { return getBaseRate() + acTax; }
}
