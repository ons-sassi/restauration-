package org.sid.restaurationbackend.exceptions;

public class UtilisateurNotFoundException extends RuntimeException {
    public UtilisateurNotFoundException(String utilisateurNotFound) {
        super(utilisateurNotFound);
    }
}
