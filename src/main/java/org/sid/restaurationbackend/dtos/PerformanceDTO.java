package org.sid.restaurationbackend.dtos;

import lombok.Data;

@Data
public class PerformanceDTO {

    private Long id_performance;
    private String periode;
    private Integer nombre_ventes;
    private Double chiffre_affaire_genere;
    private Double note_evaluation;

    // Relation ManyToOne
    private EmployeeDTO employee;
}
