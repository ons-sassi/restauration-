package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.PresenceDTO;
import org.sid.restaurationbackend.dtos.TaxeDTO;
import org.sid.restaurationbackend.exceptions.TaxeNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TaxeService {
    TaxeDTO saveTaxe(TaxeDTO taxeDTO);
    TaxeDTO updateTaxe(Long id, TaxeDTO taxeDTO) throws TaxeNotFoundException;
    void deleteTaxe(Long id) throws TaxeNotFoundException;
    TaxeDTO getTaxe(Long id) throws TaxeNotFoundException;
    List<TaxeDTO> getAllTaxes();

    @Transactional(readOnly = true)
    List<TaxeDTO> searchTaxes(
            String nom,
            String applicableA,
            String statut,
            Double tauxMin,
            Double tauxMax);

    @Transactional(readOnly = true)
    List<TaxeDTO> getTaxesByStatut(
            String statut);

    @Transactional(readOnly = true)
    List<TaxeDTO> getTaxesActives();
}
