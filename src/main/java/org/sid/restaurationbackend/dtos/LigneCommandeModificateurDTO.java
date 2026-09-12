package org.sid.restaurationbackend.dtos;

import lombok.Data;

@Data
public class LigneCommandeModificateurDTO {

    private Long id_ligne_commande_modificateur;
    private Integer quantite;

    // Relation ManyToOne
    private LigneCommandeDTO ligneCommande;

    // Relation ManyToOne
    private ModificateurDTO modificateur;
}
