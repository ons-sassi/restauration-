package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.FournisseurDTO;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.FournisseurNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.FournisseurService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — Lot 2.7
 * =========================================================
 * Décision produit retenue (voir entité Fournisseur) : privé par
 * restaurant, pas un catalogue partagé. Même pattern que
 * ProduitController : le frontend continue d'envoyer un restaurant
 * dans le FournisseurDTO (create/update), mais il est désormais
 * VÉRIFIÉ contre le restaurant réel de l'employé connecté — jamais
 * fait confiance tel quel. Les listes (getAll/search) sont filtrées
 * côté serveur sur ce même restaurant.
 *
 * AVANT ce correctif : contrôleur intégralement ouvert, aucune
 * vérification nulle part — tout employé authentifié, quel que soit
 * son restaurant, pouvait lire/modifier/supprimer les fournisseurs de
 * n'importe quel autre restaurant.
 */
@RequestMapping("/api/fournisseurs")
@RestController
@Slf4j
@AllArgsConstructor
public class FournisseurController {

    private final FournisseurService fournisseurService;
    private final CurrentUserService currentUserService;

    @GetMapping("/{id}")
    public FournisseurDTO getFournisseur(@PathVariable Long id)
            throws FournisseurNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        FournisseurDTO fournisseur = fournisseurService.getFournisseur(id);

        currentUserService.verifierAccesRestaurant(
                fournisseur.getRestaurant() != null
                        ? fournisseur.getRestaurant().getId_restaurant()
                        : null
        );

        return fournisseur;
    }

    // Avant : renvoyait TOUS les fournisseurs, tous restaurants
    // confondus. Filtré maintenant sur le restaurant connecté.
    @GetMapping
    public List<FournisseurDTO> getFournisseurs()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return fournisseurService.listFournisseurs()
                .stream()
                .filter(f -> f.getRestaurant() != null
                        && restaurantId.equals(f.getRestaurant().getId_restaurant()))
                .toList();
    }

    // Même remarque : la recherche portait avant sur TOUS les
    // fournisseurs de la plateforme. Filtrée maintenant comme
    // getFournisseurs() ci-dessus.
    @GetMapping("/search")
    public List<FournisseurDTO> searchFournisseurs(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String num)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return fournisseurService.searchFournisseurs(nom, adresse, email, num)
                .stream()
                .filter(f -> f.getRestaurant() != null
                        && restaurantId.equals(f.getRestaurant().getId_restaurant()))
                .toList();
    }

    @PostMapping("/ajouter")
    public FournisseurDTO ajouterFournisseur(
            @RequestBody FournisseurDTO fournisseurDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                fournisseurDTO.getRestaurant() != null
                        ? fournisseurDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return fournisseurService.saveFournisseur(fournisseurDTO);
    }

    @PutMapping("/modifier/{id}")
    public FournisseurDTO modifierFournisseur(
            @PathVariable Long id,
            @RequestBody FournisseurDTO fournisseurDTO)
            throws FournisseurNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        // Le fournisseur existant doit appartenir au restaurant connecté...
        FournisseurDTO existant = fournisseurService.getFournisseur(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        // ...et on ne doit pas pouvoir le "déplacer" vers un autre restaurant.
        currentUserService.verifierAccesRestaurant(
                fournisseurDTO.getRestaurant() != null
                        ? fournisseurDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return fournisseurService.updateFournisseur(id, fournisseurDTO);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteFournisseur(@PathVariable Long id)
            throws FournisseurNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        FournisseurDTO existant = fournisseurService.getFournisseur(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        fournisseurService.deleteFournisseur(id);
    }
}
