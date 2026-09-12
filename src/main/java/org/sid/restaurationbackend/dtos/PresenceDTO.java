package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.StatutPresence;

import java.util.Date;

@Data
public class PresenceDTO {

    private Long id_presence;
    private Date date;
    private String heure_arrivee;
    private String heure_depart;
    private Double heures_travaillees_total;

    // Statut du jour marqué par le responsable : PRESENT / ABSENT / CONGE
    private StatutPresence statut;

    // Relation ManyToOne
    private EmployeeDTO employee;
}
