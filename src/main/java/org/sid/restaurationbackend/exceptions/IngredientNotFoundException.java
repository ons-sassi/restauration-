package org.sid.restaurationbackend.exceptions;

public class IngredientNotFoundException extends Exception {
    public IngredientNotFoundException(String ingredientNotFound) {
        super(ingredientNotFound);
    }
}
