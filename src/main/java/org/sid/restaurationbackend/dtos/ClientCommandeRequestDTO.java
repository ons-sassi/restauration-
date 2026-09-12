package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.ModeCommande;

import java.util.List;

/**
 * Corps de POST /api/client/commandes (étape 5 — panier + finalisation).
 *
 * Remplace l'ancien usage de CommandeDTO sur cet endpoint : CommandeDTO
 * n'a pas de champ "lignes" et n'était donc jamais exploitable pour
 * transmettre le panier réel (voir le commentaire de sécurité dans
 * ClientCommandeServiceImpl). Ce DTO est volontairement propre à
 * l'espace client et ne transite jamais par RestaurantMapper.
 */
@Data
public class ClientCommandeRequestDTO {

    /**
     * Un seul des trois modes suivants est accepté pour une commande
     * client (voir ClientCommandeServiceImpl.MODES_AUTORISES) :
     * A_EMPORTER, LIVRAISON, SAISIE_MANUELLE_NUMERO_TABLE.
     */
    private ModeCommande modeCommande;

    /**
     * Requis (et utilisé) uniquement si modeCommande =
     * SAISIE_MANUELLE_NUMERO_TABLE. Résolu en une vraie TableRestaurant
     * du restaurant du client — jamais stocké tel quel.
     */
    private Integer numeroTable;

    /**
     * Requis (et utilisé) uniquement si modeCommande = LIVRAISON.
     */
    private String adresseLivraison;

    private List<ClientLigneCommandeRequestDTO> lignes;
}
