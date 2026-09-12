package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteRecapitulatifDTO {

    /**
     * Chiffre d'affaires encaissé.
     */
    private Double chiffreAffaires;

    /**
     * Montant total avant réduction.
     */
    private Double venteBrute;

    /**
     * Total réel des réductions appliquées.
     */
    private Double reductions;

    /**
     * Montant total après réduction.
     */
    private Double venteNette;

    /**
     * Marge brute réelle.
     */
    private Double margeBrute;

    /**
     * Évolution des ventes.
     */
    private List<VenteGraphDTO> evolution;
}