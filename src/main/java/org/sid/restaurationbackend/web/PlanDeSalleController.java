package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.PlanDeSalleDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.PlanDeSalleNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.PlanDeSalleService;
import org.sid.restaurationbackend.services.RestaurantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * PlanDeSalle est rattaché directement à un restaurant.
 * Toutes les méthodes de ce contrôleur vérifient désormais que
 * ce restaurant est bien celui de l'employé connecté, au lieu
 * de faire confiance à un restaurantId fourni par le frontend
 * (paramètre de chemin ou champ du DTO), ou de renvoyer les
 * plans de tous les restaurants confondus.
 */
@RestController
@RequestMapping("/api/plans-de-salle")
@AllArgsConstructor
@Slf4j
public class PlanDeSalleController {

    private final PlanDeSalleService planDeSalleService;
    private final RestaurantService restaurantService;
    private final CurrentUserService currentUserService;


    // Ajouter un plan de salle
    @PostMapping
    public PlanDeSalleDTO savePlanDeSalle(
            @RequestBody PlanDeSalleDTO planDeSalleDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                planDeSalleDTO.getRestaurant() != null
                        ? planDeSalleDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return planDeSalleService
                .savePlanDeSalle(planDeSalleDTO);
    }


    // Récupérer tous les plans de salle DU RESTAURANT CONNECTÉ
    @GetMapping
    public List<PlanDeSalleDTO> getAllPlansDeSalle()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return planDeSalleService
                .listPlanDeSalle()
                .stream()
                .filter(p -> p.getRestaurant() != null
                        && restaurantId.equals(p.getRestaurant().getId_restaurant()))
                .toList();
    }


    // Récupérer un plan de salle par ID
    @GetMapping("/{id}")
    public PlanDeSalleDTO getPlanDeSalle(
            @PathVariable Long id)
            throws PlanDeSalleNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PlanDeSalleDTO plan = planDeSalleService
                .getPlanDeSalle(id);

        currentUserService.verifierAccesRestaurant(
                plan.getRestaurant() != null
                        ? plan.getRestaurant().getId_restaurant()
                        : null
        );

        return plan;
    }


    // Modifier un plan de salle
    //
    // Le plan existant doit appartenir au restaurant connecté, et on
    // ne doit pas pouvoir le "déplacer" vers un autre restaurant.
    @PutMapping("/{id}")
    public PlanDeSalleDTO updatePlanDeSalle(
            @PathVariable Long id,
            @RequestBody PlanDeSalleDTO planDeSalleDTO)
            throws PlanDeSalleNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PlanDeSalleDTO existant = planDeSalleService.getPlanDeSalle(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        if (planDeSalleDTO.getRestaurant() != null) {
            currentUserService.verifierAccesRestaurant(
                    planDeSalleDTO.getRestaurant().getId_restaurant()
            );
        }

        return planDeSalleService
                .updatePlanDeSalle(
                        id,
                        planDeSalleDTO
                );
    }


    // Supprimer un plan de salle
    @DeleteMapping("/{id}")
    public void deletePlanDeSalle(
            @PathVariable Long id)
            throws PlanDeSalleNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PlanDeSalleDTO existant = planDeSalleService.getPlanDeSalle(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        planDeSalleService
                .deletePlanDeSalle(id);
    }


    // Plans de salle par restaurant
    //
    // Le restaurantId demandé DOIT être celui de l'employé connecté.
    @GetMapping("/restaurant/{restaurantId}")
    public List<PlanDeSalleDTO> getPlansByRestaurant(
            @PathVariable Long restaurantId)
            throws RestaurantNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(restaurantId);

        RestaurantDTO restaurant =
                restaurantService.getRestaurant(restaurantId);

        return planDeSalleService
                .listPlanDeSalleByRestaurant(
                        restaurant
                );
    }


    // Plan de salle courant d'un restaurant
    //
    // Le restaurantId demandé DOIT être celui de l'employé connecté.
    @GetMapping("/restaurant/{restaurantId}/current")
    public PlanDeSalleDTO getCurrentPlanByRestaurant(
            @PathVariable Long restaurantId)
            throws RestaurantNotFoundException,
            PlanDeSalleNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(restaurantId);

        RestaurantDTO restaurant =
                restaurantService.getRestaurant(restaurantId);

        return planDeSalleService
                .getCurrentPlanDeSalleByRestaurant(
                        restaurant
                );
    }
}