package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Un produit favori du client connecté. Les infos produit utiles à
 * l'affichage (nom, image, prix, disponibilité) sont déjà résolues ici
 * pour que l'écran Angular n'ait pas besoin d'un aller-retour
 * supplémentaire vers le menu pour chaque favori — même principe que
 * ClientLigneCommandeConfirmationDTO pour l'historique de commandes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientFavoriDTO {

    private Long id_favori;
    private Long produitId;
    private String nomProduit;
    private String imageProduit;
    private Double prixProduit;
    private Boolean disponibleProduit;
    private Date dateAjout;
}
