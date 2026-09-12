
package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.TaxeDTO;
import org.sid.restaurationbackend.exceptions.TaxeNotFoundException;
import org.sid.restaurationbackend.services.TaxeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taxes")
@AllArgsConstructor
@Slf4j
public class TaxeController {


    private final TaxeService taxeService;



    @PostMapping
    public TaxeDTO saveTaxe(
            @RequestBody TaxeDTO taxeDTO) {

        return taxeService.saveTaxe(taxeDTO);
    }




    @GetMapping
    public List<TaxeDTO> getAllTaxes() {

        return taxeService.getAllTaxes();
    }




    @GetMapping("/{id}")
    public TaxeDTO getTaxe(
            @PathVariable Long id)
            throws TaxeNotFoundException {

        return taxeService.getTaxe(id);
    }




    @PutMapping("/{id}")
    public TaxeDTO updateTaxe(
            @PathVariable Long id,
            @RequestBody TaxeDTO taxeDTO)
            throws TaxeNotFoundException {

        return taxeService.updateTaxe(
                id,
                taxeDTO
        );
    }




    @DeleteMapping("/{id}")
    public void deleteTaxe(
            @PathVariable Long id)
            throws TaxeNotFoundException {

        taxeService.deleteTaxe(id);
    }



    @GetMapping("/search")
    public List<TaxeDTO> searchTaxes(

            @RequestParam(required = false)
            String nom,

            @RequestParam(required = false)
            String applicableA,

            @RequestParam(required = false)
            String statut,

            @RequestParam(required = false)
            Double tauxMin,

            @RequestParam(required = false)
            Double tauxMax) {

        return taxeService.searchTaxes(
                nom,
                applicableA,
                statut,
                tauxMin,
                tauxMax
        );
    }




    @GetMapping("/statut/{statut}")
    public List<TaxeDTO> getTaxesByStatut(
            @PathVariable String statut) {

        return taxeService
                .getTaxesByStatut(statut);
    }




    @GetMapping("/actives")
    public List<TaxeDTO> getTaxesActives() {

        return taxeService
                .getTaxesActives();
    }
}

