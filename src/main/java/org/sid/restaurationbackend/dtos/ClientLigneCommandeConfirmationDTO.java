package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Une ligne telle que réellement enregistrée en base, renvoyée après
 * création de la commande (étape 5). Les noms (produit, modificateurs)
 * sont déjà résolus en texte pour que l'écran de confirmation Angular
 * n'ait pas besoin de refaire d'appels supplémentaires.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientLigneCommandeConfirmationDTO {

    private Long id_ligne_commande;
    private String nomProduit;
    private Integer quantite;
    private Double prixUnitaire;
    private List<String> modificateurs;
    private String categorie;
    private String sousCategorie;
}
