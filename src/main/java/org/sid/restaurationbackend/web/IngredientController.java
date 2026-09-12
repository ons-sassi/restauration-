package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sid.restaurationbackend.dtos.IngredientDTO;

import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.IngredientNotFoundException;

import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.IngredientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Predicate;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — Lot 2.7
 * =========================================================
 * Décision produit retenue (voir entité Ingredient) : privé par
 * restaurant, colonne DIRECTE (indépendante de "fournisseur", qui
 * reste nullable). Même pattern que FournisseurController/
 * ProduitController.
 *
 * AVANT ce correctif : contrôleur intégralement ouvert — les niveaux
 * de stock (quantite_stock, seuil_alerte, dates de péremption...) de
 * n'importe quel restaurant étaient lisibles et modifiables par
 * n'importe quel employé authentifié.
 */
@RequestMapping("/api/ingredients")
@RestController
@Slf4j
@AllArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;
    private final CurrentUserService currentUserService;

    // Filtre réutilisé par tous les endpoints "liste" ci-dessous.
    private Predicate<IngredientDTO> duRestaurant(Long restaurantId) {
        return i -> i.getRestaurant() != null
                && restaurantId.equals(i.getRestaurant().getId_restaurant());
    }

    @GetMapping("/{id}")
    public IngredientDTO getIngredient(@PathVariable Long id)
            throws IngredientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        IngredientDTO ingredient = ingredientService.getIngredient(id);

        currentUserService.verifierAccesRestaurant(
                ingredient.getRestaurant() != null
                        ? ingredient.getRestaurant().getId_restaurant()
                        : null
        );

        return ingredient;
    }

    // Avant : renvoyait TOUS les ingrédients, tous restaurants
    // confondus (niveaux de stock inclus).
    @GetMapping
    public List<IngredientDTO> getIngredients()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ingredientService.getAllIngredients()
                .stream()
                .filter(duRestaurant(restaurantId))
                .toList();
    }

    // Même remarque : recherche non filtrée avant ce correctif.
    @GetMapping("/search")
    public List<IngredientDTO> searchIngredients(
            @RequestParam(required = false) String nomIngredient,
            @RequestParam(required = false) String nomFournisseur,
            @RequestParam(required = false) String nomProduit)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ingredientService.searchIngredients(
                        nomIngredient, nomFournisseur, nomProduit)
                .stream()
                .filter(duRestaurant(restaurantId))
                .toList();
    }

    @PostMapping("/ajouter")
    public IngredientDTO ajouterIngredient(
            @RequestBody IngredientDTO ingredientDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                ingredientDTO.getRestaurant() != null
                        ? ingredientDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return ingredientService.saveIngredient(ingredientDTO);
    }

    @PutMapping("/modifier/{id}")
    public IngredientDTO modifierIngredient(
            @PathVariable Long id,
            @RequestBody IngredientDTO ingredientDTO)
            throws IngredientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        IngredientDTO existant = ingredientService.getIngredient(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        currentUserService.verifierAccesRestaurant(
                ingredientDTO.getRestaurant() != null
                        ? ingredientDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return ingredientService.updateIngredient(id, ingredientDTO);
    }

    @DeleteMapping("/delete/{id}")
    public void deleterIngredient(@PathVariable Long id)
            throws IngredientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        IngredientDTO existant = ingredientService.getIngredient(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        ingredientService.deleteIngredient(id);
    }

    // Avant : pas de filtre, tous restaurants confondus.
    @GetMapping("/under-seuil")
    public List<IngredientDTO> getIngredientsUnderSeuil()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ingredientService.underSeuil()
                .stream()
                .filter(duRestaurant(restaurantId))
                .toList();
    }

    // Avant : pas de filtre, tous restaurants confondus.
    @GetMapping("/pres-de-peremption")
    public List<IngredientDTO> getIngredientsPresDePeremption(
            @RequestParam Integer nbrJourRestant)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ingredientService.presDePeremption(nbrJourRestant)
                .stream()
                .filter(duRestaurant(restaurantId))
                .toList();
    }

    // Avant : pas de filtre, tous restaurants confondus.
    @GetMapping("/perimes")
    public List<IngredientDTO> getIngredientsPerimes()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ingredientService.getIngredientsPerimes()
                .stream()
                .filter(duRestaurant(restaurantId))
                .toList();
    }
}
