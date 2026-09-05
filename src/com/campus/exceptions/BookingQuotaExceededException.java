package com.campus.exceptions;

public class BookingQuotaExceededException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookingQuotaExceededException(String userName, int limit, int currentCount) {
        super("Quota exceeded: " + userName + " has reached max booking limit (" + currentCount + "/" + limit + ").");
    }
}
