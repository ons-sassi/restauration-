package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.dtos.SuggestionDTO;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.SuggestionNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.SuggestionService;
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
 * Décision produit retenue (voir entité Suggestion) : privé par
 * restaurant, colonne directe (client restant nullable, pas de
 * scoping indirect fiable) — même pattern que Reduction.
 *
 * Seuls getAllSuggestions (liste), prendreEnCompte et deleteSuggestion
 * sont réellement appelés par le front actuel (écran "Conseils
 * clients"/conseil-list) : le reste (create, update, getById,
 * getSuggestionsByClient, getSuggestionsByPriseEnCompte,
 * searchSuggestions, getSuggestionsByDate) n'a aucun consommateur
 * Angular aujourd'hui — scopé quand même ici par cohérence et parce
 * que rien n'empêche un futur écran de les appeler (même logique que
 * pour Notification en son temps).
 *
 * CORRECTIF (suite à relecture) : aucune gestion du cas SUPERADMIN
 * n'existait ici. Bypass ajouté, même pattern que Reclamation/
 * ClientAuthentifie/Fidelite/PrevisionStock. Pour create, comme pour
 * ClientAuthentifie, le SUPERADMIN n'a lui-même aucun restaurant : le
 * restaurant doit être explicitement fourni dans la requête.
 *
 * AVANT ce correctif : n'importe quel employé, quel que soit son
 * restaurant, pouvait lister/consulter/modifier/supprimer les
 * suggestions clients de n'importe quel autre restaurant.
 */
@RestController
@RequestMapping("/api/suggestions")
@AllArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;
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

    private void verifierAccesSuggestion(SuggestionDTO suggestion)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        if (estSuperAdminConnecte()) {
            return;
        }

        currentUserService.verifierAccesRestaurant(
                suggestion != null && suggestion.getRestaurant() != null
                        ? suggestion.getRestaurant().getId_restaurant()
                        : null
        );
    }


    @PostMapping
    public SuggestionDTO saveSuggestion(
            @RequestBody SuggestionDTO suggestionDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        if (estSuperAdminConnecte()) {

            if (suggestionDTO.getRestaurant() == null
                    || suggestionDTO.getRestaurant().getId_restaurant() == null) {

                throw new AccesRestaurantNonAutoriseException(
                        "Un restaurant doit être précisé pour créer "
                                + "une suggestion en tant que SUPERADMIN.");
            }

        } else {

            // Forcé au restaurant de l'employé connecté (aucun
            // consommateur front actuellement, comme Notification en son
            // temps) : on ne fait pas confiance à un éventuel restaurant
            // envoyé par le front.
            Long restaurantId = currentUserService.getRestaurantIdConnecte();
            RestaurantDTO restaurant = new RestaurantDTO();
            restaurant.setId_restaurant(restaurantId);
            suggestionDTO.setRestaurant(restaurant);
        }

        return suggestionService.saveSuggestion(suggestionDTO);
    }


    @PutMapping("/{id}")
    public SuggestionDTO updateSuggestion(
            @PathVariable Long id,
            @RequestBody SuggestionDTO suggestionDTO)
            throws SuggestionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        SuggestionDTO existante = suggestionService.getSuggestion(id);
        verifierAccesSuggestion(existante);

        // Ne doit pas pouvoir être "déplacée" vers un autre restaurant.
        suggestionDTO.setRestaurant(existante.getRestaurant());

        return suggestionService.updateSuggestion(id, suggestionDTO);
    }


    @DeleteMapping("/{id}")
    public void deleteSuggestion(
            @PathVariable Long id)
            throws SuggestionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        SuggestionDTO existante = suggestionService.getSuggestion(id);
        verifierAccesSuggestion(existante);

        suggestionService.deleteSuggestion(id);
    }


    @GetMapping("/{id}")
    public SuggestionDTO getSuggestion(
            @PathVariable Long id)
            throws SuggestionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        SuggestionDTO suggestion = suggestionService.getSuggestion(id);

        verifierAccesSuggestion(suggestion);

        return suggestion;
    }


    // Avant : renvoyait TOUTES les suggestions, tous restaurants
    // confondus (seul endpoint réellement branché à l'UI actuelle).
    // Non filtré pour le SUPERADMIN.
    @GetMapping
    public List<SuggestionDTO> getAllSuggestions()
            throws EmployeeNotFoundException {

        List<SuggestionDTO> toutes = suggestionService.getAllSuggestions();

        if (estSuperAdminConnecte()) {
            return toutes;
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return toutes.stream()
                .filter(s -> s.getRestaurant() != null
                        && restaurantId.equals(s.getRestaurant().getId_restaurant()))
                .toList();
    }


    @PostMapping("/by-client")
    public List<SuggestionDTO> getSuggestionsByClient(
            @RequestBody ClientAuthentifieDTO clientAuthentifieDTO)
            throws EmployeeNotFoundException {

        List<SuggestionDTO> resultats =
                suggestionService.getSuggestionsByClient(clientAuthentifieDTO);

        if (estSuperAdminConnecte()) {
            return resultats;
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return resultats.stream()
                .filter(s -> s.getRestaurant() != null
                        && restaurantId.equals(s.getRestaurant().getId_restaurant()))
                .toList();
    }


    @GetMapping("/by-prise-en-compte")
    public List<SuggestionDTO> getSuggestionsByPriseEnCompte(
            @RequestParam Boolean value)
            throws EmployeeNotFoundException {

        List<SuggestionDTO> resultats =
                suggestionService.getSuggestionsByPriseEnCompte(value);

        if (estSuperAdminConnecte()) {
            return resultats;
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return resultats.stream()
                .filter(s -> s.getRestaurant() != null
                        && restaurantId.equals(s.getRestaurant().getId_restaurant()))
                .toList();
    }


    @PatchMapping("/{id}/prise-en-compte")
    public SuggestionDTO prendreEnCompte(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") Boolean priseEnCompte)
            throws SuggestionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        SuggestionDTO existante = suggestionService.getSuggestion(id);
        verifierAccesSuggestion(existante);

        return suggestionService.prendreEnCompte(id, priseEnCompte);
    }


    @GetMapping("/search")
    public List<SuggestionDTO> searchSuggestions(
            @RequestParam String contenu)
            throws EmployeeNotFoundException {

        List<SuggestionDTO> resultats =
                suggestionService.searchSuggestions(contenu);

        if (estSuperAdminConnecte()) {
            return resultats;
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return resultats.stream()
                .filter(s -> s.getRestaurant() != null
                        && restaurantId.equals(s.getRestaurant().getId_restaurant()))
                .toList();
    }


    @GetMapping("/by-date")
    public List<SuggestionDTO> getSuggestionsByDate(
            @RequestParam Date dateDebut,
            @RequestParam Date dateFin)
            throws EmployeeNotFoundException {

        List<SuggestionDTO> resultats =
                suggestionService.getSuggestionsByDate(dateDebut, dateFin);

        if (estSuperAdminConnecte()) {
            return resultats;
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return resultats.stream()
                .filter(s -> s.getRestaurant() != null
                        && restaurantId.equals(s.getRestaurant().getId_restaurant()))
                .toList();
    }
}
