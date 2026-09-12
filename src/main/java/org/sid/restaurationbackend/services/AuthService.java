package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.AuthResponseDTO;
import org.sid.restaurationbackend.dtos.BackOfficeLoginDTO;
import org.sid.restaurationbackend.dtos.ClientLoginDTO;
import org.sid.restaurationbackend.dtos.ClientRegisterDTO;
import org.sid.restaurationbackend.dtos.LoginDTO;
import org.sid.restaurationbackend.dtos.PdvLoginDTO;
import org.sid.restaurationbackend.dtos.SuperAdminLoginDTO;
import org.sid.restaurationbackend.exceptions.PointDeVenteNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;

public interface AuthService {

    /**
     * Connexion universelle : Employee, Client ou SuperAdmin.
     */
    AuthResponseDTO login(LoginDTO loginDTO);

    /**
     * Connexion Back Office
     *
     * Email + mot de passe
     */
    AuthResponseDTO loginBackOffice(
            BackOfficeLoginDTO loginDTO
    );


    /**
     * Connexion PDV
     *
     * PIN + PDV
     */
    AuthResponseDTO loginPdv(
            PdvLoginDTO loginDTO
    ) throws PointDeVenteNotFoundException;


    /**
     * Connexion client
     */
    AuthResponseDTO loginClient(
            ClientLoginDTO loginDTO
    );


    /**
     * Auto-inscription client (endpoint public).
     *
     * Le client doit avoir choisi son restaurant au préalable
     * (ClientAuthentifie.restaurant est obligatoire) : voir
     * ClientRegisterDTO.restaurantId. Connecte automatiquement le
     * client après création, comme un login classique.
     */
    AuthResponseDTO registerClient(
            ClientRegisterDTO registerDTO
    ) throws RestaurantNotFoundException;


    /**
     * Connexion super-admin plateforme
     *
     * Compte distinct de Employee (pas de restaurant, pas de PDV,
     * pas de rôle Role/RoleFonctionnalite) — seul habilité à
     * créer/lister-tous/rechercher/supprimer des restaurants
     * (voir RestaurantController).
     */
    AuthResponseDTO loginSuperAdmin(
            SuperAdminLoginDTO loginDTO
    );


    /**
     * Sélectionne un restaurant pour le SUPERADMIN sans changer son rôle.
     * Le nouveau JWT contient le restaurant sélectionné.
     */
    AuthResponseDTO selectRestaurant(Long restaurantId)
            throws RestaurantNotFoundException;
}