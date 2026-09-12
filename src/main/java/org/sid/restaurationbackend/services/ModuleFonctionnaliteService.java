package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.FonctionnaliteDTO;
import org.sid.restaurationbackend.dtos.ModuleDTO;
import org.sid.restaurationbackend.exceptions.FonctionnaliteNotFoundException;
import org.sid.restaurationbackend.exceptions.ModuleNotFoundException;

import java.util.List;

public interface ModuleFonctionnaliteService {

    ModuleDTO saveModule(ModuleDTO moduleDTO);

    ModuleDTO updateModule(
            Long moduleId,
            ModuleDTO moduleDTO)
            throws ModuleNotFoundException;

    ModuleDTO getModuleById(Long moduleId)
            throws ModuleNotFoundException;

    ModuleDTO getModuleByNom(String nom)
            throws ModuleNotFoundException;

    List<ModuleDTO> getAllModules();

    List<ModuleDTO> searchModules(String keyword);

    void deleteModule(Long moduleId)
            throws ModuleNotFoundException;

    Boolean disponiblePdv(Long moduleId)
            throws ModuleNotFoundException;

    Boolean disponibleBackOffice(Long moduleId)
            throws ModuleNotFoundException;

    ModuleDTO activerPourPdv(Long moduleId)
            throws ModuleNotFoundException;

    ModuleDTO desactiverPourPdv(Long moduleId)
            throws ModuleNotFoundException;

    ModuleDTO activerPourBackOffice(Long moduleId)
            throws ModuleNotFoundException;

    ModuleDTO desactiverPourBackOffice(Long moduleId)
            throws ModuleNotFoundException;

    FonctionnaliteDTO saveFonctionnalite(
            FonctionnaliteDTO fonctionnaliteDTO);

    FonctionnaliteDTO updateFonctionnalite(
            Long fonctionnaliteId,
            FonctionnaliteDTO fonctionnaliteDTO)
            throws FonctionnaliteNotFoundException;

    FonctionnaliteDTO getFonctionnaliteById(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException;

    List<FonctionnaliteDTO> getAllFonctionnalites();

    List<FonctionnaliteDTO> searchFonctionnalites(
            String keyword);

    void deleteFonctionnalite(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException;

    void assignModuleToFonctionnalite(
            Long moduleId,
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException,
            ModuleNotFoundException;

    List<FonctionnaliteDTO> getFonctionnalitesByModule(
            ModuleDTO moduleDTO)
            throws ModuleNotFoundException;

    void assignFonctionnaliteToFonctionnaliteParent(
            Long fonctionnaliteParentId,
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException;

    List<FonctionnaliteDTO>
    getFonctionnalitesByFonctionnaliteParent(
            FonctionnaliteDTO fonctionnaliteDTO);

    Boolean fonctionnaliteDisponiblePdv(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException;

    Boolean fonctionnaliteDisponibleBackOffice(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException;

    FonctionnaliteDTO activerFonctionnalitePdv(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException;

    FonctionnaliteDTO desactiverFonctionnalitePdv(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException;

    FonctionnaliteDTO activerFonctionnaliteBackOffice(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException;

    FonctionnaliteDTO desactiverFonctionnaliteBackOffice(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException;
}
