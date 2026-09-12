package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.StatutVersement;

import java.util.Date;

@Data
public class VersementSalaireDTO {

    private Long id_versement;
    private String periode;
    private Double montant;
    private Date date_versement;
    private StatutVersement statut;

    // Relation ManyToOne
    private EmployeeDTO employee;
}
