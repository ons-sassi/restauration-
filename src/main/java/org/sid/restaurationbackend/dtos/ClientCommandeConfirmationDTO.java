package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.ModeCommande;
import org.sid.restaurationbackend.enums.StatutCommande;

import java.util.Date;
import java.util.List;

/**
 * Réponse de POST /api/client/commandes (étape 5). Volontairement séparé
 * de CommandeDTO (qui n'a pas de champ "lignes" et est partagé avec le
 * PDV / back-office) pour ne rien risquer sur son mapping générique.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientCommandeConfirmationDTO {

    private Long id_commande;
    private Date dateCommande;
    private StatutCommande statut;
    private ModeCommande modeCommande;
    private Double montant_total;

    /** Non-nul uniquement si modeCommande = SAISIE_MANUELLE_NUMERO_TABLE. */
    private Integer numeroTable;

    /** Non-nulle uniquement si modeCommande = LIVRAISON. */
    private String adresseLivraison;

    private List<ClientLigneCommandeConfirmationDTO> lignes;
}
