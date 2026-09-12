package org.sid.restaurationbackend.dtos;

import lombok.Data;

@Data
public class FonctionnaliteDTO {

    private Long id_fonctionnalite;
    private String nomFonctionnalite;
    private String codeFonctionnalite;
    private Boolean disponiblePdv;
    private Boolean disponibleBackoffice;
    private Integer ordre_affichage;

    // Relation ManyToOne
    private ModuleDTO module;

    // Relation ManyToOne
    private FonctionnaliteDTO fonctionnaliteParent;
}
