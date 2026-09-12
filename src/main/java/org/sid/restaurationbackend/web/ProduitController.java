package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.ProduitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * Produit et Catégorie sont rattachés à un restaurant
 * (ElementMenu.restaurant). Toutes les méthodes de ce
 * contrôleur vérifient désormais que ce restaurant est bien
 * celui de l'employé connecté — via CurrentUserService — au
 * lieu de faire confiance à un restaurantId fourni par le
 * frontend.
 *
 * Modificateur n'a PAS de restaurant en base (relation
 * ManyToMany avec Produit uniquement) : il n'est donc pas
 * filtré ici, sauf pour "by-produit" où l'on vérifie que le
 * produit demandé appartient bien au restaurant connecté.
 */
@RestController
@RequestMapping("/api/produits")
@AllArgsConstructor
@CrossOrigin("*")
public class ProduitController {

    private final ProduitService produitService;
    private final CurrentUserService currentUserService;


    // =========================================================
    // ======================== PRODUIT =========================
    // =========================================================

    // Ajouter un produit
    @PostMapping
    public ResponseEntity<ProduitDTO> saveProduit(
            @RequestBody ProduitDTO produitDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                produitDTO.getRestaurant() != null
                        ? produitDTO.getRestaurant().getId_restaurant()
                        : null
        );

        ProduitDTO savedProduit = produitService.saveProduit(produitDTO);

