package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.ModeleRecu;
import org.sid.restaurationbackend.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ModeleRecuRepository extends JpaRepository<ModeleRecu, Long> {

    @Query("SELECT m FROM ModeleRecu m " +
           "WHERE m.restaurant = :restaurant " +
           "ORDER BY m.date_modification DESC, m.id_modele DESC")
    List<ModeleRecu> findByRestaurant(@Param("restaurant") Restaurant restaurant);

    Collection<ModeleRecu> findByNomModeleContainingIgnoreCase(String trim);

    Collection<ModeleRecu> findByRestaurantAndNomModeleContainingIgnoreCase(Restaurant restaurant, String trim);

    @Query("SELECT m FROM ModeleRecu m " +
           "WHERE m.restaurant = :restaurant " +
           "ORDER BY m.date_modification DESC, m.id_modele DESC")
    List<ModeleRecu> findRecentsByRestaurant(@Param("restaurant") Restaurant restaurant);

    @Query("SELECT m FROM ModeleRecu m " +
           "WHERE m.restaurant = :restaurant " +
           "ORDER BY m.date_modification DESC, m.id_modele DESC")
    Optional<ModeleRecu> findTopByRestaurantOrderByDateModificationDescIdModeleDesc(
            @Param("restaurant") Restaurant restaurant);
}
