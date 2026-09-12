package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.ClientNonAuthentifie;
import org.sid.restaurationbackend.entities.ElementMenu;
import org.sid.restaurationbackend.enums.Devise;
import org.sid.restaurationbackend.enums.StatutRestaurant;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;

import java.util.List;

public interface RestaurantService {
    RestaurantDTO saveRestaurant(RestaurantDTO restaurantDTO);

    RestaurantDTO getRestaurant(Long id) throws RestaurantNotFoundException;

    RestaurantDTO updateRestaurant(Long id,RestaurantDTO restaurantDTO) throws RestaurantNotFoundException;

    void deleteRestaurant(Long id) throws RestaurantNotFoundException;

    List<RestaurantDTO> getAllRestaurants();

    List<RestaurantDTO> getRestaurantByName(String name);

    List<RestaurantDTO> getRestaurantByAdresse(String adresse);


    List<RestaurantDTO> searchRestaurants(String keyword);

    List<RestaurantDTO> getRestaurantsByStatut(
            StatutRestaurant statut);

    List<RestaurantDTO> getRestaurantsByDevise(
            Devise devise);
}
