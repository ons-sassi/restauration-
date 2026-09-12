package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ReductionDTO;
import org.sid.restaurationbackend.enums.TypeReduction;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.ReductionNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.ReductionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — famille
 * Stock/Fidélité.
 * =========================================================
 * Décision produit retenue (voir entité Reduction) : privé par
 * restaurant, colonne directe (pas de scoping indirect fiable),
 * même pattern que Fournisseur/Ingredient.
 *
 * Seuls getAll (liste), getById, create, update, delete, activer et
 * desactiver sont réellement branchés à l'écran back-office
 * "menu/réductions" (reduction-list / reduction-form). getByType,
 * getActives, getByDates et getReductionsApplicables n'ont aucun
 * consommateur Angular actuel — scopés quand même par cohérence.
 *
 * CORRECTIF (suite à relecture) : aucune gestion du cas SUPERADMIN
 * n'existait ici. Bypass ajouté, même pattern que Reclamation/
 * ClientAuthentifie/Suggestion/Fidelite/PrevisionStock. Pour create,
 * le SUPERADMIN n'ayant lui-même aucun restaurant, la vérification
 * "restaurant envoyé == restaurant de l'employé" ne s'applique pas :
 * le restaurant fourni est simplement requis et fait confiance
 * (comme pour ClientAuthentifie/Suggestion en tant que SUPERADMIN).
 *
 * AVANT ce correctif : n'importe quel employé, quel que soit son
 * restaurant, pouvait lister/consulter/modifier/supprimer/activer/
 * désactiver les réductions de n'importe quel autre restaurant.
 */
@RestController
@RequestMapping("/api/reductions")
@AllArgsConstructor
public class ReductionController {

    private final ReductionService reductionService;
    private final CurrentUserService currentUserService;

    // ---------------------------------------------------------------
    // Utilitaires internes de scoping restaurant (voir javadoc classe)
    // ---------------------------------------------------------------

    private boolean estSuperAdminConnecte() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_SUPERADMIN".equals(
                                authority.getAuthority()
                        )
                );
    }

    private void verifierAccesReduction(ReductionDTO reduction)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        if (estSuperAdminConnecte()) {
            return;
        }

        currentUserService.verifierAccesRestaurant(
                reduction != null && reduction.getRestaurant() != null
                        ? reduction.getRestaurant().getId_restaurant()
                        : null
        );
    }

    @PostMapping
    public ResponseEntity<ReductionDTO> saveReduction(
            @RequestBody ReductionDTO reductionDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        if (estSuperAdminConnecte()) {

            if (reductionDTO.getRestaurant() == null
                    || reductionDTO.getRestaurant().getId_restaurant() == null) {

                throw new AccesRestaurantNonAutoriseException(
                        "Un restaurant doit être précisé pour créer "
                                + "une réduction en tant que SUPERADMIN.");
            }

        } else {

            currentUserService.verifierAccesRestaurant(
                    reductionDTO.getRestaurant() != null
                            ? reductionDTO.getRestaurant().getId_restaurant()
                            : null
            );
        }

        ReductionDTO savedReduction =
                reductionService.saveReduction(reductionDTO);

        return new ResponseEntity<>(
                savedReduction,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReductionDTO> getReduction(
            @PathVariable Long id)
            throws ReductionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReductionDTO reduction = reductionService.getReduction(id);

        verifierAccesReduction(reduction);

        return ResponseEntity.ok(reduction);
    }

    // Avant : renvoyait TOUTES les réductions, tous restaurants
    // confondus. Non filtré pour le SUPERADMIN.
    @GetMapping
    public ResponseEntity<List<ReductionDTO>> getAllReductions()
            throws EmployeeNotFoundException {

        List<ReductionDTO> toutes = reductionService.getAllReductions();

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(toutes);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                toutes.stream()
                        .filter(r -> r.getRestaurant() != null
                                && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                        .toList()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReductionDTO> updateReduction(
            @PathVariable Long id,
            @RequestBody ReductionDTO reductionDTO)
            throws ReductionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        // La réduction existante doit appartenir au restaurant connecté
        // (bypass SUPERADMIN)...
        ReductionDTO existante = reductionService.getReduction(id);
        verifierAccesReduction(existante);

        // ...et on ne doit pas pouvoir la "déplacer" vers un autre
        // restaurant.
        reductionDTO.setRestaurant(existante.getRestaurant());

        return ResponseEntity.ok(
                reductionService.updateReduction(
                        id,
                        reductionDTO
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReduction(
            @PathVariable Long id)
            throws ReductionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReductionDTO existante = reductionService.getReduction(id);
        verifierAccesReduction(existante);

        reductionService.deleteReduction(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReductionDTO>> getReductionsByType(
            @PathVariable TypeReduction type)
            throws EmployeeNotFoundException {

        List<ReductionDTO> resultats = reductionService.getReductionsByType(type);

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(r -> r.getRestaurant() != null
                                && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                        .toList()
        );
    }

    @GetMapping("/actives")
    public ResponseEntity<List<ReductionDTO>> getReductionsActives()
            throws EmployeeNotFoundException {

        List<ReductionDTO> resultats = reductionService.getReductionsActives();

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(r -> r.getRestaurant() != null
                                && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                        .toList()
        );
    }

    /**
     * Active manuellement une réduction (bouton "Activer" du menu
     * Réductions).
     */
    @PutMapping("/{id}/activer")
    public ResponseEntity<ReductionDTO> activerReduction(
            @PathVariable Long id)
            throws ReductionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReductionDTO existante = reductionService.getReduction(id);
        verifierAccesReduction(existante);

        return ResponseEntity.ok(
                reductionService.activerReduction(id)
        );
    }

    /**
     * Désactive manuellement une réduction (bouton "Désactiver" du menu
     * Réductions). Une réduction désactivée n'est plus jamais appliquée,
     * automatiquement ou manuellement, tant qu'elle n'est pas réactivée.
     */
    @PutMapping("/{id}/desactiver")
    public ResponseEntity<ReductionDTO> desactiverReduction(
            @PathVariable Long id)
            throws ReductionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReductionDTO existante = reductionService.getReduction(id);
        verifierAccesReduction(existante);

        return ResponseEntity.ok(
                reductionService.desactiverReduction(id)
        );
    }

    @GetMapping("/dates")
    public ResponseEntity<List<ReductionDTO>> getReductionsBetweenDates(
            @RequestParam Date dateDebut,
            @RequestParam Date dateFin)
            throws EmployeeNotFoundException {

        List<ReductionDTO> resultats =
                reductionService.getReductionsBetweenDates(
                        dateDebut,
                        dateFin
                );

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(r -> r.getRestaurant() != null
                                && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                        .toList()
        );
    }

    /**
     * Réductions applicables automatiquement à un panier donné.
     * Utilisé par la caisse / le tunnel de commande pour appliquer
     * les réductions sans intervention manuelle.
     *
     * Exemple : GET /api/reductions/applicables?montant=45.5&produitsIds=1,2,3
     */
    @GetMapping("/applicables")
    public ResponseEntity<List<ReductionDTO>> getReductionsApplicables(
            @RequestParam(required = false) Double montant,
            @RequestParam(required = false) List<Long> produitsIds)
            throws EmployeeNotFoundException {

        List<ReductionDTO> resultats =
                reductionService.getReductionsApplicables(
                        montant,
                        produitsIds
                );

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(r -> r.getRestaurant() != null
                                && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                        .toList()
        );
    }
}
