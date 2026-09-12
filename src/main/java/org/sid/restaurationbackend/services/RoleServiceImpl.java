package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Fonctionnalite;
import org.sid.restaurationbackend.entities.Role;
import org.sid.restaurationbackend.entities.RoleFonctionnalite;
import org.sid.restaurationbackend.enums.InterfaceType;
import org.sid.restaurationbackend.exceptions.RoleNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.EmployeeRepository;
import org.sid.restaurationbackend.repositories.FonctionnaliteRepository;
import org.sid.restaurationbackend.repositories.RoleFonctionnaliteRepository;
import org.sid.restaurationbackend.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private RestaurantMapper dtotMapper;

    private RoleRepository roleRepository;

    /*
     * ============================================================
     * NOUVEAUX REPOSITORIES
     * ============================================================
     */

    private FonctionnaliteRepository fonctionnaliteRepository;

    private RoleFonctionnaliteRepository roleFonctionnaliteRepository;

    private EmployeeRepository employeeRepository;


    // ============================================================
    // ANCIENNES METHODES ROLE
    // ============================================================

    @Override
    public RoleDTO saveRole(RoleDTO roleDTO) {

        Role role = dtotMapper.fromRoleDTO(roleDTO);

        Role savedRole = roleRepository.save(role);

        return dtotMapper.fromRole(savedRole);
    }


    @Override
    public RoleDTO updateRole(
            Long id,
            RoleDTO roleDTO
    ) throws RoleNotFoundException {

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "Role not found"
                        )
                );

        dtotMapper.updateRoleFromDto(
                roleDTO,
                role
        );

        Role updatedRole = roleRepository.save(role);

        return dtotMapper.fromRole(updatedRole);
    }


    @Override
    public void deleteRole(
            Long id
    ) throws RoleNotFoundException {

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "Role not found"
                        )
                );

        /*
         * On supprime les permissions du rôle
         * avant de supprimer le rôle.
         */
        roleFonctionnaliteRepository.deleteByRole(role);

        roleRepository.deleteById(id);
    }


    @Override
    public RoleDTO getRole(
            Long id
    ) throws RoleNotFoundException {

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "Role not found"
                        )
                );

        return dtotMapper.fromRole(role);
    }


    @Override
    public List<RoleDTO> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(dtotMapper::fromRole)
                .toList();
    }


    @Override
    public List<RoleDTO> getRolesByEmployeeAttribuePar(
            EmployeeDTO employee
    ) {

        return roleRepository
                .findByAttribuePar(
                        dtotMapper.fromEmployeeDTO(employee)
                )
                .stream()
                .map(dtotMapper::fromRole)
                .toList();
    }


    // ============================================================
    // NOUVELLE METHODE
    // CREER UN ROLE + SES PERMISSIONS
    // ============================================================

    @Override
    public RoleDTO createRoleWithPermissions(
            RoleConfigurationDTO configuration
    ) {

        if (configuration == null) {
            throw new IllegalArgumentException(
                    "La configuration du rôle est obligatoire."
            );
        }


        /*
         * --------------------------------------------------------
         * 1. Création du rôle
         * --------------------------------------------------------
         */

        Role role = new Role();

        role.setNom_role(
                configuration.getNom_role()
        );

        role.setDescription(
                configuration.getDescription()
        );

        role.setAcces_pdv(
                Boolean.TRUE.equals(
                        configuration.getAcces_pdv()
                )
        );

        role.setAcces_backoffice(
                Boolean.TRUE.equals(
                        configuration.getAcces_backoffice()
                )
        );

        role.setDate_creation(
                new Date()
        );


        /*
         * --------------------------------------------------------
         * 2. Sauvegarde du rôle
         * --------------------------------------------------------
         */

        Role savedRole =
                roleRepository.save(role);


        /*
         * --------------------------------------------------------
         * 3. Sauvegarde des permissions
         * --------------------------------------------------------
         */

        savePermissions(
                savedRole,
                configuration.getPermissions()
        );


        return dtotMapper.fromRole(
                savedRole
        );
    }


    // ============================================================
    // NOUVELLE METHODE
    // MODIFIER UN ROLE + SES PERMISSIONS
    // ============================================================

    @Override
    public RoleDTO updateRoleWithPermissions(
            Long id,
            RoleConfigurationDTO configuration
    ) throws RoleNotFoundException {

        if (configuration == null) {
            throw new IllegalArgumentException(
                    "La configuration du rôle est obligatoire."
            );
        }


        /*
         * --------------------------------------------------------
         * 1. Recherche du rôle
         * --------------------------------------------------------
         */

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "Role not found"
                        )
                );


        /*
         * --------------------------------------------------------
         * 2. Modification des informations du rôle
         * --------------------------------------------------------
         */

        role.setNom_role(
                configuration.getNom_role()
        );

        role.setDescription(
                configuration.getDescription()
        );

        role.setAcces_pdv(
                Boolean.TRUE.equals(
                        configuration.getAcces_pdv()
                )
        );

        role.setAcces_backoffice(
                Boolean.TRUE.equals(
                        configuration.getAcces_backoffice()
                )
        );


        roleRepository.save(role);


        /*
         * --------------------------------------------------------
         * 3. Suppression des anciennes permissions
         * --------------------------------------------------------
         */

        roleFonctionnaliteRepository.deleteByRole(
                role
        );


        /*
         * --------------------------------------------------------
         * 4. Ajout des nouvelles permissions
         * --------------------------------------------------------
         */

        savePermissions(
                role,
                configuration.getPermissions()
        );


        return dtotMapper.fromRole(
                role
        );
    }


    // ============================================================
    // SAUVEGARDE DES PERMISSIONS
    // ============================================================

    private void savePermissions(
            Role role,
            List<RoleConfigurationDTO.PermissionDTO> permissions
    ) {

        if (permissions == null ||
                permissions.isEmpty()) {

            return;
        }


        /*
         * Parcours de toutes les fonctionnalités
         * cochées dans Angular.
         */

        for (
                RoleConfigurationDTO.PermissionDTO permission
                : permissions
        ) {

            if (permission == null) {
                continue;
            }


            if (permission.getFonctionnaliteId() == null) {
                continue;
            }


            if (permission.getInterfaceType() == null) {
                continue;
            }


            /*
             * On ne sauvegarde que les cases cochées.
             */

            if (!Boolean.TRUE.equals(
                    permission.getAutorise()
            )) {

                continue;
            }


            /*
             * ----------------------------------------------------
             * Recherche de la fonctionnalité
             * ----------------------------------------------------
             */

            Fonctionnalite fonctionnalite =
                    fonctionnaliteRepository
                            .findById(
                                    permission
                                            .getFonctionnaliteId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Fonctionnalité introuvable : "
                                                    +
                                                    permission
                                                            .getFonctionnaliteId()
                                    )
                            );


            InterfaceType interfaceType =
                    permission.getInterfaceType();


            /*
             * ----------------------------------------------------
             * Vérification de disponibilité
             * ----------------------------------------------------
             */

            validateAvailability(
                    fonctionnalite,
                    interfaceType
            );


            /*
             * ----------------------------------------------------
             * Création de la permission demandée
             * ----------------------------------------------------
             */

            createRoleFonctionnalite(
                    role,
                    fonctionnalite,
                    interfaceType
            );


            /*
             * ====================================================
             * REGLE DEMANDEE :
             *
             * Si une fonctionnalité est cochée en PDV,
             * elle doit automatiquement être cochée en
             * BACK OFFICE.
             * ====================================================
             */

            if (interfaceType == InterfaceType.PDV) {

                if (Boolean.TRUE.equals(
                        fonctionnalite
                                .getDisponibleBackoffice()
                )) {

                    createRoleFonctionnalite(
                            role,
                            fonctionnalite,
                            InterfaceType.BACKOFFICE
                    );
                }
            }


            /*
             * ====================================================
             * GESTION DU PARENT
             *
             * Exemple :
             *
             * MENU_PRODUITS
             *       |
             *       +--- MENU_PRODUITS_VOIR
             *       +--- MENU_PRODUITS_AJOUTER
             *       +--- MENU_PRODUITS_MODIFIER
             *       +--- MENU_PRODUITS_SUPPRIMER
             *
             * Si une sous-fonctionnalité est cochée,
             * le parent est automatiquement activé.
             * ====================================================
             */

            Fonctionnalite parent =
                    fonctionnalite
                            .getFonctionnaliteParent();


            if (parent != null) {

                /*
                 * Activation du parent
                 * dans la même interface.
                 */

                createRoleFonctionnalite(
                        role,
                        parent,
                        interfaceType
                );


                /*
                 * Si la sous-fonctionnalité est PDV,
                 * le parent est également activé en BO.
                 */

                if (interfaceType == InterfaceType.PDV) {

                    if (Boolean.TRUE.equals(
                            parent
                                    .getDisponibleBackoffice()
                    )) {

                        createRoleFonctionnalite(
                                role,
                                parent,
                                InterfaceType.BACKOFFICE
                        );
                    }
                }
            }
        }
    }


    // ============================================================
    // CREER ROLE-FONCTIONNALITE
    // ============================================================

    private void createRoleFonctionnalite(
            Role role,
            Fonctionnalite fonctionnalite,
            InterfaceType interfaceType
    ) {

        /*
         * Recherche d'une association existante.
         *
         * Cela évite les doublons.
         */

        Optional<RoleFonctionnalite> existing =
                roleFonctionnaliteRepository
                        .findByRoleAndFonctionnaliteAndInterfaceType(
                                role,
                                fonctionnalite,
                                interfaceType
                        );


        /*
         * --------------------------------------------------------
         * Association déjà existante
         * --------------------------------------------------------
         */

        if (existing.isPresent()) {

            RoleFonctionnalite roleFonctionnalite =
                    existing.get();

            roleFonctionnalite.setAutorise(
                    true
            );

            roleFonctionnalite.setDate_attribution(
                    new Date()
            );

            roleFonctionnaliteRepository.save(
                    roleFonctionnalite
            );

            return;
        }


        /*
         * --------------------------------------------------------
         * Nouvelle association
         * --------------------------------------------------------
         */

        RoleFonctionnalite roleFonctionnalite =
                new RoleFonctionnalite();

        roleFonctionnalite.setRole(
                role
        );

        roleFonctionnalite.setFonctionnalite(
                fonctionnalite
        );

        roleFonctionnalite.setInterfaceType(
                interfaceType
        );

        roleFonctionnalite.setAutorise(
                true
        );

        roleFonctionnalite.setDate_attribution(
                new Date()
        );


        roleFonctionnaliteRepository.save(
                roleFonctionnalite
        );
    }


    // ============================================================
    // VERIFICATION DISPONIBILITE
    // ============================================================

    private void validateAvailability(
            Fonctionnalite fonctionnalite,
            InterfaceType interfaceType
    ) {

        /*
         * --------------------------------------------------------
         * PDV
         * --------------------------------------------------------
         */

        if (interfaceType == InterfaceType.PDV) {

            if (!Boolean.TRUE.equals(
                    fonctionnalite
                            .getDisponiblePdv()
            )) {

                throw new IllegalArgumentException(
                        "La fonctionnalité '"
                                +
                                fonctionnalite
                                        .getNomFonctionnalite()
                                +
                                "' n'est pas disponible en PDV."
                );
            }
        }


        /*
         * --------------------------------------------------------
         * BACK OFFICE
         * --------------------------------------------------------
         */

        if (interfaceType == InterfaceType.BACKOFFICE) {

            if (!Boolean.TRUE.equals(
                    fonctionnalite
                            .getDisponibleBackoffice()
            )) {

                throw new IllegalArgumentException(
                        "La fonctionnalité '"
                                +
                                fonctionnalite
                                        .getNomFonctionnalite()
                                +
                                "' n'est pas disponible en Back Office."
                );
            }
        }
    }
}