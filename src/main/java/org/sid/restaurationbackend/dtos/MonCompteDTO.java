package org.sid.restaurationbackend.dtos;

import lombok.Data;

/**
 * =============================================================
 * DTO DÉDIÉ À LA PAGE "MON COMPTE"
 * =============================================================
 *
 * Sert l'auto-modification du compte connecté, quel que soit son
 * type réel (Employee, SuperAdmin, ClientAuthentifie) : ne porte
 * que les champs communs définis sur Utilisateur (nom, prénom,
 * email, téléphone, photo, mot de passe), plus deux champs
 * d'affichage en lecture seule (matricule, nomRole) renseignés
 * uniquement quand le compte connecté est un Employee.
 *
 * Volontairement distinct de UtilisateurDTO : celui-ci est déjà
 * réutilisé ailleurs (ex. NotificationController, pour représenter
 * emetteur/destinataire) et n'expose pas mot_de_passe — l'y ajouter
 * pour les besoins de cette page aurait fait fuiter le hash du mot
 * de passe dans des réponses qui n'ont rien à voir avec "Mon compte".
 */
@Data
public class MonCompteDTO {

    private Long id_utilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String mot_de_passe;
    private String photo_profil;

    // Renseignés uniquement si le compte connecté est un Employee ;
    // restent null pour un SuperAdmin ou un ClientAuthentifie.
    private String matricule;
    private String nomRole;

    // =============================================================
    // CHAMPS CLIENTAUTHENTIFIE
    // =============================================================
    // Renseignés uniquement si le compte connecté est un
    // ClientAuthentifie ; restent null pour un Employee ou un
    // SuperAdmin (voir MonCompteController.toDto).
    //
    // adresse / allergie / preferences : modifiables par le client
    // lui-même (voir updateMonCompte).
    //

    private String adresse;
    private String allergie;
    private String preferences;
    private String codeParrainage;
}