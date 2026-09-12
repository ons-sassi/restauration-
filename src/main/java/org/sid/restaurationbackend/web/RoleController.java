package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.EmployeeDTO;
import org.sid.restaurationbackend.dtos.RoleConfigurationDTO;
import org.sid.restaurationbackend.dtos.RoleDTO;

import org.sid.restaurationbackend.exceptions.RoleNotFoundException;
import org.sid.restaurationbackend.services.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@AllArgsConstructor
public class RoleController {

    private final RoleService roleService;


    @PostMapping
    public RoleDTO saveRole(@RequestBody RoleDTO roleDTO) {
        return roleService.saveRole(roleDTO);
    }


    @PutMapping("/{id}")
    public RoleDTO updateRole(
            @PathVariable Long id,
            @RequestBody RoleDTO roleDTO
    ) throws RoleNotFoundException {

        return roleService.updateRole(id, roleDTO);
    }


    @DeleteMapping("/{id}")
    public void deleteRole(
            @PathVariable Long id
    ) throws RoleNotFoundException {

        roleService.deleteRole(id);
    }


    @GetMapping("/{id}")
    public RoleDTO getRole(
            @PathVariable Long id
    ) throws RoleNotFoundException {

        return roleService.getRole(id);
    }


    @GetMapping
    public List<RoleDTO> getAllRoles() {
        return roleService.getAllRoles();
    }


    @PostMapping("/by-attribue-par")
    public List<RoleDTO> getRolesByEmployeeAttribuePar(
            @RequestBody EmployeeDTO employee
    ) {

        return roleService.getRolesByEmployeeAttribuePar(employee);
    }

    @PostMapping("/configure")
    public RoleDTO createRoleWithPermissions(
            @RequestBody RoleConfigurationDTO configuration) {

        return roleService.createRoleWithPermissions(
                configuration
        );
    }

    @PutMapping("/{id}/configure")
    public RoleDTO updateRoleWithPermissions(
            @PathVariable Long id,
            @RequestBody RoleConfigurationDTO configuration)
            throws RoleNotFoundException {

        return roleService.updateRoleWithPermissions(
                id,
                configuration
        );
    }




}