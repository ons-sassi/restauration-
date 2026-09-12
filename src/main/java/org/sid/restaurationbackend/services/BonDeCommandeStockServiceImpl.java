package org.sid.restaurationbackend.services;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.enums.StatutBonCommande;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.*;
import org.sid.restaurationbackend.requests.PasserBonDeCommandeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
@AllArgsConstructor
@Slf4j class BonDeCommandeStockServiceImpl implements BonDeCommandeStockService {
    private IngredientService ingredientService;
    private FournisseurService fournisseurService;
    private RestaurantMapper dtotMapper;
    private BonDeCommandeStockRepository bonDeCommandeStockRepository;
    private LigneBonDeCommandeRepository ligneBonDeCommandeRepository;
    private IngredientRepository ingredientRepository;



    @Override
    public BonDeCommandeStockDTO saveBonDeCommandeStock(BonDeCommandeStockDTO bonDeCommandeStockDTO) {
        BonDeCommandeStock bonDeCommandeStock = dtotMapper.fromBonDeCommandeStockDTO(bonDeCommandeStockDTO);
        bonDeCommandeStock.setDateCommande( LocalDate.now());
        BonDeCommandeStock savedBonDeCommandeStock = bonDeCommandeStockRepository.save(bonDeCommandeStock);
        return dtotMapper.fromBonDeCommandeStock(savedBonDeCommandeStock);
    }

    @Override
    public BonDeCommandeStockDTO updateBonDeCommandeStock(Long id, BonDeCommandeStockDTO bonDeCommandeStockDTO) throws BonDeCommandeStockNotFoundException {
        BonDeCommandeStock bonDeCommandeStock = bonDeCommandeStockRepository.findById(id)
                .orElseThrow(() -> new BonDeCommandeStockNotFoundException("Bon de commande stock not found"));

        dtotMapper.updateBonDeCommandeStockFromDto(bonDeCommandeStockDTO, bonDeCommandeStock);

        BonDeCommandeStock updatedBonDeCommandeStock = bonDeCommandeStockRepository.save(bonDeCommandeStock);
        return dtotMapper.fromBonDeCommandeStock(updatedBonDeCommandeStock);
    }

    @Override
    public void deleteBonDeCommandeStock(Long id) throws BonDeCommandeStockNotFoundException {
        BonDeCommandeStock bonDeCommandeStock = bonDeCommandeStockRepository.findById(id).orElseThrow(() -> new BonDeCommandeStockNotFoundException("Bon de commande stock not found"));
        bonDeCommandeStockRepository.delete(bonDeCommandeStock);
    }

    @Override
    public BonDeCommandeStockDTO getBonDeCommandeStock(Long id) throws BonDeCommandeStockNotFoundException {
        BonDeCommandeStock bonDeCommandeStock = bonDeCommandeStockRepository.findById(id).orElseThrow(() -> new BonDeCommandeStockNotFoundException("Bon de commande stock not found"));
        return dtotMapper.fromBonDeCommandeStock(bonDeCommandeStock);
    }

    @Override
    public List<BonDeCommandeStockDTO> getAllBonDeCommandeStocks() {

        return bonDeCommandeStockRepository.findAll().stream().map(dtotMapper::fromBonDeCommandeStock).collect(Collectors.toList());
    }

    @Override
    public List<BonDeCommandeStockDTO> getAllBonDeCommandeStocksByFournisseur(FournisseurDTO fournisseur) {

        return bonDeCommandeStockRepository.findByFournisseur(dtotMapper.fromFournisseurDTO(fournisseur)).stream().map(dtotMapper::fromBonDeCommandeStock).collect(Collectors.toList());
    }

    @Override
    public List<BonDeCommandeStockDTO> getAllBonDeCommandeStocksByStatut(StatutBonCommande statut) {

        return bonDeCommandeStockRepository.findByStatut(statut).stream().map(dtotMapper::fromBonDeCommandeStock).collect(Collectors.toList());
    }

    @Override
    public List<LigneBonDeCommandeDTO> getLignesDeCommandeByBonCommande(BonDeCommandeStockDTO commande) {
        return ligneBonDeCommandeRepository.findByBonCommande(dtotMapper.fromBonDeCommandeStockDTO(commande)).stream().map(dtotMapper::fromLigneBonDeCommande).collect(Collectors.toList());
    }

