package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.IngredientDTO;
import org.sid.restaurationbackend.dtos.PrevisionStockDTO;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.IngredientNotFoundException;
import org.sid.restaurationbackend.exceptions.PrevisionStockNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.IngredientService;
import org.sid.restaurationbackend.services.PrevisionStockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — Lot 2.8
 * (dernier morceau de la famille Stock, cf. Reduction/Suggestion/
 * Fidelite/ClientAuthentifie faits avant dans ce même lot)
 * =========================================================
 * PrevisionStock n'a PAS de colonne restaurant en base, et n'en a
 * pas besoin : décision produit tranchée comme pour BonDeCommandeStock
 * (Lot 2.7) — le scoping est INDIRECT via PrevisionStock.ingredient
 * .restaurant, colonne déjà NOT NULL sur Ingredient depuis ce même
 * lot. Même logique que ReclamationController (indirect via
 * Commande), dupliquée ici volontairement pour rester cohérent avec
 * le reste du fichier (pas de helper partagé dans ce projet).
 *
 * CAS LIMITE (ingredient == null) : le champ PrevisionStock.ingredient
 * est nullable en base (pas de nullable=false sur la relation).
 * Sans ingredient, aucun moyen de déterminer le restaurant légitime.
 * Même décision conservatrice que pour Reclamation : on ferme (403)
 * pour un employé classique, seul le SUPERADMIN peut agir dessus.
 *
 * ⚠️ Aucun consommateur Angular actuel pour ce contrôleur
 * (PrevisionStockService existe côté frontend mais n'est appelé
 * par aucun composant) — scopé quand même par cohérence, comme
 * Suggestion en son temps.
 */
@RestController
@RequestMapping("/api/previsions-stock")
@AllArgsConstructor
public class PrevisionStockController {

    private final PrevisionStockService previsionStockService;
    private final IngredientService ingredientService;
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

    private Long restaurantIdDeIngredient(IngredientDTO ingredient) {

        if (ingredient == null || ingredient.getRestaurant() == null) {
            return null;
        }

        return ingredient.getRestaurant().getId_restaurant();
    }

    // À utiliser sur une PrevisionStockDTO déjà rechargée depuis le
    // service (jamais sur un objet envoyé tel quel par le frontend).
    // Bypass total pour le SUPERADMIN. Ferme (403) si l'ingrédient
    // est absent : voir javadoc de la classe (cas limite).
    private void verifierAccesPrevisionStock(PrevisionStockDTO previsionStock)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        if (estSuperAdminConnecte()) {
            return;
        }

        Long restaurantId = previsionStock != null
                ? restaurantIdDeIngredient(previsionStock.getIngredient())
                : null;

        if (restaurantId == null) {
            throw new AccesRestaurantNonAutoriseException(
                    "Prévision de stock non rattachée à un restaurant "
                            + "déterminable (ingrédient absent) : "
                            + "réservé au super-admin.");
        }

