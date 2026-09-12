package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    /*
     * ================================
     * STATISTIQUES PRINCIPALES
     * ================================
     */

    private Double ventesDuJour;

    private Long commandesEnCours;

    private Long tablesOccupees;

    private Long nombreTables;

    private Long nombreClients;


    /*
     * ================================
     * COMPARAISON AVEC HIER
     * ================================
     */

    private Double ventesHier;

    private Long commandesHier;

    private Long clientsHier;


    /*
     * ================================
     * GRAPHIQUE DES VENTES
     * ================================
     */

    private List<VenteGraphDTO> ventes;


    /*
     * ================================
     * DERNIÈRES COMMANDES
     * ================================
     */

    private List<DashboardCommandeDTO> dernieresCommandes;
}