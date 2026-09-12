package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.enums.InterfaceType;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class RoleFonctionnaliteServiceImpl
        implements RoleFonctionnaliteService {

    private RestaurantMapper dtotMapper;

    private RoleFonctionnaliteRepository roleFonctionnaliteRepository;
    private RoleRepository roleRepository;
    private FonctionnaliteRepository fonctionnaliteRepository;
    private EmployeeRepository employeeRepository;



    @Override
    public RoleFonctionnaliteDTO saveRoleFonctionnalite(
            RoleFonctionnaliteDTO dto) {

        RoleFonctionnalite roleFonctionnalite =
                dtotMapper.fromRoleFonctionnaliteDTO(dto);

        RoleFonctionnalite saved =
                roleFonctionnaliteRepository.save(roleFonctionnalite);

        return dtotMapper.fromRoleFonctionnalite(saved);
    }


    @Override
    public RoleFonctionnaliteDTO updateRoleFonctionnalite(
            Long id,
            RoleFonctionnaliteDTO dto)
            throws RoleFonctionnaliteNotFoundException {

        RoleFonctionnalite roleFonctionnalite =
                roleFonctionnaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new RoleFonctionnaliteNotFoundException(
                                        "RoleFonctionnalite not found"));

        dtotMapper.updateRoleFonctionnaliteFromDto(
                dto,
                roleFonctionnalite);

        RoleFonctionnalite updated =
                roleFonctionnaliteRepository.save(roleFonctionnalite);

        return dtotMapper.fromRoleFonctionnalite(updated);
    }


    @Override
    public void deleteRoleFonctionnalite(Long id)
            throws RoleFonctionnaliteNotFoundException {

        RoleFonctionnalite roleFonctionnalite =
                roleFonctionnaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new RoleFonctionnaliteNotFoundException(
                                        "RoleFonctionnalite not found"));

        roleFonctionnaliteRepository.delete(roleFonctionnalite);
    }


    @Override
    public RoleFonctionnaliteDTO getRoleFonctionnalite(Long id)
            throws RoleFonctionnaliteNotFoundException {

        RoleFonctionnalite roleFonctionnalite =
                roleFonctionnaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new RoleFonctionnaliteNotFoundException(
                                        "RoleFonctionnalite not found"));

        return dtotMapper.fromRoleFonctionnalite(roleFonctionnalite);
    }


    @Override
    public List<RoleFonctionnaliteDTO> getAllRoleFonctionnalites() {

        return roleFonctionnaliteRepository.findAll()
                .stream()
                .map(dtotMapper::fromRoleFonctionnalite)
                .toList();
    }



    @Override
    public List<RoleFonctionnaliteDTO> getByRole(RoleDTO roleDTO) {

        Role role = dtotMapper.fromRoleDTO(roleDTO);

        return roleFonctionnaliteRepository
                .findByRole(role)
                .stream()
                .map(dtotMapper::fromRoleFonctionnalite)
                .toList();
    }



    @Override
    public List<RoleFonctionnaliteDTO> getByFonctionnalite(
            FonctionnaliteDTO fonctionnaliteDTO) {

        Fonctionnalite fonctionnalite =
                dtotMapper.fromFonctionnaliteDTO(fonctionnaliteDTO);

        return roleFonctionnaliteRepository
                .findByFonctionnalite(fonctionnalite)
                .stream()
                .map(dtotMapper::fromRoleFonctionnalite)
                .toList();
    }



    @Override
    public List<RoleFonctionnaliteDTO> getByInterfaceType(
            InterfaceType interfaceType) {

        return roleFonctionnaliteRepository
                .findByInterfaceType(interfaceType)
                .stream()
                .map(dtotMapper::fromRoleFonctionnalite)
                .toList();
    }

    public List<RoleFonctionnaliteDTO> getByRoleAndInterface(
            RoleDTO roleDTO,
            InterfaceType interfaceType) {

        Role role = dtotMapper.fromRoleDTO(roleDTO);

        return roleFonctionnaliteRepository
                .findByRoleAndInterfaceType(
                        role,
                        interfaceType)
                .stream()
                .map(dtotMapper::fromRoleFonctionnalite)
                .toList();
    }


    @Override
    public List<RoleFonctionnaliteDTO>
    getByFonctionnaliteAndInterface(
            FonctionnaliteDTO fonctionnaliteDTO,
                InterfaceType interfaceType) {

        Fonctionnalite fonctionnalite =
                dtotMapper.fromFonctionnaliteDTO(fonctionnaliteDTO);

        return roleFonctionnaliteRepository
                .findByFonctionnaliteAndInterfaceType(
                        fonctionnalite,
                        interfaceType)
                .stream()
                .map(dtotMapper::fromRoleFonctionnalite)
                .toList();
    }




    @Override
    public List<RoleFonctionnaliteDTO>
    getAutoriseesByRoleAndInterface(
            RoleDTO roleDTO,
            InterfaceType interfaceType) {

        Role role = dtotMapper.fromRoleDTO(roleDTO);

        return roleFonctionnaliteRepository
                .findByRoleAndInterfaceTypeAndAutoriseTrue(
                        role,
                        interfaceType)
                .stream()
                .map(dtotMapper::fromRoleFonctionnalite)
                .toList();
    }



    @Override
    public RoleFonctionnaliteDTO assignerFonctionnalite(
            Long roleId,
            Long fonctionnaliteId,
            InterfaceType interfaceType,
            Boolean autorise,
            Long employeeId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        Fonctionnalite fonctionnalite =
                fonctionnaliteRepository.findById(fonctionnaliteId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Fonctionnalite not found"));

        Employee employee = null;

        if (employeeId != null) {
            employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Employee not found"));
        }

        // Vérifier si l'association existe déjà
        RoleFonctionnalite existante =
                roleFonctionnaliteRepository
                        .findByRoleAndFonctionnaliteAndInterfaceType(
                                role,
                                fonctionnalite,
                                interfaceType)
                        .orElse(null);

        if (existante != null) {

            existante.setAutorise(autorise);
            existante.setAttribuePar(employee);
            existante.setDate_attribution(new Date());

            return dtotMapper.fromRoleFonctionnalite(
                    roleFonctionnaliteRepository.save(existante));
        }

        RoleFonctionnalite roleFonctionnalite =
                new RoleFonctionnalite();

        roleFonctionnalite.setRole(role);
        roleFonctionnalite.setFonctionnalite(fonctionnalite);
        roleFonctionnalite.setInterfaceType(interfaceType);
        roleFonctionnalite.setAutorise(autorise);
        roleFonctionnalite.setAttribuePar(employee);
        roleFonctionnalite.setDate_attribution(new Date());

        RoleFonctionnalite saved =
                roleFonctionnaliteRepository.save(
                        roleFonctionnalite);

        return dtotMapper.fromRoleFonctionnalite(saved);
    }



    @Override
    public void modifierAutorisation(
            Long id,
            Boolean autorise)
            throws RoleFonctionnaliteNotFoundException {

        RoleFonctionnalite roleFonctionnalite =
                roleFonctionnaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new RoleFonctionnaliteNotFoundException(
                                        "RoleFonctionnalite not found"));

        roleFonctionnalite.setAutorise(autorise);

        roleFonctionnaliteRepository.save(
                roleFonctionnalite);
    }
}