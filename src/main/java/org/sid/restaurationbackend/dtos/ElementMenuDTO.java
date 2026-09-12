package org.sid.restaurationbackend.dtos;

import lombok.Data;

@Data
public class ElementMenuDTO {

    private Long id_element;
    private String nom;
    private Integer ordre_affichage;
    private String image;

    // Relation ManyToOne
    private CategorieDTO categorieParent;

    // Relation ManyToOne
    private RestaurantDTO restaurant;
}
