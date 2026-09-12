package org.sid.restaurationbackend.exceptions;

/**
 * Un compte client existe déjà avec cet email.
 * Le GlobalExceptionHandler convertit cette exception en HTTP 409
 * (règle générique : tout "XxxEnUtilisationException" -> CONFLICT).
 */
public class EmailEnUtilisationException extends RuntimeException {

    public EmailEnUtilisationException(String message) {
        super(message);
    }
}
