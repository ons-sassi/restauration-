package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.LigneBonDeCommande;
import org.sid.restaurationbackend.exceptions.IngredientNotFoundException;

import java.util.List;

public interface IngredientService {
    IngredientDTO saveIngredient(IngredientDTO ingredientDTO);
    IngredientDTO updateIngredient(Long id ,IngredientDTO ingredientDTO) throws IngredientNotFoundException;
    void deleteIngredient(Long id) throws IngredientNotFoundException;
    IngredientDTO getIngredient(Long id) throws IngredientNotFoundException;
    List<IngredientDTO> getAllIngredients();
    List<IngredientDTO> getIngredientsByFournisseur(FournisseurDTO fournisseur);
    List<IngredientDTO> getIngredientsByProduit(ProduitDTO produit);



    List<IngredientDTO> searchIngredients(
            String nomIngredient,
            String nomFournisseur,
            String nomProduit);
    List<IngredientDTO> underSeuil ();
    List<IngredientDTO> presDePeremption(Integer nbrJourRestant);

    List<IngredientDTO> getIngredientsPerimes();
}
