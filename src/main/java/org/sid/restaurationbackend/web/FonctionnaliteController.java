package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.FonctionnaliteDTO;
import org.sid.restaurationbackend.exceptions.FonctionnaliteNotFoundException;
import org.sid.restaurationbackend.services.FonctionnaliteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fonctionnalites")
@AllArgsConstructor
public class FonctionnaliteController {

    private final FonctionnaliteService fonctionnaliteService;

    @PostMapping
    public ResponseEntity<FonctionnaliteDTO> saveFonctionnalite(
            @RequestBody FonctionnaliteDTO fonctionnaliteDTO) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        fonctionnaliteService
                                .saveFonctionnalite(fonctionnaliteDTO)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<FonctionnaliteDTO> updateFonctionnalite(
            @PathVariable Long id,
            @RequestBody FonctionnaliteDTO fonctionnaliteDTO)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                fonctionnaliteService.updateFonctionnalite(
                        id,
                        fonctionnaliteDTO
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFonctionnalite(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        fonctionnaliteService.deleteFonctionnalite(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FonctionnaliteDTO> getFonctionnalite(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                fonctionnaliteService.getFonctionnalite(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<FonctionnaliteDTO>>
    getAllFonctionnalites() {

        return ResponseEntity.ok(
                fonctionnaliteService.getAllFonctionnalites()
        );
    }

    @GetMapping("/search/nom")
    public ResponseEntity<List<FonctionnaliteDTO>>
    getByNom(@RequestParam String nom) {

        return ResponseEntity.ok(
                fonctionnaliteService
                        .getFonctionnalitesByNom(nom)
        );
    }

    @GetMapping("/search/code")
    public ResponseEntity<List<FonctionnaliteDTO>>
    getByCode(@RequestParam String code) {

        return ResponseEntity.ok(
                fonctionnaliteService
                        .getFonctionnalitesByCode(code)
        );
    }

    @GetMapping("/disponible/pdv")
    public ResponseEntity<List<FonctionnaliteDTO>>
    getByDisponibilitePdv(
            @RequestParam Boolean disponible) {

        return ResponseEntity.ok(
                fonctionnaliteService
                        .getFonctionnalitesByDisponibilitePdv(
                                disponible
                        )
        );
    }

    @GetMapping("/disponible/backoffice")
    public ResponseEntity<List<FonctionnaliteDTO>>
    getByDisponibiliteBackoffice(
            @RequestParam Boolean disponible) {

        return ResponseEntity.ok(
                fonctionnaliteService
                        .getFonctionnalitesByDisponibiliteBackoffice(
                                disponible
                        )
        );
    }

    @GetMapping("/parents")
    public ResponseEntity<List<FonctionnaliteDTO>>
    getFonctionnalitesParent() {

        return ResponseEntity.ok(
                fonctionnaliteService
                        .getFonctionnalitesParent()
        );
    }

    @GetMapping("/{id}/sous-fonctionnalites")
    public ResponseEntity<List<FonctionnaliteDTO>>
    getSousFonctionnalites(
            @PathVariable Long id)
            throws FonctionnaliteNotFoundException {

        return ResponseEntity.ok(
                fonctionnaliteService
                        .getSousFonctionnalites(id)
        );
    }
}