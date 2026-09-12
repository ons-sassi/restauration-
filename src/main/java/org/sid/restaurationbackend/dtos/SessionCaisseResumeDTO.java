package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

/**
 * Résumé financier d'une session de caisse.
 *
 * Permet d'afficher, avant (ou après) la fermeture d'une caisse :
 *
 *   - le montant total vendu pendant la session (totalVentes),
 *   - le montant théorique attendu en caisse
 *     (montantOuverture + totalVentes),
 *   - et, si un montant compté est fourni, l'écart correspondant
 *     (surplus si positif, manque si négatif).
 */
@Data
public class SessionCaisseResumeDTO {

    private Long id_session_caisse;

    private Double montantOuverture;

    /**
     * Somme des ventes (montant TTC) réalisées sur ce point de
     * vente entre l'ouverture de la session et la date de
     * fermeture (ou l'instant présent si la session est encore
     * ouverte).
     */
    private Double totalVentes;

    /**
     * Montant théorique attendu en caisse :
     * montantOuverture + totalVentes.
     */
    private Double montantTheorique;

    /**
     * Montant réellement compté en caisse.
     * Null tant que la session n'est pas fermée et qu'aucun
     * montant n'a été saisi.
     */
    private Double montantCompte;

    /**
     * Écart = montantCompte - montantTheorique.
     * Positif = surplus, négatif = manque, null si montantCompte
     * n'est pas encore connu.
     */
    private Double ecart;

    private Date dateOuverture;

    /**
     * Date de fermeture réelle de la session, ou null si la
     * session est encore ouverte (dans ce cas, les ventes sont
     * comptabilisées jusqu'à l'instant présent).
     */
    private Date dateFermeture;

    private boolean sessionOuverte;
}