package org.sid.restaurationbackend.exceptions;

public class ReservationNotFoundException extends Exception {
    public ReservationNotFoundException(String reservationNotFound) {
        super(reservationNotFound);
    }
}
