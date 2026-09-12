package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.PointDeVenteDTO;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.PointDeVenteNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.PointDeVenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * PointDeVente est rattaché directement à un restaurant.
 * Toutes les méthodes de ce contrôleur vérifient désormais que
 * ce restaurant est bien celui de l'employé connecté, au lieu
 * de faire confiance à un restaurantId fourni par le frontend
 * (paramètre de chemin ou champ du DTO), ou de renvoyer les
 * points de vente de tous les restaurants confondus.
 */
@RestController
@RequestMapping("/api/points-de-vente")
@AllArgsConstructor
public class PointDeVenteController {

    private final PointDeVenteService pointDeVenteService;
    private final CurrentUserService currentUserService;


    // Ajouter un point de vente
    @PostMapping
    public ResponseEntity<PointDeVenteDTO> savePointDeVente(
            @RequestBody PointDeVenteDTO pointDeVenteDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                pointDeVenteDTO.getRestaurant() != null
                        ? pointDeVenteDTO.getRestaurant().getId_restaurant()
                        : null
        );

        PointDeVenteDTO savedPointDeVente =
                pointDeVenteService.savePointDeVente(pointDeVenteDTO);

        return new ResponseEntity<>(
                savedPointDeVente,
                HttpStatus.CREATED
        );
    }


    // Récupérer un point de vente par ID
    @GetMapping("/{id}")
    public ResponseEntity<PointDeVenteDTO> getPointDeVente(
            @PathVariable Long id)
            throws PointDeVenteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PointDeVenteDTO pdv = pointDeVenteService.getPointDeVente(id);

        currentUserService.verifierAccesRestaurant(
                pdv.getRestaurant() != null
                        ? pdv.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(pdv);
    }


    // Récupérer tous les points de vente DU RESTAURANT CONNECTÉ
    @GetMapping
    public ResponseEntity<List<PointDeVenteDTO>> getAllPointsDeVente()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<PointDeVenteDTO> pdvs = pointDeVenteService.getAllPointsDeVente()
                .stream()
                .filter(p -> p.getRestaurant() != null
                        && restaurantId.equals(p.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(pdvs);
    }


    // Modifier un point de vente
    //
    // Le point de vente existant doit appartenir au restaurant
    // connecté, et on ne doit pas pouvoir le "déplacer" vers un autre
    // restaurant via le DTO envoyé.
    @PutMapping("/{id}")
    public ResponseEntity<PointDeVenteDTO> updatePointDeVente(
            @PathVariable Long id,
            @RequestBody PointDeVenteDTO pointDeVenteDTO)
            throws PointDeVenteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PointDeVenteDTO existant = pointDeVenteService.getPointDeVente(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        if (pointDeVenteDTO.getRestaurant() != null) {
            currentUserService.verifierAccesRestaurant(
                    pointDeVenteDTO.getRestaurant().getId_restaurant()
            );
        }

        // On s'assure que l'ID de l'URL est utilisé
        pointDeVenteDTO.setId_pdv(id);

        PointDeVenteDTO updatedPointDeVente =
                pointDeVenteService.updatePointDeVente(
                        pointDeVenteDTO
                );

        return ResponseEntity.ok(updatedPointDeVente);
    }


    // Supprimer un point de vente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePointDeVente(
            @PathVariable Long id)
            throws PointDeVenteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        PointDeVenteDTO existant = pointDeVenteService.getPointDeVente(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        pointDeVenteService.deletePointDeVente(id);

        return ResponseEntity.noContent().build();
    }


    // Recherche par nom, DU RESTAURANT CONNECTÉ
    @GetMapping("/search")
    public ResponseEntity<List<PointDeVenteDTO>> searchByNom(
            @RequestParam String name)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<PointDeVenteDTO> pdvs = pointDeVenteService.getPointDeVenteByNom_pdv(name)
                .stream()
                .filter(p -> p.getRestaurant() != null
                        && restaurantId.equals(p.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(pdvs);
    }


    // Points de vente par restaurant
    //
    // Le restaurantId demandé DOIT être celui de l'employé connecté.
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<PointDeVenteDTO>>
    getPointDeVenteByRestaurant(
            @PathVariable Long restaurantId)
            throws RestaurantNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(restaurantId);

        return ResponseEntity.ok(
                pointDeVenteService.getPointDeVenteByRestaurant(
                        restaurantId
                )
        );
    }


    // Points de vente par statut de connexion, DU RESTAURANT CONNECTÉ
    @GetMapping("/statut-connexion/{statutConnexion}")
    public ResponseEntity<List<PointDeVenteDTO>>
    getPointDeVenteByStatutConnexion(
            @PathVariable String statutConnexion)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<PointDeVenteDTO> pdvs = pointDeVenteService
                .getPointDeVenteByStatutConnexion(
                        statutConnexion
                )
                .stream()
                .filter(p -> p.getRestaurant() != null
                        && restaurantId.equals(p.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(pdvs);
    }
}