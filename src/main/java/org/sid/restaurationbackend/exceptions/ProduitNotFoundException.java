package org.sid.restaurationbackend.exceptions;

public class ProduitNotFoundException extends Exception {
    public ProduitNotFoundException(String produitNotFound) {
        super(produitNotFound);
    }
}
