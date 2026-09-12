package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.FonctionnaliteDTO;
import org.sid.restaurationbackend.entities.Fonctionnalite;
import org.sid.restaurationbackend.exceptions.FonctionnaliteNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.FonctionnaliteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class FonctionnaliteServiceImpl
        implements FonctionnaliteService {

    private RestaurantMapper dtotMapper;
    private FonctionnaliteRepository fonctionnaliteRepository;

    @Override
    public FonctionnaliteDTO saveFonctionnalite(
            FonctionnaliteDTO fonctionnaliteDTO) {

        Fonctionnalite fonctionnalite =
                dtotMapper.fromFonctionnaliteDTO(fonctionnaliteDTO);

        Fonctionnalite savedFonctionnalite =
                fonctionnaliteRepository.save(fonctionnalite);

        return dtotMapper.fromFonctionnalite(savedFonctionnalite);
    }

    @Override
    public FonctionnaliteDTO updateFonctionnalite(
            Long id,
            FonctionnaliteDTO fonctionnaliteDTO)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        dtotMapper.updateFonctionnaliteFromDto(
                fonctionnaliteDTO,
                fonctionnalite
        );

        Fonctionnalite updatedFonctionnalite =
                fonctionnaliteRepository.save(fonctionnalite);

        return dtotMapper.fromFonctionnalite(updatedFonctionnalite);
    }

    @Override
    public void deleteFonctionnalite(Long id)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        fonctionnaliteRepository.delete(fonctionnalite);
    }

    @Override
    public FonctionnaliteDTO getFonctionnalite(Long id)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        return dtotMapper.fromFonctionnalite(fonctionnalite);
    }

    @Override
    public List<FonctionnaliteDTO> getAllFonctionnalites() {

        return fonctionnaliteRepository.findAll()
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }

    @Override
    public List<FonctionnaliteDTO> getFonctionnalitesByNom(
            String nom) {

        return fonctionnaliteRepository
                .findByNomFonctionnaliteContainingIgnoreCase(nom)
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }

    @Override
    public List<FonctionnaliteDTO> getFonctionnalitesByCode(
            String code) {

        return fonctionnaliteRepository
                .findByCodeFonctionnaliteContainingIgnoreCase(code)
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }

    @Override
    public List<FonctionnaliteDTO> getFonctionnalitesByDisponibilitePdv(
            Boolean disponiblePdv) {

        return fonctionnaliteRepository
                .findByDisponiblePdv(disponiblePdv)
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }

    @Override
    public List<FonctionnaliteDTO> getFonctionnalitesByDisponibiliteBackoffice(
            Boolean disponibleBackoffice) {

        return fonctionnaliteRepository
                .findByDisponibleBackoffice(disponibleBackoffice)
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }

    @Override
    public List<FonctionnaliteDTO> getFonctionnalitesParent() {

        return fonctionnaliteRepository
                .findByFonctionnaliteParentIsNull()
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }

    @Override
    public List<FonctionnaliteDTO> getSousFonctionnalites(
            Long fonctionnaliteParentId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite parent =
                fonctionnaliteRepository.findById(
                        fonctionnaliteParentId
                ).orElseThrow(() ->
                        new FonctionnaliteNotFoundException(
                                "Fonctionnalite parent not found"
                        ));

        return fonctionnaliteRepository
                .findByFonctionnaliteParent(parent)
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }
}
