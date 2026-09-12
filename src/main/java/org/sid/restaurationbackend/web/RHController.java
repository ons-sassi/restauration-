package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.enums.StatutPresence;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.enums.StatutVersement;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.EmployeeService;
import org.sid.restaurationbackend.services.RHService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * Presence, Penalite, VersementSalaire et Performance n'ont pas de
 * colonne restaurant propre : le restaurant est indirect, via
 * l'Employee auquel chaque enregistrement est rattaché (même
 * situation qu'EmployeeController lui-même). Toutes les méthodes
 * ci-dessous vérifient donc le restaurant de l'Employee concerné —
 * jamais un restaurantId fourni par le frontend, qui de toute façon
 * n'existe pas sur ces DTOs.
 *
 * Deux méthodes utilitaires privées centralisent le pattern pour
 * éviter de le dupliquer 4 fois (présences / pénalités / versements
 * / performances) :
 *  - verifierAccesEmployee(EmployeeDTO) : vérifie le restaurant d'un
 *    EmployeeDTO déjà chargé (ex: renvoyé par rhService.getX(id)).
 *  - chargerEmployeeAutorise(Long employeeId) : recharge le VRAI
 *    Employee depuis la base à partir d'un id (jamais depuis le
 *    sous-objet "employee" envoyé dans un DTO par le frontend — un
 *    employeeDTO.getRestaurant() dans le corps d'une requête POST
 *    pourrait être falsifié) puis vérifie son restaurant.
 *
 * Remarque découverte au passage, hors scope de ce correctif : les
 * endpoints pénalités / versements / performances n'ont ici aucun
 * @PreAuthorize (contrairement aux présences, protégées par
 * EMPLOYES_PRESENCE), alors qu'il s'agit aussi de données RH/salaires
 * sensibles. Je n'ai pas ajouté de permission ici (ça change le
 * comportement pour les rôles existants, décision produit à part) —
 * seul le scoping restaurant est traité dans ce lot.
 */
@RestController
@RequestMapping("/api/rh")
@AllArgsConstructor
public class RHController {

    private final RHService rhService;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;


    // ---------------------------------------------------------------
    // Utilitaires internes de scoping restaurant (voir javadoc classe)
    // ---------------------------------------------------------------

