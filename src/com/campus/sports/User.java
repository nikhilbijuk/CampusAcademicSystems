package com.campus.sports;

import java.io.Serializable;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String name;
    private double fineBalance;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.fineBalance = 0.0;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public double getFineBalance() { return fineBalance; }
    public void addFine(double amount) { this.fineBalance += amount; }
    public void payFine(double amount) {
        this.fineBalance = Math.max(0.0, this.fineBalance - amount);
    }
    public void clearFine() { this.fineBalance = 0.0; }
    
    public abstract int getBookingLimit(); 

    public String getRole() {
        return getClass().getSimpleName();
    }
}
