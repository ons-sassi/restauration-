package org.sid.restaurationbackend.exceptions;

/**
 * Indique que les identifiants fournis pour une authentification sont invalides.
 * Le GlobalExceptionHandler convertit cette exception en HTTP 401.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
