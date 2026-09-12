package org.sid.restaurationbackend.exceptions;

public class PresenceNotFoundException extends Exception {
    public PresenceNotFoundException(String presenceNotFound) {
        super(presenceNotFound);
    }
}
