package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteGraphDTO {

    /**
     * Libellé de la période affichée sur le graphique.
     *
     * Exemples :
     *
     * JOUR       -> 09:00
     * SEMAINE    -> Lundi
     * MOIS       -> 01
     * TRIMESTRE  -> Juillet
     * ANNEE      -> Janvier
     */
    private String periode;

    /**
     * Montant des ventes pour cette période.
     */
    private Double montant;
}