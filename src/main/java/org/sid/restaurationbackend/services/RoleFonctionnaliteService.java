package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.enums.InterfaceType;
import org.sid.restaurationbackend.exceptions.RoleFonctionnaliteNotFoundException;

import java.util.List;

public interface RoleFonctionnaliteService {

    RoleFonctionnaliteDTO saveRoleFonctionnalite(
            RoleFonctionnaliteDTO roleFonctionnaliteDTO);

    RoleFonctionnaliteDTO updateRoleFonctionnalite(
            Long id,
            RoleFonctionnaliteDTO roleFonctionnaliteDTO)
            throws RoleFonctionnaliteNotFoundException;

    void deleteRoleFonctionnalite(Long id)
            throws RoleFonctionnaliteNotFoundException;

    RoleFonctionnaliteDTO getRoleFonctionnalite(Long id)
            throws RoleFonctionnaliteNotFoundException;

    List<RoleFonctionnaliteDTO> getAllRoleFonctionnalites();

    List<RoleFonctionnaliteDTO> getByRole(RoleDTO roleDTO);

    List<RoleFonctionnaliteDTO> getByFonctionnalite(
            FonctionnaliteDTO fonctionnaliteDTO);

    List<RoleFonctionnaliteDTO> getByInterfaceType(
            InterfaceType interfaceType);

    List<RoleFonctionnaliteDTO> getByRoleAndInterface(
            RoleDTO roleDTO,
            InterfaceType interfaceType);

    List<RoleFonctionnaliteDTO> getByFonctionnaliteAndInterface(
            FonctionnaliteDTO fonctionnaliteDTO,
            InterfaceType interfaceType);

    List<RoleFonctionnaliteDTO> getAutoriseesByRoleAndInterface(
            RoleDTO roleDTO,
            InterfaceType interfaceType);

    RoleFonctionnaliteDTO assignerFonctionnalite(
            Long roleId,
            Long fonctionnaliteId,
            InterfaceType interfaceType,
            Boolean autorise,
            Long employeeId);

    void modifierAutorisation(
            Long id,
            Boolean autorise)
            throws RoleFonctionnaliteNotFoundException;
}