package com.campus.exceptions;

/**
 * Checked exception thrown when an invalid internal assessment mark component
 * is entered (e.g., negative score or exceeding maximum allowable ceiling).
 */
public class InvalidMarkException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidMarkException(String message) {
        super(message);
    }
}
