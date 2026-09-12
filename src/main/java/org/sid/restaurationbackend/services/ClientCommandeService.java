package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientCommandeConfirmationDTO;
import org.sid.restaurationbackend.dtos.ClientCommandeRequestDTO;
import org.sid.restaurationbackend.dtos.ClientLigneCommandeConfirmationDTO;
import org.sid.restaurationbackend.dtos.ModeleRecuDTO;
import org.sid.restaurationbackend.dtos.TableRestaurantDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.CommandeNotFoundException;
import org.sid.restaurationbackend.exceptions.ModificateurNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;

import java.util.List;

/**
 * Finalisation du panier de l'espace client (étape 5 du plan).
 *
 * Séparé de CommandeService pour la même raison que ClientMenuService
 * est séparé de ProduitService/CategorieService : le restaurant est
 * toujours dérivé du ClientAuthentifie connecté (jamais un id envoyé
 * par le frontend), et CommandeDTO n'a pas de champ "lignes" exploitable
 * pour transmettre un panier réel — voir ClientCommandeServiceImpl.
 */
public interface ClientCommandeService {

    ClientCommandeConfirmationDTO creerCommandeDepuisPanier(
            ClientCommandeRequestDTO requestDTO)
            throws ClientNotFoundException, ProduitNotFoundException,
            ModificateurNotFoundException, TableNotFoundException;

    /*
     * Historique — détail d'une commande passée.
     *
     * GET /api/commandes/{id}/lignes (CommandeController, back-office)
     * ne peut pas être réutilisé ici : il appelle
     * currentUserService.verifierAccesRestaurant(...), qui suppose un
     * Employee connecté (EmployeeNotFoundException sinon) — même bug
     * récurrent que celui déjà corrigé pour Réclamations/Suggestions.
     * Ownership vérifiée directement via commande.getClient(), jamais
     * via un restaurant indirect. 404 (pas 403) si la commande
     * appartient à un autre client.
     */
    List<ClientLigneCommandeConfirmationDTO> getMesLignesCommande(
            Long commandeId)
            throws ClientNotFoundException, CommandeNotFoundException;

    /** Modèle de reçu du restaurant du client authentifié. */
    ModeleRecuDTO getModeleRecuClient() throws ClientNotFoundException;

    /**
     * Tables actuellement LIBRES du restaurant du client connecté.
     *
     * Utilisé par l'écran de finalisation du panier (mode
     * SAISIE_MANUELLE_NUMERO_TABLE) pour proposer un sélecteur ne
     * listant que des tables réellement disponibles, au lieu de laisser
     * le client saisir n'importe quel numéro à la main (source de
     * l'ancien bug : numéro inexistant, table déjà occupée, ou table
     * d'un autre restaurant).
     *
     * ⚠️ Ne réutilise PAS TableController "/api/tables/disponibles" :
     * cet endpoint dérive le restaurant via
     * CurrentUserService.getRestaurantIdConnecte(), qui suppose un
     * Employee connecté et lève EmployeeNotFoundException pour un
     * ClientAuthentifie (même bug de fond que ClientMenuController,
     * voir sa javadoc). Ici, le restaurant est toujours dérivé du
     * ClientAuthentifie connecté.
     */
    List<TableRestaurantDTO> getTablesDisponibles() throws ClientNotFoundException;
}
