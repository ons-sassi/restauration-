package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.exceptions.CategorieEnUtilisationException;
import org.sid.restaurationbackend.exceptions.CategorieNotFoundException;
import org.sid.restaurationbackend.exceptions.ModificateurNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitEnUtilisationException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ProduitServiceImpl implements ProduitService {

    private final RestaurantService restaurantService;
    private RestaurantMapper dtotMapper;
    private ProduitRepository produitRepository;
    private CategorieRepository categorieRepository;
    private ModificateurRepository modificateurRepository;
    private ElementMenuRepository elementMenuRepository;
    private LigneCommandeRepository ligneCommandeRepository;


    // ================= PRODUIT =================

    @Override
    public ProduitDTO saveProduit(ProduitDTO produitDTO) {
        Produit produit = dtotMapper.fromProduitDTO(produitDTO);
        Produit savedProduit = produitRepository.save(produit);
        return dtotMapper.fromProduit(savedProduit);
    }

    @Override
    public ProduitDTO updateProduit(Long id, ProduitDTO produitDTO ) throws ProduitNotFoundException {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException("Produit not found"));

        dtotMapper.updateProduitFromDto(produitDTO, produit);

        Produit updatedProduit = produitRepository.save(produit);
        return dtotMapper.fromProduit(updatedProduit);
    }

    @Override
    public void deleteProduit(Long id) throws ProduitNotFoundException, ProduitEnUtilisationException {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException("Produit not found"));

        // Un produit déjà commandé ne doit pas être supprimé physiquement :
        // ça casserait l'historique des commandes (contrainte FK
        // ligne_commande.id_produit) et fausserait les rapports de vente.
        // On bloque donc la suppression avec un message clair, et on
        // recommande de désactiver le produit (disponible = false) via
        // updateProduit à la place.
        boolean estUtiliseDansDesCommandes = !ligneCommandeRepository.findByProduit(produit).isEmpty();
        if (estUtiliseDansDesCommandes) {
            throw new ProduitEnUtilisationException(
                    "Impossible de supprimer le produit '" + produit.getNom() +
                            "' : il est référencé dans au moins une commande. " +
                            "Désactivez-le (disponible = false) au lieu de le supprimer.");
        }

        produitRepository.deleteById(id);
    }

    @Override
    public ProduitDTO getProduit(Long id) throws ProduitNotFoundException {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException("Produit not found"));
        return dtotMapper.fromProduit(produit);
    }

    @Override
    public ProduitDTO getProduitByNom(String nom) throws ProduitNotFoundException {
        Produit produit = produitRepository.findByNom(nom)
                .orElseThrow(() -> new ProduitNotFoundException("Produit not found"));
        return dtotMapper.fromProduit(produit);
    }

    @Override
    public List<ProduitDTO> getAllProduits() {
        return produitRepository.findAll().stream()
                .map(dtotMapper::fromProduit)
                .toList();
    }

    @Override
    public List<ProduitDTO> getProduitsByIngredient(IngredientDTO ingredient) {
        return produitRepository.findByIngredientsContains(dtotMapper.fromIngredientDTO(ingredient)).stream()
                .map(dtotMapper::fromProduit)
                .toList();
    }

    @Override
    public List<ProduitDTO> getProduitsByCategorieParent(CategorieDTO categorie_parent) {
        return produitRepository.findByCategorieParent(dtotMapper.fromCategorieDTO(categorie_parent)).stream()
                .map(dtotMapper::fromProduit)
                .toList();
    }

    @Override
    public List<ProduitDTO> getProduitsByRestaurant(RestaurantDTO restaurant) {
        return produitRepository.findByRestaurant(dtotMapper.fromRestaurantDTO(restaurant)).stream()
                .map(dtotMapper::fromProduit)
                .toList();
    }

    @Override
    public List<ProduitDTO> getProduitsByDisponible(Boolean dispo) {
        return produitRepository.findByDisponible(dispo).stream().map(dtotMapper::fromProduit).toList();
    }


    @Override
    public CategorieDTO saveCategorie(CategorieDTO categorieDTO) {
        Categorie categorie = dtotMapper.fromCategorieDTO(categorieDTO);
        Categorie savedCategorie = categorieRepository.save(categorie);
        return dtotMapper.fromCategorie(savedCategorie);
    }

    @Override
    public CategorieDTO updateCategorie(Long id, CategorieDTO categorieDTO) throws CategorieNotFoundException {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new CategorieNotFoundException("Categorie not found"));

        dtotMapper.updateCategorieFromDto(categorieDTO, categorie);

        Categorie updatedCategorie = categorieRepository.save(categorie);
        return dtotMapper.fromCategorie(updatedCategorie);
    }

    @Override
    public void deleteCategorie(Long id) throws CategorieNotFoundException, CategorieEnUtilisationException {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new CategorieNotFoundException("Categorie not found"));

        // Une catégorie qui contient encore des produits ou des sous-catégories
        // ne doit pas être supprimée directement (contrainte FK
        // element_menu.id_categorie_parent). On bloque avec un message clair :
        // il faut d'abord déplacer/supprimer ces éléments.
        List<ElementMenu> elementsRestants = elementMenuRepository.findByCategorieParent(categorie).stream().toList();
        if (!elementsRestants.isEmpty()) {
            throw new CategorieEnUtilisationException(
                    "Impossible de supprimer la catégorie '" + categorie.getNom() +
                            "' : elle contient encore " + elementsRestants.size() +
                            " élément(s) (produits et/ou sous-catégories). " +
                            "Déplacez-les vers une autre catégorie ou supprimez-les d'abord.");
        }

        categorieRepository.deleteById(id);
    }

    @Override
    public CategorieDTO getCategorie(Long id) throws CategorieNotFoundException {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new CategorieNotFoundException("Categorie not found"));
        return dtotMapper.fromCategorie(categorie);
    }

    @Override
    public List<CategorieDTO> getAllCategories() {
        return categorieRepository.findAll().stream()
                .map(dtotMapper::fromCategorie)
                .toList();
    }

    @Override
    public List<ElementMenuDTO> getElementsMenuByCategorie(CategorieDTO categorie) {
        return elementMenuRepository.findByCategorieParent(dtotMapper.fromCategorieDTO(categorie)).stream()
                .map(dtotMapper::fromElementMenu)
                .toList();
    }

    @Override
    public List<CategorieDTO> getCategoriesByCategorieParent(CategorieDTO categorie_parent) {
        return categorieRepository.findByCategorieParent(dtotMapper.fromCategorieDTO(categorie_parent)).stream()
                .map(dtotMapper::fromCategorie)
                .toList();
    }

    @Override
    public List<CategorieDTO> getCategoriesByRestaurant(RestaurantDTO restaurantDTO) {
        return categorieRepository.findByRestaurant(dtotMapper.fromRestaurantDTO(restaurantDTO)).stream()
                .map(dtotMapper::fromCategorie)
                .toList();
    }



    @Override
    public ModificateurDTO saveModificateur(ModificateurDTO modificateurDTO) {
        Modificateur modificateur = dtotMapper.fromModificateurDTO(modificateurDTO);
        Modificateur savedModificateur = modificateurRepository.save(modificateur);
        return dtotMapper.fromModificateur(savedModificateur);
    }

    @Override
    public ModificateurDTO updateModificateur(Long id, ModificateurDTO modificateurDTO) throws ModificateurNotFoundException {
        Modificateur modificateur = modificateurRepository.findById(id)
                .orElseThrow(() -> new ModificateurNotFoundException("Modificateur not found"));

        dtotMapper.updateModificateurFromDto(modificateurDTO, modificateur);

        Modificateur updatedModificateur = modificateurRepository.save(modificateur);
        return dtotMapper.fromModificateur(updatedModificateur);
    }

    @Override
    public void deleteModificateur(Long id) throws ModificateurNotFoundException {
        Modificateur modificateur = modificateurRepository.findById(id)
                .orElseThrow(() -> new ModificateurNotFoundException("Modificateur not found"));
        modificateurRepository.deleteById(id);
    }

    @Override
    public ModificateurDTO getModificateur(Long id) throws ModificateurNotFoundException {
        Modificateur modificateur = modificateurRepository.findById(id)
                .orElseThrow(() -> new ModificateurNotFoundException("Modificateur not found"));
        return dtotMapper.fromModificateur(modificateur);
    }

    @Override
    public List<ModificateurDTO> getAllModificateurs() {
        return modificateurRepository.findAll().stream()
                .map(dtotMapper::fromModificateur)
                .toList();
    }

    @Override
    public List<ModificateurDTO> getAllModificateurByProduit(ProduitDTO produitDTO) {
        return modificateurRepository.findByProduitsContains(dtotMapper.fromProduitDTO(produitDTO)).stream()
                .map(dtotMapper::fromModificateur)
                .toList();
    }

    @Override
    public CategorieDTO getCategorieByNom(String name) throws CategorieNotFoundException {

        Categorie categorie = categorieRepository.findByNom(name)
                .orElseThrow(() -> new CategorieNotFoundException("Categorie not found"));
        return dtotMapper.fromCategorie(categorie);
    }


    @Override
    public List<ProduitDTO> searchProduits(
            String nom,
            Long categorieId,
            Long restaurantId,
            Boolean disponible)
            throws CategorieNotFoundException, RestaurantNotFoundException {

        Categorie categorie = null;
        Restaurant restaurant = null;

        if (categorieId != null) {
            categorie =categorieRepository.findById(categorieId).orElseThrow(()->new CategorieNotFoundException("categorie not found"));
        }

        if (restaurantId != null) {
            restaurant = dtotMapper.fromRestaurantDTO(restaurantService.getRestaurant(restaurantId));
        }

        return produitRepository.search(
                        nom,
                        categorie,
                        restaurant,
                        disponible)
                .stream()
                .map(dtotMapper::fromProduit)
                .toList();
    }








}