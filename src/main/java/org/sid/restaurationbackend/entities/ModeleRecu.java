package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeleRecu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_modele;

    private String nomModele;
    private String entete_personnalisee;
    private String pied_de_page_personnalise;
    private Boolean afficher_logo;
    private Boolean afficher_infos_client;
    private Boolean afficher_commentaire_client;
    private Boolean afficher_modificateurs_commande;
    private Boolean afficher_categorie_article;
    private Boolean afficher_allergies_client;

    /** Ordre des blocs du reçu, séparés par des virgules. */
    @Column(length = 2000)
    private String ordre_elements;

    /** Paramètres visuels partagés par le back-office et le client. */
    private Integer largeur_ticket;
    private Integer taille_police;
    private String famille_police;
    private String alignement;
    private Boolean afficher_ligne_separation;
    private Date date_creation;
    private Date date_modification;

    @ManyToOne
    @JoinColumn(name = "id_restaurant")
    private Restaurant restaurant;

    @OneToMany(mappedBy = "modele")
    private List<Recu> recus;
}
