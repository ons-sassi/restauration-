package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.ModeleRecuDTO;
import org.sid.restaurationbackend.dtos.ParametreCompteDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.ModeleRecuNotFoundException;
import org.sid.restaurationbackend.exceptions.ParametreCompteNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.ParametreCompteService;
import org.sid.restaurationbackend.services.RestaurantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * ParametreCompte et ModeleRecu sont rattachés directement à
 * un restaurant. Toutes les méthodes de ce contrôleur
 * vérifient désormais que ce restaurant est bien celui de
 * l'employé connecté — via CurrentUserService — au lieu de
 * faire confiance à un restaurantId fourni par le frontend
 * (paramètre de chemin ou champ du DTO), ou de renvoyer les
 * données de tous les restaurants confondus.
 */
@RestController
@RequestMapping("/api/parametres-compte")
@AllArgsConstructor
@Slf4j
public class ParametreCompteController {


    private final ParametreCompteService parametreCompteService;
    private final RestaurantService restaurantService;
    private final CurrentUserService currentUserService;


    // Ajouter un paramètre de compte
    @PostMapping
    public ParametreCompteDTO saveParametreCompte(
            @RequestBody ParametreCompteDTO parametreCompteDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                parametreCompteDTO.getRestaurant() != null
                        ? parametreCompteDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return parametreCompteService
                .saveParametreCompte(parametreCompteDTO);
    }


