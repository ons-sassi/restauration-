package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.StatutPriseEnCharge;

import java.util.Date;

@Data
public class PriseEnChargeTableDTO {

    private Long id_prise_en_charge;

    // Relation ManyToOne
    private TableRestaurantDTO table;

    // Relation ManyToOne
    private EmployeeDTO employee;

    private Date dateDebut;
    private Date dateFin;

    private StatutPriseEnCharge statut;
}
