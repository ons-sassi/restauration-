package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.ClientFavoriDTO;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Favori;
import org.sid.restaurationbackend.entities.Produit;
import org.sid.restaurationbackend.entities.Restaurant;

import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;

import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.sid.restaurationbackend.repositories.FavoriRepository;
import org.sid.restaurationbackend.repositories.ProduitRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Voir ClientFavoriService pour le pourquoi des choix de conception.
 */
@Service
@Transactional
@AllArgsConstructor
public class ClientFavoriServiceImpl implements ClientFavoriService {

    private final ClientAuthentifieRepository clientAuthentifieRepository;
    private final ProduitRepository produitRepository;
    private final FavoriRepository favoriRepository;

    @Override
    public List<ClientFavoriDTO> getMesFavoris() throws ClientNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        return favoriRepository.findByClient(client).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ClientFavoriDTO ajouterFavori(Long produitId)
            throws ClientNotFoundException, ProduitNotFoundException {

        ClientAuthentifie client = getClientConnecte();
        Produit produit = getProduitDuRestaurant(produitId, client.getRestaurant());

        Favori favori = favoriRepository.findByClientAndProduit(client, produit)
                .orElseGet(() -> {
                    Favori nouveau = new Favori();
                    nouveau.setClient(client);
                    nouveau.setProduit(produit);
                    nouveau.setDateAjout(new Date());
                    return favoriRepository.save(nouveau);
                });

        return toDto(favori);
    }

    @Override
    public void supprimerFavori(Long produitId) throws ClientNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        // Pas besoin de vérifier ici que le produit appartient au
        // restaurant du client : findByClientAndProduit est déjà scopé
        // sur CE client, donc un id de produit d'un autre restaurant
        // (ou inexistant) ne peut simplement matcher aucun favori à
        // supprimer — idempotent par construction, sans IDOR possible.
        Produit produitRef = new Produit();
        produitRef.setId_element(produitId);

        favoriRepository.findByClientAndProduit(client, produitRef)
                .ifPresent(favoriRepository::delete);
    }

    @Override
    public boolean estFavori(Long produitId) throws ClientNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        Produit produitRef = new Produit();
        produitRef.setId_element(produitId);

        return favoriRepository.findByClientAndProduit(client, produitRef).isPresent();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private ClientAuthentifie getClientConnecte() throws ClientNotFoundException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new ClientNotFoundException("Client non authentifié");
        }

        return clientAuthentifieRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new ClientNotFoundException(
                        "Client authentifié introuvable"
                ));
    }

    private Produit getProduitDuRestaurant(Long produitId, Restaurant restaurant)
            throws ProduitNotFoundException {

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ProduitNotFoundException(
                        "Produit introuvable avec l'id : " + produitId));

        if (produit.getRestaurant() == null
                || !restaurant.getId_restaurant().equals(produit.getRestaurant().getId_restaurant())) {
            // 404 plutôt que 403, comme dans ClientMenuServiceImpl : ne
            // pas confirmer à un client l'existence d'un produit d'un
            // autre restaurant.
            throw new ProduitNotFoundException(
                    "Produit introuvable avec l'id : " + produitId);
        }

        return produit;
    }

    private ClientFavoriDTO toDto(Favori favori) {

        Produit produit = favori.getProduit();

        return new ClientFavoriDTO(
                favori.getId_favori(),
                produit != null ? produit.getId_element() : null,
                produit != null ? produit.getNom() : null,
                produit != null ? produit.getImage() : null,
                produit != null ? produit.getPrix() : null,
                produit != null ? produit.getDisponible() : null,
                favori.getDateAjout()
        );
    }
}
