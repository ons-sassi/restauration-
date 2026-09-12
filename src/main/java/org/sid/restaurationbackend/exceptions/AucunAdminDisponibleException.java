package org.sid.restaurationbackend.exceptions;

/**
 * Levée quand le SUPERADMIN demande à superviser (impersonate) un
 * restaurant qui n'a aucun employé avec le rôle "Admin" (accès
 * back-office) — ex : restaurant nouvellement créé, sans encore
 * d'ADMIN configuré.
 *
 * Le suffixe "DisponibleException" est mappé par
 * GlobalExceptionHandler vers un HTTP 409 (Conflict), même principe
 * que AucunEmployeeDisponibleException : situation métier temporaire,
 * pas une ressource introuvable (404).
 */
public class AucunAdminDisponibleException extends Exception {
    public AucunAdminDisponibleException(String message) {
        super(message);
    }
}
