package org.sid.restaurationbackend.dtos;

import lombok.Data;

@Data
public class PointDeVenteDTO {

    private Long id_pdv;
    private String nomPdv;
    private String appareil_pos;
    private String statutConnexion;

    // Relation ManyToOne
    private RestaurantDTO restaurant;
}
