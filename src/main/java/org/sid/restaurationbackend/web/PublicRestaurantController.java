package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.enums.StatutRestaurant;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.services.RestaurantService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints publics (sans authentification) pour l'écran client
 * "Choisir un restaurant" (features/client/restaurant-selection).
 *
 * GET /api/restaurants (RestaurantController) est réservé au
 * SUPERADMIN — il ne peut pas servir cet écran. Ce contrôleur expose
 * volontairement un sous-ensemble minimal des champs (nom, adresse,
 * logo) via RestaurantDTO, sans aucune donnée sensible ni relation
 * (pdvs, tables, menu...), et ne liste que les restaurants réellement
 * ouverts au public.
 */
@RestController
@RequestMapping("/api/public/restaurants")
@AllArgsConstructor
public class PublicRestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsPublics() {

        List<RestaurantDTO> restaurants =
                restaurantService.getAllRestaurants()
                        .stream()
                        .filter(r ->
                                r.getStatut() == StatutRestaurant.ACTIF ||
                                        r.getStatut() == StatutRestaurant.OPEN
                        )
                        .toList();

        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDTO> getRestaurantPublic(
            @PathVariable Long id)
            throws RestaurantNotFoundException {

        return ResponseEntity.ok(
                restaurantService.getRestaurant(id)
        );
    }
}
