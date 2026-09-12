package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.ApplicationReduction;
import org.sid.restaurationbackend.enums.TypeReduction;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_reduction;

    // SÉCURITÉ MULTI-RESTAURANT — famille "Stock/Fidélité" : aucune
    // colonne restaurant, aucun scoping indirect fiable (produits est
    // vide si TOUS_PRODUITS, ventes ne se peuple qu'après usage réel).
    // Le front (menu/reductions back-office) la traite clairement
    // comme une ressource de gestion PAR restaurant : colonne directe,
    // même pattern que Fournisseur/Ingredient (Lot 2.7).
    @ManyToOne
    @JoinColumn(name = "id_restaurant", nullable = false)
    private Restaurant restaurant;

    private String nom_reduction;

    @Enumerated(EnumType.STRING)
    private TypeReduction type;

    /**
     * Valeur de la réduction.
     *
     * Si type = POURCENTAGE :
     *     valeur = pourcentage, exemple 10 = 10%
     *
     * Si type = MONTANT :
     *     valeur = montant fixe, exemple 10 = 10 DT
     */
    private Double valeur;

    private Date dateDebut;

    private Date dateFin;

    private String conditions_application;

    /**
     * Détermine si la réduction s'applique à tous les produits
     * ou seulement à une sélection précise (voir {@link #produits}).
     */
    @Enumerated(EnumType.STRING)
    private ApplicationReduction applicationProduits = ApplicationReduction.TOUS_PRODUITS;

    /**
     * Produits concernés par la réduction lorsque
     * applicationProduits = PRODUITS_SPECIFIQUES.
     * Ignoré (et non utilisé) lorsque applicationProduits = TOUS_PRODUITS.
     */
    @ManyToMany
    @JoinTable(
            name = "reduction_produit",
            joinColumns = @JoinColumn(name = "id_reduction"),
            inverseJoinColumns = @JoinColumn(name = "id_element")
    )
    private List<Produit> produits;

    /**
     * Montant minimum (HT) de la commande à partir duquel la réduction
     * devient applicable.
     *
     * null ou 0 => la réduction est applicable quel que soit le montant
     * de la commande.
     */
    private Double montantMinimum;

    /**
     * Si vrai, la réduction est appliquée automatiquement dès que ses
     * conditions sont réunies (dates de validité, montant minimum,
     * produits concernés), sans sélection manuelle par le caissier.
     *
     * Si faux, la réduction reste disponible mais doit être choisie
     * manuellement.
     */
    private Boolean automatique = true;

    /**
     * Active manuellement la réduction.
     *
     * Si faux, la réduction est totalement désactivée : elle n'est ni
     * proposée automatiquement, ni sélectionnable manuellement en
     * caisse, même si ses dates de validité et ses autres conditions
     * sont respectées. Permet d'activer/désactiver une réduction sans
     * avoir à modifier ses dates.
     */
    private Boolean active = true;

    /**
     * Nombre maximum de fois où cette réduction peut être appliquée
     * (toutes ventes confondues).
     *
     * null => nombre d'applications illimité.
     */
    private Integer nombreApplicationsAutorise;

    /**
     * Nombre de fois où cette réduction a déjà été appliquée sur une
     * vente. Incrémenté automatiquement par le backend, ne doit
     * jamais être modifié manuellement depuis le front.
     */
    private Integer nombreApplicationsEffectuees = 0;

    /**
     * Ventes sur lesquelles cette réduction a été appliquée.
     */
    @OneToMany(mappedBy = "reduction")
    private List<Vente> ventes;
}