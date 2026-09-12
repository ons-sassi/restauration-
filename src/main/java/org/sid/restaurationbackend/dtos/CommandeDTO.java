package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.ModeCommande;
import org.sid.restaurationbackend.enums.StatutCommande;

import java.util.Date;

@Data
public class CommandeDTO {

    private Long id_commande;
    private Date dateCommande;
    private StatutCommande statut;
    private ModeCommande modeCommande;
    private Double montant_total;

    /**
     * Adresse de livraison, renseignée uniquement quand modeCommande =
     * LIVRAISON (voir ClientCommandeServiceImpl.creerCommandeDepuisPanier).
     * Nulle pour tous les autres modes.
     *
     * ⚠️ Corrigé ici : ce champ existe sur l'entité Commande depuis le
     * départ, mais n'avait jamais été ajouté au DTO. Résultat : le back
     * office ne pouvait jamais afficher l'adresse d'une commande en
     * livraison (RestaurantMapper.fromCommande utilise BeanUtils.copy
     * Properties, qui copie automatiquement par nom de propriété — donc
     * ce seul ajout suffit à faire remonter la valeur, sans toucher au
     * mapper).
     */
    private String adresseLivraison;

    // Relation ManyToOne
    private ClientAuthentifieDTO client;

    // Relation ManyToOne
    private ClientNonAuthentifieDTO clientNonAuthentifie;

    // Relation ManyToOne
    private TableRestaurantDTO table;

    // Relation ManyToOne
    private EmployeeDTO employee;
}
