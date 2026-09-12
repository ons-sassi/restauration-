package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produit extends ElementMenu {
    private String description;
    private Double prix;
    private Boolean disponible;
    private Integer temps_preparation;

    /**
     * Coût de revient unitaire du produit (ce que le restaurant paie
     * pour préparer 1 unité : ingrédients, etc.).
     *
     * Saisi manuellement en attendant un calcul automatique basé
     * sur une vraie recette (quantité par ingrédient).
     *
     * Utilisé pour calculer la marge brute des ventes.
     */
    private Double coutUnitaire;

    @ManyToMany
    @JoinTable(
            name = "produit_ingredient",
            joinColumns = @JoinColumn(name = "id_produit"),
            inverseJoinColumns = @JoinColumn(name = "id_ingredient")
    )
    private List<Ingredient> ingredients;

    @ManyToMany(mappedBy = "produits")
    private List<Modificateur> modificateurs;

    @OneToMany(mappedBy = "produit")
    private List<LigneCommande> lignesCommande;
}