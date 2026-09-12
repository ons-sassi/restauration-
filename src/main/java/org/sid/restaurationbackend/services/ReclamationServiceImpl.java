package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.CommandeDTO;
import org.sid.restaurationbackend.dtos.ReclamationDTO;
import org.sid.restaurationbackend.entities.Reclamation;
import org.sid.restaurationbackend.enums.StatutReclamation;
import org.sid.restaurationbackend.exceptions.ReclamationNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.ReclamationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ReclamationServiceImpl implements ReclamationService {

    private RestaurantMapper dtotMapper;
    private ReclamationRepository reclamationRepository;

    @Override
    public ReclamationDTO saveReclamation(
            ReclamationDTO reclamationDTO) {

        Reclamation reclamation =
                dtotMapper.fromReclamationDTO(reclamationDTO);

        if (reclamation.getDate_creation() == null) {
            reclamation.setDate_creation(new Date());
        }

        if (reclamation.getStatut() == null) {
            // À adapter à ton enum
            reclamation.setStatut(StatutReclamation.EN_ATTENTE);
        }

        Reclamation savedReclamation =
                reclamationRepository.save(reclamation);

        return dtotMapper.fromReclamation(savedReclamation);
    }

    @Override
    public ReclamationDTO updateReclamation(Long id, ReclamationDTO reclamationDTO) throws ReclamationNotFoundException {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ReclamationNotFoundException("Reclamation not found"));

        dtotMapper.updateReclamationFromDto(reclamationDTO, reclamation);

        Reclamation updatedReclamation = reclamationRepository.save(reclamation);
        return dtotMapper.fromReclamation(updatedReclamation);
    }

    @Override
    public ReclamationDTO getReclamation(Long id_reclamation) throws ReclamationNotFoundException {
        Reclamation reclamation = reclamationRepository.findById(id_reclamation)
                .orElseThrow(() -> new ReclamationNotFoundException("Reclamation not found"));
        return dtotMapper.fromReclamation(reclamation);
    }

    @Override
    public void deleteReclamation(Long id_reclamation) throws ReclamationNotFoundException {
        Reclamation reclamation = reclamationRepository.findById(id_reclamation)
                .orElseThrow(() -> new ReclamationNotFoundException("Reclamation not found"));
        reclamationRepository.deleteById(id_reclamation);
    }

    @Override
    public List<ReclamationDTO> getAllReclamations() {
        return reclamationRepository.findAll().stream()
                .map(dtotMapper::fromReclamation)
                .toList();
    }

    @Override
    public List<ReclamationDTO> getReclamationsByClient(ClientAuthentifieDTO client) {
        return reclamationRepository.findByClient(dtotMapper.fromClientAuthentifieDTO(client)).stream()
                .map(dtotMapper::fromReclamation)
                .toList();
    }

    @Override
    public List<ReclamationDTO> getReclamationsByCommande(CommandeDTO commande) {
        return reclamationRepository.findByCommande(dtotMapper.fromCommandeDTO(commande)).stream()
                .map(dtotMapper::fromReclamation)
                .toList();
    }

    @Override
    public List<ReclamationDTO> getReclamationsByStatut(
            StatutReclamation statut) {

        return reclamationRepository
                .findByStatut(statut)
                .stream()
                .map(dtotMapper::fromReclamation)
                .toList();
    }

    @Override
    public ReclamationDTO repondreReclamation(
            Long id,
            String reponse)
            throws ReclamationNotFoundException {

        Reclamation reclamation =
                reclamationRepository.findById(id)
                        .orElseThrow(() ->
                                new ReclamationNotFoundException(
                                        "Reclamation not found"));

        if (reponse == null || reponse.isBlank()) {
            throw new IllegalArgumentException(
                    "La réponse ne peut pas être vide");
        }

        reclamation.setReponse_employee(reponse);
        reclamation.setDate_reponse(new Date());


        reclamation.setStatut(StatutReclamation.RESOLUE);

        Reclamation savedReclamation =
                reclamationRepository.save(reclamation);

        return dtotMapper.fromReclamation(savedReclamation);
    }
}