package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.StatutTable;

import java.util.Date;

@Data
public class TableRestaurantDTO {

    private Long id_table;
    private Integer numeroTable;
    private Integer capacite;
    private StatutTable statut;
    private String codeQr;
    private String urlQr;
    private Boolean qrActif;
    private Date dateGenerationQr;

    // Position dans le plan visuel du restaurant (futur plan de salle)
    private Integer positionX;
    private Integer positionY;

    // Relation ManyToOne
    private RestaurantDTO restaurant;

    // Relation ManyToOne
    private EmployeeDTO serveurAttribue;

    // Relation ManyToOne
    private EmployeeDTO generePar;
}
