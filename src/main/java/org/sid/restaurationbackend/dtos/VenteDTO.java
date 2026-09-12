package org.sid.restaurationbackend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class VenteDTO {

    private Long id_vente;

    @JsonProperty("date_vente")
    private Date dateVente;

    @JsonProperty("montant_ht")
    private Double montantHt;

    @JsonProperty("montant_ttc")
    private Double montantTtc;

    /**
     * Réduction appliquée à cette vente.
     */
    private ReductionDTO reduction;

    /**
     * Montant réellement déduit de la vente.
     */
    @JsonProperty("montant_reduction")
    private Double montantReduction;

    /**
     * Reçu associé à la vente.
     *
     * Le RecuDTO ne doit pas contenir à nouveau VenteDTO
     * afin d'éviter une récursion JSON.
     */
    private RecuDTO recu;

    private CommandeDTO commande;

    private PointDeVenteDTO pointDeVente;

    private EmployeeDTO employee;

    private ModePaiementDTO modePaiement;
}