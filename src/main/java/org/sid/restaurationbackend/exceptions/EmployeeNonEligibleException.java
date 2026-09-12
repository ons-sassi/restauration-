package org.sid.restaurationbackend.exceptions;

/**
 * Levée quand le responsable tente d'attribuer manuellement une prise
 * en charge à un employé dont Employee.eligibleAttributionAutomatique
 * n'est pas à true (§21 de la spec Phase 3C).
 *
 * Le suffixe "NonEligibleException" est mappé par
 * GlobalExceptionHandler vers un HTTP 403 (Forbidden), sur le même
 * principe que les suffixes déjà en place dans le projet.
 */
public class EmployeeNonEligibleException extends Exception {
    public EmployeeNonEligibleException(String message) {
        super(message);
    }
}
