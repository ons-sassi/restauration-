package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.ClientNonAuthentifieDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.dtos.TableRestaurantDTO;
import org.sid.restaurationbackend.enums.OrigineSession;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;
import org.sid.restaurationbackend.services.ClientNonAuthentifieService;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.RestaurantService;
import org.sid.restaurationbackend.services.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * Ce contrôleur n'est PAS accessible aux clients eux-mêmes
 * (aucune route sous "/api/clients-non-authentifies" n'est
 * "permitAll" dans SecurityConfig : elle tombe dans
 * anyRequest().authenticated()). C'est une API de BACK OFFICE
 * pour consulter/gérer les sessions client créées lorsqu'un
 * client scanne un QR code — donc appelée par un employé
 * authentifié, ce qui rend CurrentUserService applicable ici
 * exactement comme pour les autres contrôleurs de ce lot.
 *
 * ClientNonAuthentifie est rattaché directement à un
 * restaurant. Toutes les méthodes vérifient désormais que ce
 * restaurant est bien celui de l'employé connecté.
 */
@RestController
@RequestMapping("/api/clients-non-authentifies")
@AllArgsConstructor
@Slf4j
public class ClientNonAuthentifieController {

    private final ClientNonAuthentifieService clientNonAuthentifieService;
    private final RestaurantService restaurantService;
    private final TableService tableService;
    private final CurrentUserService currentUserService;


    // Créer une session client
    @PostMapping
    public ResponseEntity<ClientNonAuthentifieDTO> saveClientNonAuthentifie(
            @RequestBody ClientNonAuthentifieDTO clientNonAuthentifieDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                clientNonAuthentifieDTO.getRestaurant() != null
                        ? clientNonAuthentifieDTO.getRestaurant().getId_restaurant()
                        : null
        );

        ClientNonAuthentifieDTO savedClient =
                clientNonAuthentifieService
                        .saveClientNonAuthentifie(
                                clientNonAuthentifieDTO
                        );

