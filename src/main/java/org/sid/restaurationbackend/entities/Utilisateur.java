package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sid.restaurationbackend.enums.StatutUtilisateur;

import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_utilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String mot_de_passe;
    private Date date_creation;
    private Date date_derniere_connection;
    @Enumerated(EnumType.STRING)
    private StatutUtilisateur statut;
    private String photo_profil;

    @OneToMany(mappedBy = "destinataire")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Notification> notifications;

    @OneToMany(mappedBy = "emetteur")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Notification> notifications_emises;
}