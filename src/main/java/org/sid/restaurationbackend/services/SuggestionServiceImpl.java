package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.SuggestionDTO;
import org.sid.restaurationbackend.entities.Suggestion;
import org.sid.restaurationbackend.exceptions.SuggestionNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.SuggestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class SuggestionServiceImpl implements SuggestionService {

    private RestaurantMapper dtoMapper;
    private SuggestionRepository suggestionRepository;

    @Override
    public SuggestionDTO saveSuggestion(SuggestionDTO suggestionDTO) {
        Suggestion suggestion = dtoMapper.fromSuggestionDTO(suggestionDTO);
        Suggestion savedSuggestion = suggestionRepository.save(suggestion);
        return dtoMapper.fromSuggestion(savedSuggestion);
    }

    @Override
    public SuggestionDTO updateSuggestion(Long id, SuggestionDTO suggestionDTO) throws SuggestionNotFoundException {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new SuggestionNotFoundException("Suggestion not found"));

        dtoMapper.updateSuggestionFromDto(suggestionDTO, suggestion);

        Suggestion updatedSuggestion = suggestionRepository.save(suggestion);
        return dtoMapper.fromSuggestion(updatedSuggestion);
    }

    @Override
    public void deleteSuggestion(Long id) throws SuggestionNotFoundException {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new SuggestionNotFoundException("Suggestion not found"));
        suggestionRepository.deleteById(id);
    }

    @Override
    public SuggestionDTO getSuggestion(Long id) throws SuggestionNotFoundException {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new SuggestionNotFoundException("Suggestion not found"));
        return dtoMapper.fromSuggestion(suggestion);
    }

    @Override
    public List<SuggestionDTO> getAllSuggestions() {
        return suggestionRepository.findAll().stream()
                .map(dtoMapper::fromSuggestion)
                .toList();
    }

    @Override
    public List<SuggestionDTO> getSuggestionsByClient(ClientAuthentifieDTO clientAuthentifie) {
        return suggestionRepository.findByClient(dtoMapper.fromClientAuthentifieDTO(clientAuthentifie)).stream()
                .map(dtoMapper::fromSuggestion)
                .toList();
    }

    @Override
    public List<SuggestionDTO> getSuggestionsByPriseEnCompte(Boolean priseEnCompte) {
        return suggestionRepository.findByPriseEnCompte(priseEnCompte)
                .stream()
                .map(dtoMapper::fromSuggestion)
                .toList();
    }

    @Override
    public SuggestionDTO prendreEnCompte(Long id, Boolean priseEnCompte)
            throws SuggestionNotFoundException {

        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() ->
                        new SuggestionNotFoundException("Suggestion not found"));

        suggestion.setPriseEnCompte(priseEnCompte != null ? priseEnCompte : true);

        Suggestion updatedSuggestion =
                suggestionRepository.save(suggestion);

        return dtoMapper.fromSuggestion(updatedSuggestion);
    }
    @Override
    public List<SuggestionDTO> searchSuggestions(String contenu) {
        return suggestionRepository
                .findByContenuContainingIgnoreCase(contenu)
                .stream()
                .map(dtoMapper::fromSuggestion)
                .toList();
    }
    @Override
    public List<SuggestionDTO> getSuggestionsByDate(
            Date dateDebut,
            Date dateFin) {

        return suggestionRepository
                .findByDateCreationBetween(dateDebut, dateFin)
                .stream()
                .map(dtoMapper::fromSuggestion)
                .toList();
    }
}