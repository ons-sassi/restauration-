package org.sid.restaurationbackend.dtos;


import lombok.Data;
import org.sid.restaurationbackend.enums.InterfaceType;

import java.util.Date;

@Data
public class RoleFonctionnaliteDTO {

    private Long id_role_fonctionnalite;


    private RoleDTO role;


    private FonctionnaliteDTO fonctionnalite;


    private InterfaceType interfaceType;

    private Boolean autorise;

    private Date date_attribution;


    private EmployeeDTO attribuePar;
}
