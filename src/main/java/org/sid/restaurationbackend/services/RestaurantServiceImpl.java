package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.entities.Restaurant;
import org.sid.restaurationbackend.enums.Devise;
import org.sid.restaurationbackend.enums.StatutRestaurant;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
@AllArgsConstructor
@Slf4j

public class RestaurantServiceImpl implements RestaurantService {
    private RestaurantMapper dtotMapper;
    private RestaurantRepository restaurantRepository;


    @Override
    public RestaurantDTO saveRestaurant(RestaurantDTO restaurantDTO) {

        Restaurant restaurant =
                dtotMapper.fromRestaurantDTO(restaurantDTO);

        if (restaurant.getStatut() == null) {
            restaurant.setStatut(StatutRestaurant.ACTIF);
        }

        Restaurant savedRestaurant =
                restaurantRepository.save(restaurant);

        return dtotMapper.fromRestaurant(savedRestaurant);
    }
    @Override
    public RestaurantDTO getRestaurant(Long id) throws RestaurantNotFoundException {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(()-> new RestaurantNotFoundException("restaurant not found"));
        return dtotMapper.fromRestaurant(restaurant);

    }

    @Override
    public RestaurantDTO updateRestaurant(Long id ,RestaurantDTO restaurantDTO) throws RestaurantNotFoundException {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(()-> new RestaurantNotFoundException("restaurant not found"));

        dtotMapper.updateRestaurantFromDto(restaurantDTO, restaurant);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return dtotMapper.fromRestaurant(updatedRestaurant);
    }

    @Override
    public void deleteRestaurant(Long id) throws RestaurantNotFoundException {
        Restaurant oldRestaurant=restaurantRepository.findById(id).orElseThrow(()->new RestaurantNotFoundException("restaurant not found"));
        restaurantRepository.delete(oldRestaurant);
    }

    @Override
    public List<RestaurantDTO> getAllRestaurants() {

        return restaurantRepository.findAll().stream().map(dtotMapper::fromRestaurant).collect(Collectors.toList());
    }

    @Override
    public List<RestaurantDTO> getRestaurantByName(String name)  {
        return restaurantRepository.findByNomRestaurant(name).stream().map(dtotMapper::fromRestaurant).collect(Collectors.toList());
    }

    @Override
    public List<RestaurantDTO> getRestaurantByAdresse(String adresse) {
        return restaurantRepository.findByAdresse(adresse).stream().map(dtotMapper::fromRestaurant).collect(Collectors.toList());

    }
    @Override
    public List<RestaurantDTO> searchRestaurants(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllRestaurants();
        }

        return restaurantRepository
                .findByNomRestaurantContainingIgnoreCaseOrAdresseContainingIgnoreCase(
                        keyword.trim(),
                        keyword.trim()
                )
                .stream()
                .map(dtotMapper::fromRestaurant)
                .toList();
    }

    @Override
    public List<RestaurantDTO> getRestaurantsByStatut(
            StatutRestaurant statut) {

        return restaurantRepository.findByStatut(statut)
                .stream()
                .map(dtotMapper::fromRestaurant)
                .toList();
    }

    @Override
    public List<RestaurantDTO> getRestaurantsByDevise(
            Devise devise) {

        return restaurantRepository.findByDevise(devise)
                .stream()
                .map(dtotMapper::fromRestaurant)
                .toList();
    }




}


