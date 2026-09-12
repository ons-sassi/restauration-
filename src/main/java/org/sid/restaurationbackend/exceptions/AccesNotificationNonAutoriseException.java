package org.sid.restaurationbackend.exceptions;

/**
 * Levée quand l'utilisateur connecté (employé, client authentifié ou
 * super-admin) tente de consulter/modifier une notification qui n'est
 * ni la sienne (il n'en est ni l'émetteur ni le destinataire), ni celle
 * d'un collègue de son propre restaurant (cas Employee — voir
 * NotificationController).
 *
 * Distincte de {@link AccesRestaurantNonAutoriseException} : Notification
 * n'a pas de colonne restaurant et concerne aussi des clients
 * authentifiés (jamais rattachés à un restaurant), d'où une règle de
 * scoping différente (identité/ownership, pas uniquement restaurant).
 *
 * Le nom se termine par "NonAutoriseException" pour être automatiquement
 * mappée en HTTP 403 par {@link GlobalExceptionHandler}, sans rien à
 * ajouter au gestionnaire d'exceptions global.
 */
public class AccesNotificationNonAutoriseException extends Exception {
    public AccesNotificationNonAutoriseException(String message) {
        super(message);
    }
}
