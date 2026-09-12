package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Produit mis en favori par un client authentifié. N'existait pas du
 * tout avant (ni entité, ni back, ni front) — voir ClientFavoriService
 * pour le détail des décisions de conception (aucune table existante
 * adaptée à réutiliser).
 *
 * Ownership directe via {@link #client} (même famille que Suggestion /
 * Reclamation), jamais via un restaurant indirect. Le produit doit en
 * revanche appartenir au restaurant du client connecté — vérifié en
 * service, jamais ici.
 *
 * Contrainte d'unicité (id_client, id_produit) au niveau base : filet
 * de sécurité contre un doublon créé par une race condition (double
 * clic, deux requêtes concurrentes), en plus de la vérification
 * applicative dans ClientFavoriServiceImpl.ajouterFavori.
 */
@Entity
@Table(
        name = "favori",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favori_client_produit",
                columnNames = {"id_client", "id_produit"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_favori;

    private Date dateAjout;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private ClientAuthentifie client;

    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;
}
