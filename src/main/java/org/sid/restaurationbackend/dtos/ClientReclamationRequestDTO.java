package org.sid.restaurationbackend.dtos;

import lombok.Data;

/**
 * Corps de POST /api/client/reclamations.
 *
 * Volontairement minimal, même logique que ClientCommandeRequestDTO :
 * le client ne peut fournir qu'un identifiant de commande (jamais un
 * sous-objet CommandeDTO complet qui pourrait être falsifié). La
 * commande référencée est toujours rechargée et revérifiée côté
 * serveur (voir ClientReclamationServiceImpl).
 */
@Data
public class ClientReclamationRequestDTO {

    private String sujet;
    private String description;

    /**
     * Optionnel : une réclamation peut ne pas être liée à une commande
     * précise (voir Reclamation.commande, nullable).
     */
    private Long commandeId;
}
