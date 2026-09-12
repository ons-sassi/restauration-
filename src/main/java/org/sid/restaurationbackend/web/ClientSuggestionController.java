package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ClientSuggestionDTO;
import org.sid.restaurationbackend.dtos.ClientSuggestionRequestDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.services.ClientSuggestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SIDEBAR CLIENT — Suggestions / conseils
 * =========================================================
 * Découverte critique (voir ClientSuggestionService) :
 * SuggestionController existant force
 * currentUserService.getRestaurantIdConnecte() pour TOUT appelant
 * non-SUPERADMIN, y compris sur le POST de création — un client
 * authentifié y provoquerait systématiquement une
 * EmployeeNotFoundException. Ce contrôleur est donc entièrement
 * dédié à l'espace client, sous /api/client/suggestions (déjà
 * couvert par la règle "/api/client/**" -> INTERFACE_CLIENT de
 * SecurityConfig, aucun ajout nécessaire).
 */
@RestController
@RequestMapping("/api/client/suggestions")
@AllArgsConstructor
public class ClientSuggestionController {

    private final ClientSuggestionService clientSuggestionService;

    /**
     * Envoyer une suggestion/conseil pour le restaurant du client
     * connecté.
     */
    @PostMapping
    public ResponseEntity<ClientSuggestionDTO> creerSuggestion(
            @RequestBody ClientSuggestionRequestDTO requestDTO)
            throws ClientNotFoundException {

        ClientSuggestionDTO suggestion =
                clientSuggestionService.creerSuggestion(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(suggestion);
    }

    /**
     * Suggestions déjà envoyées par le client connecté.
     */
    @GetMapping
    public ResponseEntity<List<ClientSuggestionDTO>> getMesSuggestions()
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientSuggestionService.getMesSuggestions()
        );
    }
}
