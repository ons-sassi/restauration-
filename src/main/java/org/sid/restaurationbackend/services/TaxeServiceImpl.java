package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.TaxeDTO;
import org.sid.restaurationbackend.entities.Taxe;
import org.sid.restaurationbackend.exceptions.TaxeNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.TaxeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TaxeServiceImpl implements TaxeService {

    private final RestaurantMapper dtoMapper;
    private final TaxeRepository taxeRepository;




    @Override
    public TaxeDTO saveTaxe(TaxeDTO taxeDTO) {

        Taxe taxe =
                dtoMapper.fromTaxeDTO(taxeDTO);


        taxe.setDateCreation(new Date());


        if (taxe.getStatut() == null ||
                taxe.getStatut().isBlank()) {

            taxe.setStatut("ACTIF");
        }

        Taxe savedTaxe =
                taxeRepository.save(taxe);

        log.info(
                "Taxe créée : {}",
                savedTaxe.getId_taxe()
        );

        return dtoMapper.fromTaxe(savedTaxe);
    }




    @Override
    public TaxeDTO updateTaxe(
            Long id,
            TaxeDTO taxeDTO)
            throws TaxeNotFoundException {

        Taxe taxe =
                taxeRepository.findById(id)
                        .orElseThrow(() ->
                                new TaxeNotFoundException(
                                        "Taxe not found"
                                )
                        );

        dtoMapper.updateTaxeFromDto(
                taxeDTO,
                taxe
        );

        Taxe updatedTaxe =
                taxeRepository.save(taxe);

        return dtoMapper.fromTaxe(updatedTaxe);
    }



    @Override
    @Transactional(readOnly = true)
    public TaxeDTO getTaxe(Long id)
            throws TaxeNotFoundException {

        Taxe taxe =
                taxeRepository.findById(id)
                        .orElseThrow(() ->
                                new TaxeNotFoundException(
                                        "Taxe not found"
                                )
                        );

        return dtoMapper.fromTaxe(taxe);
    }




    @Override
    public void deleteTaxe(Long id)
            throws TaxeNotFoundException {

        Taxe taxe =
                taxeRepository.findById(id)
                        .orElseThrow(() ->
                                new TaxeNotFoundException(
                                        "Taxe not found"
                                )
                        );

        taxeRepository.delete(taxe);
    }



    @Override
    @Transactional(readOnly = true)
    public List<TaxeDTO> getAllTaxes() {

        return taxeRepository
                .findAll()
                .stream()
                .map(dtoMapper::fromTaxe)
                .toList();
    }




    @Transactional(readOnly = true)
    @Override
    public List<TaxeDTO> searchTaxes(
            String nom,
            String applicableA,
            String statut,
            Double tauxMin,
            Double tauxMax) {

        return taxeRepository
                .search(
                        nom,
                        applicableA,
                        statut,
                        tauxMin,
                        tauxMax
                )
                .stream()
                .map(dtoMapper::fromTaxe)
                .toList();
    }



    @Transactional(readOnly = true)
    @Override
    public List<TaxeDTO> getTaxesByStatut(
            String statut) {

        return taxeRepository
                .findByStatutIgnoreCase(statut)
                .stream()
                .map(dtoMapper::fromTaxe)
                .toList();
    }




    @Transactional(readOnly = true)
    @Override
    public List<TaxeDTO> getTaxesActives() {

        return taxeRepository
                .findByStatutIgnoreCase("ACTIF")
                .stream()
                .map(dtoMapper::fromTaxe)
                .toList();
    }
}

