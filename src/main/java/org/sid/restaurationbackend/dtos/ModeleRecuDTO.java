package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class ModeleRecuDTO {

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

    private String ordre_elements;
    private Integer largeur_ticket;
    private Integer taille_police;
    private String famille_police;
    private String alignement;
    private Boolean afficher_ligne_separation;
    private Date date_creation;
    private Date date_modification;

    // Relation ManyToOne
    private RestaurantDTO restaurant;
}
