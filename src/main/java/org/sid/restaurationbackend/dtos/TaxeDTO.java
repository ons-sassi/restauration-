package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class TaxeDTO {

    private Long id_taxe;
    private String nom_taxe;
    private Double taux;
    private String applicable_a;
    private Date date_creation;
    private String statut;
}
