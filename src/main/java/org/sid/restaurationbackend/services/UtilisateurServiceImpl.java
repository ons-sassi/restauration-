package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.UtilisateurDTO;
import org.sid.restaurationbackend.entities.Utilisateur;
import org.sid.restaurationbackend.exceptions.UtilisateurNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@Service
@Transactional
@AllArgsConstructor
@Slf4j

public class UtilisateurServiceImpl implements UtilisateurService {

    private RestaurantMapper dtotMapper;
    private UtilisateurRepository utilisateurRepository;
    @Override
    public UtilisateurDTO getUtilisateurById(Long id) throws UtilisateurNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElseThrow(() -> new UtilisateurNotFoundException("Utilisateur not found"));
        return dtotMapper.fromUtilisateur(utilisateur);
    }

    @Override
    public List<UtilisateurDTO> getAllUtilisateurs() {

        return utilisateurRepository.findAll()
                .stream()
                .map(dtotMapper::fromUtilisateur)
                .toList();
    }

    @Override
    public UtilisateurDTO saveUtilisateur(UtilisateurDTO utilisateurDTO) {
        Utilisateur utilisateur = dtotMapper.fromUtilisateurDTO(utilisateurDTO);
        utilisateur.setDate_creation(new Date());
        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        return dtotMapper.fromUtilisateur(savedUtilisateur);
    }

    @Override
    public UtilisateurDTO updateUtilisateur(Long id, UtilisateurDTO utilisateurDTO) throws UtilisateurNotFoundException {

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException("Utilisateur not found"));

        dtotMapper.updateUtilisateurFromDto(utilisateurDTO, utilisateur);

        Utilisateur updatedUtilisateur = utilisateurRepository.save(utilisateur);
        return dtotMapper.fromUtilisateur(updatedUtilisateur);
    }

    @Override
    public void deleteUtilisateur(Long id) throws UtilisateurNotFoundException {
        utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException("Utilisateur not found"));
        utilisateurRepository.deleteById(id);
    }
}


