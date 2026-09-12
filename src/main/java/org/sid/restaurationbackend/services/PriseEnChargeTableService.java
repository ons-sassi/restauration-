package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.AttributionStatsDTO;
import org.sid.restaurationbackend.dtos.PriseEnChargeTableDTO;
import org.sid.restaurationbackend.exceptions.AucunEmployeeDisponibleException;
import org.sid.restaurationbackend.exceptions.EmployeeNonEligibleException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeNonAutoriseeException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeTableEnUtilisationException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeTableNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PriseEnChargeTableService {

    /*
     * L'employé qui démarre la prise en charge n'est JAMAIS lu depuis
     * un paramètre envoyé par le frontend : il est déduit du token JWT
     * (SecurityContextHolder), exactement comme pour
     * TableService.getTablesDuServeurConnecte(). Seul le responsable
     * permanent de la table (serveurAttribue) peut démarrer une prise
     * en charge dessus.
     */
    PriseEnChargeTableDTO commencerPriseEnCharge(Long tableId)
            throws TableNotFoundException,
            EmployeeNotFoundException,
            PriseEnChargeTableEnUtilisationException,
            PriseEnChargeNonAutoriseeException;

    /*
     * Seul l'employé ayant démarré la prise en charge active peut la
     * terminer ("Client parti").
     */
    PriseEnChargeTableDTO terminerPriseEnCharge(Long tableId)
            throws TableNotFoundException,
            PriseEnChargeTableNotFoundException,
            EmployeeNotFoundException,
            PriseEnChargeNonAutoriseeException;

    @Transactional(readOnly = true)
    PriseEnChargeTableDTO getPriseEnChargeActiveByTable(Long tableId)
            throws TableNotFoundException, PriseEnChargeTableNotFoundException;

    @Transactional(readOnly = true)
    List<PriseEnChargeTableDTO> getPrisesEnChargeActivesByEmployee(Long employeeId)
            throws EmployeeNotFoundException;

    @Transactional(readOnly = true)
    long getChargeActiveEmployee(Long employeeId)
            throws EmployeeNotFoundException;


    // =========================================================
    // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
    // =========================================================

    /*
     * Choisit automatiquement, parmi les employés éligibles
     * (Employee.eligibleAttributionAutomatique = true), celui ayant
     * actuellement le moins de prises en charge ACTIVES, et lui crée
     * une nouvelle prise en charge pour cette table.
     *
     * Contrairement à commencerPriseEnCharge (phase 3B), l'employé
     * n'a PAS besoin d'être le responsable permanent de la table
     * (serveurAttribue) : c'est précisément le but de l'automatisation
     * (§6 et §15 de la spec). L'affectation permanente n'est jamais
     * modifiée par cette méthode.
     *
     * La décision est prise entièrement côté backend (§19) : le
     * frontend n'envoie jamais l'employé choisi.
     */
    PriseEnChargeTableDTO attribuerAutomatiquement(Long tableId)
            throws TableNotFoundException,
            PriseEnChargeTableEnUtilisationException,
            AucunEmployeeDisponibleException;

    /*
     * Attribution manuelle par le responsable : celui-ci choisit
     * explicitement l'employé (§12/§20 de la spec). L'employé doit
     * être éligible (même condition que pour l'automatique, §21),
     * sans quoi l'opération est refusée.
     */
    PriseEnChargeTableDTO attribuerManuellement(Long tableId, Long employeeId)
            throws TableNotFoundException,
            EmployeeNotFoundException,
            PriseEnChargeTableEnUtilisationException,
            EmployeeNonEligibleException;

    /*
     * Vue d'ensemble pour le responsable (§22/§23) : pour chaque
     * employé éligible, son nombre actuel de prises en charge
     * actives, afin de comprendre pourquoi l'automatisation
     * choisirait tel employé plutôt qu'un autre.
     */
    @Transactional(readOnly = true)
    List<AttributionStatsDTO> getStatistiquesAttribution();
}
