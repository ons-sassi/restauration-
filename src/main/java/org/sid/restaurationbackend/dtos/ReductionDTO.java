package org.sid.restaurationbackend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReductionDTO {

    private Long id_reduction;

    // Lot Stock/Fidélité : privé par restaurant (voir entité Reduction)
    private RestaurantDTO restaurant;

    @JsonProperty("nom_reduction")
    private String nomReduction;

    private String type;

    private Double valeur;

    @JsonProperty("date_debut")
    private Date dateDebut;

    @JsonProperty("date_fin")
    private Date dateFin;

    @JsonProperty("conditions_application")
    private String conditionsApplication;

    /**
     * "TOUS_PRODUITS" ou "PRODUITS_SPECIFIQUES".
     */
    @JsonProperty("application_produits")
    private String applicationProduits;

    /**
     * Ids des produits concernés, à envoyer uniquement quand
     * application_produits = "PRODUITS_SPECIFIQUES".
     */
    @JsonProperty("produits_ids")
    private List<Long> produitsIds;

    /**
     * Résumé (id + nom) des produits concernés, renvoyé en lecture
     * pour affichage côté front sans devoir recharger chaque produit.
     */
    private List<ReductionProduitDTO> produits;

    /**
     * null ou 0 => applicable à partir de n'importe quel montant.
     */
    @JsonProperty("montant_minimum")
    private Double montantMinimum;

    /**
     * true => la réduction est appliquée automatiquement quand ses
     * conditions sont réunies, sans sélection manuelle.
     */
    private Boolean automatique;

    /**
     * true => la réduction est active (proposable automatiquement et
     * sélectionnable manuellement en caisse).
     * false => la réduction est désactivée manuellement, elle n'est
     * jamais appliquée même si ses dates de validité sont respectées.
     */
    private Boolean active;

    /**
     * Nombre maximum de fois où la réduction peut être appliquée.
     * null => illimité.
     */
    @JsonProperty("nombre_applications_autorise")
    private Integer nombreApplicationsAutorise;

    /**
     * Nombre de fois où la réduction a déjà été appliquée. Renvoyé en
     * lecture uniquement, calculé par le backend.
     */
    @JsonProperty("nombre_applications_effectuees")
    private Integer nombreApplicationsEffectuees;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReductionProduitDTO {
        private Long id;
        private String nom;
    }
}