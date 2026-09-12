package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.ParametreCompte;
import org.sid.restaurationbackend.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametreCompteRepository extends JpaRepository<ParametreCompte, Long> {
    List<ParametreCompte> findByRestaurant(Restaurant restaurant);
}
