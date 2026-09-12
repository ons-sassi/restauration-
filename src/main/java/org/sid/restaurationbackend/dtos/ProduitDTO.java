package org.sid.restaurationbackend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProduitDTO extends ElementMenuDTO {
    private String description;
    private Double prix;
    private Boolean disponible;
    private Integer temps_preparation;

    /**
     * Coût de revient unitaire du produit, utilisé pour calculer
     * la marge brute des ventes.
     */
    @JsonProperty("cout_unitaire")
    private Double coutUnitaire;

    // Relation ManyToMany
    private List<IngredientDTO> ingredients;
}