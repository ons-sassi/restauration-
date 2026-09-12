package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.TypeNotification;
import org.sid.restaurationbackend.enums.StatutEnvoi;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_notification;
    @ManyToOne
    @JoinColumn(name = "emetteur")
    private Utilisateur emetteur;

    @ManyToOne
    @JoinColumn(name = "destinataire")
    private Utilisateur destinataire;

    @Enumerated(EnumType.STRING)
    private TypeNotification type;
    private String contenu;
    private Date dateEnvoi;
    @Enumerated(EnumType.STRING)
    private StatutEnvoi statutEnvoi;
    private String declencheur;
}
