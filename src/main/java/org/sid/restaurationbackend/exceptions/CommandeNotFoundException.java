package org.sid.restaurationbackend.exceptions;

public class CommandeNotFoundException extends Exception {
    public CommandeNotFoundException(String message) {
        super(message);
    }
}
