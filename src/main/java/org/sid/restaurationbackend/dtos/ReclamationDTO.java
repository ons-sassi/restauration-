package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.StatutReclamation;

import java.util.Date;

@Data
public class ReclamationDTO {

    private Long id_reclamation;
    private String sujet;
    private String description;
    private Date date_creation;
    private StatutReclamation statut;
    private String reponse_employee;
    private Date date_reponse;

    // Relation ManyToOne
    private ClientAuthentifieDTO client;

    // Relation ManyToOne
    private CommandeDTO commande;
}
