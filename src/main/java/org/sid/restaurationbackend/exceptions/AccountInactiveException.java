package org.sid.restaurationbackend.exceptions;

/**
 * Indique qu'un compte existe mais n'est pas autorisé à se connecter car il est inactif.
 * Le GlobalExceptionHandler convertit cette exception en HTTP 403.
 */
public class AccountInactiveException extends RuntimeException {

    public AccountInactiveException(String message) {
        super(message);
    }
}
