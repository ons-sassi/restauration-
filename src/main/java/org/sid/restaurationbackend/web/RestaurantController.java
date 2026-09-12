package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.entities.Restaurant;
import org.sid.restaurationbackend.enums.Devise;
import org.sid.restaurationbackend.enums.StatutRestaurant;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.repositories.RestaurantRepository;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.FileStorageService;
import org.sid.restaurationbackend.services.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * =============================================================
 * SÉCURITÉ MULTI-RESTAURANT — RESTAURANTCONTROLLER (solution 1)
 * =============================================================
 *
 * AVANT : aucune vérification. N'importe quel Employee authentifié
 * pouvait créer, lister tous les restaurants, consulter/modifier/
 * supprimer un restaurant qui n'était pas le sien. C'était le bug
 * le plus grave des 34 contrôleurs du chantier de sécurité
 * multi-tenant (les autres laissaient fuiter des données ; celui-ci
 * permettait de supprimer un restaurant entier).
 *
 * APRÈS (solution 1 — super-admin plateforme, voir SuperAdmin,
 * AuthService.loginSuperAdmin) :
 *
 * - Créer / lister tous / rechercher / filtrer par statut ou devise /
 *   supprimer : réservé au SUPERADMIN (authority ROLE_SUPERADMIN,
 *   présente uniquement dans un JWT émis par
 *   POST /api/auth/login/super-admin). Ce n'est PAS un Employee :
 *   il n'a pas de restaurant, pas de rôle Role/RoleFonctionnalite.
 *
 * - Consulter / modifier un restaurant par id : autorisé au
 *   SUPERADMIN (n'importe quel restaurant) OU à l'employé connecté
 *   SEULEMENT sur son propre restaurant — vérifié via
 *   CurrentUserService.verifierAccesRestaurant(id), exactement comme
 *   les autres contrôleurs du chantier. On ne fait jamais confiance
 *   à l'id fourni par le frontend sans vérification.
 *
 * ⚠️ Le frontend Angular actuel (options-restauration.component.ts,
 * back-office-login.component.ts / écran restaurant-selection)
 * suppose encore qu'un ADMIN de restaurant peut lister/gérer TOUS
 * les restaurants — ce qui est désormais bloqué en 403 côté backend.
 * Ces écrans doivent être adaptés (voir le lot de correctifs Angular
 * associé) pour n'agir que sur le restaurant de l'ADMIN connecté.
 */
@RestController
@RequestMapping("/api/restaurants")
@AllArgsConstructor
@CrossOrigin("*")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final CurrentUserService currentUserService;
    private final RestaurantRepository restaurantRepository;
    private final FileStorageService fileStorageService;


    // =========================================================
    // HELPER — SUPERADMIN ?
    // =========================================================
    //
    // Le SuperAdmin n'est pas un Employee : CurrentUserService (qui
    // recharge un Employee par email) ne peut donc pas être utilisé
    // pour lui. On vérifie directement l'authority posée par
    // JwtAuthenticationFilter à partir du claim "role" du JWT
    // (ROLE_SUPERADMIN, uniquement pour un token émis par
    // loginSuperAdmin).

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


    // =========================================================
    // ======================== CREATE ==========================
    // =========================================================

    /**
     * Créer un restaurant — réservé au super-admin plateforme.
     * (Il n'y a pas d'inscription publique : un futur client de la
     * plateforme se voit créer son premier restaurant par le
     * super-admin, qui lui communique ensuite les identifiants de
     * son premier ADMIN.)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<RestaurantDTO> saveRestaurant(
            @RequestBody RestaurantDTO restaurantDTO) {

        RestaurantDTO savedRestaurant =
                restaurantService.saveRestaurant(restaurantDTO);

        return new ResponseEntity<>(
                savedRestaurant,
                HttpStatus.CREATED
        );
    }


    // =========================================================
    // ======================== READ =============================
    // =========================================================

    /**
     * Lister TOUS les restaurants — réservé au super-admin
     * plateforme. Un employé ne doit voir que le sien (déjà connu
     * côté client via AuthResponseDTO.restaurantId au login).
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {

        return ResponseEntity.ok(
                restaurantService.getAllRestaurants()
        );
    }


    /**
     * Récupérer un restaurant par id.
     *
     * Autorisé : SUPERADMIN (n'importe quel restaurant), ou
     * l'employé connecté sur SON propre restaurant uniquement.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDTO> getRestaurant(
            @PathVariable Long id)
            throws RestaurantNotFoundException,
            EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        if (!estSuperAdminConnecte()) {
            currentUserService.verifierAccesRestaurant(id);
        }

        return ResponseEntity.ok(
                restaurantService.getRestaurant(id)
        );
    }


    /**
     * Rechercher les restaurants par nom exact — réservé au
     * super-admin (recherche transverse à tous les restaurants).
     *
     * GET /api/restaurants/nom/{name}
     */
    @GetMapping("/nom/{name}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantByName(
            @PathVariable String name) {

        return ResponseEntity.ok(
                restaurantService.getRestaurantByName(name)
        );
    }


    /**
     * Rechercher les restaurants par adresse — réservé au
     * super-admin.
     *
     * GET /api/restaurants/adresse/{adresse}
     */
    @GetMapping("/adresse/{adresse}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantByAdresse(
            @PathVariable String adresse) {

        return ResponseEntity.ok(
                restaurantService.getRestaurantByAdresse(adresse)
        );
    }


    // =========================================================
    // ======================= SEARCH ===========================
    // =========================================================

    /**
     * Recherche par nom OU adresse, tous restaurants confondus —
     * réservé au super-admin.
     *
     * GET /api/restaurants/search?keyword=tunis
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<RestaurantDTO>> searchRestaurants(
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(
                restaurantService.searchRestaurants(keyword)
        );
    }


    // =========================================================
    // ======================== FILTER ==========================
    // =========================================================

    /**
     * Restaurants par statut, tous confondus — réservé au
     * super-admin.
     *
     * GET /api/restaurants/statut/ACTIF
     */
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByStatut(
            @PathVariable StatutRestaurant statut) {

        return ResponseEntity.ok(
                restaurantService.getRestaurantsByStatut(statut)
        );
    }


    /**
     * Restaurants par devise, tous confondus — réservé au
     * super-admin.
     *
     * GET /api/restaurants/devise/TND
     */
    @GetMapping("/devise/{devise}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByDevise(
            @PathVariable Devise devise) {

        return ResponseEntity.ok(
                restaurantService.getRestaurantsByDevise(devise)
        );
    }


    // =========================================================
    // ======================== UPDATE ==========================
    // =========================================================

    /**
     * Modifier un restaurant.
     *
     * Autorisé : SUPERADMIN (n'importe quel restaurant), ou
     * l'employé connecté sur SON propre restaurant uniquement —
     * même vérification que getRestaurant ci-dessus.
     *
     * PUT /api/restaurants/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDTO> updateRestaurant(
            @PathVariable Long id,
            @RequestBody RestaurantDTO restaurantDTO)
            throws RestaurantNotFoundException,
            EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        if (!estSuperAdminConnecte()) {
            currentUserService.verifierAccesRestaurant(id);
        }

        return ResponseEntity.ok(
                restaurantService.updateRestaurant(
                        id,
                        restaurantDTO
                )
        );
    }


    // =========================================================
    // ===================== LOGO (upload) ======================
    // =========================================================

    /**
     * Uploader (remplacer) le logo du restaurant — même principe que
     * MonCompteController.uploadPhoto : le fichier est enregistré
     * immédiatement sur le disque (voir FileStorageService, prévu
     * dès l'origine pour ce cas d'usage) et persisté en base tout de
     * suite, sans passer par le formulaire "Enregistrer" (évite de
     * combiner multipart/form-data et JSON dans une seule requête).
     *
     * Même autorisation que updateRestaurant : SUPERADMIN (n'importe
     * quel restaurant), ou l'employé connecté sur SON propre
     * restaurant uniquement.
     *
     * POST /api/restaurants/{id}/logo
     */
    @PostMapping(
            value = "/{id}/logo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file)
            throws RestaurantNotFoundException,
            EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        if (!estSuperAdminConnecte()) {
            currentUserService.verifierAccesRestaurant(id);
        }

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("restaurant not found"));

        String url;

        try {
            url = fileStorageService.enregistrerImage(
                    file,
                    "logos-restaurant",
                    "restaurant-" + id
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(java.util.Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "message",
                            "Impossible d'enregistrer l'image."
                    ));
        }

        restaurant.setLogo(url);

        Restaurant sauvegarde = restaurantRepository.save(restaurant);

        return ResponseEntity.ok(
                restaurantService.getRestaurant(sauvegarde.getId_restaurant())
        );
    }


    // =========================================================
    // ======================== DELETE ==========================
    // =========================================================

    /**
     * Supprimer un restaurant — réservé au super-admin plateforme.
     * C'était le point le plus grave de la faille d'origine :
     * n'importe quel employé pouvait effacer n'importe quel
     * restaurant.
     *
     * DELETE /api/restaurants/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable Long id)
            throws RestaurantNotFoundException {

        restaurantService.deleteRestaurant(id);

        return ResponseEntity.noContent().build();
    }
}