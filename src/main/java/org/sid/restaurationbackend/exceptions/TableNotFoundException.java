package org.sid.restaurationbackend.exceptions;

public class TableNotFoundException extends Exception {
    public TableNotFoundException(String message) {
        super(message);
    }
}
