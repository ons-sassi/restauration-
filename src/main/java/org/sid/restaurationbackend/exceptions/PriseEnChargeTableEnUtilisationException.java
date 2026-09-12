package org.sid.restaurationbackend.exceptions;

/**
 * Levée quand on tente de démarrer une nouvelle prise en charge sur une
 * table qui possède déjà une prise en charge ACTIVE. Il faut d'abord
 * terminer la prise en charge en cours (bouton "Client parti") avant
 * d'en démarrer une nouvelle.
 *
 * Le suffixe "EnUtilisationException" est repris volontairement :
 * GlobalExceptionHandler mappe déjà toute exception se terminant par
 * ce suffixe vers un HTTP 409 (Conflict).
 */
public class PriseEnChargeTableEnUtilisationException extends Exception {
    public PriseEnChargeTableEnUtilisationException(String message) {
        super(message);
    }
}
