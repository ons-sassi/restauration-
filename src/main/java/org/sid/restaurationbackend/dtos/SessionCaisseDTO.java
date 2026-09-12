package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class SessionCaisseDTO {

    private Long id_session_caisse;
    private Double montantOuverture;
    private Double montantFermeture;
    private Date dateOuverture;
    private Date dateFermeture;
    private Double ecartCaisse;

    // Relation ManyToOne
    private PointDeVenteDTO pointDeVente;

    // Relation ManyToOne
    private EmployeeDTO employee;
}
