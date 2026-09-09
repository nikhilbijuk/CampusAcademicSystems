package com.campus.exceptions;

public class BorrowingLimitExceededException extends Exception {
    private static final long serialVersionUID = 1L;

    public BorrowingLimitExceededException(String userName, int limit, int currentCount) {
        super("Library borrowing limit exceeded: " + userName + " already has " + currentCount + " book(s) on loan (Max allowed: " + limit + ").");
    }
}
