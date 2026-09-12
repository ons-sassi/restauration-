package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteParModePaiementDTO {

    private Long modePaiementId;

    private String modePaiement;

    private Integer nombreVentes;

    private Double chiffreAffaires;

    private Double pourcentage;
}