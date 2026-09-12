package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutTable;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRestaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_table;

    private Integer numeroTable;
    private Integer capacite;
    @Enumerated(EnumType.STRING)
    private StatutTable statut;
    private String codeQr;
    private String urlQr;
    private Boolean qrActif;
    private Date dateGenerationQr;

    // =========================================================
    // Position de la table dans le plan visuel du restaurant.
    // Utilisées uniquement pour l'affichage futur du plan
    // (drag & drop). Null tant que la table n'a pas encore été
    // positionnée par un responsable.
    // =========================================================
    private Integer positionX;
    private Integer positionY;

    @ManyToOne
    @JoinColumn(name = "id_restaurant")
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "id_serveur_attribue")
    private Employee serveurAttribue;

    @ManyToOne
    @JoinColumn(name = "genere_par")
    private Employee generePar;

    @OneToMany(mappedBy = "table")
    private List<Commande> commandes;

    @OneToMany(mappedBy = "table")
    private List<Reservation> reservations;


}
