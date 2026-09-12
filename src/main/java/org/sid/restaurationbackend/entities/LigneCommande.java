package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ligne_commande;

    private Integer quantite;
    private Double prix_unitaire;

    /**
     * @deprecated champ texte libre historique, non alimenté par le flux de
     * commande actuel. Conservé pour compatibilité d'affichage. Utiliser
     * {@link #modificateursSelectionnes} pour toute agrégation/statistique.
     */
    @Deprecated
    private String modificateurs_choisis;

    private String remarque;

    @ManyToOne
    @JoinColumn(name = "id_commande")
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "id_produit")
    private Produit produit;

    /**
     * Modificateurs réellement choisis sur cette ligne, avec quantité.
     * C'est cette relation qui permet le rapport "vente par modificateur".
     */
    @OneToMany(mappedBy = "ligneCommande")
    private List<LigneCommandeModificateur> modificateursSelectionnes;
}
