package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.entities.ClientAuthentifie;

@Data
public class ModePaiementDTO {

    private Long id_mode_paiement;
    private String libelle;
    private Boolean actif;

    private ClientAuthentifieDTO client;
}
