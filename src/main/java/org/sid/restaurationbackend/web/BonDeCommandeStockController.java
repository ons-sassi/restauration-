package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.BonDeCommandeStockDTO;
import org.sid.restaurationbackend.dtos.FournisseurDTO;
import org.sid.restaurationbackend.dtos.LigneBonDeCommandeDTO;
import org.sid.restaurationbackend.enums.StatutBonCommande;
import org.sid.restaurationbackend.exceptions.BonDeCommandeStockNotFoundException;
import org.sid.restaurationbackend.exceptions.FournisseurNotFoundException;
import org.sid.restaurationbackend.exceptions.LigneBonDeCommandeNotFoundException;
import org.sid.restaurationbackend.requests.PasserBonDeCommandeRequest;
import org.sid.restaurationbackend.services.BonDeCommandeStockService;
import org.sid.restaurationbackend.services.FournisseurService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bonDeCommandeStocks")
@AllArgsConstructor
@Slf4j
public class BonDeCommandeStockController {

    private final BonDeCommandeStockService bonDeCommandeStockService;
    private final FournisseurService fournisseurService;


    // ============================================================
    // GET : Récupérer une commande de stock par ID
    // ============================================================

    @GetMapping("/{id}")
    public BonDeCommandeStockDTO getBonDeCommandeStock(
            @PathVariable Long id
    ) throws BonDeCommandeStockNotFoundException {

        return bonDeCommandeStockService.getBonDeCommandeStock(id);
    }


    // ============================================================
    // GET : Récupérer toutes les commandes de stock
    // ============================================================

    @GetMapping
    public List<BonDeCommandeStockDTO> getAllBonDeCommandeStocks() {

        return bonDeCommandeStockService.getAllBonDeCommandeStocks();
    }


    // ============================================================
    // GET : Recherche avec filtres
    //
    // Exemple :
    // /bonDeCommandeStocks/search?fournisseurId=1
    //
    // /bonDeCommandeStocks/search?statut=EN_ATTENTE
    //
    // /bonDeCommandeStocks/search?dateDebut=2026-08-01&dateFin=2026-08-17
    //
    // /bonDeCommandeStocks/search?fournisseurId=1&statut=LIVRE
    // ============================================================

    @GetMapping("/search")
    public List<BonDeCommandeStockDTO> searchBonDeCommandeStocks(
            @RequestParam(required = false) Long fournisseurId,

            @RequestParam(required = false)
            StatutBonCommande statut,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateDebut,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFin
    ) throws FournisseurNotFoundException {

        FournisseurDTO fournisseurDTO = null;

        /*
         * On cherche le fournisseur uniquement si fournisseurId
         * a réellement été fourni.
         */
        if (fournisseurId != null) {
            fournisseurDTO = fournisseurService.getFournisseur(fournisseurId);
        }

        return bonDeCommandeStockService.search(
                fournisseurDTO,
                statut,
                dateDebut,
                dateFin
        );
    }


    // ============================================================
    // GET : Récupérer les lignes d'une commande de stock
    //
    // Exemple :
    // GET /bonDeCommandeStocks/5/lignes
    // ============================================================

    @GetMapping("/{bonCommandeId}/lignes")
    public List<LigneBonDeCommandeDTO> getLignesDeCommandeParIdCommande(
            @PathVariable Long bonCommandeId
    ) throws BonDeCommandeStockNotFoundException {

        BonDeCommandeStockDTO bonDeCommandeStockDTO =
                bonDeCommandeStockService.getBonDeCommandeStock(bonCommandeId);

        return bonDeCommandeStockService
                .getLignesDeCommandeByBonCommande(bonDeCommandeStockDTO);
    }


    // ============================================================
    // GET : Rechercher les commandes par ingrédient
    //
    // Exemple :
    // GET /bonDeCommandeStocks/ingredient?keyword=tomate
    // ============================================================

