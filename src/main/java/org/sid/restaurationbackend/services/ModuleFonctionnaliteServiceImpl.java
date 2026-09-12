package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.FonctionnaliteDTO;
import org.sid.restaurationbackend.dtos.ModuleDTO;
import org.sid.restaurationbackend.entities.Fonctionnalite;
import org.sid.restaurationbackend.entities.Module;
import org.sid.restaurationbackend.exceptions.FonctionnaliteNotFoundException;
import org.sid.restaurationbackend.exceptions.ModuleNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.FonctionnaliteRepository;
import org.sid.restaurationbackend.repositories.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ModuleFonctionnaliteServiceImpl
        implements ModuleFonctionnaliteService {

    private final RestaurantMapper dtotMapper;
    private final ModuleRepository moduleRepository;
    private final FonctionnaliteRepository fonctionnaliteRepository;



    @Override
    public ModuleDTO saveModule(ModuleDTO moduleDTO) {

        Module module = dtotMapper.fromModuleDTO(moduleDTO);

        Module savedModule = moduleRepository.save(module);

        return dtotMapper.fromModule(savedModule);
    }


    @Override
    public ModuleDTO updateModule(
            Long moduleId,
            ModuleDTO moduleDTO)
            throws ModuleNotFoundException {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "Module not found"));

        dtotMapper.updateModuleFromDto(
                moduleDTO,
                module
        );

        Module updatedModule =
                moduleRepository.save(module);

        return dtotMapper.fromModule(updatedModule);
    }


    @Override
    public ModuleDTO getModuleById(Long moduleId)
            throws ModuleNotFoundException {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "Module not found"));

        return dtotMapper.fromModule(module);
    }


    @Override
    public ModuleDTO getModuleByNom(String nom)
            throws ModuleNotFoundException {

        Module module =
                moduleRepository.findByNomModuleIgnoreCase(nom)
                        .orElseThrow(() ->
                                new ModuleNotFoundException(
                                        "Module not found"));

        return dtotMapper.fromModule(module);
    }


    @Override
    public List<ModuleDTO> getAllModules() {

        return moduleRepository.findAll()
                .stream()
                .map(dtotMapper::fromModule)
                .toList();
    }


    @Override
    public List<ModuleDTO> searchModules(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllModules();
        }

        return moduleRepository
                .findByNomModuleContainingIgnoreCase(
                        keyword.trim()
                )
                .stream()
                .map(dtotMapper::fromModule)
                .toList();
    }


    @Override
    public void deleteModule(Long moduleId)
            throws ModuleNotFoundException {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "Module not found"));

        moduleRepository.delete(module);
    }



    @Override
    public Boolean disponiblePdv(Long moduleId)
            throws ModuleNotFoundException {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "Module not found"));

        return module.getDisponiblePdv();
    }


    @Override
    public Boolean disponibleBackOffice(Long moduleId)
            throws ModuleNotFoundException {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "Module not found"));

        return module.getDisponible_backoffice();
    }


    @Override
    public ModuleDTO activerPourPdv(Long moduleId)
            throws ModuleNotFoundException {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "Module not found"));

        module.setDisponiblePdv(true);

        return dtotMapper.fromModule(
                moduleRepository.save(module)
        );
    }


    @Override
    public ModuleDTO desactiverPourPdv(Long moduleId)
            throws ModuleNotFoundException {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "Module not found"));

        module.setDisponiblePdv(false);

        return dtotMapper.fromModule(
                moduleRepository.save(module)
        );
    }


    @Override
    public ModuleDTO activerPourBackOffice(Long moduleId)
            throws ModuleNotFoundException {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "Module not found"));

        module.setDisponible_backoffice(true);

        return dtotMapper.fromModule(
                moduleRepository.save(module)
        );
    }


    @Override
    public ModuleDTO desactiverPourBackOffice(Long moduleId)
            throws ModuleNotFoundException {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "Module not found"));

        module.setDisponible_backoffice(false);

        return dtotMapper.fromModule(
                moduleRepository.save(module)
        );
    }



    @Override
    public FonctionnaliteDTO saveFonctionnalite(
            FonctionnaliteDTO fonctionnaliteDTO) {

        Fonctionnalite fonctionnalite =
                dtotMapper.fromFonctionnaliteDTO(
                        fonctionnaliteDTO
                );

        Fonctionnalite savedFonctionnalite =
                fonctionnaliteRepository.save(fonctionnalite);

        return dtotMapper.fromFonctionnalite(
                savedFonctionnalite
        );
    }


    @Override
    public FonctionnaliteDTO updateFonctionnalite(
            Long fonctionnaliteId,
            FonctionnaliteDTO fonctionnaliteDTO)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        dtotMapper.updateFonctionnaliteFromDto(
                fonctionnaliteDTO,
                fonctionnalite
        );

        Fonctionnalite updatedFonctionnalite =
                fonctionnaliteRepository.save(
                        fonctionnalite
                );

        return dtotMapper.fromFonctionnalite(
                updatedFonctionnalite
        );
    }


    @Override
    public FonctionnaliteDTO getFonctionnaliteById(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        return dtotMapper.fromFonctionnalite(
                fonctionnalite
        );
    }


    @Override
    public List<FonctionnaliteDTO> getAllFonctionnalites() {

        return fonctionnaliteRepository.findAll()
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }


    @Override
    public List<FonctionnaliteDTO> searchFonctionnalites(
            String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllFonctionnalites();
        }

        return fonctionnaliteRepository
                .findByNomFonctionnaliteContainingIgnoreCase(
                        keyword.trim()
                )
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }


    @Override
    public void deleteFonctionnalite(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        fonctionnaliteRepository.delete(fonctionnalite);
    }




    @Override
    public void assignModuleToFonctionnalite(
            Long moduleId,
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException,
            ModuleNotFoundException {

        Module module =
                moduleRepository.findById(moduleId)
                        .orElseThrow(() ->
                                new ModuleNotFoundException(
                                        "Module not found"
                                ));

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        fonctionnalite.setModule(module);

        fonctionnaliteRepository.save(fonctionnalite);
    }


    @Override
    public List<FonctionnaliteDTO> getFonctionnalitesByModule(
            ModuleDTO moduleDTO)
            throws ModuleNotFoundException {

        Module module =
                moduleRepository.findById(
                                moduleDTO.getId_module()
                        )
                        .orElseThrow(() ->
                                new ModuleNotFoundException(
                                        "Module not found"
                                ));

        return fonctionnaliteRepository
                .findByModule(module)
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }




    @Override
    public void assignFonctionnaliteToFonctionnaliteParent(
            Long fonctionnaliteParentId,
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        Fonctionnalite parent =
                fonctionnaliteRepository.findById(
                                fonctionnaliteParentId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite parent not found"
                                ));


        if (fonctionnaliteId.equals(fonctionnaliteParentId)) {
            throw new IllegalArgumentException(
                    "Une fonctionnalité ne peut pas être son propre parent"
            );
        }

        fonctionnalite.setFonctionnaliteParent(parent);

        fonctionnaliteRepository.save(fonctionnalite);
    }


    @Override
    public List<FonctionnaliteDTO>
    getFonctionnalitesByFonctionnaliteParent(
            FonctionnaliteDTO fonctionnaliteDTO) {

        Fonctionnalite parent =
                dtotMapper.fromFonctionnaliteDTO(
                        fonctionnaliteDTO
                );

        return fonctionnaliteRepository
                .findByFonctionnaliteParent(parent)
                .stream()
                .map(dtotMapper::fromFonctionnalite)
                .toList();
    }




    @Override
    public Boolean fonctionnaliteDisponiblePdv(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        return fonctionnalite.getDisponiblePdv();
    }


    @Override
    public Boolean fonctionnaliteDisponibleBackOffice(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        return fonctionnalite.getDisponibleBackoffice();
    }


    @Override
    public FonctionnaliteDTO activerFonctionnalitePdv(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        fonctionnalite.setDisponiblePdv(true);

        return dtotMapper.fromFonctionnalite(
                fonctionnaliteRepository.save(fonctionnalite)
        );
    }


    @Override
    public FonctionnaliteDTO desactiverFonctionnalitePdv(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        fonctionnalite.setDisponiblePdv(false);

        return dtotMapper.fromFonctionnalite(
                fonctionnaliteRepository.save(fonctionnalite)
        );
    }


    @Override
    public FonctionnaliteDTO activerFonctionnaliteBackOffice(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        fonctionnalite.setDisponibleBackoffice(true);

        return dtotMapper.fromFonctionnalite(
                fonctionnaliteRepository.save(fonctionnalite)
        );
    }


    @Override
    public FonctionnaliteDTO desactiverFonctionnaliteBackOffice(
            Long fonctionnaliteId)
            throws FonctionnaliteNotFoundException {

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(
                                fonctionnaliteId
                        )
                        .orElseThrow(() ->
                                new FonctionnaliteNotFoundException(
                                        "Fonctionnalite not found"
                                ));

        fonctionnalite.setDisponibleBackoffice(false);

        return dtotMapper.fromFonctionnalite(
                fonctionnaliteRepository.save(fonctionnalite)
        );
    }
}