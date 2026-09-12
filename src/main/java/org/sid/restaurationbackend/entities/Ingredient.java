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
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ingredient;

    private String nom;
    private String unite_mesure;
    private Double quantite_stock;
    private Double seuil_alerte;
    private Date date_peremption;

    @ManyToOne
    @JoinColumn(name = "id_fournisseur")
    private Fournisseur fournisseur;

    // SÉCURITÉ MULTI-RESTAURANT — Lot 2.7 (voir Fournisseur.java) :
    // colonne DIRECTE plutôt que dérivée de "fournisseur", qui reste
    // nullable (un ingrédient peut exister sans fournisseur assigné) —
    // et surtout parce que quantite_stock est un niveau de stock
    // PHYSIQUE, propre à un restaurant : deux restaurants ne peuvent
    // pas partager le même compteur de stock.
    @ManyToOne
    @JoinColumn(name = "id_restaurant", nullable = false)
    private Restaurant restaurant;

    @ManyToMany(mappedBy = "ingredients")
    private List<Produit> produits;

    @OneToMany(mappedBy = "ingredient")
    private List<LigneBonDeCommande> lignesCommande;

    @OneToMany(mappedBy = "ingredient")
    private List<PrevisionStock> previsions;
}
