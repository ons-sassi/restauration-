package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.StatutBonCommande;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class BonDeCommandeStockDTO {

    private Long id_bon_commande;
    private LocalDate dateCommande;
    private StatutBonCommande statut;
    private LocalDateTime dateDeLivraison;
    private LocalDate dateDeLivraisonPrevu;
    private Double montant_total;

    // Relation ManyToOne
    private FournisseurDTO fournisseur;

    // Lot 2.7 : privé par restaurant, dérivé du fournisseur à la
    // création (voir entité BonDeCommandeStock)
    private RestaurantDTO restaurant;
}
