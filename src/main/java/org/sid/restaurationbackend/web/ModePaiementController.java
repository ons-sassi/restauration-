package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ModePaiementDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ModePaiementNotFoundException;
import org.sid.restaurationbackend.services.ModePaiementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modes-paiement")
@AllArgsConstructor
public class ModePaiementController {

    private final ModePaiementService modePaiementService;

    @PostMapping
    public ResponseEntity<ModePaiementDTO> save(
            @RequestBody ModePaiementDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modePaiementService.saveModePaiement(dto));
    }

    @GetMapping
    public ResponseEntity<List<ModePaiementDTO>> getAll() {

        return ResponseEntity.ok(
                modePaiementService.getAllModePaiements()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModePaiementDTO> getById(
            @PathVariable Long id)
            throws ModePaiementNotFoundException {

        return ResponseEntity.ok(
                modePaiementService.getModePaiement(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModePaiementDTO> update(
            @PathVariable Long id,
            @RequestBody ModePaiementDTO dto)
            throws ModePaiementNotFoundException {

        return ResponseEntity.ok(
                modePaiementService.updateModePaiement(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id)
            throws ModePaiementNotFoundException {

        modePaiementService.deleteModePaiement(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<ModePaiementDTO>> getActifs() {

        return ResponseEntity.ok(
                modePaiementService.getModesPaiementActifs()
        );
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ModePaiementDTO>> getByClient(
            @PathVariable Long clientId)
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                modePaiementService.getModesPaiementByClient(clientId)
        );
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<ModePaiementDTO> activer(
            @PathVariable Long id)
            throws ModePaiementNotFoundException {

        return ResponseEntity.ok(
                modePaiementService.activerModePaiement(id)
        );
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<ModePaiementDTO> desactiver(
            @PathVariable Long id)
            throws ModePaiementNotFoundException {

        return ResponseEntity.ok(
                modePaiementService.desactiverModePaiement(id)
        );
    }
}