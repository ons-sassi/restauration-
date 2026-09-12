package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.DashboardDTO;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.DashboardService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — Lot 2.6
 * =========================================================
 * ⚠️ Découverte : restaurantId était reçu tel quel du frontend et
 * transmis directement au service, sans AUCUNE vérification — un
 * employé pouvait consulter le dashboard (chiffre d'affaires, stats)
 * de N'IMPORTE QUEL AUTRE RESTAURANT simplement en changeant ce
 * paramètre dans l'URL. Corrigé en vérifiant que restaurantId
 * correspond bien au restaurant de l'employé connecté.
 */
@RestController
@RequestMapping("/api/back-office/dashboard")
@AllArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;


    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(

            @RequestParam Long restaurantId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date

    ) throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(restaurantId);

        DashboardDTO dashboard =
                dashboardService.getDashboard(
                        restaurantId,
                        date
                );


        return ResponseEntity.ok(
                dashboard
        );
    }
}