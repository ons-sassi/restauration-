

package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteParRecuDTO {

    private Long id_vente;

    private String numeroRecu;

    private Date dateEmission;

    private Date dateVente;

    private Double montantTtc;

    private String modePaiement;

    private String pointDeVente;
}

