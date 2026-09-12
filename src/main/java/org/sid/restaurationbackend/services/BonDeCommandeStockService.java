package org.sid.restaurationbackend.services;


import org.sid.restaurationbackend.dtos.*;

import org.sid.restaurationbackend.enums.StatutBonCommande;
import org.sid.restaurationbackend.exceptions.BonDeCommandeStockNotFoundException;
import org.sid.restaurationbackend.exceptions.FournisseurNotFoundException;
import org.sid.restaurationbackend.exceptions.LigneBonDeCommandeNotFoundException;
import org.sid.restaurationbackend.requests.PasserBonDeCommandeRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BonDeCommandeStockService {

    BonDeCommandeStockDTO saveBonDeCommandeStock(BonDeCommandeStockDTO bonDeCommandeStockDTO);
    BonDeCommandeStockDTO updateBonDeCommandeStock(Long id,BonDeCommandeStockDTO bonDeCommandeStockDTO) throws BonDeCommandeStockNotFoundException;
    void deleteBonDeCommandeStock(Long id) throws BonDeCommandeStockNotFoundException;
    BonDeCommandeStockDTO getBonDeCommandeStock(Long id) throws BonDeCommandeStockNotFoundException;
    List<BonDeCommandeStockDTO> getAllBonDeCommandeStocks();
    List<BonDeCommandeStockDTO> getAllBonDeCommandeStocksByFournisseur(FournisseurDTO fournisseur);
    List<BonDeCommandeStockDTO> getAllBonDeCommandeStocksByStatut(StatutBonCommande statut);
    List<LigneBonDeCommandeDTO> getLignesDeCommandeByBonCommande(BonDeCommandeStockDTO bonCommandeDTO);
    List<BonDeCommandeStockDTO> getAllBonDeCommandeStocksByDate(LocalDate dateDebut, LocalDate dateFin);



    List<BonDeCommandeStockDTO> search(FournisseurDTO fournisseur, StatutBonCommande statut, LocalDate dateDebut, LocalDate dateFin);


    LigneBonDeCommandeDTO ajouterLigne(Long id, LigneBonDeCommandeDTO ligneBonDeCommandeDTO) throws BonDeCommandeStockNotFoundException;

    LigneBonDeCommandeDTO updateLigneBonCommande(Long id , LigneBonDeCommandeDTO ligneBonDeCommandeDTO) throws LigneBonDeCommandeNotFoundException;
    void deleteLigneBonDeCommande(Long id) throws LigneBonDeCommandeNotFoundException;
    LigneBonDeCommandeDTO getLigneBonDeCommande(Long id) throws LigneBonDeCommandeNotFoundException;
    List<LigneBonDeCommandeDTO> getLignesBonDeCommandeByIngredient(IngredientDTO ingredient);


    List<BonDeCommandeStockDTO> getBonDeCommandeByIngredient(String keyword);



    BonDeCommandeStockDTO updateStatutBonDeCommand(Long id, StatutBonCommande statutBonCommande) throws BonDeCommandeStockNotFoundException;

    @Transactional
    BonDeCommandeStockDTO passerUneBonDeCommande(PasserBonDeCommandeRequest request) throws FournisseurNotFoundException, BonDeCommandeStockNotFoundException;


    BonDeCommandeStockDTO updateDateLivraisonPrevu(Long id, LocalDate date) throws BonDeCommandeStockNotFoundException;
}
