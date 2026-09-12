package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ModeleRecuDTO;
import org.sid.restaurationbackend.dtos.ParametreCompteDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.exceptions.ModeleRecuNotFoundException;
import org.sid.restaurationbackend.exceptions.ParametreCompteNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ParametreCompteService {
    ParametreCompteDTO saveParametreCompte(ParametreCompteDTO parametreCompteDTO);
    ParametreCompteDTO updateParametreCompte(Long id ,ParametreCompteDTO parametreCompteDTO) throws ParametreCompteNotFoundException;
    ParametreCompteDTO getParametreCompteById(Long id) throws ParametreCompteNotFoundException;
    void deleteParametreCompte(Long id) throws ParametreCompteNotFoundException;
    List<ParametreCompteDTO> getAllParametreComptes();
    List<ParametreCompteDTO> getAllParametreComptesByRestaurant(RestaurantDTO restaurant);

    ModeleRecuDTO saveModelRecu(ModeleRecuDTO modeleRecuDTO);
    ModeleRecuDTO updateModeleRecu(Long id ,ModeleRecuDTO modeleRecuDTO) throws ModeleRecuNotFoundException, ParametreCompteNotFoundException;
    ModeleRecuDTO getModeleRecuById(Long id) throws ModeleRecuNotFoundException;
    void deleteModeleRecu(Long id) throws ModeleRecuNotFoundException;
    List<ModeleRecuDTO> getAllModeleRecus();

    List<ModeleRecuDTO> getAllModelRecusByRestaurant(RestaurantDTO restaurant);

    @Transactional
    List<ModeleRecuDTO> searchModelesRecus(
            String keyword);

    @Transactional
    List<ModeleRecuDTO> searchModelesRecusByRestaurant(
            RestaurantDTO restaurant,
            String keyword);
}
