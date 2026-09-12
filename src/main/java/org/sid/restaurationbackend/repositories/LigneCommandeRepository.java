package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Commande;
import org.sid.restaurationbackend.entities.LigneCommande;
import org.sid.restaurationbackend.entities.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {
    List<LigneCommande> findByCommande(Commande commande);
    List<LigneCommande> findByProduit(Produit produit);

    Collection<LigneCommande> findByProduitNomContainingIgnoreCase(String keyword);

    // Utilisé par ClientMenuServiceImpl pour calculer les "meilleures
    // ventes" du restaurant en une seule requête (plutôt qu'un
    // findByProduit(...) par produit, en O(n) allers-retours DB).
    List<LigneCommande> findByProduitIn(Collection<Produit> produits);
}
