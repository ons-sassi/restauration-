package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class ClientAuthentifieDTO extends UtilisateurDTO {
    private String adresse;
    private String preferences;
    private String allergie;
    private String codeParrainage;
    private Date date_modification_profil;

    // Sécurité multi-restaurant (voir entité ClientAuthentifie) :
    // privé par restaurant côté back-office.
    private RestaurantDTO restaurant;
}
