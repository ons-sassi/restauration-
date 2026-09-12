package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.ClientReclamationDTO;
import org.sid.restaurationbackend.dtos.ClientReclamationRequestDTO;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Commande;
import org.sid.restaurationbackend.entities.Reclamation;

import org.sid.restaurationbackend.enums.StatutReclamation;

import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.CommandeNotFoundException;
import org.sid.restaurationbackend.exceptions.ReclamationNotFoundException;

import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.sid.restaurationbackend.repositories.CommandeRepository;
import org.sid.restaurationbackend.repositories.ReclamationRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Voir ClientReclamationService pour le pourquoi de cette séparation.
 *
 * Contrairement à Produit/Modificateur/Table (scopés par restaurant),
 * Reclamation est directement rattachée à un ClientAuthentifie
 * (colonne id_client, voir l'entité) : la vérification d'appartenance
 * est donc directe (reclamation.getClient() == client connecté),
 * jamais indirecte via un restaurant.
 *
 * Ownership : une réclamation qui n'appartient pas au client connecté
 * renvoie un ReclamationNotFoundException (404), jamais un 403 — même
 * principe que ClientCommandeServiceImpl.getProduitDuRestaurant : ne
 * pas confirmer à un client l'existence de la réclamation d'un autre
 * client.
 */
@Service
@Transactional
@AllArgsConstructor
public class ClientReclamationServiceImpl implements ClientReclamationService {

    private final ClientAuthentifieRepository clientAuthentifieRepository;
    private final CommandeRepository commandeRepository;
    private final ReclamationRepository reclamationRepository;

    @Override
    public ClientReclamationDTO creerReclamation(ClientReclamationRequestDTO requestDTO)
            throws ClientNotFoundException, CommandeNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        if (requestDTO == null
                || requestDTO.getSujet() == null
                || requestDTO.getSujet().isBlank()
                || requestDTO.getDescription() == null
                || requestDTO.getDescription().isBlank()) {

            throw new IllegalArgumentException(
                    "Le sujet et la description sont requis"
            );
        }

        Commande commande = null;

        if (requestDTO.getCommandeId() != null) {
            commande = getCommandeDuClient(requestDTO.getCommandeId(), client);
        }

        Reclamation reclamation = new Reclamation();
        reclamation.setSujet(requestDTO.getSujet());
        reclamation.setDescription(requestDTO.getDescription());
        reclamation.setDate_creation(new Date());
        reclamation.setStatut(StatutReclamation.EN_ATTENTE);
        reclamation.setClient(client);
        reclamation.setCommande(commande);

        Reclamation sauvegardee = reclamationRepository.save(reclamation);

        return toDto(sauvegardee);
    }

    @Override
    public List<ClientReclamationDTO> getMesReclamations() throws ClientNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        return reclamationRepository.findByClient(client).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ClientReclamationDTO getMaReclamation(Long id)
            throws ClientNotFoundException, ReclamationNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ReclamationNotFoundException(
                        "Réclamation introuvable avec l'id : " + id));

        if (reclamation.getClient() == null
                || !client.getId_utilisateur().equals(reclamation.getClient().getId_utilisateur())) {
            // 404 plutôt que 403 : ne pas confirmer l'existence de la
            // réclamation d'un autre client (voir javadoc classe).
            throw new ReclamationNotFoundException(
                    "Réclamation introuvable avec l'id : " + id);
        }

        return toDto(reclamation);
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

    private Commande getCommandeDuClient(Long commandeId, ClientAuthentifie client)
            throws CommandeNotFoundException {

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new CommandeNotFoundException(
                        "Commande introuvable avec l'id : " + commandeId));

        if (commande.getClient() == null
                || !client.getId_utilisateur().equals(commande.getClient().getId_utilisateur())) {
            // 404 : ne pas confirmer l'existence de la commande d'un
            // autre client (même logique que pour les produits/tables
            // d'un autre restaurant dans ClientCommandeServiceImpl).
            throw new CommandeNotFoundException(
                    "Commande introuvable avec l'id : " + commandeId);
        }

        return commande;
    }

    private ClientReclamationDTO toDto(Reclamation reclamation) {

        return new ClientReclamationDTO(
                reclamation.getId_reclamation(),
                reclamation.getSujet(),
                reclamation.getDescription(),
                reclamation.getDate_creation(),
                reclamation.getStatut(),
                reclamation.getReponse_employee(),
                reclamation.getDate_reponse(),
                reclamation.getCommande() != null
                        ? reclamation.getCommande().getId_commande()
                        : null
        );
    }
}