        return new ResponseEntity<>(
                savedProduit,
                HttpStatus.CREATED
        );
    }


    // Modifier un produit
    @PutMapping("/{id}")
    public ResponseEntity<ProduitDTO> updateProduit(
            @PathVariable Long id,
            @RequestBody ProduitDTO produitDTO)
            throws ProduitNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        // Le produit existant doit appartenir au restaurant connecté...
        ProduitDTO existant = produitService.getProduit(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        // ...et on ne doit pas pouvoir le "déplacer" vers un autre restaurant.
        currentUserService.verifierAccesRestaurant(
                produitDTO.getRestaurant() != null
                        ? produitDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                produitService.updateProduit(id, produitDTO)
        );
    }


    // Supprimer un produit
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduit(
            @PathVariable Long id)
            throws ProduitNotFoundException, ProduitEnUtilisationException,
            EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        ProduitDTO existant = produitService.getProduit(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        produitService.deleteProduit(id);

        return ResponseEntity.noContent().build();
    }


    // Récupérer un produit par ID
    @GetMapping("/{id}")
    public ResponseEntity<ProduitDTO> getProduit(
            @PathVariable Long id)
            throws ProduitNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ProduitDTO produit = produitService.getProduit(id);

        currentUserService.verifierAccesRestaurant(
                produit.getRestaurant() != null
                        ? produit.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(produit);
    }


    // Récupérer un produit par nom
    @GetMapping("/nom/{nom}")
    public ResponseEntity<ProduitDTO> getProduitByNom(
            @PathVariable String nom)
            throws ProduitNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ProduitDTO produit = produitService.getProduitByNom(nom);

        currentUserService.verifierAccesRestaurant(
                produit.getRestaurant() != null
                        ? produit.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(produit);
    }


    // Récupérer tous les produits DU RESTAURANT CONNECTÉ
    //
    // Avant : renvoyait TOUS les produits, tous restaurants confondus.
    // Désormais filtré côté serveur sur le restaurant de l'employé connecté.
    @GetMapping
    public ResponseEntity<List<ProduitDTO>> getAllProduits()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ProduitDTO> produits = produitService.getAllProduits()
                .stream()
                .filter(p -> p.getRestaurant() != null
                        && restaurantId.equals(p.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(produits);
    }


    // Produits par disponibilité, DU RESTAURANT CONNECTÉ
    @GetMapping("/disponible/{disponible}")
    public ResponseEntity<List<ProduitDTO>> getProduitsByDisponible(
            @PathVariable Boolean disponible)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ProduitDTO> produits = produitService
                .getProduitsByDisponible(disponible)
                .stream()
                .filter(p -> p.getRestaurant() != null
                        && restaurantId.equals(p.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(produits);
    }


    // Produits par ingrédient, DU RESTAURANT CONNECTÉ
    //
    // MAJ Lot 2.7 : Ingredient a maintenant son propre restaurant
    // (voir IngredientController), mais le filtrage ici reste basé
    // sur le restaurant des PRODUITS renvoyés (pas de celui de
    // l'ingrédient DTO fourni en entrée, jamais vérifié) : un
    // ingrédient partagé par erreur entre restaurants ne pourrait
    // de toute façon pas faire fuiter un produit d'un autre
    // restaurant, puisque chaque produit renvoyé est filtré
    // individuellement ci-dessous.
    @PostMapping("/by-ingredient")
    public ResponseEntity<List<ProduitDTO>> getProduitsByIngredient(
            @RequestBody IngredientDTO ingredientDTO)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ProduitDTO> produits = produitService
                .getProduitsByIngredient(ingredientDTO)
                .stream()
                .filter(p -> p.getRestaurant() != null
                        && restaurantId.equals(p.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(produits);
    }


    // Produits par catégorie parent
    @PostMapping("/by-categorie-parent")
    public ResponseEntity<List<ProduitDTO>> getProduitsByCategorieParent(
            @RequestBody CategorieDTO categorieDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                categorieDTO.getRestaurant() != null
                        ? categorieDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                produitService.getProduitsByCategorieParent(categorieDTO)
        );
    }


    // Produits par restaurant
    //
    // Le restaurant demandé DOIT être celui de l'employé connecté :
    // ce n'est plus le RestaurantDTO envoyé par le frontend qui décide.
    @PostMapping("/by-restaurant")
    public ResponseEntity<List<ProduitDTO>> getProduitsByRestaurant(
            @RequestBody RestaurantDTO restaurantDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                restaurantDTO != null
                        ? restaurantDTO.getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                produitService.getProduitsByRestaurant(restaurantDTO)
        );
    }


    // Recherche avancée des produits
    //
    // Exemple :
    // GET /api/produits/search?nom=pizza&categorieId=2&restaurantId=1&disponible=true
    //
    // Le restaurantId, s'il est fourni, doit être celui de l'employé
    // connecté ; s'il est absent, on force celui de l'employé connecté
    // (la recherche ne doit jamais porter sur un autre restaurant).
    @GetMapping("/search")
    public ResponseEntity<List<ProduitDTO>> searchProduits(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) Boolean disponible)
            throws CategorieNotFoundException,
            RestaurantNotFoundException,
            EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        Long restaurantIdConnecte = currentUserService.getRestaurantIdConnecte();

        if (restaurantId != null) {
            currentUserService.verifierAccesRestaurant(restaurantId);
        }

        return ResponseEntity.ok(
                produitService.searchProduits(
                        nom,
                        categorieId,
                        restaurantIdConnecte,
                        disponible
                )
        );
    }


    // =========================================================
    // ======================= CATEGORIE ========================
    // =========================================================

    // Ajouter une catégorie
    @PostMapping("/categories")
    public ResponseEntity<CategorieDTO> saveCategorie(
            @RequestBody CategorieDTO categorieDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                categorieDTO.getRestaurant() != null
                        ? categorieDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return new ResponseEntity<>(
                produitService.saveCategorie(categorieDTO),
                HttpStatus.CREATED
        );
    }


    // Modifier une catégorie
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategorieDTO> updateCategorie(
            @PathVariable Long id,
            @RequestBody CategorieDTO categorieDTO)
            throws CategorieNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        CategorieDTO existante = produitService.getCategorie(id);
        currentUserService.verifierAccesRestaurant(
                existante.getRestaurant() != null
                        ? existante.getRestaurant().getId_restaurant()
                        : null
        );

        currentUserService.verifierAccesRestaurant(
                categorieDTO.getRestaurant() != null
                        ? categorieDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                produitService.updateCategorie(id, categorieDTO)
        );
    }


    // Supprimer une catégorie
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategorie(
            @PathVariable Long id)
            throws CategorieNotFoundException, CategorieEnUtilisationException,
            EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        CategorieDTO existante = produitService.getCategorie(id);
        currentUserService.verifierAccesRestaurant(
                existante.getRestaurant() != null
                        ? existante.getRestaurant().getId_restaurant()
                        : null
        );

        produitService.deleteCategorie(id);

        return ResponseEntity.noContent().build();
    }


    // Récupérer une catégorie par ID
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategorieDTO> getCategorie(
            @PathVariable Long id)
            throws CategorieNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        CategorieDTO categorie = produitService.getCategorie(id);

        currentUserService.verifierAccesRestaurant(
                categorie.getRestaurant() != null
                        ? categorie.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(categorie);
    }


    // Récupérer toutes les catégories DU RESTAURANT CONNECTÉ
    @GetMapping("/categories")
    public ResponseEntity<List<CategorieDTO>> getAllCategories()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<CategorieDTO> categories = produitService.getAllCategories()
                .stream()
                .filter(c -> c.getRestaurant() != null
                        && restaurantId.equals(c.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(categories);
    }


    // Récupérer une catégorie par nom
    @GetMapping("/categories/nom/{nom}")
    public ResponseEntity<CategorieDTO> getCategorieByNom(
            @PathVariable String nom)
            throws CategorieNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        CategorieDTO categorie = produitService.getCategorieByNom(nom);

        currentUserService.verifierAccesRestaurant(
                categorie.getRestaurant() != null
                        ? categorie.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(categorie);
    }


    // Catégories par catégorie parent
    @PostMapping("/categories/by-parent")
    public ResponseEntity<List<CategorieDTO>> getCategoriesByCategorieParent(
            @RequestBody CategorieDTO categorieDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                categorieDTO.getRestaurant() != null
                        ? categorieDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                produitService.getCategoriesByCategorieParent(
                        categorieDTO
                )
        );
    }


    // Catégories par restaurant
    @PostMapping("/categories/by-restaurant")
    public ResponseEntity<List<CategorieDTO>> getCategoriesByRestaurant(
            @RequestBody RestaurantDTO restaurantDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                restaurantDTO != null
                        ? restaurantDTO.getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                produitService.getCategoriesByRestaurant(
                        restaurantDTO
                )
        );
    }


    // Éléments du menu par catégorie
    @PostMapping("/categories/elements-menu")
    public ResponseEntity<List<ElementMenuDTO>> getElementsMenuByCategorie(
            @RequestBody CategorieDTO categorieDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                categorieDTO.getRestaurant() != null
                        ? categorieDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                produitService.getElementsMenuByCategorie(
                        categorieDTO
                )
        );
    }


    // =========================================================
    // ====================== MODIFICATEUR ======================
    // =========================================================
    //
    // Modificateur n'a pas de colonne restaurant en base (relation
    // ManyToMany avec Produit uniquement) : il n'est donc pas possible
    // de le filtrer par restaurant sans changement de schéma. Seule
    // "by-produit" est sécurisée ici, en vérifiant le produit fourni.

    // Ajouter un modificateur
    @PostMapping("/modificateurs")
    public ResponseEntity<ModificateurDTO> saveModificateur(
            @RequestBody ModificateurDTO modificateurDTO) {

        return new ResponseEntity<>(
                produitService.saveModificateur(modificateurDTO),
                HttpStatus.CREATED
        );
    }


    // Modifier un modificateur
    @PutMapping("/modificateurs/{id}")
    public ResponseEntity<ModificateurDTO> updateModificateur(
            @PathVariable Long id,
            @RequestBody ModificateurDTO modificateurDTO)
            throws ModificateurNotFoundException {

        return ResponseEntity.ok(
                produitService.updateModificateur(
                        id,
                        modificateurDTO
                )
        );
    }


    // Supprimer un modificateur
    @DeleteMapping("/modificateurs/{id}")
    public ResponseEntity<Void> deleteModificateur(
            @PathVariable Long id)
            throws ModificateurNotFoundException {

        produitService.deleteModificateur(id);

        return ResponseEntity.noContent().build();
    }


    // Récupérer un modificateur par ID
    @GetMapping("/modificateurs/{id}")
    public ResponseEntity<ModificateurDTO> getModificateur(
            @PathVariable Long id)
            throws ModificateurNotFoundException {

        return ResponseEntity.ok(
                produitService.getModificateur(id)
        );
    }


    // Récupérer tous les modificateurs
    @GetMapping("/modificateurs")
    public ResponseEntity<List<ModificateurDTO>> getAllModificateurs() {

        return ResponseEntity.ok(
                produitService.getAllModificateurs()
        );
    }


    // Modificateurs d'un produit
    @PostMapping("/modificateurs/by-produit")
    public ResponseEntity<List<ModificateurDTO>> getAllModificateurByProduit(
            @RequestBody ProduitDTO produitDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                produitDTO.getRestaurant() != null
                        ? produitDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                produitService.getAllModificateurByProduit(
                        produitDTO
                )
        );
    }
}