        currentUserService.verifierAccesRestaurant(restaurantId);
    }

    // Recharge le VRAI ingrédient depuis son id (jamais depuis le
    // sous-objet "ingredient" envoyé dans le corps d'une requête par
    // le frontend, qui pourrait être falsifié), vérifie son
    // restaurant, et le renvoie pour être utilisé à la place de
    // l'objet fourni.
    private IngredientDTO chargerIngredientAutorise(Long ingredientId)
            throws IngredientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        if (ingredientId == null) {
            throw new IngredientNotFoundException("Ingredient not found");
        }

        IngredientDTO ingredient = ingredientService.getIngredient(ingredientId);

        if (!estSuperAdminConnecte()) {
            currentUserService.verifierAccesRestaurant(
                    restaurantIdDeIngredient(ingredient));
        }

        return ingredient;
    }


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Pas de vérification de restaurant si aucun ingrédient n'est
     * fourni : même choix que ReclamationController.saveReclamation
     * (pas de changement fonctionnel au-delà du scoping — une
     * prévision sans ingrédient reste possible comme avant, elle
     * tombera simplement dans le cas limite décrit dans la javadoc
     * de la classe : orpheline, réservée au SUPERADMIN ensuite). Si
     * un ingrédient est fourni, on recharge la VRAIE entité depuis
     * son id et on vérifie qu'elle appartient au restaurant connecté
     * — jamais le sous-objet "ingredient" tel qu'envoyé par le front.
     */
    @PostMapping
    public ResponseEntity<PrevisionStockDTO> savePrevisionStock(
            @RequestBody PrevisionStockDTO previsionStockDTO)
            throws IngredientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        if (previsionStockDTO.getIngredient() != null
                && previsionStockDTO.getIngredient().getId_ingredient() != null) {

            previsionStockDTO.setIngredient(
                    chargerIngredientAutorise(
                            previsionStockDTO.getIngredient().getId_ingredient()));
        }

        PrevisionStockDTO savedPrevisionStock =
                previsionStockService.savePrevisionStock(
                        previsionStockDTO
                );

        return new ResponseEntity<>(
                savedPrevisionStock,
                HttpStatus.CREATED
        );
    }




    @GetMapping("/{id}")
    public ResponseEntity<PrevisionStockDTO> getPrevisionStock(
            @PathVariable Long id)
            throws PrevisionStockNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PrevisionStockDTO previsionStock =
                previsionStockService.getPrevisionStock(id);

        verifierAccesPrevisionStock(previsionStock);

        return ResponseEntity.ok(previsionStock);
    }



    // Toutes les prévisions DU RESTAURANT CONNECTÉ (ou toutes, pour
    // le SUPERADMIN). Avant : renvoyait les prévisions de tous les
    // restaurants confondus, y compris les orphelines sans ingrédient.
    @GetMapping
    public ResponseEntity<List<PrevisionStockDTO>>
    listPrevisionStocks()
            throws EmployeeNotFoundException {

        List<PrevisionStockDTO> toutes =
                previsionStockService.listPrevisionStocks();

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(toutes);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                toutes.stream()
                        .filter(p -> restaurantId.equals(
                                restaurantIdDeIngredient(p.getIngredient())))
                        .toList()
        );
    }



    /**
     * Si le corps de la requête rattache la prévision à un AUTRE
     * ingrédient, ce nouvel ingrédient doit lui aussi appartenir au
     * restaurant connecté (jamais suivi aveuglément).
     */
    @PutMapping("update/{id}")
    public ResponseEntity<PrevisionStockDTO> updatePrevisionStock(
            @PathVariable Long id,
            @RequestBody PrevisionStockDTO previsionStockDTO)
            throws PrevisionStockNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException, IngredientNotFoundException {

        PrevisionStockDTO existante = previsionStockService.getPrevisionStock(id);
        verifierAccesPrevisionStock(existante);

        Long ancienIngredientId = existante.getIngredient() != null
                ? existante.getIngredient().getId_ingredient()
                : null;

        if (previsionStockDTO.getIngredient() != null
                && previsionStockDTO.getIngredient().getId_ingredient() != null
                && !Objects.equals(
                previsionStockDTO.getIngredient().getId_ingredient(),
                ancienIngredientId)) {

            previsionStockDTO.setIngredient(
                    chargerIngredientAutorise(
                            previsionStockDTO.getIngredient().getId_ingredient()));
        } else {
            // Pas de changement d'ingrédient : on réapplique le vrai
            // ingrédient existant, pour ne jamais persister le
            // sous-objet potentiellement falsifié envoyé par le
            // frontend.
            previsionStockDTO.setIngredient(existante.getIngredient());
        }

        return ResponseEntity.ok(
                previsionStockService.updatePrevisionStock(
                        id,
                        previsionStockDTO
                )
        );
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrevisionStock(
            @PathVariable Long id)
            throws PrevisionStockNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PrevisionStockDTO existante = previsionStockService.getPrevisionStock(id);
        verifierAccesPrevisionStock(existante);

        previsionStockService.deletePrevisionStock(id);

        return ResponseEntity.noContent().build();
    }



    // Globale (tous restaurants confondus côté service) : filtrée ici
    // par restaurant connecté, sauf pour le SUPERADMIN.
    @GetMapping("/periode/{periode}")
    public ResponseEntity<List<PrevisionStockDTO>>
    listPrevisionStocksByDate(
            @PathVariable String periode)
            throws EmployeeNotFoundException {

        List<PrevisionStockDTO> resultats =
                previsionStockService.listPrevisionStocksByDate(
                        periode
                );

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(p -> restaurantId.equals(
                                restaurantIdDeIngredient(p.getIngredient())))
                        .toList()
        );
    }



    // IDOR corrigé : avant, l'IngredientDTO envoyé par le frontend
    // (id_ingredient arbitraire, voire un objet sans id du tout) était
    // utilisé tel quel pour la requête, sans vérifier que cet
    // ingrédient appartenait au restaurant connecté. Le vrai
    // ingrédient est maintenant rechargé depuis son id et vérifié
    // avant d'être utilisé.
    @PostMapping("/ingredient")
    public ResponseEntity<List<PrevisionStockDTO>>
    listPrevisionStocksByIngredient(
            @RequestBody IngredientDTO ingredientDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException,
            IngredientNotFoundException {

        IngredientDTO ingredientAutorise = chargerIngredientAutorise(
                ingredientDTO != null ? ingredientDTO.getId_ingredient() : null);

        return ResponseEntity.ok(
                previsionStockService.listPrevisionStocksByIngredient(
                        ingredientAutorise
                )
        );
    }




    // Globale (tous restaurants confondus côté service) : filtrée ici
    // par restaurant connecté, sauf pour le SUPERADMIN.
    @GetMapping("/basee-sur-ventes/{baseeSurVentes}")
    public ResponseEntity<List<PrevisionStockDTO>>
    listPrevisionStocksByBaseeSurVentes(
            @PathVariable Boolean baseeSurVentes)
            throws EmployeeNotFoundException {

        List<PrevisionStockDTO> resultats =
                previsionStockService
                        .listPrevisionStocksByBaseeSurVentes(
                                baseeSurVentes
                        );

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(p -> restaurantId.equals(
                                restaurantIdDeIngredient(p.getIngredient())))
                        .toList()
        );
    }
}
