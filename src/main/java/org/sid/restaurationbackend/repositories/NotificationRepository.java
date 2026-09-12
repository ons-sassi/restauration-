package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Notification;
import org.sid.restaurationbackend.entities.Utilisateur;
import org.sid.restaurationbackend.enums.StatutEnvoi;
import org.sid.restaurationbackend.enums.TypeNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDestinataire(Utilisateur destinataire);

    List<Notification> findByEmetteur(Utilisateur emetteur);

    List<Notification> findByDestinataireAndStatutEnvoi(
            Utilisateur destinataire,
            StatutEnvoi statutEnvoi
    );

    List<Notification> findByDestinataireAndType(
            Utilisateur destinataire,
            TypeNotification type
    );

    List<Notification> findByStatutEnvoi(StatutEnvoi statutEnvoi);

    List<Notification> findByType(TypeNotification type);

    List<Notification> findByDestinataireOrderByDateEnvoiDesc(
            Utilisateur destinataire
    );
}
