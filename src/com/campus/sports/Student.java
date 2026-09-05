package com.campus.sports;

public class Student extends User {
    private static final long serialVersionUID = 1L;
    public Student(String userId, String name) { super(userId, name); }
    @Override
    public int getBookingLimit() { return 2; }
}
