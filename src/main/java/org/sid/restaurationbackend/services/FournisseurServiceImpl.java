package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.exceptions.FournisseurNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
@AllArgsConstructor
@Slf4j

public class FournisseurServiceImpl implements FournisseurService {
    private RestaurantMapper dtotMapper;
    private FournisseurRepository fournisseurRepository;
    @Override
    public FournisseurDTO saveFournisseur(FournisseurDTO fournisseurDTO) {
        Fournisseur fournisseur = dtotMapper.fromFournisseurDTO(fournisseurDTO);
        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);
        return dtotMapper.fromFournisseur(savedFournisseur);
    }

    @Override
    public FournisseurDTO updateFournisseur(Long id ,FournisseurDTO fournisseurDTO) throws FournisseurNotFoundException {

        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new FournisseurNotFoundException("Fournisseur not found"));

        dtotMapper.updateFournisseurFromDto(fournisseurDTO, fournisseur);

        Fournisseur updatedFournisseur = fournisseurRepository.save(fournisseur);
        return dtotMapper.fromFournisseur(updatedFournisseur);
    }

    @Override
    public FournisseurDTO getFournisseur(Long id_fournisseur) throws FournisseurNotFoundException {
        Fournisseur fournisseur = fournisseurRepository.findById(id_fournisseur)
                .orElseThrow(() -> new FournisseurNotFoundException("Fournisseur not found"));
        return dtotMapper.fromFournisseur(fournisseur);
    }

    @Override
    public void deleteFournisseur(Long id_fournisseur) throws FournisseurNotFoundException {
        Fournisseur fournisseur = fournisseurRepository.findById(id_fournisseur)
                .orElseThrow(() -> new FournisseurNotFoundException("Fournisseur not found"));
        fournisseurRepository.deleteById(id_fournisseur);
    }



    @Override
    public List<FournisseurDTO> listFournisseurs() {

        return fournisseurRepository.findAll().stream()
                .map(dtotMapper::fromFournisseur)
                .toList();
    }

    @Override
    public List<FournisseurDTO> searchFournisseurs(String nom, String adresse, String email, String num) {
        return fournisseurRepository.search(nom,adresse,email,num).stream()
                .map(dtotMapper::fromFournisseur)
                .toList();
    }


}
