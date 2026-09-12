package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.ElementMenu;
import org.sid.restaurationbackend.exceptions.CategorieEnUtilisationException;
import org.sid.restaurationbackend.exceptions.CategorieNotFoundException;
import org.sid.restaurationbackend.exceptions.ModificateurNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitEnUtilisationException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;

import java.util.List;

public interface ProduitService {
    ProduitDTO saveProduit(ProduitDTO produitDTO);
    ProduitDTO updateProduit(Long id,ProduitDTO produitDTO) throws ProduitNotFoundException;
    void deleteProduit(Long id) throws ProduitNotFoundException, ProduitEnUtilisationException;
    ProduitDTO getProduit(Long id) throws ProduitNotFoundException;
    ProduitDTO getProduitByNom(String nom) throws ProduitNotFoundException;
    List<ProduitDTO> getAllProduits();
    List<ProduitDTO> getProduitsByIngredient(IngredientDTO ingredientDTO);
    List<ProduitDTO> getProduitsByCategorieParent(CategorieDTO categorie_parent);
    List<ProduitDTO> getProduitsByRestaurant(RestaurantDTO restaurantDTO);
    List<ProduitDTO> getProduitsByDisponible(Boolean dispo);

    CategorieDTO saveCategorie(CategorieDTO categorieDTO);
    CategorieDTO updateCategorie(Long id, CategorieDTO categorieDTO) throws CategorieNotFoundException;
    void deleteCategorie(Long id) throws CategorieNotFoundException, CategorieEnUtilisationException;
    CategorieDTO getCategorie(Long id) throws CategorieNotFoundException;
    List<CategorieDTO> getAllCategories();
    List<ElementMenuDTO> getElementsMenuByCategorie( CategorieDTO categorieDTO);
    List<CategorieDTO> getCategoriesByCategorieParent(CategorieDTO categorieParent);
    List<CategorieDTO> getCategoriesByRestaurant(RestaurantDTO restaurant);

    ModificateurDTO saveModificateur(ModificateurDTO modificateurDTO);
    ModificateurDTO updateModificateur(Long id, ModificateurDTO modificateurDTO) throws ModificateurNotFoundException;
    void deleteModificateur(Long id) throws ModificateurNotFoundException;
    ModificateurDTO getModificateur(Long id) throws ModificateurNotFoundException;
    List<ModificateurDTO> getAllModificateurs();


    List<ModificateurDTO> getAllModificateurByProduit(ProduitDTO produitDTO);

    CategorieDTO getCategorieByNom(String name) throws CategorieNotFoundException;





    List<ProduitDTO> searchProduits(
            String nom,
            Long categorieId,
            Long restaurantId,
            Boolean disponible)
            throws CategorieNotFoundException, RestaurantNotFoundException;
}