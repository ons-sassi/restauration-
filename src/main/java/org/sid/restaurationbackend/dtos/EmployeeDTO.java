package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutPresence;
import org.sid.restaurationbackend.enums.StatutUtilisateur;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private Long id_utilisateur;

    private String nom;

    private String prenom;

    private String email;

    private String telephone;

    private String mot_de_passe;

    private Date date_creation;

    private Date date_derniere_connection;

    private StatutUtilisateur statut;

    private String photo_profil;


    // =========================================================
    // INFORMATIONS EMPLOYE
    // =========================================================

    private String matricule;

    private Date date_embauche;

    private String codePin;

    private Long salaire_base;

    private StatutPresence statutPresence;

    // Phase 3C : éligibilité à recevoir une prise en charge (auto ou
    // manuelle). Voir Employee.eligibleAttributionAutomatique.
    private Boolean eligibleAttributionAutomatique;


    // =========================================================
    // ROLE
    // =========================================================

    private RoleDTO role;


    // =========================================================
    // RESTAURANT
    // =========================================================

    private RestaurantDTO restaurant;


    // =========================================================
    // POINT DE VENTE
    // =========================================================

    private PointDeVenteDTO pdvAffecte;
}