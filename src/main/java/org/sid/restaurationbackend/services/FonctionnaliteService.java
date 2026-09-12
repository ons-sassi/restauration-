package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.FonctionnaliteDTO;
import org.sid.restaurationbackend.exceptions.FonctionnaliteNotFoundException;

import java.util.List;

public interface FonctionnaliteService {

    FonctionnaliteDTO saveFonctionnalite(FonctionnaliteDTO fonctionnaliteDTO);

    FonctionnaliteDTO updateFonctionnalite(
            Long id,
            FonctionnaliteDTO fonctionnaliteDTO)
            throws FonctionnaliteNotFoundException;

    void deleteFonctionnalite(Long id)
            throws FonctionnaliteNotFoundException;

    FonctionnaliteDTO getFonctionnalite(Long id)
            throws FonctionnaliteNotFoundException;

    List<FonctionnaliteDTO> getAllFonctionnalites();

    List<FonctionnaliteDTO> getFonctionnalitesByNom(
            String nom);

    List<FonctionnaliteDTO> getFonctionnalitesByCode(
            String code);

    List<FonctionnaliteDTO> getFonctionnalitesByDisponibilitePdv(
            Boolean disponiblePdv);

    List<FonctionnaliteDTO> getFonctionnalitesByDisponibiliteBackoffice(
            Boolean disponibleBackoffice);

    List<FonctionnaliteDTO> getFonctionnalitesParent();

    List<FonctionnaliteDTO> getSousFonctionnalites(
            Long fonctionnaliteParentId)
            throws FonctionnaliteNotFoundException;
}