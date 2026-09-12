package org.sid.restaurationbackend.dtos;

import lombok.Data;

@Data
public class FournisseurDTO {

    private Long id_fournisseur;
    private String nom;
    private String numTel;
    private String adresse;
    private Integer delai_livraison_moyen;
    private String email;

    // Relation ManyToOne
    private RestaurantDTO restaurant;
}
