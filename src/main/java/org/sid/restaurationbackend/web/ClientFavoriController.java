package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ClientFavoriDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;
import org.sid.restaurationbackend.services.ClientFavoriService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Favoris de l'espace client. N'existait pas du tout avant (ni entité,
 * ni back, ni front) — voir ClientFavoriService pour le détail des
 * décisions de conception (aucune table existante adaptée à réutiliser,
 * ownership directe via Favori.client, endpoints idempotents pour
 * ajout/suppression).
 */
@RestController
@RequestMapping("/api/client/favoris")
@AllArgsConstructor
public class ClientFavoriController {

    private final ClientFavoriService clientFavoriService;

    /**
     * Liste des produits favoris du client connecté.
     */
    @GetMapping
    public ResponseEntity<List<ClientFavoriDTO>> getMesFavoris()
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientFavoriService.getMesFavoris()
        );
    }

    /**
     * Ajoute un produit aux favoris du client connecté. Idempotent :
     * un produit déjà en favori ne crée pas de doublon (voir
     * ClientFavoriServiceImpl.ajouterFavori).
     */
    @PostMapping("/{produitId}")
    public ResponseEntity<ClientFavoriDTO> ajouterFavori(
            @PathVariable Long produitId)
            throws ClientNotFoundException, ProduitNotFoundException {

        ClientFavoriDTO favori = clientFavoriService.ajouterFavori(produitId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(favori);
    }

    /**
     * Retire un produit des favoris du client connecté. Idempotent :
     * pas d'erreur si le produit n'était pas en favori.
     */
    @DeleteMapping("/{produitId}")
    public ResponseEntity<Void> supprimerFavori(
            @PathVariable Long produitId)
            throws ClientNotFoundException {

        clientFavoriService.supprimerFavori(produitId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Pratique pour la fiche produit / le menu : savoir si CE produit
     * précis est déjà en favori sans recharger toute la liste.
     */
    @GetMapping("/{produitId}/est-favori")
    public ResponseEntity<Boolean> estFavori(
            @PathVariable Long produitId)
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientFavoriService.estFavori(produitId)
        );
    }
}
