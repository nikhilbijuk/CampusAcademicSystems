package com.campus.sports;

public class Faculty extends User {
    private static final long serialVersionUID = 1L;
    public Faculty(String userId, String name) { super(userId, name); }
    @Override
    public int getBookingLimit() { return 5; }
}
