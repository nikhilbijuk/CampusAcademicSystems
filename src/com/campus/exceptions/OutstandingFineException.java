package com.campus.exceptions;

public class OutstandingFineException extends Exception {
    private static final long serialVersionUID = 1L;

    public OutstandingFineException(String userName, double fineBalance) {
        super("Booking blocked: User '" + userName + "' has an unpaid fine of Rs. " + fineBalance + ". Clear fine before booking.");
    }
}
