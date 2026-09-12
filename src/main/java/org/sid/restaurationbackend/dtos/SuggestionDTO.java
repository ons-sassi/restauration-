package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class SuggestionDTO {

    private Long id_suggestion;
    private String contenu;
    private Date dateCreation;
    private Boolean priseEnCompte;

    // Relation ManyToOne
    private ClientAuthentifieDTO client;

    // Lot Stock/Fidélité : privé par restaurant (voir entité Suggestion)
    private RestaurantDTO restaurant;
}