    @Override
    public List<BonDeCommandeStockDTO> getAllBonDeCommandeStocksByDate(LocalDate dateDebut, LocalDate dateFin) {
        return bonDeCommandeStockRepository.findByDateCommandeBetween(dateDebut,dateFin).stream().map(dtotMapper::fromBonDeCommandeStock).collect(Collectors.toList());

    }


    @Override
    public List<BonDeCommandeStockDTO> search(
            FournisseurDTO fournisseur,
            StatutBonCommande statut,
            LocalDate dateDebut,
            LocalDate dateFin) {

        return bonDeCommandeStockRepository.search(
                        fournisseur == null ? null : dtotMapper.fromFournisseurDTO(fournisseur),
                        statut,
                        dateDebut,
                        dateFin)
                .stream()
                .map(dtotMapper::fromBonDeCommandeStock)
                .collect(Collectors.toList());
    }

    @Override
    public LigneBonDeCommandeDTO ajouterLigne(Long id, LigneBonDeCommandeDTO ligneBonDeCommandeDTO) throws BonDeCommandeStockNotFoundException {
        BonDeCommandeStockDTO bonDeCommandeStockDTO=getBonDeCommandeStock(id);
        ligneBonDeCommandeDTO.setBonCommande(bonDeCommandeStockDTO);
        LigneBonDeCommande ligneBonDeCommande = dtotMapper.fromLigneBonDeCommandeDTO(ligneBonDeCommandeDTO);
        LigneBonDeCommande savedLigneBonDeCommande = ligneBonDeCommandeRepository.save(ligneBonDeCommande);
        return dtotMapper.fromLigneBonDeCommande(savedLigneBonDeCommande);
    }

    @Override
    public LigneBonDeCommandeDTO updateLigneBonCommande(Long id, LigneBonDeCommandeDTO ligneBonDeCommandeDTO) throws LigneBonDeCommandeNotFoundException {
        LigneBonDeCommande ligneBonDeCommande = ligneBonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new LigneBonDeCommandeNotFoundException("Ligne bon de commande not found"));

        dtotMapper.updateLigneBonDeCommandeFromDto(ligneBonDeCommandeDTO, ligneBonDeCommande);

