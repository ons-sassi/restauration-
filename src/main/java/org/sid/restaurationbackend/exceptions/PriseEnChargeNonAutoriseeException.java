package org.sid.restaurationbackend.exceptions;

/**
 * Levée quand un employé tente de démarrer ou de terminer une prise en
 * charge sur une table dont il n'est pas responsable (permanent) ou
 * dont il n'est pas l'employé ayant démarré la prise en charge active.
 *
 * Le suffixe "NonAutoriseeException" est mappé par GlobalExceptionHandler
 * vers un HTTP 403 (Forbidden), sur le même principe que les suffixes
 * "NotFoundException" (404) et "EnUtilisationException" (409) déjà en
 * place dans le projet.
 */
public class PriseEnChargeNonAutoriseeException extends Exception {
    public PriseEnChargeNonAutoriseeException(String message) {
        super(message);
    }
}
