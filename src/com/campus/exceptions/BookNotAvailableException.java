package com.campus.exceptions;

public class BookNotAvailableException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookNotAvailableException(String isbn, String title) {
        super("Book unavailable: '" + title + "' (ISBN: " + isbn + ") is already issued to another user.");
    }
}
