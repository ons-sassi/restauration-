package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.InterfaceType;

import java.util.List;

@Data
public class RoleConfigurationDTO {

    private String nom_role;

    private String description;

    private Boolean acces_pdv;

    private Boolean acces_backoffice;

    private List<PermissionDTO> permissions;


    @Data
    public static class PermissionDTO {

        private Long fonctionnaliteId;

        private InterfaceType interfaceType;

        private Boolean autorise;
    }
}