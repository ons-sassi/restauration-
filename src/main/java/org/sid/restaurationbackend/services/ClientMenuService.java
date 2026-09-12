package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.CategorieDTO;
import org.sid.restaurationbackend.dtos.ModificateurDTO;
import org.sid.restaurationbackend.dtos.ProduitDTO;
import org.sid.restaurationbackend.exceptions.CategorieNotFoundException;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;

import java.util.List;

/**
 * Lecture du menu (catégories / produits) côté espace client public.
 *
 * Différence essentielle avec {@link ProduitService} (utilisé par le
 * Back Office) : le restaurant n'est jamais fourni par le frontend ni
 * déduit d'un Employee connecté (voir CurrentUserService, qui lève
 * EmployeeNotFoundException pour un ClientAuthentifie). Ici, le
 * restaurant est TOUJOURS celui du ClientAuthentifie connecté
 * (ClientAuthentifieService.getClientConnecte().getRestaurant()),
 * conformément à la contrainte ClientAuthentifie.restaurant
 * (nullable = false) déjà appliquée à l'étape 1 (auth client).
 */
public interface ClientMenuService {

    /**
     * Catégories de premier niveau (categorieParent == null) du
     * restaurant du client connecté, triées par ordre_affichage.
     */
    List<CategorieDTO> getCategoriesRacines() throws ClientNotFoundException;

    /**
     * Sous-catégories directes d'une catégorie du restaurant du
     * client connecté.
     */
    List<CategorieDTO> getSousCategories(Long categorieParentId)
            throws ClientNotFoundException, CategorieNotFoundException;

    /**
     * Produits disponibles d'une catégorie (ou toutes catégories
     * confondues si categorieId est null) du restaurant du client
     * connecté, triés par ordre_affichage.
     */
    List<ProduitDTO> getProduits(Long categorieId)
            throws ClientNotFoundException, CategorieNotFoundException;

    /**
     * Les `limite` produits disponibles les plus vendus (quantité
     * totale commandée, tous historiques confondus) du restaurant du
     * client connecté.
     */
    List<ProduitDTO> getMeilleuresVentes(int limite) throws ClientNotFoundException;

    /**
     * Suggestions pour le client connecté.
     *
     * Implémentation actuelle volontairement simple (produits
     * disponibles les plus récemment ajoutés, hors meilleures
     * ventes) : il n'existe pas encore de moteur de recommandation
     * basé sur l'historique de commandes du client. À affiner plus
     * tard si besoin (ex : basé sur ses commandes passées ou ses
     * préférences/allergies déclarées).
     */
    List<ProduitDTO> getSuggestions(int limite) throws ClientNotFoundException;

    /**
     * Détail d'un produit (fiche produit), scoppé au restaurant du
     * client connecté — jamais un produit d'un autre restaurant même
     * si son id est deviné/forgé dans l'URL.
     */
    ProduitDTO getProduit(Long produitId) throws ClientNotFoundException, ProduitNotFoundException;

    /**
     * Modificateurs disponibles pour un produit du restaurant du
     * client connecté (choix à la commande, ex. "sans oignon",
     * "supplément fromage"...).
     */
    List<ModificateurDTO> getModificateurs(Long produitId)
            throws ClientNotFoundException, ProduitNotFoundException;
}
