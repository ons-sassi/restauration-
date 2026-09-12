package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.PresenceDTO;
import org.sid.restaurationbackend.dtos.SuggestionDTO;
import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.exceptions.SuggestionNotFoundException;

import java.util.Date;
import java.util.List;

public interface SuggestionService {
    SuggestionDTO saveSuggestion(SuggestionDTO suggestionDTO);
    SuggestionDTO updateSuggestion(Long id, SuggestionDTO suggestionDTO) throws SuggestionNotFoundException;
    void deleteSuggestion(Long id) throws SuggestionNotFoundException;
    SuggestionDTO getSuggestion(Long id) throws SuggestionNotFoundException;
    List<SuggestionDTO> getAllSuggestions();
    List<SuggestionDTO> getSuggestionsByClient(ClientAuthentifieDTO clientAuthentifieDTO);

    List<SuggestionDTO> getSuggestionsByPriseEnCompte(Boolean priseEnCompte);

    SuggestionDTO prendreEnCompte(Long id, Boolean priseEnCompte)
            throws SuggestionNotFoundException;

    List<SuggestionDTO> searchSuggestions(String contenu);

    List<SuggestionDTO> getSuggestionsByDate(
            Date dateDebut,
            Date dateFin);
}