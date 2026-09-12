package org.sid.restaurationbackend.dtos;

import lombok.Data;

@Data
public class ModuleDTO {

    private Long id_module;
    private String nomModule;
    private String icone;
    private Integer ordre_affichage;
    private Boolean disponiblePdv;
    private Boolean disponible_backoffice;
}