        return new ResponseEntity<>(
                savedClient,
                HttpStatus.CREATED
        );
    }


    // Récupérer toutes les sessions client DU RESTAURANT CONNECTÉ
    @GetMapping
    public ResponseEntity<List<ClientNonAuthentifieDTO>>
    getAllClientNonAuthentifie()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ClientNonAuthentifieDTO> clients = clientNonAuthentifieService
                .getAllClientNonAuthentifie()
                .stream()
                .filter(c -> c.getRestaurant() != null
                        && restaurantId.equals(c.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(clients);
    }


    // Récupérer une session client par ID
    @GetMapping("/{id}")
    public ResponseEntity<ClientNonAuthentifieDTO>
    getClientNonAuthentifieById(
            @PathVariable Long id)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientNonAuthentifieDTO client = clientNonAuthentifieService
                .getClientNonAuthentifieById(id);

        currentUserService.verifierAccesRestaurant(
                client.getRestaurant() != null
                        ? client.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(client);
    }


    // Modifier une session client
    //
    // La session existante doit appartenir au restaurant connecté, et
    // on ne doit pas pouvoir la "déplacer" vers un autre restaurant.
    @PutMapping("/{id}")
    public ResponseEntity<ClientNonAuthentifieDTO>
    updateClientNonAuthentifie(
            @PathVariable Long id,
            @RequestBody ClientNonAuthentifieDTO clientNonAuthentifieDTO)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientNonAuthentifieDTO existant = clientNonAuthentifieService
                .getClientNonAuthentifieById(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        if (clientNonAuthentifieDTO.getRestaurant() != null) {
            currentUserService.verifierAccesRestaurant(
                    clientNonAuthentifieDTO.getRestaurant().getId_restaurant()
            );
        }

        return ResponseEntity.ok(
                clientNonAuthentifieService
                        .updateClientNonAuthentifie(
                                id,
                                clientNonAuthentifieDTO
                        )
        );
    }


    // Supprimer une session client
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClientNonAuthentifie(
            @PathVariable Long id)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientNonAuthentifieDTO existant = clientNonAuthentifieService
                .getClientNonAuthentifieById(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        clientNonAuthentifieService
                .deleteClientNonAuthentifie(id);

        return ResponseEntity.noContent().build();
    }


    // Sessions client par restaurant
    //
    // Le restaurant demandé DOIT être celui de l'employé connecté.
    @PostMapping("/restaurant")
    public ResponseEntity<List<ClientNonAuthentifieDTO>>
    getClientsByRestaurant(
            @RequestBody RestaurantDTO restaurant)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                restaurant != null
                        ? restaurant.getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                clientNonAuthentifieService
                        .getClientNonAuthentifiesByRestaurant(
                                restaurant
                        )
        );
    }


    // Sessions client par table
    //
    // La table fournie doit appartenir au restaurant connecté : on
    // recharge la table réelle en base (au lieu de faire confiance au
    // TableRestaurantDTO envoyé par le frontend) pour vérifier son
    // vrai restaurant.
    @PostMapping("/table")
    public ResponseEntity<List<ClientNonAuthentifieDTO>>
    getClientsByTable(
            @RequestBody TableRestaurantDTO table)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException,
            TableNotFoundException {

        if (table == null || table.getId_table() == null) {
            currentUserService.verifierAccesRestaurant(null);
        } else {
            TableRestaurantDTO tableReelle =
                    tableService.getTable(table.getId_table());

            currentUserService.verifierAccesRestaurant(
                    tableReelle.getRestaurant() != null
                            ? tableReelle.getRestaurant().getId_restaurant()
                            : null
            );
        }

        return ResponseEntity.ok(
                clientNonAuthentifieService
                        .getClientNonAuthentifiesByTableRestaurant(
                                table
                        )
        );
    }


    // Sessions client par origine, DU RESTAURANT CONNECTÉ
    @GetMapping("/origine/{origine}")
    public ResponseEntity<List<ClientNonAuthentifieDTO>>
    getClientsByOrigine(
            @PathVariable OrigineSession origine)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ClientNonAuthentifieDTO> clients = clientNonAuthentifieService
                .getClientNonAuthentifiesByOrigineSession(
                        origine
                )
                .stream()
                .filter(c -> c.getRestaurant() != null
                        && restaurantId.equals(c.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(clients);
    }


    // ============================================================
    // GET BY RESTAURANT + TABLE
    // ============================================================

    /*
     * IMPORTANT :
     *
     * Il ne faut PAS avoir deux @RequestBody.
     *
     * On utilise les IDs comme paramètres de requête.
     *
     * Exemple :
     *
     * GET/POST
     * /api/clients-non-authentifies/restaurant/table
     *     ?restaurantId=1
     *     &tableId=5
     *
     * Le restaurantId demandé DOIT être celui de l'employé connecté.
     */

    @GetMapping("/restaurant/table")
    public ResponseEntity<List<ClientNonAuthentifieDTO>>
    getSessionsByRestaurantAndTable(
            @RequestParam Long restaurantId,
            @RequestParam Long tableId)
            throws RestaurantNotFoundException, TableNotFoundException,
            EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(restaurantId);

        RestaurantDTO restaurant = restaurantService.getRestaurant(restaurantId);
        TableRestaurantDTO table = tableService.getTable(tableId);

        return ResponseEntity.ok(
                clientNonAuthentifieService
                        .getSessionsByRestaurantAndTable(
                                restaurant,
                                table
                        )
        );
    }


    // Fermer une session client
    @PatchMapping("/{id}/fermer")
    public ResponseEntity<Void> fermerSession(
            @PathVariable Long id)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientNonAuthentifieDTO existant = clientNonAuthentifieService
                .getClientNonAuthentifieById(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        clientNonAuthentifieService
                .fermerSession(id);

        return ResponseEntity.noContent().build();
    }


    // Consulter si une session client est active
    @GetMapping("/{id}/active")
    public ResponseEntity<Boolean> sessionActive(
            @PathVariable Long id)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientNonAuthentifieDTO existant = clientNonAuthentifieService
                .getClientNonAuthentifieById(id);
        currentUserService.verifierAccesRestaurant(
                existant.getRestaurant() != null
                        ? existant.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                clientNonAuthentifieService
                        .sessionActive(id)
        );
    }
}