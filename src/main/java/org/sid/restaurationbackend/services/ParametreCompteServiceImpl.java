
package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.ModeleRecuDTO;
import org.sid.restaurationbackend.dtos.ParametreCompteDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.entities.ModeleRecu;
import org.sid.restaurationbackend.entities.ParametreCompte;
import org.sid.restaurationbackend.exceptions.ModeleRecuNotFoundException;
import org.sid.restaurationbackend.exceptions.ParametreCompteNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.ModeleRecuRepository;
import org.sid.restaurationbackend.repositories.ParametreCompteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ParametreCompteServiceImpl
        implements ParametreCompteService {

    private final ModeleRecuRepository modeleRecuRepository;
    private final ParametreCompteRepository parametreCompteRepository;
    private final RestaurantMapper dtoMapper;




    @Override
    public ParametreCompteDTO saveParametreCompte(
            ParametreCompteDTO parametreCompteDTO) {

        ParametreCompte parametreCompte =
                dtoMapper.fromParametreCompteDTO(parametreCompteDTO);

        ParametreCompte savedParametreCompte =
                parametreCompteRepository.save(parametreCompte);

        return dtoMapper.fromParametreCompte(savedParametreCompte);
    }


    @Override
    public ParametreCompteDTO updateParametreCompte(
            Long id,
            ParametreCompteDTO parametreCompteDTO)
            throws ParametreCompteNotFoundException {

        ParametreCompte parametreCompte =
                parametreCompteRepository.findById(id)
                        .orElseThrow(() ->
                                new ParametreCompteNotFoundException(
                                        "Paramètre de compte not found"
                                ));

        dtoMapper.updateParametreCompteFromDto(
                parametreCompteDTO,
                parametreCompte
        );

        ParametreCompte updatedParametreCompte =
                parametreCompteRepository.save(parametreCompte);

        return dtoMapper.fromParametreCompte(updatedParametreCompte);
    }


    @Override
    @Transactional(readOnly = true)
    public ParametreCompteDTO getParametreCompteById(
            Long id)
            throws ParametreCompteNotFoundException {

        ParametreCompte parametreCompte =
                parametreCompteRepository.findById(id)
                        .orElseThrow(() ->
                                new ParametreCompteNotFoundException(
                                        "Paramètre de compte not found"
                                ));

        return dtoMapper.fromParametreCompte(parametreCompte);
    }


    @Override
    public void deleteParametreCompte(Long id)
            throws ParametreCompteNotFoundException {

        ParametreCompte parametreCompte =
                parametreCompteRepository.findById(id)
                        .orElseThrow(() ->
                                new ParametreCompteNotFoundException(
                                        "Paramètre de compte not found"
                                ));

        parametreCompteRepository.delete(parametreCompte);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ParametreCompteDTO> getAllParametreComptes() {

        return parametreCompteRepository.findAll()
                .stream()
                .map(dtoMapper::fromParametreCompte)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<ParametreCompteDTO> getAllParametreComptesByRestaurant(
            RestaurantDTO restaurant) {

        return parametreCompteRepository
                .findByRestaurant(
                        dtoMapper.fromRestaurantDTO(restaurant)
                )
                .stream()
                .map(dtoMapper::fromParametreCompte)
                .toList();
    }



    @Override
    public ModeleRecuDTO saveModelRecu(
            ModeleRecuDTO modeleRecuDTO) {

        ModeleRecu modeleRecu =
                dtoMapper.fromModeleRecuDTO(modeleRecuDTO);

        Date maintenant = new Date();


        modeleRecu.setDate_creation(maintenant);


        modeleRecu.setDate_modification(maintenant);

        ModeleRecu savedModeleRecu =
                modeleRecuRepository.save(modeleRecu);

        return dtoMapper.fromModeleRecu(savedModeleRecu);
    }


    @Override
    public ModeleRecuDTO updateModeleRecu(
            Long id,
            ModeleRecuDTO modeleRecuDTO)
            throws ModeleRecuNotFoundException {

        ModeleRecu modeleRecu =
                modeleRecuRepository.findById(id)
                        .orElseThrow(() ->
                                new ModeleRecuNotFoundException(
                                        "Modèle de reçu not found"
                                ));

        dtoMapper.updateModeleRecuFromDto(
                modeleRecuDTO,
                modeleRecu
        );


        modeleRecu.setDate_modification(new Date());

        ModeleRecu updatedModeleRecu =
                modeleRecuRepository.save(modeleRecu);

        return dtoMapper.fromModeleRecu(updatedModeleRecu);
    }


    @Override
    @Transactional(readOnly = true)
    public ModeleRecuDTO getModeleRecuById(
            Long id)
            throws ModeleRecuNotFoundException {

        ModeleRecu modeleRecu =
                modeleRecuRepository.findById(id)
                        .orElseThrow(() ->
                                new ModeleRecuNotFoundException(
                                        "Modèle de reçu not found"
                                ));

        return dtoMapper.fromModeleRecu(modeleRecu);
    }


    @Override
    public void deleteModeleRecu(Long id)
            throws ModeleRecuNotFoundException {

        ModeleRecu modeleRecu =
                modeleRecuRepository.findById(id)
                        .orElseThrow(() ->
                                new ModeleRecuNotFoundException(
                                        "Modèle de reçu not found"
                                ));

        modeleRecuRepository.delete(modeleRecu);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ModeleRecuDTO> getAllModeleRecus() {

        return modeleRecuRepository.findAll()
                .stream()
                .map(dtoMapper::fromModeleRecu)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<ModeleRecuDTO> getAllModelRecusByRestaurant(
            RestaurantDTO restaurant) {

        return modeleRecuRepository
                .findByRestaurant(
                        dtoMapper.fromRestaurantDTO(restaurant)
                )
                .stream()
                .map(dtoMapper::fromModeleRecu)
                .toList();
    }



    @Transactional
    @Override
    public List<ModeleRecuDTO> searchModelesRecus(
            String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllModeleRecus();
        }

        return modeleRecuRepository
                .findByNomModeleContainingIgnoreCase(
                        keyword.trim()
                )
                .stream()
                .map(dtoMapper::fromModeleRecu)
                .toList();
    }


    @Transactional
    @Override
    public List<ModeleRecuDTO> searchModelesRecusByRestaurant(
            RestaurantDTO restaurant,
            String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllModelRecusByRestaurant(restaurant);
        }

        return modeleRecuRepository
                .findByRestaurantAndNomModeleContainingIgnoreCase(
                        dtoMapper.fromRestaurantDTO(restaurant),
                        keyword.trim()
                )
                .stream()
                .map(dtoMapper::fromModeleRecu)
                .toList();
    }
}
