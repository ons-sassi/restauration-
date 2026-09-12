package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.dtos.CategorieDTO;
import org.sid.restaurationbackend.entities.Categorie;
import org.sid.restaurationbackend.entities.Ingredient;
import org.sid.restaurationbackend.entities.Produit;
import org.sid.restaurationbackend.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Optional<Produit> findByNom(String nom);

    List<Produit> findByDisponible(Boolean disponible);

    List<Produit> findByIngredientsContains(Ingredient ingredient);

    List<Produit> findByCategorieParent(Categorie categorie);

    List<Produit> findByRestaurant(Restaurant restaurant);


    @Query("""
        SELECT p
        FROM Produit p
        WHERE (:nom IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :nom, '%')))
        AND (:categorie IS NULL OR p.categorieParent = :categorie)
        AND (:restaurant IS NULL OR p.restaurant = :restaurant)
        AND (:disponible IS NULL OR p.disponible = :disponible)
    """)
    List<Produit> search(
            @Param("nom") String nom,
            @Param("categorie") Categorie categorie,
            @Param("restaurant") Restaurant restaurant,
            @Param("disponible") Boolean disponible
    );
}
