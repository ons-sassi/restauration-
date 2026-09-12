package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.enums.StatutUtilisateur;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.services.ClientAuthentifieService;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — famille
 * Stock/Fidélité, périmètre BACK-OFFICE uniquement.
 * =========================================================
 * Décision produit retenue (voir entité ClientAuthentifie) : privé
 * par restaurant côté back-office, même pattern que Fournisseur /
 * Ingredient / Reduction. Seul ce contrôleur (consommé uniquement par
 * client-list / client-details côté Angular) est
 * concerné par ce correctif :
 *  - getAll / search / getClientsByStatut : filtrés en mémoire sur le
 *    restaurant de l'employé connecté (ou non filtrés pour le
 *    SUPERADMIN, voir ci-dessous) ;
 *  - get / update / delete / desactiver / activer /
 *    préférences / allergies : le client ciblé doit appartenir au
 *    restaurant connecté (verifierAccesRestaurant), sinon 403 ;
 *  - create : le restaurant n'est jamais celui envoyé par le front —
 *    il est FORCÉ à celui de l'employé connecté (le front n'a de toute
 *    façon jamais eu ce champ à remplir jusqu'ici).
 *
 * CORRECTIF (suite à relecture) : ce contrôleur ne gérait AUCUN cas
 * SUPERADMIN, contrairement à Reclamation/Notification/Restaurant/
 * PrevisionStock. Comme SuperAdmin est un sous-type de Utilisateur
 * distinct d'Employee (pas de restaurant rattaché — voir entité
 * SuperAdmin), CurrentUserService.getEmployeeConnecte() (et donc
 * verifierAccesRestaurant/getRestaurantIdConnecte) levait
 * systématiquement EmployeeNotFoundException pour un SUPERADMIN :
 * TOUS les endpoints de ce contrôleur lui étaient donc inaccessibles
 * (404), alors qu'il doit pouvoir superviser tous les restaurants
 * (bypass total, même logique que ReclamationController). Seule
 * exception : create, où le SUPERADMIN n'ayant justement AUCUN
 * restaurant "à lui", le restaurant doit être explicitement fourni
 * dans la requête (voir saveClientAuthentifie).
 *
 * Le flux d'auto-inscription publique (client qui s'inscrit lui-même,
 * sans contexte employé) et l'authentification client
 * (ClientUserDetailsService / findByEmail) sont volontairement HORS
 * PÉRIMÈTRE de ce lot — aucun consommateur Angular actuel ne les
 * utilise, on ne touche pas à ce contrat pour l'instant.
 *
 * AVANT ce correctif : n'importe quel employé authentifié, quel que
 * soit son restaurant, pouvait lister/consulter/modifier/supprimer les
 * clients de n'importe quel autre restaurant (coordonnées, adresse,
 * allergies inclus).
 */
@RestController
@RequestMapping("/api/clients-authentifies")
@AllArgsConstructor
public class ClientAuthentifieController {

    private final ClientAuthentifieService clientAuthentifieService;
    private final CurrentUserService currentUserService;


    // ---------------------------------------------------------------
    // Utilitaires internes de scoping restaurant (voir javadoc classe)
    // ---------------------------------------------------------------

    // Copié des autres contrôleurs déjà à jour (Reclamation,
    // Notification, Restaurant, PrevisionStock) : pas de helper
    // partagé dans ce projet, chaque contrôleur duplique son propre
    // utilitaire de scoping — convention déjà en place.
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

    // À utiliser sur un ClientAuthentifieDTO déjà rechargé depuis le
    // service (jamais sur un objet envoyé tel quel par le frontend).
    // Bypass total pour le SUPERADMIN.
    private void verifierAccesClient(ClientAuthentifieDTO client)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        if (estSuperAdminConnecte()) {
            return;
        }

