package org.sid.restaurationbackend.exceptions;

public class FonctionnaliteNotFoundException extends Exception {
    public FonctionnaliteNotFoundException(String fonctionnaliteNotFound) {
        super(fonctionnaliteNotFound);
    }
}
