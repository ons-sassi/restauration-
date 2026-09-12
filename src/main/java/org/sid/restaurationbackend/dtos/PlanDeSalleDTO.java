package org.sid.restaurationbackend.dtos;

import lombok.Data;


import java.util.Date;

@Data
public class PlanDeSalleDTO {

    private Long id_plan;

    private String disposition_tables;

    private Date dateMiseAJour;



    // Relation ManyToOne
    private RestaurantDTO restaurant;
}
