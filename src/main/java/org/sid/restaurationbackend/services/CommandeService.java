package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.CommandeDTO;
import org.sid.restaurationbackend.dtos.LigneCommandeDTO;
import org.sid.restaurationbackend.enums.StatutCommande;
import org.sid.restaurationbackend.exceptions.*;

import java.util.Date;
import java.util.List;

public interface CommandeService {

    CommandeDTO updateCommande(
            Long id,
            CommandeDTO commandeDTO
    ) throws CommandeNotFoundException;

    void deleteCommande(
            Long id
    ) throws CommandeNotFoundException;

    CommandeDTO getCommande(
            Long id
    ) throws CommandeNotFoundException;

    List<CommandeDTO> getAllCommandes();

    List<CommandeDTO> searchCommandes(
            Long clientId,
            StatutCommande statut,
            Date dateDebut,
            Date dateFin
    ) throws ClientNotFoundException;

    List<CommandeDTO> getCommandesByClient(
            Long clientId
    ) throws ClientNotFoundException;

    List<CommandeDTO> getCommandesByClientNonAuthentifie(
            Long sessionId
    ) throws ClientNotFoundException;

    List<CommandeDTO> getCommandesByTable(
            Long tableId
    ) throws TableNotFoundException;

    List<CommandeDTO> getCommandesByEmployee(
            Long employeeId
    ) throws EmployeeNotFoundException;

    List<CommandeDTO> getCommandesByStatut(
            StatutCommande statut
    );

    List<CommandeDTO> getCommandesByDate(
            Date dateDebut,
            Date dateFin
    );

    CommandeDTO changerStatut(
            Long commandeId,
            StatutCommande statut
    ) throws CommandeNotFoundException;

    /*
     * Annule une commande.
     *
     * Si la commande est rattachée à une table pour laquelle il existe
     * une réservation active (EN_ATTENTE ou CONFIRMEE) à la même date,
     * cette réservation est également annulée et la table est libérée
     * (statut LIBRE).
     */
    CommandeDTO annulerCommande(
            Long commandeId
    ) throws CommandeNotFoundException;

    // Ajouté au Lot 2.5 (sécurité multi-restaurant) : recharge une ligne
    // par son propre id, avec sa commande (et donc son restaurant réel)
    // mappée, pour vérification avant modifierLigne/supprimerLigne.
    LigneCommandeDTO getLigneCommande(
            Long ligneId
    ) throws LigneCommandeNotFoundException;

    LigneCommandeDTO ajouterLigne(
            Long commandeId,
            LigneCommandeDTO ligneDTO
    ) throws CommandeNotFoundException;

    LigneCommandeDTO modifierLigne(
            Long ligneId,
            LigneCommandeDTO ligneDTO
    ) throws LigneCommandeNotFoundException,
            CommandeNotFoundException;

    void supprimerLigne(
            Long ligneId
    ) throws LigneCommandeNotFoundException,
            CommandeNotFoundException;

    List<LigneCommandeDTO> getLignesCommande(
            Long commandeId
    ) throws CommandeNotFoundException;

    Double calculerMontantTotal(
            Long commandeId
    ) throws CommandeNotFoundException;

    CommandeDTO recalculerMontantTotal(
            Long commandeId
    ) throws CommandeNotFoundException;

    CommandeDTO creerCommandeClient(
            CommandeDTO commandeDTO
    );

    CommandeDTO creerCommandeAnonyme(
            CommandeDTO commandeDTO
    );

    /*
     * NOUVELLE MÉTHODE
     *
     * Permet de récupérer une commande uniquement
     * si elle appartient au client connecté.
     */
    CommandeDTO getCommandeClient(
            Long commandeId,
            Long clientId
    ) throws CommandeNotFoundException;
}