    // Récupérer tous les paramètres de compte DU RESTAURANT CONNECTÉ
    @GetMapping
    public List<ParametreCompteDTO> getAllParametreComptes()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return parametreCompteService
                .getAllParametreComptes()
                .stream()
                .filter(p -> p.getRestaurant() != null
                        && restaurantId.equals(p.getRestaurant().getId_restaurant()))
                .toList();
    }


    // Récupérer un paramètre de compte par ID
    @GetMapping("/{id}")
    public ParametreCompteDTO getParametreCompte(
            @PathVariable Long id)
            throws ParametreCompteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ParametreCompteDTO parametre = parametreCompteService
                .getParametreCompteById(id);

        currentUserService.verifierAccesRestaurant(
                parametre.getRestaurant() != null
                        ? parametre.getRestaurant().getId_restaurant()
                        : null
        );

        return parametre;
    }


    // Modifier un paramètre de compte
    //
    // Le paramètre existant doit appartenir au restaurant connecté, et
    // on ne doit pas pouvoir le "déplacer" vers un autre restaurant via
    // le DTO envoyé.
    @PutMapping("/{id}")
    public ParametreCompteDTO updateParametreCompte(
            @PathVariable Long id,
            @RequestBody ParametreCompteDTO parametreCompteDTO)
            throws ParametreCompteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ParametreCompteDTO existant = parametreCompteService
                .getParametreCompteById(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        if (parametreCompteDTO.getRestaurant() != null) {
            currentUserService.verifierAccesRestaurant(
                    parametreCompteDTO.getRestaurant().getId_restaurant()
            );
        }

        return parametreCompteService
                .updateParametreCompte(
                        id,
                        parametreCompteDTO
                );
    }


    // Supprimer un paramètre de compte
    @DeleteMapping("/{id}")
    public void deleteParametreCompte(
            @PathVariable Long id)
            throws ParametreCompteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ParametreCompteDTO existant = parametreCompteService
                .getParametreCompteById(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        parametreCompteService
                .deleteParametreCompte(id);
    }


    // Paramètres de compte par restaurant
    //
    // Le restaurantId demandé DOIT être celui de l'employé connecté.
    @GetMapping("/restaurant/{restaurantId}")
    public List<ParametreCompteDTO>
    getParametresByRestaurant(
            @PathVariable Long restaurantId)
            throws RestaurantNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(restaurantId);

        RestaurantDTO restaurant =
                restaurantService.getRestaurant(restaurantId);

        return parametreCompteService
                .getAllParametreComptesByRestaurant(
                        restaurant
                );
    }


    // Ajouter un modèle de reçu
    @PostMapping("/modeles-recus")
    public ModeleRecuDTO saveModeleRecu(
            @RequestBody ModeleRecuDTO modeleRecuDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                modeleRecuDTO.getRestaurant() != null
                        ? modeleRecuDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return parametreCompteService
                .saveModelRecu(modeleRecuDTO);
    }


    // Récupérer tous les modèles de reçus DU RESTAURANT CONNECTÉ
    @GetMapping("/modeles-recus")
    public List<ModeleRecuDTO> getAllModeleRecus()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return parametreCompteService
                .getAllModeleRecus()
                .stream()
                .filter(m -> m.getRestaurant() != null
                        && restaurantId.equals(m.getRestaurant().getId_restaurant()))
                .toList();
    }


    // Récupérer un modèle de reçu par ID
    @GetMapping("/modeles-recus/{id}")
    public ModeleRecuDTO getModeleRecu(
            @PathVariable Long id)
            throws ModeleRecuNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ModeleRecuDTO modele = parametreCompteService
                .getModeleRecuById(id);

        currentUserService.verifierAccesRestaurant(
                modele.getRestaurant() != null
                        ? modele.getRestaurant().getId_restaurant()
                        : null
        );

        return modele;
    }


    // Modifier un modèle de reçu
    @PutMapping("/modeles-recus/{id}")
    public ModeleRecuDTO updateModeleRecu(
            @PathVariable Long id,
            @RequestBody ModeleRecuDTO modeleRecuDTO)
            throws ModeleRecuNotFoundException, ParametreCompteNotFoundException,
            EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        ModeleRecuDTO existant = parametreCompteService
                .getModeleRecuById(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        if (modeleRecuDTO.getRestaurant() != null) {
            currentUserService.verifierAccesRestaurant(
                    modeleRecuDTO.getRestaurant().getId_restaurant()
            );
        }

        return parametreCompteService
                .updateModeleRecu(
                        id,
                        modeleRecuDTO
                );
    }


    // Supprimer un modèle de reçu
    @DeleteMapping("/modeles-recus/{id}")
    public void deleteModeleRecu(
            @PathVariable Long id)
            throws ModeleRecuNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ModeleRecuDTO existant = parametreCompteService
                .getModeleRecuById(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        parametreCompteService
                .deleteModeleRecu(id);
    }


    // Modèles de reçus par restaurant
    //
    // Le restaurantId demandé DOIT être celui de l'employé connecté.
    @GetMapping("/modeles-recus/restaurant/{restaurantId}")
    public List<ModeleRecuDTO> getModelesRecusByRestaurant(
            @PathVariable Long restaurantId)
            throws RestaurantNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(restaurantId);

        RestaurantDTO restaurant =
                restaurantService.getRestaurant(restaurantId);

        return parametreCompteService
                .getAllModelRecusByRestaurant(
                        restaurant
                );
    }


    // Recherche de modèles de reçus, DU RESTAURANT CONNECTÉ
    @GetMapping("/modeles-recus/search")
    public List<ModeleRecuDTO> searchModelesRecus(
            @RequestParam(required = false) String keyword)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return parametreCompteService
                .searchModelesRecus(keyword)
                .stream()
                .filter(m -> m.getRestaurant() != null
                        && restaurantId.equals(m.getRestaurant().getId_restaurant()))
                .toList();
    }


    // Recherche de modèles de reçus par restaurant
    //
    // Le restaurantId demandé DOIT être celui de l'employé connecté.
    @GetMapping("/modeles-recus/restaurant/{restaurantId}/search")
    public List<ModeleRecuDTO> searchModelesRecusByRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String keyword)
            throws RestaurantNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(restaurantId);

        RestaurantDTO restaurant =
                restaurantService.getRestaurant(restaurantId);

        return parametreCompteService
                .searchModelesRecusByRestaurant(
                        restaurant,
                        keyword
                );
    }
}