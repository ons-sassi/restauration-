package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.Devise;
import org.sid.restaurationbackend.enums.LangueParDefaut;
import org.sid.restaurationbackend.enums.StatutRestaurant;

import java.time.LocalTime;

@Data
public class RestaurantDTO {

    private Long id_restaurant;
    private String nomRestaurant;
    private String adresse;
    private String logo;
    private Devise devise;
    private LangueParDefaut langue_par_defaut;
    private LocalTime horaires_ouverture;
    private LocalTime horaires_fermeture;
    private StatutRestaurant statut;
}
