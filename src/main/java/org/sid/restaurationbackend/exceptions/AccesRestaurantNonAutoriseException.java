package org.sid.restaurationbackend.exceptions;

/**
 * Levée quand l'employé connecté tente d'accéder à des données
 * (ou d'agir sur des données) d'un restaurant qui n'est pas le sien
 * — typiquement parce que le restaurantId fourni par le frontend ne
 * correspond pas à celui renvoyé par {@code CurrentUserService
 * .getRestaurantIdConnecte()}.
 *
 * Le nom se termine par "NonAutoriseeException" pour être
 * automatiquement mappée en HTTP 403 par
 * {@link GlobalExceptionHandler} (même convention que
 * {@link PriseEnChargeNonAutoriseeException}), sans rien à ajouter
 * au gestionnaire d'exceptions global.
 */
public class AccesRestaurantNonAutoriseException extends Exception {
    public AccesRestaurantNonAutoriseException(String message) {
        super(message);
    }
}
