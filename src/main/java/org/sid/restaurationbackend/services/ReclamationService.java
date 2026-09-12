package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.CommandeDTO;
import org.sid.restaurationbackend.dtos.ReclamationDTO;
import org.sid.restaurationbackend.enums.StatutReclamation;
import org.sid.restaurationbackend.exceptions.ReclamationNotFoundException;

import java.util.List;

public interface ReclamationService {
        ReclamationDTO saveReclamation(ReclamationDTO reclamationDTO);
        ReclamationDTO updateReclamation(Long id , ReclamationDTO reclamationDTO) throws ReclamationNotFoundException;
        ReclamationDTO getReclamation(Long id_reclamation) throws ReclamationNotFoundException;
        void deleteReclamation(Long id_reclamation) throws ReclamationNotFoundException;
        List<ReclamationDTO> getAllReclamations();
        List<ReclamationDTO> getReclamationsByClient(ClientAuthentifieDTO client);
        List<ReclamationDTO> getReclamationsByCommande(CommandeDTO commande);

    List<ReclamationDTO> getReclamationsByStatut(
            StatutReclamation statut);

        ReclamationDTO repondreReclamation(
                Long id,
                String reponse)
                throws ReclamationNotFoundException;
}