        LigneBonDeCommande updatedLigneBonDeCommande = ligneBonDeCommandeRepository.save(ligneBonDeCommande);
        return dtotMapper.fromLigneBonDeCommande(updatedLigneBonDeCommande);

    }

    @Override
    public void deleteLigneBonDeCommande(Long id) throws LigneBonDeCommandeNotFoundException {
        LigneBonDeCommande ligneBonDeCommande = ligneBonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new LigneBonDeCommandeNotFoundException("Ligne bon de commande not found"));
        ligneBonDeCommandeRepository.deleteById(id);

    }

    @Override
    public LigneBonDeCommandeDTO getLigneBonDeCommande(Long id) throws LigneBonDeCommandeNotFoundException {
        LigneBonDeCommande ligneBonDeCommande = ligneBonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new LigneBonDeCommandeNotFoundException("Ligne bon de commande not found"));
        return dtotMapper.fromLigneBonDeCommande(ligneBonDeCommande);
    }



    @Override
    public List<LigneBonDeCommandeDTO> getLignesBonDeCommandeByIngredient(IngredientDTO ingredient) {
        return ligneBonDeCommandeRepository.findByIngredient(dtotMapper.fromIngredientDTO(ingredient)).stream().map(dtotMapper::fromLigneBonDeCommande).collect(Collectors.toList());
    }

    @Override
    public List<BonDeCommandeStockDTO> getBonDeCommandeByIngredient(String keyword) {

        return ligneBonDeCommandeRepository
                .findByIngredientNomContainingIgnoreCase(keyword)
                .stream()
                .map(LigneBonDeCommande::getBonCommande)
                .distinct()
                .map(dtotMapper::fromBonDeCommandeStock)
                .collect(Collectors.toList());
    }

    @Override
    public BonDeCommandeStockDTO updateStatutBonDeCommand(Long id, StatutBonCommande statutBonCommande) throws BonDeCommandeStockNotFoundException {
        BonDeCommandeStock bonDeCommandeStock = bonDeCommandeStockRepository.findById(id)
                .orElseThrow(() -> new BonDeCommandeStockNotFoundException("Bon de commande stock not found"));

        StatutBonCommande ancienStatut = bonDeCommandeStock.getStatut();

        bonDeCommandeStock.setStatut(statutBonCommande);

        if (statutBonCommande.equals(StatutBonCommande.LIVRE)) {
            bonDeCommandeStock.setDateDeLivraison(LocalDateTime.now());

            // On n'ajoute la quantité au stock que si la commande n'était pas déjà livrée
            // (évite de créditer le stock plusieurs fois pour la même commande).
            if (ancienStatut != StatutBonCommande.LIVRE) {
                List<LigneBonDeCommande> lignes = ligneBonDeCommandeRepository.findByBonCommande(bonDeCommandeStock);

                for (LigneBonDeCommande ligne : lignes) {
                    Ingredient ingredient = ligne.getIngredient();

                    if (ingredient != null && ligne.getQuantite_commandee() != null) {
                        double stockActuel = ingredient.getQuantite_stock() != null
                                ? ingredient.getQuantite_stock()
                                : 0.0;

                        ingredient.setQuantite_stock(stockActuel + ligne.getQuantite_commandee());
                        ingredientRepository.save(ingredient);
                    }
                }
            }
        }

        BonDeCommandeStock updatedBonDeCommande = bonDeCommandeStockRepository.save(bonDeCommandeStock);
        return dtotMapper.fromBonDeCommandeStock(updatedBonDeCommande);
    }


    @Override
    @Transactional
    public BonDeCommandeStockDTO passerUneBonDeCommande(PasserBonDeCommandeRequest request)
            throws FournisseurNotFoundException, BonDeCommandeStockNotFoundException {

        // Création du bon de commande
        BonDeCommandeStockDTO bonCommande = new BonDeCommandeStockDTO();

        FournisseurDTO fournisseur = fournisseurService.getFournisseur(request.getFournisseurId());
        bonCommande.setFournisseur(fournisseur);
        bonCommande.setStatut(StatutBonCommande.EN_ATTENTE);

        // SÉCURITÉ MULTI-RESTAURANT — Lot 2.7 : le restaurant du bon de
        // commande est toujours dérivé de celui de son fournisseur (voir
        // BonDeCommandeStock.java, colonne id_restaurant NOT NULL). Sans
        // cette ligne, l'INSERT viole la contrainte NOT NULL et échoue
        // avec une DataIntegrityViolationException générique.
        bonCommande.setRestaurant(fournisseur.getRestaurant());

        // Sauvegarde du bon de commande (dateCommande est définie ici)
        BonDeCommandeStockDTO savedBonCommande = saveBonDeCommandeStock(bonCommande);

        double montantTotal = 0.0;

        // Ajout des lignes
        for (LigneBonDeCommandeDTO ligne : request.getLignes()) {

            ajouterLigne(savedBonCommande.getId_bon_commande(),ligne);

            montantTotal += ligne.getQuantite_commandee() * ligne.getPrix_unitaire();
        }

        // Mise à jour du montant
        BonDeCommandeStock bonCommandeEntity =
                bonDeCommandeStockRepository.findById(savedBonCommande.getId_bon_commande())
                        .orElseThrow(() -> new BonDeCommandeStockNotFoundException(
                                "Bon de commande introuvable"));

        bonCommandeEntity.setMontant_total(montantTotal);

        BonDeCommandeStock updatedBonCommande =
                bonDeCommandeStockRepository.save(bonCommandeEntity);

        return dtotMapper.fromBonDeCommandeStock(updatedBonCommande);
    }
    @Override
    public BonDeCommandeStockDTO updateDateLivraisonPrevu(Long id, LocalDate date) throws BonDeCommandeStockNotFoundException {
        BonDeCommandeStockDTO bonDeCommandeStockDTO=getBonDeCommandeStock(id);
        bonDeCommandeStockDTO.setDateDeLivraisonPrevu(date);
        BonDeCommandeStock updatedBonDeCommande =bonDeCommandeStockRepository.save(dtotMapper.fromBonDeCommandeStockDTO(bonDeCommandeStockDTO));
        return dtotMapper.fromBonDeCommandeStock(updatedBonDeCommande);
    }
}