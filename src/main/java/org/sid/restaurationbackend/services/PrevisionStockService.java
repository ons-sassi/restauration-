package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.IngredientDTO;
import org.sid.restaurationbackend.dtos.PrevisionStockDTO;
import org.sid.restaurationbackend.exceptions.PrevisionStockNotFoundException;

import java.util.List;

public interface PrevisionStockService {
    PrevisionStockDTO savePrevisionStock(PrevisionStockDTO previsionStockDTO);
    PrevisionStockDTO updatePrevisionStock(Long id ,PrevisionStockDTO previsionStockDTO) throws PrevisionStockNotFoundException;
    void deletePrevisionStock(Long id) throws PrevisionStockNotFoundException;
    PrevisionStockDTO getPrevisionStock(Long id) throws PrevisionStockNotFoundException;
    List<PrevisionStockDTO> listPrevisionStocks();
    List<PrevisionStockDTO> listPrevisionStocksByDate(String date);
    List<PrevisionStockDTO> listPrevisionStocksByIngredient(IngredientDTO ingredientDTO);

    List<PrevisionStockDTO> listPrevisionStocksByBaseeSurVentes(
            Boolean baseeSurVentes);
}
