package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.EmployeeDTO;
import org.sid.restaurationbackend.dtos.PointDeVenteDTO;
import org.sid.restaurationbackend.dtos.RoleDTO;
import org.sid.restaurationbackend.enums.StatutPresence;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.PointDeVenteNotFoundException;
import org.sid.restaurationbackend.exceptions.RoleNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.EmployeeService;
import org.sid.restaurationbackend.services.PointDeVenteService;
import org.sid.restaurationbackend.services.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * Employee est rattaché directement à un restaurant
 * (Employee.restaurant). Toutes les méthodes de ce contrôleur
 * vérifient désormais que ce restaurant est bien celui de
 * l'employé connecté — via CurrentUserService — au lieu de
 * faire confiance à un restaurantId fourni par le frontend,
 * ou de renvoyer les employés de tous les restaurants
 * confondus.
 *
 * Remarque : les listes renvoyées par EmployeeService (par
 * rôle, par statut de présence, par éligibilité, recherche...)
 * ne sont pas filtrées côté requête SQL ; le filtrage est fait
 * ici, en mémoire, sur le résultat — même approche que
 * ProduitController, pour ne pas toucher aux repositories dans
 * ce lot.
 */
@RestController
@RequestMapping("/api/employees")
@AllArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;
    private final RoleService roleService;
    private final PointDeVenteService pointDeVenteService;
    private final CurrentUserService currentUserService;


    // Ajouter un employé
    //
    // Le restaurant fourni dans le DTO doit être celui de l'employé
    // connecté : impossible de créer un employé pour un autre restaurant.
    @PostMapping
    public ResponseEntity<EmployeeDTO> saveEmployee(
            @RequestBody EmployeeDTO employeeDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                employeeDTO.getRestaurant() != null
                        ? employeeDTO.getRestaurant().getId_restaurant()
                        : null
        );

        EmployeeDTO savedEmployee =
                employeeService.saveEmployee(employeeDTO);

        return new ResponseEntity<>(
                savedEmployee,
                HttpStatus.CREATED
        );
    }


    // Récupérer un employé par ID
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(
            @PathVariable Long id)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        EmployeeDTO employee = employeeService.getEmployee(id);

        currentUserService.verifierAccesRestaurant(
                employee.getRestaurant() != null
                        ? employee.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(employee);
    }


    // Récupérer tous les employés DU RESTAURANT CONNECTÉ
    //
    // Avant : renvoyait TOUS les employés, tous restaurants confondus.
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>>
    getAllEmployees()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<EmployeeDTO> employees = employeeService.getAllEmployees()
                .stream()
                .filter(e -> e.getRestaurant() != null
                        && restaurantId.equals(e.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(employees);
    }


    // Recherche d'employés, DU RESTAURANT CONNECTÉ
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>>
    searchEmployees(
            @RequestParam(required = false)
            String keyword)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<EmployeeDTO> employees = employeeService.searchEmployees(keyword)
                .stream()
                .filter(e -> e.getRestaurant() != null
                        && restaurantId.equals(e.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(employees);
    }


    // Récupérer un employé par matricule
    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<EmployeeDTO>
    getEmployeeByMatricule(
            @PathVariable String matricule)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        EmployeeDTO employee = employeeService.getEmployeeByMatricule(
                matricule
        );

        currentUserService.verifierAccesRestaurant(
                employee.getRestaurant() != null
                        ? employee.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(employee);
    }


    // Récupérer un employé par email
    @GetMapping("/email/{email}")
    public ResponseEntity<EmployeeDTO>
    getEmployeeByEmail(
            @PathVariable String email)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        EmployeeDTO employee = employeeService.getEmployeeByEmail(email);

        currentUserService.verifierAccesRestaurant(
                employee.getRestaurant() != null
                        ? employee.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(employee);
    }


    // Récupérer un employé par code PIN
    //
    // Utilisé notamment pour l'ouverture de session PDV : on vérifie
    // quand même que l'employé identifié appartient au restaurant
    // connecté, pour ne pas exposer un employé d'un autre restaurant.
    @GetMapping("/code-pin/{code}")
    public ResponseEntity<EmployeeDTO>
    getEmployeeByCodePin(
            @PathVariable String code)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        EmployeeDTO employee = employeeService.getEmployeeByCodePin(code);

        currentUserService.verifierAccesRestaurant(
                employee.getRestaurant() != null
                        ? employee.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(employee);
    }


    // Employés par rôle, DU RESTAURANT CONNECTÉ
    //
    // ⚠️ Découverte indépendante du scoping restaurant : ce endpoint
    // ignorait déjà le paramètre roleId avant ce correctif (il appelait
    // employeeService.getEmployeesByRole(new RoleDTO()) sans jamais
    // charger le rôle demandé). Je n'ai pas changé ce comportement
    // pré-existant ici — seul le filtrage par restaurant est ajouté —
    // mais il faudra le corriger séparément si ce endpoint est utilisé.
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<EmployeeDTO>>
    getEmployeesByRole(
            @PathVariable Long roleId)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<EmployeeDTO> employees = employeeService.getEmployeesByRole(
                        new RoleDTO()
                )
                .stream()
                .filter(e -> e.getRestaurant() != null
                        && restaurantId.equals(e.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(employees);
    }


    // Employés par point de vente
    //
    // Le PDV demandé doit appartenir au restaurant connecté.
    @GetMapping("/pdv/{pdvId}")
    public List<EmployeeDTO> getEmployeesByPdv(
            @PathVariable Long pdvId)
            throws PointDeVenteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PointDeVenteDTO pdv =
                pointDeVenteService.getPointDeVente(pdvId);

        currentUserService.verifierAccesRestaurant(
                pdv.getRestaurant() != null
                        ? pdv.getRestaurant().getId_restaurant()
                        : null
        );

        return employeeService.getEmployeeByPdvAffecte(pdv);
    }


    // Employés par statut de présence, DU RESTAURANT CONNECTÉ
    @GetMapping("/statut-presence/{statut}")
    public ResponseEntity<List<EmployeeDTO>>
    getEmployeesByStatutPresence(
            @PathVariable StatutPresence statut)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<EmployeeDTO> employees = employeeService.getEmployeesByStatutPresence(
                        statut
                )
                .stream()
                .filter(e -> e.getRestaurant() != null
                        && restaurantId.equals(e.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(employees);
    }


    // NOTE : la self-modification du compte connecté ("Mon compte")
    // ne vit plus ici — voir MonCompteController (/api/mon-compte),
    // qui fonctionne au niveau Utilisateur et couvre donc aussi bien
    // Employee que SuperAdmin (un premier essai avait été fait ici
    // avec un endpoint PUT /me, mais il échouait avec un 404 pour
    // tout compte SUPERADMIN, qui n'est pas un Employee).


    // Modifier un employé
    //
    // L'employé existant doit appartenir au restaurant connecté, et on
    // ne doit pas pouvoir le "déplacer" vers un autre restaurant via le
    // DTO envoyé.
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO>
    updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeDTO employeeDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        EmployeeDTO existant = employeeService.getEmployee(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        if (employeeDTO.getRestaurant() != null) {
            currentUserService.verifierAccesRestaurant(
                    employeeDTO.getRestaurant().getId_restaurant()
            );
        }

        return ResponseEntity.ok(
                employeeService.updateEmployee(
                        id,
                        employeeDTO
                )
        );
    }


    // Modifier le statut de présence
    @PatchMapping("/{id}/statut-presence")
    public ResponseEntity<EmployeeDTO>
    updateStatutPresence(
            @PathVariable Long id,
            @RequestParam StatutPresence statut)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        EmployeeDTO existant = employeeService.getEmployee(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                employeeService.updateStatutPresence(
                        id,
                        statut
                )
        );
    }


    // =========================================================
    // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
    // =========================================================
    // Réservé au responsable : même permission que
    // TableController.assignerServeurResponsable / le contrôle des
    // employés éligibles est une décision sensible (§30 de la spec),
    // un employé normal ne doit pas pouvoir se rendre éligible
    // lui-même ni rendre un collègue éligible.

    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @PatchMapping("/{id}/eligibilite-attribution-automatique")
    public ResponseEntity<EmployeeDTO> updateEligibiliteAttributionAutomatique(
            @PathVariable Long id,
            @RequestParam Boolean eligible)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        EmployeeDTO existant = employeeService.getEmployee(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                employeeService.updateEligibiliteAttributionAutomatique(
                        id,
                        eligible
                )
        );
    }


    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @GetMapping("/eligibles-attribution-automatique")
    public ResponseEntity<List<EmployeeDTO>> getEmployesEligiblesAttributionAutomatique()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<EmployeeDTO> employees = employeeService
                .getEmployesEligiblesAttributionAutomatique()
                .stream()
                .filter(e -> e.getRestaurant() != null
                        && restaurantId.equals(e.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(employees);
    }


    // Supprimer un employé
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long id)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        EmployeeDTO existant = employeeService.getEmployee(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        employeeService.deleteEmployee(id);

        return ResponseEntity.noContent().build();
    }
}