package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutBonCommande;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneBonDeCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ligne;

    private Integer quantite_commandee;
    private Double prix_unitaire;

    @Enumerated(EnumType.STRING)
    private StatutBonCommande statut;

    @ManyToOne
    @JoinColumn(name = "id_bon_commande")
    private BonDeCommandeStock bonCommande;

    @ManyToOne
    @JoinColumn(name = "id_ingredient")
    private Ingredient ingredient;
}
