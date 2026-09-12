package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientReclamationDTO;
import org.sid.restaurationbackend.dtos.ClientReclamationRequestDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.CommandeNotFoundException;
import org.sid.restaurationbackend.exceptions.ReclamationNotFoundException;

import java.util.List;

/**
 * Voir ClientReclamationServiceImpl pour le pourquoi de cette
 * séparation : ReclamationService (existant) est consommé par
 * ReclamationController, dont les méthodes de scoping restaurant
 * (verifierAccesReclamation, getReclamationsByClient) supposent un
 * Employee connecté via CurrentUserService — même bug déjà corrigé
 * pour ProduitController à l'étape 3 et CommandeService à l'étape 5.
 * Un ClientAuthentifie y provoquerait une EmployeeNotFoundException.
 */
public interface ClientReclamationService {

    ClientReclamationDTO creerReclamation(ClientReclamationRequestDTO requestDTO)
            throws ClientNotFoundException, CommandeNotFoundException;

    List<ClientReclamationDTO> getMesReclamations() throws ClientNotFoundException;

    ClientReclamationDTO getMaReclamation(Long id)
            throws ClientNotFoundException, ReclamationNotFoundException;
}
