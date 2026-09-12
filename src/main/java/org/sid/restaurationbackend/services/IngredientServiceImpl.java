package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.FournisseurDTO;
import org.sid.restaurationbackend.dtos.IngredientDTO;
import org.sid.restaurationbackend.dtos.ProduitDTO;
import org.sid.restaurationbackend.entities.Ingredient;
import org.sid.restaurationbackend.exceptions.IngredientNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class IngredientServiceImpl implements IngredientService {
    private RestaurantMapper dtotMapper;
    private IngredientRepository ingredientRepository;

    @Override
    public IngredientDTO saveIngredient(IngredientDTO ingredientDTO) {
        Ingredient ingredient = dtotMapper.fromIngredientDTO(ingredientDTO);
        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return dtotMapper.fromIngredient(savedIngredient);
    }

    @Override
    public IngredientDTO updateIngredient(Long id, IngredientDTO ingredientDTO) throws IngredientNotFoundException {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IngredientNotFoundException("Ingredient not found"));

        dtotMapper.updateIngredientFromDto(ingredientDTO, ingredient);

        Ingredient updatedIngredient = ingredientRepository.save(ingredient);
        return dtotMapper.fromIngredient(updatedIngredient);
    }

    @Override
    public void deleteIngredient(Long id) throws IngredientNotFoundException {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IngredientNotFoundException("Ingredient not found"));
        ingredientRepository.delete(ingredient);

    }

    @Override
    public IngredientDTO getIngredient(Long id) throws IngredientNotFoundException {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IngredientNotFoundException("Ingredient not found"));
        return dtotMapper.fromIngredient(ingredient);
    }

    @Override
    public List<IngredientDTO> getAllIngredients() {

        return ingredientRepository.findAll()
                .stream()
                .map(dtotMapper::fromIngredient)
                .toList();
    }

    @Override
    public List<IngredientDTO> getIngredientsByFournisseur(FournisseurDTO fournisseur) {

        return ingredientRepository.findByFournisseur(dtotMapper.fromFournisseurDTO(fournisseur))
                .stream()
                .map(dtotMapper::fromIngredient)
                .toList();
    }

    @Override
    public List<IngredientDTO> getIngredientsByProduit(ProduitDTO produit) {
        return ingredientRepository.findByProduitsContains(dtotMapper.fromProduitDTO(produit))
                .stream()
                .map(dtotMapper::fromIngredient)
                .toList();
    }

    @Override
    public List<IngredientDTO> searchIngredients(
            String nomIngredient,
            String nomFournisseur,
            String nomProduit) {

        return ingredientRepository.search(
                        nomIngredient,
                        nomFournisseur,
                        nomProduit)
                .stream()
                .map(dtotMapper::fromIngredient)
                .toList();
    }

    @Override
    public List<IngredientDTO> underSeuil() {
        return ingredientRepository.underSeuil().stream().map(dtotMapper::fromIngredient).toList();
    }

    @Override
    public List<IngredientDTO> presDePeremption(Integer nbrJourRestant) {

        if (nbrJourRestant == null || nbrJourRestant < 0) {
            throw new IllegalArgumentException(
                    "Le nombre de jours restant doit être positif"
            );
        }

        Date aujourdHui = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(aujourdHui);
        calendar.add(Calendar.DAY_OF_MONTH, nbrJourRestant);

        Date dateLimite = calendar.getTime();

        return ingredientRepository
                .findIngredientsPresDePeremption(aujourdHui, dateLimite)
                .stream()
                .map(dtotMapper::fromIngredient)
                .toList();
    }

    @Override
    public List<IngredientDTO> getIngredientsPerimes() {

        Date aujourdHui = new Date();

        return ingredientRepository
                .findIngredientsPerimes(aujourdHui)
                .stream()
                .map(dtotMapper::fromIngredient)
                .toList();
    }




}
