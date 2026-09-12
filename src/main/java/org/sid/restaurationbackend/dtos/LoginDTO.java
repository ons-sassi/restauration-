package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Connexion universelle : un seul email + mot de passe pour
 * SUPERADMIN, EMPLOYEE et CLIENT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    private String email;
    private String mot_de_passe;
}
