package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.AttributionStatsDTO;
import org.sid.restaurationbackend.dtos.EmployeeDTO;
import org.sid.restaurationbackend.dtos.PriseEnChargeTableDTO;
import org.sid.restaurationbackend.dtos.TableRestaurantDTO;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.AucunEmployeeDisponibleException;
import org.sid.restaurationbackend.exceptions.EmployeeNonEligibleException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeNonAutoriseeException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeTableEnUtilisationException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeTableNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.EmployeeService;
import org.sid.restaurationbackend.services.PriseEnChargeTableService;
import org.sid.restaurationbackend.services.TableService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Prise en charge TEMPORAIRE d'un client à une table (phase 3B), à
 * distinguer de l'affectation PERMANENTE gérée par TableController
 * (serveurAttribue).
 *
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * PriseEnChargeTable n'a pas de colonne restaurant en base : son
 * restaurant est déterminé INDIRECTEMENT via la table concernée
 * (Table.restaurant) et/ou l'employé concerné (Employee.restaurant).
 * Toutes les méthodes prenant un tableId/employeeId en paramètre
 * vérifient désormais que la table/l'employé appartient bien au
 * restaurant de l'employé connecté.
 */
@RestController
@RequestMapping("/api/prises-en-charge")
@AllArgsConstructor
public class PriseEnChargeTableController {

    private final PriseEnChargeTableService priseEnChargeTableService;
    private final TableService tableService;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;


    // L'employé qui démarre la prise en charge n'est jamais pris dans
    // l'URL ou le corps de la requête : il est déduit du token JWT côté
    // service (SecurityContextHolder), comme /api/tables/my-tables.
    // La table concernée doit en revanche appartenir au restaurant
    // connecté.
    // Protégé par EMPLOYES_MES_TABLES : action self-service de
    // l'employé sur SES tables (voir /api/tables/my-tables), distincte
    // de EMPLOYES_TABLES (attribution, plus bas).
    @PreAuthorize("hasAuthority('EMPLOYES_MES_TABLES')")
    @PostMapping("/tables/{tableId}/commencer")
    public PriseEnChargeTableDTO commencerPriseEnCharge(
            @PathVariable Long tableId)
            throws TableNotFoundException,
            EmployeeNotFoundException,
            PriseEnChargeTableEnUtilisationException,
            PriseEnChargeNonAutoriseeException,
            AccesRestaurantNonAutoriseException {

        verifierTableDuRestaurantConnecte(tableId);

        return priseEnChargeTableService
                .commencerPriseEnCharge(tableId);
    }


    @PreAuthorize("hasAuthority('EMPLOYES_MES_TABLES')")
    @PatchMapping("/tables/{tableId}/terminer")
    public PriseEnChargeTableDTO terminerPriseEnCharge(
            @PathVariable Long tableId)
            throws TableNotFoundException,
            PriseEnChargeTableNotFoundException,
            EmployeeNotFoundException,
            PriseEnChargeNonAutoriseeException,
            AccesRestaurantNonAutoriseException {

        verifierTableDuRestaurantConnecte(tableId);

        return priseEnChargeTableService.terminerPriseEnCharge(tableId);
    }


    @PreAuthorize("hasAuthority('EMPLOYES_MES_TABLES')")
    @GetMapping("/tables/{tableId}/active")
    public PriseEnChargeTableDTO getPriseEnChargeActiveByTable(
            @PathVariable Long tableId)
            throws TableNotFoundException, PriseEnChargeTableNotFoundException,
            EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        verifierTableDuRestaurantConnecte(tableId);

        return priseEnChargeTableService
                .getPriseEnChargeActiveByTable(tableId);
    }


    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @GetMapping("/employees/{employeeId}/actives")
    public List<PriseEnChargeTableDTO> getPrisesEnChargeActivesByEmployee(
            @PathVariable Long employeeId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        verifierEmployeeDuRestaurantConnecte(employeeId);

        return priseEnChargeTableService
                .getPrisesEnChargeActivesByEmployee(employeeId);
    }


    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @GetMapping("/employees/{employeeId}/charge-active")
    public long getChargeActiveEmployee(
            @PathVariable Long employeeId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        verifierEmployeeDuRestaurantConnecte(employeeId);

        return priseEnChargeTableService
                .getChargeActiveEmployee(employeeId);
    }


    // =========================================================
    // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
    // =========================================================
    // Réservé au responsable (même permission que
    // TableController.assignerServeurResponsable) : contrairement à
    // /commencer ci-dessus, l'employé n'est pas déduit du token JWT
    // ici, c'est précisément le but de ces deux endpoints de désigner
    // (automatiquement ou manuellement) l'employé qui prendra en
    // charge le client.

    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @PostMapping("/tables/{tableId}/auto-assign")
    public PriseEnChargeTableDTO attribuerAutomatiquement(
            @PathVariable Long tableId)
            throws TableNotFoundException,
            PriseEnChargeTableEnUtilisationException,
            AucunEmployeeDisponibleException,
            EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierTableDuRestaurantConnecte(tableId);

        return priseEnChargeTableService
                .attribuerAutomatiquement(tableId);
    }


    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @PostMapping("/tables/{tableId}/manual-assign/{employeeId}")
    public PriseEnChargeTableDTO attribuerManuellement(
            @PathVariable Long tableId,
            @PathVariable Long employeeId)
            throws TableNotFoundException,
            EmployeeNotFoundException,
            PriseEnChargeTableEnUtilisationException,
            EmployeeNonEligibleException,
            AccesRestaurantNonAutoriseException {

        verifierTableDuRestaurantConnecte(tableId);
        verifierEmployeeDuRestaurantConnecte(employeeId);

        return priseEnChargeTableService
                .attribuerManuellement(tableId, employeeId);
    }


    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @GetMapping("/statistiques-attribution")
    public List<AttributionStatsDTO> getStatistiquesAttribution()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return priseEnChargeTableService.getStatistiquesAttribution()
                .stream()
                .filter(stat -> stat.getEmployee() != null
                        && stat.getEmployee().getRestaurant() != null
                        && restaurantId.equals(
                        stat.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }


    // =========================================================
    // HELPERS INTERNES DE VÉRIFICATION RESTAURANT
    // =========================================================

    private void verifierTableDuRestaurantConnecte(Long tableId)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO table = tableService.getTable(tableId);

        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );
    }


    private void verifierEmployeeDuRestaurantConnecte(Long employeeId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        EmployeeDTO employee = employeeService.getEmployee(employeeId);

        currentUserService.verifierAccesRestaurant(
                employee.getRestaurant() != null
                        ? employee.getRestaurant().getId_restaurant()
                        : null
        );
    }
}