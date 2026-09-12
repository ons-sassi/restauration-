package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.enums.StatutUtilisateur;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;

import java.util.List;

public interface ClientAuthentifieService {
    ClientAuthentifieDTO saveClientAuthentifie(ClientAuthentifieDTO clientAuthentifieDTO);
    ClientAuthentifieDTO updateClientAuthentifie(Long id ,ClientAuthentifieDTO clientAuthentifieDTO) throws ClientNotFoundException;
    void deleteClientAuthentifie(Long id) throws ClientNotFoundException;
    ClientAuthentifieDTO getClientAuthentifie(Long id) throws ClientNotFoundException;
    List<ClientAuthentifieDTO> getAllClientAuthentifies();
    List<ClientAuthentifieDTO> searchClientAuthentifies(String keyword);

    List<ClientAuthentifieDTO> getClientsByStatut(
            StatutUtilisateur statut);

    void updateDerniereConnexion(Long id)
            throws ClientNotFoundException;
ClientAuthentifieDTO desactiverClient(Long id)
            throws ClientNotFoundException;

    ClientAuthentifieDTO activerClient(Long id)
            throws ClientNotFoundException;

    ClientAuthentifieDTO getClientByCodeParrainage(String codeParrainage)
            throws ClientNotFoundException;

    ClientAuthentifieDTO updatePreferences(
            Long id,
            String preferences)
            throws ClientNotFoundException;

    ClientAuthentifieDTO updateAllergies(
            Long id,
            String allergies)
            throws ClientNotFoundException;

    ClientAuthentifieDTO getClientConnecte() throws ClientNotFoundException;
}
