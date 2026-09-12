package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutBonCommande;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonDeCommandeStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_bon_commande;

    private LocalDate dateCommande;
    @Enumerated(EnumType.STRING)
    private StatutBonCommande statut;
    private LocalDateTime dateDeLivraison;
    private LocalDate dateDeLivraisonPrevu;
    private Double montant_total;

    @ManyToOne
    @JoinColumn(name = "id_fournisseur")
    private Fournisseur fournisseur;

    // SÉCURITÉ MULTI-RESTAURANT — Lot 2.7 (voir Fournisseur.java).
    // Toujours dérivé du restaurant du fournisseur choisi au moment de
    // la création (voir BonDeCommandeStockServiceImpl
    // .passerUneBonDeCommande) — jamais fourni directement par le
    // frontend, colonne directe malgré tout (plutôt qu'un recalcul via
    // "fournisseur" à chaque lecture) pour rester cohérent avec
    // Fournisseur/Ingredient et robuste si "fournisseur" est un jour
    // rendu nullable.
    @ManyToOne
    @JoinColumn(name = "id_restaurant", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "bonCommande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneBonDeCommande> lignes;
}
