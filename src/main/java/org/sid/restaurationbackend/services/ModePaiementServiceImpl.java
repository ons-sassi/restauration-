package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.ModePaiementDTO;
import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Fournisseur;
import org.sid.restaurationbackend.entities.ModePaiement;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ModePaiementNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.sid.restaurationbackend.repositories.ModePaiementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ModePaiementServiceImpl implements ModePaiementService {
    private RestaurantMapper dtotMapper;
    private ModePaiementRepository modePaiementRepository;
    private ClientAuthentifieRepository clientAuthentifieRepository;

    @Override
    public ModePaiementDTO saveModePaiement(ModePaiementDTO modePaiementDTO) {

        ModePaiement modePaiement =
                dtotMapper.fromModePaiementDTO(modePaiementDTO);

        if (modePaiement.getActif() == null) {
            modePaiement.setActif(true);
        }

        ModePaiement savedModePaiement =
                modePaiementRepository.save(modePaiement);

        return dtotMapper.fromModePaiement(savedModePaiement);
    }

    @Override
    public ModePaiementDTO updateModePaiement(Long id, ModePaiementDTO modePaiementDTO) throws ModePaiementNotFoundException {

        ModePaiement modePaiement = modePaiementRepository.findById(id)
                .orElseThrow(() -> new ModePaiementNotFoundException("ModePaiement not found"));

        dtotMapper.updateModePaiementFromDto(modePaiementDTO, modePaiement);

        ModePaiement updatedModePaiement = modePaiementRepository.save(modePaiement);
        return dtotMapper.fromModePaiement(updatedModePaiement);
    }

    @Override
    public ModePaiementDTO getModePaiement(Long id) throws ModePaiementNotFoundException {
        ModePaiement modePaiement = modePaiementRepository.findById(id)
                .orElseThrow(() -> new ModePaiementNotFoundException("ModePaiement not found"));
        return dtotMapper.fromModePaiement(modePaiement);
    }


    @Override
    public void deleteModePaiement(Long id) throws ModePaiementNotFoundException {
        ModePaiement modePaiement = modePaiementRepository.findById(id)
                .orElseThrow(() -> new ModePaiementNotFoundException("ModePaiement not found"));
        modePaiementRepository.delete(modePaiement);
    }

    @Override
    public List<ModePaiementDTO> getAllModePaiements() {

        return modePaiementRepository.findAll()
                .stream()
                .map(dtotMapper::fromModePaiement)
                .toList();
    }



    @Override
    public List<ModePaiementDTO> getModesPaiementActifs() {

        return modePaiementRepository.findByActifTrue()
                .stream()
                .map(dtotMapper::fromModePaiement)
                .toList();
    }

    @Override
    public ModePaiementDTO activerModePaiement(Long id)
            throws ModePaiementNotFoundException {

        ModePaiement modePaiement =
                modePaiementRepository.findById(id)
                        .orElseThrow(() ->
                                new ModePaiementNotFoundException(
                                        "ModePaiement not found"));

        modePaiement.setActif(true);

        return dtotMapper.fromModePaiement(
                modePaiementRepository.save(modePaiement)
        );
    }

    @Override
    public ModePaiementDTO desactiverModePaiement(Long id)
            throws ModePaiementNotFoundException {

        ModePaiement modePaiement =
                modePaiementRepository.findById(id)
                        .orElseThrow(() ->
                                new ModePaiementNotFoundException(
                                        "ModePaiement not found"));

        modePaiement.setActif(false);

        return dtotMapper.fromModePaiement(
                modePaiementRepository.save(modePaiement)
        );
    }
    @Override
    public List<ModePaiementDTO> getModesPaiementByClient(Long clientId)
            throws ClientNotFoundException {

        ClientAuthentifie client =
                clientAuthentifieRepository.findById(clientId)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Client authentifié not found"));

        return modePaiementRepository.findByClient(client)
                .stream()
                .map(dtotMapper::fromModePaiement)
                .toList();
    }






}
