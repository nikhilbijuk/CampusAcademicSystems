package com.campus.sports;

public class Coach extends User {
    private static final long serialVersionUID = 1L;
    public Coach(String userId, String name) { super(userId, name); }
    @Override
    public int getBookingLimit() { return 10; }
}
