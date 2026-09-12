package org.sid.restaurationbackend.requests;

import lombok.Data;
import org.sid.restaurationbackend.dtos.LigneBonDeCommandeDTO;

import java.util.List;

@Data
public class PasserBonDeCommandeRequest {

    private Long fournisseurId;

    private List<LigneBonDeCommandeDTO> lignes;
}