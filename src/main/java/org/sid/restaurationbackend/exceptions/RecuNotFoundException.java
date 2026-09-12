package org.sid.restaurationbackend.exceptions;

public class RecuNotFoundException extends Exception {
    public RecuNotFoundException(String recuNotFound) {
        super(recuNotFound);
    }
}
