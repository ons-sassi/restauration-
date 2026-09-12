package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_fournisseur;

    private String nom;
    private String numTel;
    private String adresse;
    private Integer delai_livraison_moyen;
    private String email;

    // =========================================================
    // SÉCURITÉ MULTI-RESTAURANT — Lot 2.7
    // =========================================================
    // Décision produit retenue : Fournisseur (comme Ingredient et
    // BonDeCommandeStock) est PRIVÉ par restaurant, pas un catalogue
    // partagé sur la plateforme. Chaque restaurant gère sa propre
    // liste de fournisseurs, même si deux restaurants font en
    // pratique appel au même fournisseur physique (pas de
    // déduplication au niveau du modèle, par cohérence avec le reste
    // de l'app : Employee, Produit, etc. sont déjà dupliqués par
    // restaurant plutôt que partagés).
    @ManyToOne
    @JoinColumn(name = "id_restaurant", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "fournisseur")
    private List<Ingredient> ingredients;

    @OneToMany(mappedBy = "fournisseur")
    private List<BonDeCommandeStock> bons;
}