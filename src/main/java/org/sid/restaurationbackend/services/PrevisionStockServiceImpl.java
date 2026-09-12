package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.IngredientDTO;
import org.sid.restaurationbackend.dtos.PrevisionStockDTO;
import org.sid.restaurationbackend.entities.PrevisionStock;
import org.sid.restaurationbackend.exceptions.PrevisionStockNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.PrevisionStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class PrevisionStockServiceImpl
        implements PrevisionStockService {

    private RestaurantMapper dtoMapper;
    private PrevisionStockRepository previsionStockRepository;


    @Override
    public PrevisionStockDTO savePrevisionStock(
            PrevisionStockDTO previsionStockDTO) {

        PrevisionStock previsionStock =
                dtoMapper.fromPrevisionStockDTO(previsionStockDTO);

        if (previsionStock.getDate_generation() == null) {
            previsionStock.setDate_generation(new java.util.Date());
        }

        PrevisionStock savedPrevisionStock =
                previsionStockRepository.save(previsionStock);

        return dtoMapper.fromPrevisionStock(savedPrevisionStock);
    }


    @Override
    public PrevisionStockDTO updatePrevisionStock(
            Long id,
            PrevisionStockDTO previsionStockDTO)
            throws PrevisionStockNotFoundException {

        PrevisionStock previsionStock =
                previsionStockRepository.findById(id)
                        .orElseThrow(() ->
                                new PrevisionStockNotFoundException(
                                        "Prevision de stock not found"));

        dtoMapper.updatePrevisionStockFromDto(
                previsionStockDTO,
                previsionStock
        );

        PrevisionStock updatedPrevisionStock =
                previsionStockRepository.save(previsionStock);

        return dtoMapper.fromPrevisionStock(
                updatedPrevisionStock
        );
    }


    @Override
    public void deletePrevisionStock(Long id)
            throws PrevisionStockNotFoundException {

        PrevisionStock previsionStock =
                previsionStockRepository.findById(id)
                        .orElseThrow(() ->
                                new PrevisionStockNotFoundException(
                                        "Prevision de stock not found"));

        previsionStockRepository.delete(previsionStock);
    }


    @Override
    public PrevisionStockDTO getPrevisionStock(Long id)
            throws PrevisionStockNotFoundException {

        PrevisionStock previsionStock =
                previsionStockRepository.findById(id)
                        .orElseThrow(() ->
                                new PrevisionStockNotFoundException(
                                        "Prevision de stock not found"));

        return dtoMapper.fromPrevisionStock(previsionStock);
    }


    @Override
    public List<PrevisionStockDTO> listPrevisionStocks() {

        return previsionStockRepository.findAll()
                .stream()
                .map(dtoMapper::fromPrevisionStock)
                .toList();
    }


    @Override
    public List<PrevisionStockDTO> listPrevisionStocksByDate(
            String date) {

        return previsionStockRepository
                .findByPeriode(date)
                .stream()
                .map(dtoMapper::fromPrevisionStock)
                .toList();
    }


    @Override
    public List<PrevisionStockDTO> listPrevisionStocksByIngredient(
            IngredientDTO ingredientDTO) {

        return previsionStockRepository
                .findByIngredient(
                        dtoMapper.fromIngredientDTO(ingredientDTO)
                )
                .stream()
                .map(dtoMapper::fromPrevisionStock)
                .toList();
    }


    @Override
    public List<PrevisionStockDTO> listPrevisionStocksByBaseeSurVentes(
            Boolean baseeSurVentes) {

        return previsionStockRepository
                .findByBaseeSurVentes(baseeSurVentes)
                .stream()
                .map(dtoMapper::fromPrevisionStock)
                .toList();
    }
}

