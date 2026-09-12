package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteParReductionDTO {

    private Long reductionId;

    private String reduction;

    private String type;

    private Double valeur;

    private Integer nombreVentes;

    private Double montantReduction;

    private Double chiffreAffaires;
}
