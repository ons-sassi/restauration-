package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.PointDeVente;
import org.sid.restaurationbackend.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointDeVenteRepository extends JpaRepository<PointDeVente, Long> {
    List<PointDeVente> findByRestaurant(Restaurant restaurant);


    List<PointDeVente> findByNomPdvContainingIgnoreCase(String name);

    List<PointDeVente> findByStatutConnexion(String statutConnexion);


}
