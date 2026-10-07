package com.campus.exceptions;

public class InsufficientPlayersException extends Exception {
    private static final long serialVersionUID = 1L;

    public InsufficientPlayersException(String sport, String format, int required, int provided) {
        super(sport + " reservation rejected: Format '" + format + "' requires at least " 
                + required + " players. Provided: " + provided + ".");
    }

    public InsufficientPlayersException(String message) {
        super(message);
    }
}
