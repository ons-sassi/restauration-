package org.sid.restaurationbackend.dtos;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.sid.restaurationbackend.enums.StatutBonCommande;

@Data
public class LigneBonDeCommandeDTO {

    private Long id_ligne;
    private Integer quantite_commandee;
    private Double prix_unitaire;

    @Enumerated(EnumType.STRING)
    private StatutBonCommande statut;

    // Relation ManyToOne
    private BonDeCommandeStockDTO bonCommande;

    // Relation ManyToOne
    private IngredientDTO ingredient;
}
