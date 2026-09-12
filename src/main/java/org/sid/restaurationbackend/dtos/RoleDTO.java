package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class RoleDTO {

    private Long id_role;
    private String nom_role;
    private String description;
    private Boolean acces_pdv;
    private Boolean acces_backoffice;
    private Date date_creation;

    // Relation ManyToOne
    private EmployeeDTO attribuePar;
}
