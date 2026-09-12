package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.PlanDeSalle;
import org.sid.restaurationbackend.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanDeSalleRepository extends JpaRepository<PlanDeSalle, Long> {
    List<PlanDeSalle> findByRestaurant(Restaurant restaurant);
    Optional<PlanDeSalle> findFirstByRestaurantOrderByDateMiseAJourDesc(Restaurant restaurant );
}
