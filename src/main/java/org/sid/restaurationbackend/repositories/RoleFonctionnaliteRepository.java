package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Fonctionnalite;
import org.sid.restaurationbackend.entities.Role;
import org.sid.restaurationbackend.entities.RoleFonctionnalite;
import org.sid.restaurationbackend.enums.InterfaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface RoleFonctionnaliteRepository
        extends JpaRepository<RoleFonctionnalite, Long> {

    Collection<RoleFonctionnalite> findByRole(Role role);

    Collection<RoleFonctionnalite> findByFonctionnalite(
            Fonctionnalite fonctionnalite
    );

    Collection<RoleFonctionnalite> findByInterfaceType(
            InterfaceType interfaceType
    );

    Collection<RoleFonctionnalite> findByRoleAndInterfaceType(
            Role role,
            InterfaceType interfaceType
    );

    Collection<RoleFonctionnalite>
    findByFonctionnaliteAndInterfaceType(
            Fonctionnalite fonctionnalite,
            InterfaceType interfaceType
    );

    Collection<RoleFonctionnalite>
    findByRoleAndInterfaceTypeAndAutoriseTrue(
            Role role,
            InterfaceType interfaceType
    );

    Optional<RoleFonctionnalite>
    findByRoleAndFonctionnaliteAndInterfaceType(
            Role role,
            Fonctionnalite fonctionnalite,
            InterfaceType interfaceType
    );

    void deleteByRole(Role role);
}