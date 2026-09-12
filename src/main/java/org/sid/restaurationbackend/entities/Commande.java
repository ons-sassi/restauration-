package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutCommande;
import org.sid.restaurationbackend.enums.ModeCommande;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_commande;

    private Date dateCommande;
    @Enumerated(EnumType.STRING)
    private StatutCommande statut;
    @Enumerated(EnumType.STRING)
    private ModeCommande modeCommande;
    private Double montant_total;

    /**
     * Adresse de livraison, renseignée uniquement quand modeCommande =
     * LIVRAISON (voir ClientCommandeServiceImpl.creerCommandeDepuisPanier).
     * Nulle pour tous les autres modes.
     */
    private String adresseLivraison;

    @ManyToOne
    @JoinColumn(
            name = "id_client",
            nullable = true
    )
    private ClientAuthentifie client;

    @ManyToOne
    @JoinColumn(
            name = "id_session",
            nullable = true
    )
    private ClientNonAuthentifie clientNonAuthentifie;;

    @ManyToOne
    @JoinColumn(name = "id_table", nullable = true)
    private TableRestaurant table;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private Employee employee;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vente> ventes;

    @OneToMany(mappedBy = "commande")
    private List<Reclamation> reclamations;
}
