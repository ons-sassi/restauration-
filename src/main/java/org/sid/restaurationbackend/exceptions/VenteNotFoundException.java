package org.sid.restaurationbackend.exceptions;

public class VenteNotFoundException extends Exception {
    public VenteNotFoundException(String venteNotFound) {
        super(venteNotFound);
    }
}
