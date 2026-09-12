package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.NotificationDTO;
import org.sid.restaurationbackend.dtos.UtilisateurDTO;
import org.sid.restaurationbackend.entities.Notification;

import org.sid.restaurationbackend.enums.StatutEnvoi;
import org.sid.restaurationbackend.enums.TypeNotification;
import org.sid.restaurationbackend.exceptions.NotificationNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;
import java.util.List;
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final RestaurantMapper dtotMapper;
    private final NotificationRepository notificationRepository;

    @Override
    public NotificationDTO saveNotification(NotificationDTO notificationDTO) {

        Notification notification =
                dtotMapper.fromNotificationDTO(notificationDTO);

        // Date automatique lors de la création
        if (notification.getDateEnvoi() == null) {
            notification.setDateEnvoi(new Date());
        }

        Notification savedNotification =
                notificationRepository.save(notification);

        return dtotMapper.fromNotification(savedNotification);
    }

    @Override
    public NotificationDTO updateNotification(
            Long id,
            NotificationDTO notificationDTO)
            throws NotificationNotFoundException {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new NotificationNotFoundException(
                                        "Notification not found"
                                ));

        dtotMapper.updateNotificationFromDto(
                notificationDTO,
                notification
        );

        Notification updatedNotification =
                notificationRepository.save(notification);

        return dtotMapper.fromNotification(updatedNotification);
    }

    @Override
    public void deleteNotification(Long id)
            throws NotificationNotFoundException {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new NotificationNotFoundException(
                                        "Notification not found"
                                ));

        notificationRepository.delete(notification);
    }

    @Override
    public NotificationDTO getNotification(Long id)
            throws NotificationNotFoundException {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new NotificationNotFoundException(
                                        "Notification not found"
                                ));

        return dtotMapper.fromNotification(notification);
    }

    @Override
    public List<NotificationDTO> getAllNotifications() {

        return notificationRepository.findAll()
                .stream()
                .map(dtotMapper::fromNotification)
                .toList();
    }

    @Override
    public List<NotificationDTO> getNotificationsByDestinataire(
            UtilisateurDTO destinataire) {

        return notificationRepository
                .findByDestinataire(
                        dtotMapper.fromUtilisateurDTO(destinataire)
                )
                .stream()
                .map(dtotMapper::fromNotification)
                .toList();
    }

    @Override
    public List<NotificationDTO> getNotificationsByEmetteur(
            UtilisateurDTO emetteur) {

        return notificationRepository
                .findByEmetteur(
                        dtotMapper.fromUtilisateurDTO(emetteur)
                )
                .stream()
                .map(dtotMapper::fromNotification)
                .toList();
    }

    @Override
    public List<NotificationDTO> getNotificationsByStatut(
            StatutEnvoi statutEnvoi) {

        return notificationRepository
                .findByStatutEnvoi(statutEnvoi)
                .stream()
                .map(dtotMapper::fromNotification)
                .toList();
    }

    @Override
    public List<NotificationDTO> getNotificationsByType(
            TypeNotification type) {

        return notificationRepository
                .findByType(type)
                .stream()
                .map(dtotMapper::fromNotification)
                .toList();
    }

    @Override
    public List<NotificationDTO>
    getNotificationsByDestinataireAndStatut(
            UtilisateurDTO destinataire,
            StatutEnvoi statutEnvoi) {

        return notificationRepository
                .findByDestinataireAndStatutEnvoi(
                        dtotMapper.fromUtilisateurDTO(destinataire),
                        statutEnvoi
                )
                .stream()
                .map(dtotMapper::fromNotification)
                .toList();
    }

    @Override
    public List<NotificationDTO>
    getNotificationsByDestinataireAndType(
            UtilisateurDTO destinataire,
            TypeNotification type) {

        return notificationRepository
                .findByDestinataireAndType(
                        dtotMapper.fromUtilisateurDTO(destinataire),
                        type
                )
                .stream()
                .map(dtotMapper::fromNotification)
                .toList();
    }

    @Override
    public List<NotificationDTO>
    getNotificationsDestinataireRecentes(
            UtilisateurDTO destinataire) {

        return notificationRepository
                .findByDestinataireOrderByDateEnvoiDesc(
                        dtotMapper.fromUtilisateurDTO(destinataire)
                )
                .stream()
                .map(dtotMapper::fromNotification)
                .toList();
    }
}
