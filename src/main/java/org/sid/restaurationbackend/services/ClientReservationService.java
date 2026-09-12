package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientReservationDTO;
import org.sid.restaurationbackend.dtos.ClientReservationRequestDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.ReservationNotFoundException;

import java.util.List;

/**
 * Voir ClientReservationServiceImpl pour le pourquoi de cette
 * séparation : ReservationService (existant) est consommé par
 * ReservationController, dont toutes les méthodes supposent un
 * Employee connecté via CurrentUserService
 * (verifierAccesRestaurant/getRestaurantIdConnecte) — même bug déjà
 * corrigé pour ProduitController (étape 3) et Reclamation. Un
 * ClientAuthentifie y provoquerait une EmployeeNotFoundException.
 */
public interface ClientReservationService {

    ClientReservationDTO creerReservation(ClientReservationRequestDTO requestDTO)
            throws ClientNotFoundException;

    List<ClientReservationDTO> getMesReservations() throws ClientNotFoundException;

    ClientReservationDTO getMaReservation(Long id)
            throws ClientNotFoundException, ReservationNotFoundException;

    ClientReservationDTO annulerMaReservation(Long id)
            throws ClientNotFoundException, ReservationNotFoundException;
}