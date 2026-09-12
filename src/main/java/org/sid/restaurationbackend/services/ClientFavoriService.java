package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientFavoriDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;

import java.util.List;

/**
 * Favoris de l'espace client — n'existait pas du tout avant (ni entité,
 * ni back, ni front, voir Favori). Même famille que
 * ClientReclamationService / ClientSuggestionService : ownership
 * directe via Favori.client, jamais via un restaurant indirect. Le
 * produit ajouté en favori doit en revanche appartenir au restaurant du
 * client connecté — même vérification que
 * ClientMenuServiceImpl.getProduitDuRestaurant / ClientCommandeServiceImpl,
 * jamais un id de produit d'un autre restaurant suivi aveuglément.
 */
public interface ClientFavoriService {

    List<ClientFavoriDTO> getMesFavoris() throws ClientNotFoundException;

    /*
     * Idempotent : si le produit est déjà en favori, renvoie le favori
     * existant sans erreur — pas de doublon créé, pas de 409 pour un
     * "double coeur" anodin côté UX (double-clic, re-appel réseau...).
     */
    ClientFavoriDTO ajouterFavori(Long produitId)
            throws ClientNotFoundException, ProduitNotFoundException;

    /*
     * Idempotent : ne renvoie pas d'erreur si le produit n'était pas en
     * favori — l'état final recherché ("ne plus être en favori") est
     * déjà atteint. Scopé sur le client connecté (jamais un id d'un
     * autre client) : pas besoin de revérifier le restaurant du produit
     * ici, voir ClientFavoriServiceImpl.
     */
    void supprimerFavori(Long produitId) throws ClientNotFoundException;

    /*
     * Pratique pour la fiche produit / le menu : savoir si CE produit
     * précis est déjà en favori sans recharger toute la liste.
     */
    boolean estFavori(Long produitId) throws ClientNotFoundException;
}