        currentUserService.verifierAccesRestaurant(
                client != null && client.getRestaurant() != null
                        ? client.getRestaurant().getId_restaurant()
                        : null
        );
    }

    @PostMapping
    public ResponseEntity<ClientAuthentifieDTO> saveClientAuthentifie(
            @RequestBody ClientAuthentifieDTO clientAuthentifieDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        if (estSuperAdminConnecte()) {

            // Le SUPERADMIN n'a lui-même aucun restaurant : il doit
            // explicitement préciser pour quel restaurant ce client
            // est créé — jamais de valeur implicite dans ce cas.
            if (clientAuthentifieDTO.getRestaurant() == null
                    || clientAuthentifieDTO.getRestaurant().getId_restaurant() == null) {

                throw new AccesRestaurantNonAutoriseException(
                        "Un restaurant doit être précisé pour créer "
                                + "un client authentifié en tant que "
                                + "SUPERADMIN.");
            }

        } else {

            // Forcé au restaurant de l'employé connecté : on ne fait
            // pas confiance à un éventuel restaurant fourni par le
            // front.
            Long restaurantId = currentUserService.getRestaurantIdConnecte();
            RestaurantDTO restaurant = new RestaurantDTO();
            restaurant.setId_restaurant(restaurantId);
            clientAuthentifieDTO.setRestaurant(restaurant);
        }

        ClientAuthentifieDTO savedClient =
                clientAuthentifieService.saveClientAuthentifie(
                        clientAuthentifieDTO
                );

        return new ResponseEntity<>(
                savedClient,
                HttpStatus.CREATED
        );
    }



    @GetMapping("/{id}")
    public ResponseEntity<ClientAuthentifieDTO> getClientAuthentifie(
            @PathVariable Long id)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientAuthentifieDTO client =
                clientAuthentifieService.getClientAuthentifie(id);

        verifierAccesClient(client);

        return ResponseEntity.ok(client);
    }



    // Avant : renvoyait TOUS les clients, tous restaurants confondus.
    // Filtré maintenant sur le restaurant connecté (ou non filtré
    // pour le SUPERADMIN, qui supervise tous les restaurants).
    @GetMapping
    public ResponseEntity<List<ClientAuthentifieDTO>>
    getAllClientAuthentifies() throws EmployeeNotFoundException {

        List<ClientAuthentifieDTO> tous =
                clientAuthentifieService.getAllClientAuthentifies();

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(tous);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                tous.stream()
                        .filter(c -> c.getRestaurant() != null
                                && restaurantId.equals(c.getRestaurant().getId_restaurant()))
                        .toList()
        );
    }



    @PutMapping("/{id}")
    public ResponseEntity<ClientAuthentifieDTO>
    updateClientAuthentifie(
            @PathVariable Long id,
            @RequestBody ClientAuthentifieDTO clientAuthentifieDTO)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        // Le client existant doit appartenir au restaurant connecté
        // (bypass SUPERADMIN)...
        ClientAuthentifieDTO existant =
                clientAuthentifieService.getClientAuthentifie(id);
        verifierAccesClient(existant);

        // ...et on ne doit pas pouvoir le "déplacer" vers un autre
        // restaurant : on ignore ce que le front envoie et on le
        // reforce au restaurant existant, comme à la création — y
        // compris pour le SUPERADMIN (déplacer un client entre
        // restaurants n'est pas une opération couverte par cet
        // endpoint).
        clientAuthentifieDTO.setRestaurant(existant.getRestaurant());

        ClientAuthentifieDTO updatedClient =
                clientAuthentifieService.updateClientAuthentifie(
                        id,
                        clientAuthentifieDTO
                );

        return ResponseEntity.ok(updatedClient);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClientAuthentifie(
            @PathVariable Long id)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientAuthentifieDTO existant =
                clientAuthentifieService.getClientAuthentifie(id);
        verifierAccesClient(existant);

        clientAuthentifieService.deleteClientAuthentifie(id);

        return ResponseEntity.noContent().build();
    }



    // Avant : recherche portant sur TOUS les clients de la plateforme.
    // Filtrée maintenant comme getAllClientAuthentifies() ci-dessus.
    @GetMapping("/search")
    public ResponseEntity<List<ClientAuthentifieDTO>>
    searchClientAuthentifies(
            @RequestParam(required = false) String keyword)
            throws EmployeeNotFoundException {

        List<ClientAuthentifieDTO> resultats =
                clientAuthentifieService.searchClientAuthentifies(keyword);

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(c -> c.getRestaurant() != null
                                && restaurantId.equals(c.getRestaurant().getId_restaurant()))
                        .toList()
        );
    }



    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<ClientAuthentifieDTO>>
    getClientsByStatut(
            @PathVariable StatutUtilisateur statut)
            throws EmployeeNotFoundException {

        List<ClientAuthentifieDTO> resultats =
                clientAuthentifieService.getClientsByStatut(statut);

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(c -> c.getRestaurant() != null
                                && restaurantId.equals(c.getRestaurant().getId_restaurant()))
                        .toList()
        );
    }




    @PatchMapping("/{id}/derniere-connexion")
    public ResponseEntity<Void> updateDerniereConnexion(
            @PathVariable Long id)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientAuthentifieDTO existant =
                clientAuthentifieService.getClientAuthentifie(id);
        verifierAccesClient(existant);

        clientAuthentifieService.updateDerniereConnexion(id);

        return ResponseEntity.noContent().build();
    }





    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<ClientAuthentifieDTO>
    desactiverClient(
            @PathVariable Long id)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientAuthentifieDTO existant =
                clientAuthentifieService.getClientAuthentifie(id);
        verifierAccesClient(existant);

        ClientAuthentifieDTO client =
                clientAuthentifieService.desactiverClient(id);

        return ResponseEntity.ok(client);
    }



    @PatchMapping("/{id}/activer")
    public ResponseEntity<ClientAuthentifieDTO>
    activerClient(
            @PathVariable Long id)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientAuthentifieDTO existant =
                clientAuthentifieService.getClientAuthentifie(id);
        verifierAccesClient(existant);

        ClientAuthentifieDTO client =
                clientAuthentifieService.activerClient(id);

        return ResponseEntity.ok(client);
    }

    // NOTE : consommé uniquement via ClientCommandeController côté
    // client (flux public, code de parrainage saisi par un client),
    // pas par le back-office — laissé non scopé volontairement, hors
    // périmètre "back office only" de ce lot. À revisiter si ce
    // endpoint devient aussi appelé côté back-office.
    @GetMapping("/parrainage/{code}")
    public ResponseEntity<ClientAuthentifieDTO> getClientByCodeParrainage(
            @PathVariable String code)
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientAuthentifieService.getClientByCodeParrainage(code)
        );
    }

    @PatchMapping("/{id}/preferences")
    public ResponseEntity<ClientAuthentifieDTO> updatePreferences(
            @PathVariable Long id,
            @RequestParam String preferences)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientAuthentifieDTO existant =
                clientAuthentifieService.getClientAuthentifie(id);
        verifierAccesClient(existant);

        return ResponseEntity.ok(
                clientAuthentifieService.updatePreferences(id, preferences)
        );
    }

    @PatchMapping("/{id}/allergies")
    public ResponseEntity<ClientAuthentifieDTO> updateAllergies(
            @PathVariable Long id,
            @RequestParam String preferences)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientAuthentifieDTO existant =
                clientAuthentifieService.getClientAuthentifie(id);
        verifierAccesClient(existant);

        return ResponseEntity.ok(
                // NOTE (bug préexistant, hors périmètre de ce lot) :
                // appelait déjà updatePreferences() au lieu
                // d'updateAllergies() avant ce correctif — laissé tel
                // quel pour ne pas mélanger un fix fonctionnel dans un
                // lot de sécurité, mais à signaler.
                clientAuthentifieService.updatePreferences(id, preferences)
        );
    }


}
