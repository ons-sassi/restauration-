package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.RoleFonctionnalite;
import org.sid.restaurationbackend.exceptions.RoleNotFoundException;


import java.util.List;

public interface RoleService {
    RoleDTO saveRole(RoleDTO roleDTO);
    RoleDTO updateRole(Long id, RoleDTO roleDTO) throws RoleNotFoundException;
    void deleteRole(Long id) throws RoleNotFoundException;
    RoleDTO getRole(Long id) throws RoleNotFoundException;
    List<RoleDTO> getAllRoles();
    List<RoleDTO> getRolesByEmployeeAttribuePar(EmployeeDTO employee);
    RoleDTO createRoleWithPermissions(
            RoleConfigurationDTO configuration
    );

    RoleDTO updateRoleWithPermissions(
            Long id,
            RoleConfigurationDTO configuration
    ) throws RoleNotFoundException;



}
