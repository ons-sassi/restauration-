package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.dtos.IngredientDTO;
import org.sid.restaurationbackend.dtos.ProduitDTO;
import org.sid.restaurationbackend.entities.Fournisseur;
import org.sid.restaurationbackend.entities.Ingredient;
import org.sid.restaurationbackend.entities.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Collection<Ingredient> findByFournisseur(Fournisseur fournisseur);

    Collection<Ingredient> findByProduitsContains(Produit produit);




    @Query("""
    SELECT DISTINCT i
    FROM Ingredient i
    LEFT JOIN i.fournisseur f
    LEFT JOIN i.produits p
    WHERE (:nomIngredient IS NULL OR LOWER(i.nom) LIKE LOWER(CONCAT('%', :nomIngredient, '%')))
      AND (:nomFournisseur IS NULL OR LOWER(f.nom) LIKE LOWER(CONCAT('%', :nomFournisseur, '%')))
      AND (:nomProduit IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :nomProduit, '%')))
""")
    List<Ingredient> search(
            @Param("nomIngredient") String nomIngredient,
            @Param("nomFournisseur") String nomFournisseur,
            @Param("nomProduit") String nomProduit);


    @Query("""
    SELECT i
    FROM Ingredient i
    WHERE i.quantite_stock <= i.seuil_alerte
""")
    List<Ingredient> underSeuil();

    @Query("""
    SELECT i
    FROM Ingredient i
    WHERE i.date_peremption BETWEEN :aujourdhui AND :dateLimite
""")
    List<Ingredient> findIngredientsPresDePeremption(
            @Param("aujourdhui") Date aujourdhui,
            @Param("dateLimite") Date dateLimite);


    @Query("""
    SELECT i
    FROM Ingredient i
    WHERE i.date_peremption < :aujourdhui
""")
    List<Ingredient> findIngredientsPerimes(
            @Param("aujourdhui") Date aujourdhui);

}
