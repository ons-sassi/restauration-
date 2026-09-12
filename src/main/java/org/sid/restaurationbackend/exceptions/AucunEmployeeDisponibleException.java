package org.sid.restaurationbackend.exceptions;

/**
 * Levée quand l'attribution automatique d'une prise en charge est
 * demandée (§7 de la spec Phase 3C) mais qu'aucun employé n'est
 * actuellement éligible (aucun employé avec
 * eligibleAttributionAutomatique = true). La prise en charge n'est
 * alors JAMAIS créée avec un employé arbitraire (§11).
 *
 * Le suffixe "DisponibleException" est mappé par
 * GlobalExceptionHandler vers un HTTP 409 (Conflict) : il s'agit d'une
 * situation métier temporaire (aucune ressource humaine disponible),
 * pas d'une ressource introuvable (404) ni d'un problème
 * d'autorisation (403).
 */
public class AucunEmployeeDisponibleException extends Exception {
    public AucunEmployeeDisponibleException(String message) {
        super(message);
    }
}
