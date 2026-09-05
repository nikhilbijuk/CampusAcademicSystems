package com.campus.exceptions;

public class InvalidLeaveDaysException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidLeaveDaysException(String message) {
        super(message);
    }
}
