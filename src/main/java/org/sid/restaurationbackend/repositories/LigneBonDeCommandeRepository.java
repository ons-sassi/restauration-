package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.BonDeCommandeStock;
import org.sid.restaurationbackend.entities.Ingredient;
import org.sid.restaurationbackend.entities.LigneBonDeCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneBonDeCommandeRepository extends JpaRepository<LigneBonDeCommande, Long> {
    List<LigneBonDeCommande> findByBonCommande(BonDeCommandeStock bonDeCommandeStock);
    List<LigneBonDeCommande> findByIngredient(Ingredient ingredient);
    List<LigneBonDeCommande> findByIngredientNomContainingIgnoreCase(String keyword);
}
