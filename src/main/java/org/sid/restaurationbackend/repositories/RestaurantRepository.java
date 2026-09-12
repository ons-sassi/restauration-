package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Restaurant;
import org.sid.restaurationbackend.enums.Devise;
import org.sid.restaurationbackend.enums.StatutRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByNomRestaurant(String nomRestaurant);
    List<Restaurant> findByAdresse(String adresse);

    List<Restaurant> findByNomRestaurantContainingIgnoreCaseOrAdresseContainingIgnoreCase(String trim, String trim1);
    List<Restaurant> findByStatut(StatutRestaurant statut);

    List<Restaurant> findByDevise(Devise devise);
}
