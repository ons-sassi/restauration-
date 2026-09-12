package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.NotificationDTO;
import org.sid.restaurationbackend.dtos.UtilisateurDTO;
import org.sid.restaurationbackend.enums.StatutEnvoi;
import org.sid.restaurationbackend.enums.TypeNotification;
import org.sid.restaurationbackend.exceptions.NotificationNotFoundException;

import java.util.List;

public interface NotificationService {
    NotificationDTO saveNotification(NotificationDTO notificationDTO);

    NotificationDTO updateNotification(
            Long id,
            NotificationDTO notificationDTO
    ) throws NotificationNotFoundException;

    void deleteNotification(Long id)
            throws NotificationNotFoundException;

    NotificationDTO getNotification(Long id)
            throws NotificationNotFoundException;

    List<NotificationDTO> getAllNotifications();

    List<NotificationDTO> getNotificationsByDestinataire(
            UtilisateurDTO destinataire
    );

    List<NotificationDTO> getNotificationsByEmetteur(
            UtilisateurDTO emetteur
    );

    List<NotificationDTO> getNotificationsByStatut(
            StatutEnvoi statutEnvoi
    );

    List<NotificationDTO> getNotificationsByType(
            TypeNotification type
    );

    List<NotificationDTO> getNotificationsByDestinataireAndStatut(
            UtilisateurDTO destinataire,
            StatutEnvoi statutEnvoi
    );

    List<NotificationDTO> getNotificationsByDestinataireAndType(
            UtilisateurDTO destinataire,
            TypeNotification type
    );

    List<NotificationDTO> getNotificationsDestinataireRecentes(
            UtilisateurDTO destinataire
    );
}
