package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_vente;

    private Date dateVente;

    private Double montantHt;

    private Double montantTtc;

    /**
     * Réduction appliquée à la vente.
     */
    @ManyToOne
    @JoinColumn(name = "id_reduction")
    private Reduction reduction;

    /**
     * Montant réel de la réduction appliquée.
     *
     * Exemple :
     * 100 DT - 10 DT = 90 DT
     */
    private Double montantReduction;

    @ManyToOne
    @JoinColumn(name = "id_commande")
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "id_pdv")
    private PointDeVente pointDeVente;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "id_mode_paiement")
    private ModePaiement modePaiement;

    /**
     * Reçu associé à cette vente.
     *
     * cascade = ALL + orphanRemoval = true :
     * quand on supprime la vente, Hibernate supprime
     * automatiquement le reçu lié (dans le bon ordre,
     * en gérant lui-même la contrainte de clé étrangère),
     * sans qu'on ait besoin de casser la relation à la main.
     */
    @OneToOne(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Recu recu;
}