package org.sid.restaurationbackend.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralise la conversion des exceptions métier en réponses HTTP propres,
 * au lieu de laisser Spring Boot renvoyer un 500 avec toute la stacktrace
 * (ce qui arrivait par ex. pour DELETE /api/produits/{id}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> build(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    // Toutes les exceptions "XxxNotFoundException" du projet -> 404
    // Toutes les exceptions "XxxEnUtilisationException" du projet -> 409
    // (suppression bloquée car l'entité est encore référencée ailleurs)
    // Identifiants invalides pendant l'authentification -> 401
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Compte existant mais inactif -> 403
    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<Object> handleAccountInactive(AccountInactiveException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // Utilisateur authentifié mais sans droit d'accès -> 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // Paramètres de requête invalides -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericAndNotFound(Exception ex) {
        String className = ex.getClass().getSimpleName();

        if (className.endsWith("NotFoundException")) {
            return build(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        if (className.endsWith("EnUtilisationException")) {
            return build(HttpStatus.CONFLICT, ex.getMessage());
        }
        // NB : deux orthographes coexistent dans le projet selon le genre
        // du mot qui précède ("prise" -> Autorisee, "accès" -> Autorise) :
        // AccesRestaurantNonAutoriseException (accord masculin, un seul
        // "e") ne matchait PAS le suffixe ci-dessous avant ce correctif,
        // et retombait donc en 500 au lieu du 403 attendu.
        if (className.endsWith("NonAutoriseeException") ||
                className.endsWith("NonAutoriseException")) {
            return build(HttpStatus.FORBIDDEN, ex.getMessage());
        }
        // Phase 3C : employé désigné manuellement mais non éligible
        // (EmployeeNonEligibleException) -> 403, même principe que
        // "NonAutoriseeException" ci-dessus.
        if (className.endsWith("NonEligibleException")) {
            return build(HttpStatus.FORBIDDEN, ex.getMessage());
        }
        // Phase 3C : aucun employé éligible pour une attribution
        // automatique (AucunEmployeeDisponibleException) -> 409,
        // même principe que "EnUtilisationException" ci-dessus.
        if (className.endsWith("DisponibleException")) {
            return build(HttpStatus.CONFLICT, ex.getMessage());
        }
        // On laisse passer les autres cas non prévus explicitement en 500,
        // mais avec un corps propre plutôt que la stacktrace brute.
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne : " + ex.getMessage());
    }

    // Filet de sécurité : toute violation de contrainte FK non anticipée -> 409
    // au lieu du 500 SQLServerException brut.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT,
                "Suppression impossible : cet élément est référencé par d'autres données " +
                        "(ex. commandes existantes). Désactivez-le au lieu de le supprimer.");
    }
}