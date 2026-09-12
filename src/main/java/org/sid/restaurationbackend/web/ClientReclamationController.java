package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ClientReclamationDTO;
import org.sid.restaurationbackend.dtos.ClientReclamationRequestDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.CommandeNotFoundException;
import org.sid.restaurationbackend.exceptions.ReclamationNotFoundException;
import org.sid.restaurationbackend.services.ClientReclamationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SIDEBAR CLIENT — Réclamations
 * =========================================================
 * Découverte critique (voir ClientReclamationService) : le
 * ReclamationController existant est inutilisable pour un
 * ClientAuthentifie — getReclamationsByClient/getReclamation y
 * appellent currentUserService.getRestaurantIdConnecte() /
 * verifierAccesRestaurant(), qui supposent un Employee connecté et
 * lèveraient une EmployeeNotFoundException pour un client. Même bug
 * pattern que ProduitController (étape 3) et l'ancien
 * CommandeService (étape 5).
 *
 * Ce contrôleur est donc entièrement dédié à l'espace client, sous
 * /api/client/reclamations (protégé INTERFACE_CLIENT, voir
 * SecurityConfig : la règle "/api/client/**" existante couvre déjà ce
 * préfixe, aucun ajout nécessaire côté sécurité). Il ne réutilise PAS
 * ReclamationController/ReclamationService : le client ne peut créer
 * qu'une réclamation pour SON PROPRE compte, et ne peut lire QUE ses
 * propres réclamations (jamais un id fourni par le frontend, jamais
 * un restaurant déduit).
 */
@RestController
@RequestMapping("/api/client/reclamations")
@AllArgsConstructor
public class ClientReclamationController {

    private final ClientReclamationService clientReclamationService;

    /**
     * Créer une réclamation pour le client connecté, optionnellement
     * liée à une de ses commandes (commandeId).
     */
    @PostMapping
    public ResponseEntity<ClientReclamationDTO> creerReclamation(
            @RequestBody ClientReclamationRequestDTO requestDTO)
            throws ClientNotFoundException, CommandeNotFoundException {

        ClientReclamationDTO reclamation =
                clientReclamationService.creerReclamation(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reclamation);
    }

    /**
     * Réclamations du client connecté.
     */
    @GetMapping
    public ResponseEntity<List<ClientReclamationDTO>> getMesReclamations()
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientReclamationService.getMesReclamations()
        );
    }

    /**
     * Détail d'une réclamation du client connecté (404 si elle
     * n'existe pas ou appartient à un autre client — voir
     * ClientReclamationServiceImpl).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientReclamationDTO> getMaReclamation(
            @PathVariable Long id)
            throws ClientNotFoundException, ReclamationNotFoundException {

        return ResponseEntity.ok(
                clientReclamationService.getMaReclamation(id)
        );
    }
}
