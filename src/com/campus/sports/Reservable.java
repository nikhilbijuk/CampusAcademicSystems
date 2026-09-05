package com.campus.sports;

public interface Reservable {
    boolean checkAvailability(String slot);
    boolean reserve(String slot);
    void release(String slot);
}
