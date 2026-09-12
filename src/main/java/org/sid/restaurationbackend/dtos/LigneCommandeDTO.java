package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.List;

@Data
public class LigneCommandeDTO {

    private Long id_ligne_commande;
    private Integer quantite;
    private Double prix_unitaire;

    /**
     * @deprecated champ texte libre historique, non exploitable pour les
     * statistiques. Utiliser modificateursSelectionnes.
     */
    @Deprecated
    private String modificateurs_choisis;

    private String remarque;

    // Relation ManyToOne
    private CommandeDTO commande;

    // Relation ManyToOne
    private ProduitDTO produit;

    // Relation OneToMany
    private List<LigneCommandeModificateurDTO> modificateursSelectionnes;
}
