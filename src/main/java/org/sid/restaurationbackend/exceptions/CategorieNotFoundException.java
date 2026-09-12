package org.sid.restaurationbackend.exceptions;

public class CategorieNotFoundException extends Exception {
    public CategorieNotFoundException(String categorieNotFound) {
        super(categorieNotFound);
    }
}
