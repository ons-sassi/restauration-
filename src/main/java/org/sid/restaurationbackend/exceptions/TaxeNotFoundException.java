package org.sid.restaurationbackend.exceptions;

public class TaxeNotFoundException extends Exception {
    public TaxeNotFoundException(String taxeNotFound) {
        super(taxeNotFound);
    }
}
