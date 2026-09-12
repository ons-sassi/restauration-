package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class RecuDTO {

    private Long id_recu;
    private String numeroRecu;
    private Date date_emission;
    private Boolean logo_affiche;
    private String entete_personnalise;
    private String pied_de_page_personnalise;
    private String commentaire_client;

    // Relation OneToOne
    private VenteDTO vente;

    // Relation ManyToOne
    private ModeleRecuDTO modele;
}
