package org.sid.restaurationbackend.dtos;

import lombok.Data;

@Data
public class ParametreCompteDTO {

    private Long id_parametre;
    private String langue;
    private String devise;
    private String logo;
    private Boolean afficher_infos_client_sur_recu;
    private Boolean afficher_commentaire;
    private String option_restauration;

    // Relation ManyToOne
    private RestaurantDTO restaurant;
}
