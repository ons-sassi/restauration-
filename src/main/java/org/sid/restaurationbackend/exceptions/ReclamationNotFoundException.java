package org.sid.restaurationbackend.exceptions;

public class ReclamationNotFoundException extends Exception {
    public ReclamationNotFoundException(String reclamationNotFound) {
        super(reclamationNotFound);
    }
}
