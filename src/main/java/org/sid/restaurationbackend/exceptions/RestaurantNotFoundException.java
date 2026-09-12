package org.sid.restaurationbackend.exceptions;

public class RestaurantNotFoundException extends Exception {
    public RestaurantNotFoundException(String message) {
        super(message);
    }
}
