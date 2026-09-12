package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutReclamation;

import java.util.Date;

/**
 * Vue "client" d'une réclamation — séparée de ReclamationDTO (partagé
 * avec le back-office) pour ne jamais exposer plus que ce dont
 * l'espace client a besoin : pas de ClientAuthentifieDTO imbriqué (le
 * client connaît déjà sa propre identité), et seulement l'id de la
 * commande liée plutôt qu'un CommandeDTO complet.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientReclamationDTO {

    private Long id_reclamation;
    private String sujet;
    private String description;
    private Date date_creation;
    private StatutReclamation statut;
    private String reponse_employee;
    private Date date_reponse;

    /**
     * Null si la réclamation n'est liée à aucune commande.
     */
    private Long commandeId;
}
