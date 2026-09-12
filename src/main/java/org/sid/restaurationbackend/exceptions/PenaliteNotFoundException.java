package org.sid.restaurationbackend.exceptions;

public class PenaliteNotFoundException extends Exception {
    public PenaliteNotFoundException(String penaliteNotFound) {
        super(penaliteNotFound);
    }
}