    private void verifierAccesEmployee(EmployeeDTO employee)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                employee != null && employee.getRestaurant() != null
                        ? employee.getRestaurant().getId_restaurant()
                        : null
        );
    }

    private EmployeeDTO chargerEmployeeAutorise(Long employeeId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        if (employeeId == null) {
            throw new EmployeeNotFoundException("Employee not found");
        }

        EmployeeDTO employee = employeeService.getEmployee(employeeId);
        verifierAccesEmployee(employee);
        return employee;
    }


    // =========================================================
    // PRESENCES
    // Réservé au responsable : permission EMPLOYES_PRESENCE
    // ("Modifier feuille de présence", cf. DataInitializer).
    // =========================================================

    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @PostMapping("/presences")
    public PresenceDTO savePresence(
            @RequestBody PresenceDTO presenceDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(
                presenceDTO.getEmployee() != null
                        ? presenceDTO.getEmployee().getId_utilisateur()
                        : null
        );

        return rhService.savePresence(presenceDTO);
    }

    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @PutMapping("/presences/{id}")
    public PresenceDTO updatePresence(
            @PathVariable Long id,
            @RequestBody PresenceDTO presenceDTO)
            throws PresenceNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PresenceDTO existante = rhService.getPresence(id);
        verifierAccesEmployee(existante.getEmployee());

        // Si le corps de la requête change l'employé rattaché, le
        // nouvel employé doit lui aussi appartenir au restaurant connecté.
        if (presenceDTO.getEmployee() != null) {
            chargerEmployeeAutorise(
                    presenceDTO.getEmployee().getId_utilisateur());
        }

        return rhService.updatePresence(id, presenceDTO);
    }

    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @GetMapping("/presences/{id}")
    public PresenceDTO getPresence(
            @PathVariable Long id)
            throws PresenceNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PresenceDTO presence = rhService.getPresence(id);
        verifierAccesEmployee(presence.getEmployee());

        return presence;
    }

    // Toutes les présences DU RESTAURANT CONNECTÉ.
    // Avant : renvoyait les présences de tous les restaurants confondus.
    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @GetMapping("/presences")
    public List<PresenceDTO> getAllPresences()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getAllPresences()
                .stream()
                .filter(p -> p.getEmployee() != null
                        && p.getEmployee().getRestaurant() != null
                        && restaurantId.equals(p.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }

    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @DeleteMapping("/presences/{id}")
    public void deletePresence(
            @PathVariable Long id)
            throws PresenceNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PresenceDTO existante = rhService.getPresence(id);
        verifierAccesEmployee(existante.getEmployee());

        rhService.deletePresence(id);
    }

    // Par Employee DTO
    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @GetMapping("/presences/employee")
    public List<PresenceDTO> getPresencesByEmployee(
            @RequestBody Long id)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(id);

        return rhService.getPresencesByEmployee(id);
    }

    // Par Employee ID
    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @GetMapping("/presences/employee/{employeeId}")
    public List<PresenceDTO> getPresencesByEmployeeId(
            @PathVariable Long employeeId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(employeeId);

        return rhService.getPresencesByEmployee(employeeId);
    }


    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @GetMapping("/presences/date")
    public List<PresenceDTO> getPresencesByDateBetween(
            @RequestParam Date dateDebut,
            @RequestParam Date dateFin)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getPresencesByDateBetween(dateDebut, dateFin)
                .stream()
                .filter(p -> p.getEmployee() != null
                        && p.getEmployee().getRestaurant() != null
                        && restaurantId.equals(p.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }


    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @GetMapping("/presences/employee/{employeeId}/date")
    public List<PresenceDTO> getPresencesByEmployeeAndDateBetween(
            @PathVariable Long employeeId,
            @RequestParam Date dateDebut,
            @RequestParam Date dateFin)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(employeeId);

        return rhService.getPresencesByEmployeeAndDateBetween(
                employeeId,
                dateDebut,
                dateFin);
    }


    // ---------------------------------------------------------------
    // PAGE "PRESENCE" — pointage par le responsable
    // ---------------------------------------------------------------

    /*
     * Marque le statut (PRESENT / ABSENT / CONGE) d'un employé pour
     * un jour donné (upsert : un seul enregistrement par employé et
     * par jour).
     */
    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @PatchMapping("/presences/marquer")
    public PresenceDTO marquerPresence(
            @RequestParam Long employeeId,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date date,
            @RequestParam StatutPresence statut)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(employeeId);

        return rhService.marquerPresence(employeeId, date, statut);
    }

    /*
     * Feuille de présence d'un jour donné, DU RESTAURANT CONNECTÉ,
     * avec les compteurs d'absences du mois et de l'année en cours.
     *
     * Avant : rhService.getFeuillePresence(date) parcourt TOUS les
     * employés (employeeService.getAllEmployees()), tous restaurants
     * confondus ; le filtrage est donc fait ici, en mémoire, sur le
     * résultat — même limitation assumée que pour getAllEmployees()
     * dans EmployeeController (pas de filtrage SQL dans ce lot).
     */
    @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')")
    @GetMapping("/presences/feuille")
    public List<FeuillePresenceDTO> getFeuillePresence(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date date)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getFeuillePresence(date)
                .stream()
                .filter(f -> f.getEmployee() != null
                        && f.getEmployee().getRestaurant() != null
                        && restaurantId.equals(f.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }




    @PostMapping("/penalites")
    public PenaliteDTO savePenalite(
            @RequestBody PenaliteDTO penaliteDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(
                penaliteDTO.getEmployee() != null
                        ? penaliteDTO.getEmployee().getId_utilisateur()
                        : null
        );

        return rhService.savePenalite(penaliteDTO);
    }

    @PutMapping("/penalites/{id}")
    public PenaliteDTO updatePenalite(
            @PathVariable Long id,
            @RequestBody PenaliteDTO penaliteDTO)
            throws PenaliteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PenaliteDTO existante = rhService.getPenalite(id);
        verifierAccesEmployee(existante.getEmployee());

        if (penaliteDTO.getEmployee() != null) {
            chargerEmployeeAutorise(
                    penaliteDTO.getEmployee().getId_utilisateur());
        }

        return rhService.updatePenalite(id, penaliteDTO);
    }

    @GetMapping("/penalites/{id}")
    public PenaliteDTO getPenalite(
            @PathVariable Long id)
            throws PenaliteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PenaliteDTO penalite = rhService.getPenalite(id);
        verifierAccesEmployee(penalite.getEmployee());

        return penalite;
    }

    // Toutes les pénalités DU RESTAURANT CONNECTÉ.
    @GetMapping("/penalites")
    public List<PenaliteDTO> getAllPenalites()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getAllPenalites()
                .stream()
                .filter(p -> p.getEmployee() != null
                        && p.getEmployee().getRestaurant() != null
                        && restaurantId.equals(p.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }

    @DeleteMapping("/penalites/{id}")
    public void deletePenalite(
            @PathVariable Long id)
            throws PenaliteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PenaliteDTO existante = rhService.getPenalite(id);
        verifierAccesEmployee(existante.getEmployee());

        rhService.deletePenalite(id);
    }

    // Par Employee DTO
    @GetMapping("/penalites/employee")
    public List<PenaliteDTO> getPenalitesByEmployee(
            @RequestBody Long id)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(id);

        return rhService.getPenalitesByEmployee(id);
    }

    // Par Employee ID
    @GetMapping("/penalites/employee/{employeeId}")
    public List<PenaliteDTO> getPenalitesByEmployeeId(
            @PathVariable Long employeeId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(employeeId);

        return rhService.getPenalitesByEmployee(employeeId);
    }

    @GetMapping("/penalites/date")
    public List<PenaliteDTO> getPenalitesByDateBetween(
            @RequestParam Date dateDebut,
            @RequestParam Date dateFin)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getPenalitesByDateBetween(dateDebut, dateFin)
                .stream()
                .filter(p -> p.getEmployee() != null
                        && p.getEmployee().getRestaurant() != null
                        && restaurantId.equals(p.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }


    @GetMapping("/penalites/employee/{employeeId}/date")
    public List<PenaliteDTO> getPenalitesByEmployeeAndDateBetween(
            @PathVariable Long employeeId,
            @RequestParam Date dateDebut,
            @RequestParam Date dateFin)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(employeeId);

        return rhService.getPenalitesByEmployeeAndDateBetween(
                employeeId,
                dateDebut,
                dateFin);
    }




    @PostMapping("/versements")
    public VersementSalaireDTO saveVersement(
            @RequestBody VersementSalaireDTO versementDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(
                versementDTO.getEmployee() != null
                        ? versementDTO.getEmployee().getId_utilisateur()
                        : null
        );

        return rhService.saveVersement(versementDTO);
    }

    @PutMapping("/versements/{id}")
    public VersementSalaireDTO updateVersement(
            @PathVariable Long id,
            @RequestBody VersementSalaireDTO versementDTO)
            throws VersementSalaireNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        VersementSalaireDTO existant = rhService.getVersement(id);
        verifierAccesEmployee(existant.getEmployee());

        if (versementDTO.getEmployee() != null) {
            chargerEmployeeAutorise(
                    versementDTO.getEmployee().getId_utilisateur());
        }

        return rhService.updateVersement(id, versementDTO);
    }

    @GetMapping("/versements/{id}")
    public VersementSalaireDTO getVersement(
            @PathVariable Long id)
            throws VersementSalaireNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        VersementSalaireDTO versement = rhService.getVersement(id);
        verifierAccesEmployee(versement.getEmployee());

        return versement;
    }

    // Tous les versements DU RESTAURANT CONNECTÉ.
    @GetMapping("/versements")
    public List<VersementSalaireDTO> getAllVersements()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getAllVersements()
                .stream()
                .filter(v -> v.getEmployee() != null
                        && v.getEmployee().getRestaurant() != null
                        && restaurantId.equals(v.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }

    @DeleteMapping("/versements/{id}")
    public void deleteVersement(
            @PathVariable Long id)
            throws VersementSalaireNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        VersementSalaireDTO existant = rhService.getVersement(id);
        verifierAccesEmployee(existant.getEmployee());

        rhService.deleteVersement(id);
    }

    @GetMapping("/versements/employee")
    public List<VersementSalaireDTO> getVersementsByEmployee(
            @RequestBody Long id)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(id);

        return rhService.getVersementsByEmployee(id);
    }


    @GetMapping("/versements/employee/{employeeId}")
    public List<VersementSalaireDTO> getVersementsByEmployeeId(
            @PathVariable Long employeeId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(employeeId);

        return rhService.getVersementsByEmployee(employeeId);
    }


    // Global (tous employés confondus côté service) : filtré ici par
    // restaurant connecté.
    @GetMapping("/versements/statut/{statut}")
    public List<VersementSalaireDTO> getVersementsByStatut(
            @PathVariable StatutVersement statut)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getVersementsByStatut(statut)
                .stream()
                .filter(v -> v.getEmployee() != null
                        && v.getEmployee().getRestaurant() != null
                        && restaurantId.equals(v.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }


    @GetMapping("/versements/periode/{periode}")
    public List<VersementSalaireDTO> getVersementsByPeriode(
            @PathVariable String periode)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getVersementsByPeriode(periode)
                .stream()
                .filter(v -> v.getEmployee() != null
                        && v.getEmployee().getRestaurant() != null
                        && restaurantId.equals(v.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }


    @GetMapping("/versements/employee/{employeeId}/statut/{statut}")
    public List<VersementSalaireDTO> getVersementsByEmployeeAndStatut(
            @PathVariable Long employeeId,
            @PathVariable StatutVersement statut)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(employeeId);

        return rhService.getVersementsByEmployeeAndStatut(
                employeeId,
                statut);
    }




    @PostMapping("/performances")
    public PerformanceDTO savePerformance(
            @RequestBody PerformanceDTO performanceDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(
                performanceDTO.getEmployee() != null
                        ? performanceDTO.getEmployee().getId_utilisateur()
                        : null
        );

        return rhService.savePerformance(performanceDTO);
    }

    @PutMapping("/performances/{id}")
    public PerformanceDTO updatePerformance(
            @PathVariable Long id,
            @RequestBody PerformanceDTO performanceDTO)
            throws PerformanceNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PerformanceDTO existante = rhService.getPerformance(id);
        verifierAccesEmployee(existante.getEmployee());

        if (performanceDTO.getEmployee() != null) {
            chargerEmployeeAutorise(
                    performanceDTO.getEmployee().getId_utilisateur());
        }

        return rhService.updatePerformance(id, performanceDTO);
    }

    @GetMapping("/performances/{id}")
    public PerformanceDTO getPerformance(
            @PathVariable Long id)
            throws PerformanceNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PerformanceDTO performance = rhService.getPerformance(id);
        verifierAccesEmployee(performance.getEmployee());

        return performance;
    }

    // Toutes les performances DU RESTAURANT CONNECTÉ.
    @GetMapping("/performances")
    public List<PerformanceDTO> getAllPerformances()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getAllPerformances()
                .stream()
                .filter(p -> p.getEmployee() != null
                        && p.getEmployee().getRestaurant() != null
                        && restaurantId.equals(p.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }

    @DeleteMapping("/performances/{id}")
    public void deletePerformance(
            @PathVariable Long id)
            throws PerformanceNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PerformanceDTO existante = rhService.getPerformance(id);
        verifierAccesEmployee(existante.getEmployee());

        rhService.deletePerformance(id);
    }

    @GetMapping("/performances/employee")
    public List<PerformanceDTO> getPerformancesByEmployee(
            @RequestBody Long id)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(id);

        return rhService.getPerformancesByEmployee(id);
    }

    @GetMapping("/performances/employee/{employeeId}")
    public List<PerformanceDTO> getPerformancesByEmployeeId(
            @PathVariable Long employeeId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(employeeId);

        return rhService.getPerformancesByEmployee(employeeId);
    }


    @GetMapping("/performances/periode/{periode}")
    public List<PerformanceDTO> getPerformancesByPeriode(
            @PathVariable String periode)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return rhService.getPerformancesByPeriode(periode)
                .stream()
                .filter(p -> p.getEmployee() != null
                        && p.getEmployee().getRestaurant() != null
                        && restaurantId.equals(p.getEmployee().getRestaurant().getId_restaurant()))
                .toList();
    }


    @GetMapping("/performances/employee/{employeeId}/periode/{periode}")
    public List<PerformanceDTO> getPerformancesByEmployeeAndPeriode(
            @PathVariable Long employeeId,
            @PathVariable String periode)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        chargerEmployeeAutorise(employeeId);

        return rhService.getPerformancesByEmployeeAndPeriode(
                employeeId,
                periode);
    }
}