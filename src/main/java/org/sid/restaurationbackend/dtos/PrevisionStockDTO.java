package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class PrevisionStockDTO {

    private Long id_prevision;
    private Double quantite_prevue;
    private String periode;
    private Boolean baseeSurVentes;
    private Date date_generation;

    // Relation ManyToOne
    private IngredientDTO ingredient;
}
