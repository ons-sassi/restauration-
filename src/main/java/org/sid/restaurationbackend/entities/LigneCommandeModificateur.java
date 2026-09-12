package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente un modificateur choisi sur une ligne de commande donnée
 * (ex : "Extra fromage" x2 sur la ligne "Burger").
 *
 * Cette entité relie {@link LigneCommande} et {@link Modificateur} de façon
 * relationnelle, ce qui permet d'agréger les ventes par modificateur
 * (cf. VenteRepository#getVentesParModificateur), contrairement au champ
 * texte libre LigneCommande#modificateurs_choisis qui n'est pas exploitable
 * pour des statistiques.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeModificateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ligne_commande_modificateur;

    private Integer quantite;

    @ManyToOne
    @JoinColumn(name = "id_ligne_commande")
    private LigneCommande ligneCommande;

    @ManyToOne
    @JoinColumn(name = "id_modificateur")
    private Modificateur modificateur;
}
