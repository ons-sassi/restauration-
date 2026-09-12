package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.ClientReservationDTO;
import org.sid.restaurationbackend.dtos.ClientReservationRequestDTO;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Reservation;

import org.sid.restaurationbackend.enums.StatutReservation;

import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ReservationNotFoundException;

import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.sid.restaurationbackend.repositories.ReservationRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Voir ClientReservationService pour le pourquoi de cette séparation.
 *
 * Contrairement à Commande (dont le restaurant est choisi par le
 * client à chaque commande, voir ClientCommandeServiceImpl),
 * Reservation reprend directement le restaurant du client connecté
 * (ClientAuthentifie.restaurant, colonne NOT NULL — un compte client
 * appartient à un seul restaurant) : jamais de restaurantId fourni
 * par le frontend.
 *
 * La table n'est JAMAIS choisie par le client à la création (voir
 * Reservation.table, nullable) : c'est le restaurant qui l'assigne
 * ensuite via ReservationController (back-office), même logique que
 * l'attribution de table pour le service en salle.
 *
 * Ownership : une réservation qui n'appartient pas au client connecté
 * renvoie un ReservationNotFoundException (404), jamais un 403 —
 * même principe que ClientReclamationServiceImpl.
 */
@Service
@Transactional
@AllArgsConstructor
public class ClientReservationServiceImpl implements ClientReservationService {

    private final ClientAuthentifieRepository clientAuthentifieRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public ClientReservationDTO creerReservation(ClientReservationRequestDTO requestDTO)
            throws ClientNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        if (requestDTO == null
                || requestDTO.getNombrePersonnes() == null
                || requestDTO.getNombrePersonnes() < 1) {

            throw new IllegalArgumentException(
                    "Le nombre de personnes est requis et doit être d'au moins 1."
            );
        }

        if (requestDTO.getDateReservation() == null) {
            throw new IllegalArgumentException(
                    "La date de réservation est requise."
            );
        }

        if (requestDTO.getHeureReservation() == null) {
            throw new IllegalArgumentException(
                    "L'heure de réservation est requise."
            );
        }

        if (requestDTO.getDateReservation().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La date de réservation ne peut pas être dans le passé."
            );
        }

        Reservation reservation = new Reservation();
        reservation.setNombre_personnes(requestDTO.getNombrePersonnes());
        reservation.setDateReservation(requestDTO.getDateReservation());
        reservation.setHeureReservation(requestDTO.getHeureReservation());
        reservation.setCommentaire_client(requestDTO.getCommentaireClient());
        reservation.setStatut(StatutReservation.EN_ATTENTE);
        reservation.setDate_creation(new Date());
        reservation.setClient(client);
        reservation.setRestaurant(client.getRestaurant());

        Reservation sauvegardee = reservationRepository.save(reservation);

        return toDto(sauvegardee);
    }

    @Override
    public List<ClientReservationDTO> getMesReservations() throws ClientNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        return reservationRepository.findByClient(client).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ClientReservationDTO getMaReservation(Long id)
            throws ClientNotFoundException, ReservationNotFoundException {

        ClientAuthentifie client = getClientConnecte();
        Reservation reservation = getReservationDuClient(id, client);

        return toDto(reservation);
    }

    @Override
    public ClientReservationDTO annulerMaReservation(Long id)
            throws ClientNotFoundException, ReservationNotFoundException {

        ClientAuthentifie client = getClientConnecte();
        Reservation reservation = getReservationDuClient(id, client);

        if (reservation.getStatut() == StatutReservation.ANNULEE
                || reservation.getStatut() == StatutReservation.HONOREE
                || reservation.getStatut() == StatutReservation.NO_SHOW) {

            throw new IllegalArgumentException(
                    "Cette réservation ne peut plus être annulée."
            );
        }

        reservation.setStatut(StatutReservation.ANNULEE);

        Reservation sauvegardee = reservationRepository.save(reservation);

        return toDto(sauvegardee);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private ClientAuthentifie getClientConnecte() throws ClientNotFoundException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new ClientNotFoundException("Client non authentifié");
        }

        return clientAuthentifieRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new ClientNotFoundException(
                        "Client authentifié introuvable"
                ));
    }

    private Reservation getReservationDuClient(Long id, ClientAuthentifie client)
            throws ReservationNotFoundException {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Réservation introuvable avec l'id : " + id));

        if (reservation.getClient() == null
                || !client.getId_utilisateur().equals(reservation.getClient().getId_utilisateur())) {

            // 404 plutôt que 403 : ne pas confirmer l'existence de la
            // réservation d'un autre client (même principe que
            // ClientReclamationServiceImpl.getMaReclamation).
            throw new ReservationNotFoundException(
                    "Réservation introuvable avec l'id : " + id);
        }

        return reservation;
    }

    private ClientReservationDTO toDto(Reservation reservation) {

        return new ClientReservationDTO(
                reservation.getId_reservation(),
                reservation.getNombre_personnes(),
                reservation.getDateReservation(),
                reservation.getHeureReservation(),
                reservation.getStatut(),
                reservation.getDate_creation(),
                reservation.getCommentaire_client(),
                reservation.getTable() != null
                        ? reservation.getTable().getNumeroTable()
                        : null
        );
    }
}