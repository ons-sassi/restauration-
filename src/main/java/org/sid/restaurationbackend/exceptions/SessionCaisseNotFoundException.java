package org.sid.restaurationbackend.exceptions;

public class SessionCaisseNotFoundException extends Exception {
    public SessionCaisseNotFoundException(String sessionCaisseNotFound) {
        super(sessionCaisseNotFound);
    }
}
