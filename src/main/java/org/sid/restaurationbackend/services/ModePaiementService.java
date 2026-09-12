package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.ModePaiementDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ModePaiementNotFoundException;

import java.util.List;

public interface ModePaiementService {
    ModePaiementDTO saveModePaiement(ModePaiementDTO modePaiementDTO);
    ModePaiementDTO updateModePaiement(Long id, ModePaiementDTO modePaiementDTO) throws ModePaiementNotFoundException;
    ModePaiementDTO getModePaiement(Long id) throws ModePaiementNotFoundException;
    void deleteModePaiement(Long id) throws ModePaiementNotFoundException;
    List<ModePaiementDTO> getAllModePaiements();
    List<ModePaiementDTO> getModesPaiementActifs();

    List<ModePaiementDTO> getModesPaiementByClient(Long clientId)
            throws ClientNotFoundException;

    ModePaiementDTO activerModePaiement(Long id)
            throws ModePaiementNotFoundException;

    ModePaiementDTO desactiverModePaiement(Long id)
            throws ModePaiementNotFoundException;



}
