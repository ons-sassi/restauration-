package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.StatutUtilisateur;

import java.util.Date;

@Data
public class UtilisateurDTO {

    private Long id_utilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    private Date date_creation;
    private Date date_derniere_connection;
    private StatutUtilisateur statut;
    private String photo_profil;
}
