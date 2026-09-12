package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.List;

/**
 * Une ligne du panier envoyée par l'espace client au moment de la
 * finalisation (étape 5). Volontairement minimal : le frontend n'envoie
 * que des identifiants + une quantité, jamais un prix — le prix réel
 * (produit.prix + modificateurs) est toujours recalculé côté backend
 * dans ClientCommandeServiceImpl, jamais fait confiance depuis Angular.
 */
@Data
public class ClientLigneCommandeRequestDTO {

    private Long produitId;
    private Integer quantite;

    /**
     * Identifiants des modificateurs cochés sur cette ligne (peut être
     * vide/null). Chacun est revérifié comme appartenant bien au produit
     * ET au restaurant du client connecté avant d'être appliqué.
     */
    private List<Long> modificateurIds;

    private String remarque;
}
