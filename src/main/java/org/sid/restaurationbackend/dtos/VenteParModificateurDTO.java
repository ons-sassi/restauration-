package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteParModificateurDTO {

    private Long modificateurId;

    private String modificateur;

    private String produit;

    private Integer quantiteVendue;

    private Double chiffreAffaires;
}
