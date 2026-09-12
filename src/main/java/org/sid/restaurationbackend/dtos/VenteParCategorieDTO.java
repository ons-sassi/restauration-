package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteParCategorieDTO {

    private Long categorieId;

    private String categorie;

    private Integer quantiteVendue;

    private Double chiffreAffaires;

    private Double cout;

    private Double margeBrute;
}