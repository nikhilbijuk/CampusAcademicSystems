package com.campus.exceptions;

public class SlotAlreadyBookedException extends Exception {
    private static final long serialVersionUID = 1L;

    public SlotAlreadyBookedException(String slot) {
        super("Slot collision: Time slot '" + slot + "' is already reserved.");
    }
}
