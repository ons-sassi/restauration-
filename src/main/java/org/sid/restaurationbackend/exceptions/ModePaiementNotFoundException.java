package org.sid.restaurationbackend.exceptions;

public class ModePaiementNotFoundException extends Exception {
    public ModePaiementNotFoundException(String modePaiementNotFound) {
        super(modePaiementNotFound);
    }
}
