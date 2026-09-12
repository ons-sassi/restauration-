package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientSuggestionDTO;
import org.sid.restaurationbackend.dtos.ClientSuggestionRequestDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;

import java.util.List;

/**
 * Voir ClientSuggestionServiceImpl pour le pourquoi de cette
 * séparation : SuggestionController (existant) appelle
 * currentUserService.getRestaurantIdConnecte() de façon inconditionnelle
 * pour tout appelant non-SUPERADMIN — y compris sur POST — ce qui
 * suppose un Employee connecté et lève une EmployeeNotFoundException
 * pour un ClientAuthentifie. Même bug pattern que ProduitController
 * (étape 3), l'ancien CommandeService (étape 5) et ReclamationController
 * (voir ClientReclamationService).
 */
public interface ClientSuggestionService {

    ClientSuggestionDTO creerSuggestion(ClientSuggestionRequestDTO requestDTO)
            throws ClientNotFoundException;

    List<ClientSuggestionDTO> getMesSuggestions() throws ClientNotFoundException;
}
