package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class PenaliteDTO {

    private Long id_penalite;
    private String motif;
    private Double montant_deduit;
    private Date date;

    // Relation ManyToOne
    private EmployeeDTO employee;
}
