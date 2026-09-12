package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.CommandeDTO;
import org.sid.restaurationbackend.services.CommandeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/public/commandes")
@AllArgsConstructor
public class PublicCommandeController {

    private final CommandeService commandeService;



    @PostMapping
    public ResponseEntity<CommandeDTO> creerCommandeAnonyme(
            @RequestBody CommandeDTO commandeDTO) {

        CommandeDTO commande =
                commandeService.creerCommandeAnonyme(
                        commandeDTO
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commande);
    }
}