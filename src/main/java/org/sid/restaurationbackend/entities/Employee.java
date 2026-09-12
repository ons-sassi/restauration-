package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sid.restaurationbackend.enums.StatutPresence;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends Utilisateur {
    private String matricule;
    private Date date_embauche;
    private String codePin;
    private Long salaire_base;
    @Enumerated(EnumType.STRING)
    private StatutPresence statutPresence;

    // =========================================================
    // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
    // =========================================================
    // Distinct de "serveurAttribue" (affectation PERMANENTE d'une
    // table, portée par TableRestaurant) : ce champ indique si CET
    // employé peut être choisi (automatiquement OU manuellement)
    // pour prendre en charge TEMPORAIREMENT un client, indépendamment
    // de toute table dont il serait responsable permanent.
    //
    // Le système de permissions existant (Role -> RoleFonctionnalite)
    // a été évalué en premier (cf. EmployeeRepository
    // .findEmployeesEligiblesParFonctionnalite) mais il ne permet
    // d'activer/désactiver une fonctionnalité que par RÔLE entier,
    // pas employé par employé (ex: Ahmed et Sarah ont tous les deux
    // le rôle "Serveur"). Le besoin métier (case à cocher par employé,
    // §5 et §26 de la spec) exige donc un champ individuel dédié.
    // Nullable en base : traité comme "false" (non éligible) partout
    // où il est lu, pour ne pas casser les employés déjà existants.
    private Boolean eligibleAttributionAutomatique;

    // =========================================================
    // IMPORTANT :
    // Toutes les relations ci-dessous sont exclues de
    // toString()/equals()/hashCode() (générés par Lombok @Data).
    //
    // Employee <-> Role sont liés dans les deux sens
    // (Employee.role et Role.employees / Role.attribuePar).
    // Sans cette exclusion, Lombok génère un toString()/equals()
    // qui s'appelle récursivement à l'infini entre les deux
    // entités (StackOverflowError), ce qui peut rendre certains
    // écrans (ex : liste des rôles) très lents, voire bloqués.
    // =========================================================

    @ManyToOne
    @JoinColumn(name = "id_role")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_restaurant", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "id_pdv_affecte")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PointDeVente pdvAffecte;

    @OneToMany(mappedBy = "attribuePar")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Role> rolesAttribues;

    @OneToMany(mappedBy = "serveurAttribue")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TableRestaurant> tablesAttribuees;

    @OneToMany(mappedBy = "employee")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Commande> commandes;

    @OneToMany(mappedBy = "employee")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Vente> ventes;

    @OneToMany(mappedBy = "employee")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Presence> presences;

    @OneToMany(mappedBy = "employee")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Penalite> penalites;

    @OneToMany(mappedBy = "employee")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<VersementSalaire> versements;

    @OneToMany(mappedBy = "employee")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Performance> performances;

    @OneToMany(mappedBy = "generePar")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TableRestaurant> qrCodesGeneres;

    @OneToMany(mappedBy = "confirmePar")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Reservation> reservationsConfirmees;

    @OneToMany(mappedBy = "attribuePar")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RoleFonctionnalite> fonctionnalitesAttribuees;
}