    @GetMapping("/ingredient")
    public List<BonDeCommandeStockDTO> getBonDeCommandeByIngredient(
            @RequestParam String keyword
    ) {

        return bonDeCommandeStockService
                .getBonDeCommandeByIngredient(keyword);
    }


    // ============================================================
    // PATCH : Modifier le statut d'une commande
    //
    // Exemple :
    // PATCH /bonDeCommandeStocks/5/statut?statut=LIVRE
    // ============================================================

    @PatchMapping("/{id}/statut")
    public BonDeCommandeStockDTO changerStatut(
            @PathVariable Long id,
            @RequestParam StatutBonCommande statut
    ) throws BonDeCommandeStockNotFoundException {

        return bonDeCommandeStockService
                .updateStatutBonDeCommand(id, statut);
    }


    // ============================================================
    // PATCH : Modifier la date de livraison prévue
    //
    // Exemple :
    // PATCH /bonDeCommandeStocks/5/date-livraison-prevu?date=2026-08-20
    // ============================================================

    @PatchMapping("/{id}/date-livraison-prevu")
    public BonDeCommandeStockDTO changerDateLivraison(
            @PathVariable Long id,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) throws BonDeCommandeStockNotFoundException {

        return bonDeCommandeStockService
                .updateDateLivraisonPrevu(id, date);
    }


    // ============================================================
    // PUT : Modifier une ligne d'une commande
    //
    // IMPORTANT :
    // Avant :
    // /{id}/lignes/modifier/{id}
    //
    // Maintenant :
    // /{bonCommandeId}/lignes/modifier/{ligneId}
    //
    // Exemple :
    // PUT /bonDeCommandeStocks/5/lignes/modifier/12
    // ============================================================

    @PutMapping("/{bonCommandeId}/lignes/modifier/{ligneId}")
    public LigneBonDeCommandeDTO modifierLigne(
            @PathVariable Long bonCommandeId,
            @PathVariable Long ligneId,
            @RequestBody LigneBonDeCommandeDTO ligne
    ) throws LigneBonDeCommandeNotFoundException {

        return bonDeCommandeStockService
                .updateLigneBonCommande(ligneId, ligne);
    }


    // ============================================================
    // DELETE : Supprimer une ligne
    //
    // Exemple :
    // DELETE /bonDeCommandeStocks/5/lignes/delete/12
    // ============================================================

    @DeleteMapping("/{bonCommandeId}/lignes/delete/{ligneId}")
    public void deleteLigne(
            @PathVariable Long bonCommandeId,
            @PathVariable Long ligneId
    ) throws LigneBonDeCommandeNotFoundException {

        bonDeCommandeStockService
                .deleteLigneBonDeCommande(ligneId);
    }


    // ============================================================
    // DELETE : Supprimer une commande de stock
    //
    // Exemple :
    // DELETE /bonDeCommandeStocks/5
    // ============================================================

    @DeleteMapping("/{id}")
    public void deleteBonCommande(
            @PathVariable Long id
    ) throws BonDeCommandeStockNotFoundException {

        bonDeCommandeStockService
                .deleteBonDeCommandeStock(id);
    }


    // ============================================================
    // POST : Passer une nouvelle commande de stock
    //
    // Exemple :
    // POST /bonDeCommandeStocks/passer-commande
    //
    // Body :
    // {
    //     "fournisseurId": 1,
    //     "lignes": [
    //         {
    //             "ingredientId": 2,
    //             "quantiteCommandee": 10,
    //             "prixUnitaire": 15.5
    //         }
    //     ]
    // }
    // ============================================================

    @PostMapping("/passer-commande")
    public BonDeCommandeStockDTO passerUneBonDeCommande(
            @RequestBody PasserBonDeCommandeRequest request
    ) throws FournisseurNotFoundException,
            BonDeCommandeStockNotFoundException {

        return bonDeCommandeStockService
                .passerUneBonDeCommande(request);
    }
}