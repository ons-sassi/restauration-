package org.sid.restaurationbackend.exceptions;

public class ModificateurNotFoundException extends Exception {
    public ModificateurNotFoundException(String modificateurNotFound) {
        super(modificateurNotFound);
    }
}
