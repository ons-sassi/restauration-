package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.entities.Utilisateur;
import org.sid.restaurationbackend.enums.StatutEnvoi;
import org.sid.restaurationbackend.enums.TypeNotification;

import java.util.Date;

@Data
public class NotificationDTO {

    private Long id_notification;
    private TypeNotification type;
    private String contenu;
    private Date dateEnvoi;
    private StatutEnvoi statutEnvoi;
    private String declencheur;

    // Relation ManyToOne
    private UtilisateurDTO destinataire;
    private UtilisateurDTO emetteur;
}
