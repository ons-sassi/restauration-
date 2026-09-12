package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Categorie;
import org.sid.restaurationbackend.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    Optional<Categorie> findByNom(String nom);

    Collection<Categorie> findByCategorieParent(Categorie categorie);

    Collection<Categorie> findByRestaurant(Restaurant restaurant);
}
