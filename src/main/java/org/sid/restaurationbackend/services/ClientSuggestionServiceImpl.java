package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.ClientSuggestionDTO;
import org.sid.restaurationbackend.dtos.ClientSuggestionRequestDTO;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Suggestion;

import org.sid.restaurationbackend.exceptions.ClientNotFoundException;

import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.sid.restaurationbackend.repositories.SuggestionRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Voir ClientSuggestionService pour le pourquoi de cette séparation.
 *
 * Restaurant toujours dérivé de client.getRestaurant() (un compte
 * client = un restaurant fixe, voir ClientAuthentifie.restaurant,
 * nullable = false) — jamais d'un id fourni par Angular. Même
 * principe que ClientMenuServiceImpl / ClientCommandeServiceImpl.
 */
@Service
@Transactional
@AllArgsConstructor
public class ClientSuggestionServiceImpl implements ClientSuggestionService {

    private final ClientAuthentifieRepository clientAuthentifieRepository;
    private final SuggestionRepository suggestionRepository;

    @Override
    public ClientSuggestionDTO creerSuggestion(ClientSuggestionRequestDTO requestDTO)
            throws ClientNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        if (requestDTO == null
                || requestDTO.getContenu() == null
                || requestDTO.getContenu().isBlank()) {

            throw new IllegalArgumentException(
                    "Le contenu de la suggestion est requis"
            );
        }

        Suggestion suggestion = new Suggestion();
        suggestion.setContenu(requestDTO.getContenu());
        suggestion.setDateCreation(new Date());
        suggestion.setPriseEnCompte(false);
        suggestion.setClient(client);
        suggestion.setRestaurant(client.getRestaurant());

        Suggestion sauvegardee = suggestionRepository.save(suggestion);

        return toDto(sauvegardee);
    }

    @Override
    public List<ClientSuggestionDTO> getMesSuggestions() throws ClientNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        return suggestionRepository.findByClient(client).stream()
                .map(this::toDto)
                .toList();
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

    private ClientSuggestionDTO toDto(Suggestion suggestion) {

        return new ClientSuggestionDTO(
                suggestion.getId_suggestion(),
                suggestion.getContenu(),
                suggestion.getDateCreation(),
                suggestion.getPriseEnCompte()
        );
    }
}
