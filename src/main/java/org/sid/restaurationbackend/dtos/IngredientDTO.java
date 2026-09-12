package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class IngredientDTO {

    private Long id_ingredient;
    private String nom;
    private String unite_mesure;
    private Double quantite_stock;
    private Double seuil_alerte;
    private Date date_peremption;

    // Relation ManyToOne
    private FournisseurDTO fournisseur;

    // Lot 2.7 : privé par restaurant (voir entité Ingredient)
    private RestaurantDTO restaurant;

    // Relation ManyToMany (cote inverse)
    private List<ProduitDTO> produits;
}
