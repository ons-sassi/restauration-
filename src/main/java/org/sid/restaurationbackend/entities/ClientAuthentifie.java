package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientAuthentifie extends Utilisateur {
    private String adresse;
    private String preferences;
    private String allergie;
    private String codeParrainage;
    private Date date_modification_profil;

    // SÉCURITÉ MULTI-RESTAURANT — famille "Stock/Fidélité" :
    // décision produit tranchée = privé par restaurant côté back-office
    // (voir ClientAuthentifieController). Conceptuellement le modèle
    // (historique de commandes, réservations, points...) suggère plutôt
    // "un compte, plusieurs enseignes", mais tant que ce flux
    // multi-enseigne n'est pas conçu, on ferme la fuite actuelle
    // (n'importe quel employé voyait tous les clients de la plateforme)
    // avec une colonne directe, comme Fournisseur/Ingredient/Reduction.
    // Ne concerne QUE la création/consultation back-office : le
    // find-by-email utilisé par l'authentification client
    // (ClientUserDetailsService) et par le flux client
    // (ClientCommandeController.getClientConnecte) n'est PAS touché ici
    // — périmètre volontairement limité au back-office pour ce lot.
    @ManyToOne
    @JoinColumn(name = "id_restaurant", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "client")
    private List<Commande> historique_commandes;

    @OneToMany(mappedBy = "client")
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "client" )
    private List<Reclamation> reclamations;

    @OneToMany(mappedBy = "client")
    private List<Suggestion> suggestions;

    @OneToMany(mappedBy = "client")
    private List<ModePaiement> modes_paiement;
}
