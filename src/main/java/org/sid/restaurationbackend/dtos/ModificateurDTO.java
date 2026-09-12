package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.List;

@Data
public class ModificateurDTO {

    private Long id_modificateur;
    private String nom_modificateur;
    private Double prix_supplementaire;

    // Relation ManyToMany : un modificateur peut être associé à plusieurs produits.
    private List<ProduitDTO> produits;
}
