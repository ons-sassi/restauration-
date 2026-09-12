package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.Devise;
import org.sid.restaurationbackend.enums.LangueParDefaut;
import org.sid.restaurationbackend.enums.StatutRestaurant;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_restaurant;

    private String nomRestaurant;
    private String adresse;
    private String logo;
    @Enumerated(EnumType.STRING)
    private Devise devise;

    @Enumerated(EnumType.STRING)
    private LangueParDefaut langue_par_defaut;

    @Enumerated(EnumType.STRING)
    private StatutRestaurant statut;
    private LocalTime horaires_ouverture;
    private LocalTime horaires_fermeture;

    @OneToMany(mappedBy = "restaurant")
    private List<PointDeVente> pdvs;

    @OneToMany(mappedBy = "restaurant")
    private List<TableRestaurant> tables;

    @OneToMany(mappedBy = "restaurant")
    private List<PlanDeSalle> plans;

    @OneToMany(mappedBy = "restaurant")
    private List<ElementMenu> menu;

    @OneToMany(mappedBy = "restaurant")
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "restaurant")
    private List<ParametreCompte> parametres;

    @OneToMany(mappedBy = "restaurant")
    private List<ClientNonAuthentifie> sessions;
}
