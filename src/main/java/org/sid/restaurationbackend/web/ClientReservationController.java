package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ClientReservationDTO;
import org.sid.restaurationbackend.dtos.ClientReservationRequestDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ReservationNotFoundException;
import org.sid.restaurationbackend.services.ClientReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SIDEBAR CLIENT — Réservations
 * =========================================================
 * Même bug pattern que ClientReclamationController /
 * ClientCommandeController : ReservationController (existant) est
 * inutilisable pour un ClientAuthentifie, toutes ses méthodes
 * supposant un Employee connecté (CurrentUserService).
 *
 * Contrôleur entièrement dédié à l'espace client, sous
 * /api/client/reservations (protégé INTERFACE_CLIENT, règle
 * "/api/client/**" déjà en place dans SecurityConfig, aucun ajout
 * nécessaire). Le client ne peut créer qu'une réservation pour SON
 * PROPRE compte, dans SON restaurant (celui de son compte), et ne
 * peut lire/annuler QUE ses propres réservations.
 */
@RestController
@RequestMapping("/api/client/reservations")
@AllArgsConstructor
public class ClientReservationController {

    private final ClientReservationService clientReservationService;

    /**
     * Créer une demande de réservation pour le client connecté.
     */
    @PostMapping
    public ResponseEntity<ClientReservationDTO> creerReservation(
            @RequestBody ClientReservationRequestDTO requestDTO)
            throws ClientNotFoundException {

        ClientReservationDTO reservation =
                clientReservationService.creerReservation(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservation);
    }

    /**
     * Réservations du client connecté.
     */
    @GetMapping
    public ResponseEntity<List<ClientReservationDTO>> getMesReservations()
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientReservationService.getMesReservations()
        );
    }

    /**
     * Détail d'une réservation du client connecté (404 si elle
     * n'existe pas ou appartient à un autre client — voir
     * ClientReservationServiceImpl).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientReservationDTO> getMaReservation(
            @PathVariable Long id)
            throws ClientNotFoundException, ReservationNotFoundException {

        return ResponseEntity.ok(
                clientReservationService.getMaReservation(id)
        );
    }

    /**
     * Annuler une réservation du client connecté (uniquement si elle
     * n'est pas déjà annulée/honorée/no-show — voir
     * ClientReservationServiceImpl).
     */
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<ClientReservationDTO> annulerMaReservation(
            @PathVariable Long id)
            throws ClientNotFoundException, ReservationNotFoundException {

        return ResponseEntity.ok(
                clientReservationService.annulerMaReservation(id)
        );
    }
}