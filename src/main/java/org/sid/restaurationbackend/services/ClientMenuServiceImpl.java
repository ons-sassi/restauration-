package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.CategorieDTO;
import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.ModificateurDTO;
import org.sid.restaurationbackend.dtos.ProduitDTO;

import org.sid.restaurationbackend.entities.Categorie;
import org.sid.restaurationbackend.entities.LigneCommande;
import org.sid.restaurationbackend.entities.Produit;
import org.sid.restaurationbackend.entities.Restaurant;

import org.sid.restaurationbackend.exceptions.CategorieNotFoundException;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;

import org.sid.restaurationbackend.mappers.RestaurantMapper;

import org.sid.restaurationbackend.repositories.CategorieRepository;
import org.sid.restaurationbackend.repositories.LigneCommandeRepository;
import org.sid.restaurationbackend.repositories.ModificateurRepository;
import org.sid.restaurationbackend.repositories.ProduitRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ClientMenuServiceImpl implements ClientMenuService {

    private final ClientAuthentifieService clientAuthentifieService;
    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;
    private final ModificateurRepository modificateurRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final RestaurantMapper dtoMapper;

    // =========================================================
    // RESTAURANT DU CLIENT CONNECTÉ
    // =========================================================

    private Restaurant getRestaurantDuClientConnecte() throws ClientNotFoundException {

        ClientAuthentifieDTO client = clientAuthentifieService.getClientConnecte();

        if (client.getRestaurant() == null || client.getRestaurant().getId_restaurant() == null) {
            // Ne devrait jamais arriver : ClientAuthentifie.restaurant est
            // non-nullable en base (voir étape 1 - auth client).
            throw new ClientNotFoundException(
                    "Aucun restaurant n'est rattaché à ce compte client");
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setId_restaurant(client.getRestaurant().getId_restaurant());
        return restaurant;
    }

    // =========================================================
    // CATEGORIES RACINES
    // =========================================================

    @Override
    public List<CategorieDTO> getCategoriesRacines() throws ClientNotFoundException {

        Restaurant restaurant = getRestaurantDuClientConnecte();

        return categorieRepository.findByRestaurant(restaurant)
                .stream()
                .filter(c -> c.getCategorieParent() == null)
                .sorted(Comparator.comparing(
                        Categorie::getOrdre_affichage,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(dtoMapper::fromCategorie)
                .toList();
    }

    // =========================================================
    // SOUS-CATEGORIES
    // =========================================================

    @Override
    public List<CategorieDTO> getSousCategories(Long categorieParentId)
            throws ClientNotFoundException, CategorieNotFoundException {

        Restaurant restaurant = getRestaurantDuClientConnecte();

        Categorie categorieParent = getCategorieDuRestaurant(categorieParentId, restaurant);

        return categorieRepository.findByCategorieParent(categorieParent)
                .stream()
                .sorted(Comparator.comparing(
                        Categorie::getOrdre_affichage,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(dtoMapper::fromCategorie)
                .toList();
    }

    // =========================================================
    // PRODUITS D'UNE CATEGORIE (OU TOUS SI categorieId == null)
    // =========================================================

    @Override
    public List<ProduitDTO> getProduits(Long categorieId)
            throws ClientNotFoundException, CategorieNotFoundException {

        Restaurant restaurant = getRestaurantDuClientConnecte();

        List<Produit> produits = produitRepository.findByRestaurant(restaurant);

        if (categorieId != null) {
            // Vérifie que la catégorie demandée appartient bien au
            // restaurant du client (jamais confiance en un id fourni
            // par le frontend).
            getCategorieDuRestaurant(categorieId, restaurant);

            produits = produits.stream()
                    .filter(p -> p.getCategorieParent() != null
                            && categorieId.equals(p.getCategorieParent().getId_element()))
                    .toList();
        }

        return produits.stream()
                .filter(p -> Boolean.TRUE.equals(p.getDisponible()))
                .sorted(Comparator.comparing(
                        Produit::getOrdre_affichage,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(dtoMapper::fromProduit)
                .toList();
    }

    // =========================================================
    // MEILLEURES VENTES
    // =========================================================

    @Override
    public List<ProduitDTO> getMeilleuresVentes(int limite) throws ClientNotFoundException {

        Restaurant restaurant = getRestaurantDuClientConnecte();

        List<Produit> produitsDisponibles = produitRepository.findByRestaurant(restaurant)
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getDisponible()))
                .toList();

        if (produitsDisponibles.isEmpty()) {
            return List.of();
        }

        // Quantité totale vendue par produit, en une seule requête.
        Map<Long, Integer> quantiteVendueParProduit = new HashMap<>();

        for (LigneCommande ligne : ligneCommandeRepository.findByProduitIn(produitsDisponibles)) {
            if (ligne.getProduit() == null || ligne.getQuantite() == null) {
                continue;
            }
            quantiteVendueParProduit.merge(
                    ligne.getProduit().getId_element(),
                    ligne.getQuantite(),
                    Integer::sum);
        }

        return produitsDisponibles.stream()
                // Seuls les produits ayant déjà été vendus au moins une
                // fois comptent comme "meilleure vente" ; sinon on
                // afficherait des produits jamais commandés en tête de
                // liste pour un restaurant tout juste lancé.
                .filter(p -> quantiteVendueParProduit.getOrDefault(p.getId_element(), 0) > 0)
                .sorted(Comparator.comparing(
                        (Produit p) -> quantiteVendueParProduit.getOrDefault(p.getId_element(), 0))
                        .reversed())
                .limit(Math.max(limite, 0))
                .map(dtoMapper::fromProduit)
                .toList();
    }

    // =========================================================
    // SUGGESTIONS
    // =========================================================

    @Override
    public List<ProduitDTO> getSuggestions(int limite) throws ClientNotFoundException {

        Restaurant restaurant = getRestaurantDuClientConnecte();

        // Implémentation simple actuelle : les produits disponibles les
        // plus récemment créés (id_element décroissant). Pas encore de
        // personnalisation par client — voir la note dans l'interface.
        return produitRepository.findByRestaurant(restaurant)
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getDisponible()))
                .sorted(Comparator.comparing(Produit::getId_element).reversed())
                .limit(Math.max(limite, 0))
                .map(dtoMapper::fromProduit)
                .toList();
    }

    // =========================================================
    // DETAIL D'UN PRODUIT
    // =========================================================

    @Override
    public ProduitDTO getProduit(Long produitId)
            throws ClientNotFoundException, ProduitNotFoundException {

        Restaurant restaurant = getRestaurantDuClientConnecte();

        Produit produit = getProduitDuRestaurant(produitId, restaurant);

        return dtoMapper.fromProduit(produit);
    }

    // =========================================================
    // MODIFICATEURS D'UN PRODUIT
    // =========================================================

    @Override
    public List<ModificateurDTO> getModificateurs(Long produitId)
            throws ClientNotFoundException, ProduitNotFoundException {

        Restaurant restaurant = getRestaurantDuClientConnecte();

        Produit produit = getProduitDuRestaurant(produitId, restaurant);

        return modificateurRepository.findByProduitsContains(produit)
                .stream()
                .map(dtoMapper::fromModificateur)
                .toList();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Produit getProduitDuRestaurant(Long produitId, Restaurant restaurant)
            throws ProduitNotFoundException {

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ProduitNotFoundException(
                        "Produit introuvable avec l'id : " + produitId));

        if (produit.getRestaurant() == null
                || !restaurant.getId_restaurant().equals(produit.getRestaurant().getId_restaurant())) {
            // Comme pour getCategorieDuRestaurant : on renvoie 404 plutôt
            // qu'un 403, pour ne pas confirmer à un client l'existence
            // d'un produit d'un autre restaurant.
            throw new ProduitNotFoundException(
                    "Produit introuvable avec l'id : " + produitId);
        }

        return produit;
    }

    private Categorie getCategorieDuRestaurant(Long categorieId, Restaurant restaurant)
            throws CategorieNotFoundException {

        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new CategorieNotFoundException(
                        "Catégorie introuvable avec l'id : " + categorieId));

        if (categorie.getRestaurant() == null
                || !restaurant.getId_restaurant().equals(categorie.getRestaurant().getId_restaurant())) {
            throw new CategorieNotFoundException(
                    "Catégorie introuvable avec l'id : " + categorieId);
        }

        return categorie;
    }
}
