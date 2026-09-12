package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.OrigineSession;

import java.util.Date;

@Data
public class ClientNonAuthentifieDTO {

    private Long id_session;

    private String allergie;
    private String restaurant_choisi;
    private OrigineSession origineSession;
    private Date date_debut_session;
    private Date date_fin_session;

    // Relation ManyToOne
    private RestaurantDTO restaurant;

    // Relation ManyToOne
    private TableRestaurantDTO tableScannee;


}
