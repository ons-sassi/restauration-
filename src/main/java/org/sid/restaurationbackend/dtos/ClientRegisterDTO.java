package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auto-inscription d'un client public (POST /api/auth/register/client).
 *
 * ClientAuthentifie.restaurant est une colonne obligatoire
 * (nullable = false) : le client choisit donc son restaurant AVANT
 * de s'inscrire (voir écran "Choisir un restaurant" côté Angular),
 * d'où le champ restaurantId ci-dessous.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisterDTO {

    private String nom;
    private String prenom;
    private String email;
    private String mot_de_passe;
    private String telephone;
    private Long restaurantId;
}
