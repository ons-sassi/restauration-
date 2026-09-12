package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Fonctionnalite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FonctionnaliteRepository extends JpaRepository<Fonctionnalite, Long> {
    List<Fonctionnalite> findByFonctionnaliteParent(Fonctionnalite fonctionnalite);
    List<Fonctionnalite> findByModule(org.sid.restaurationbackend.entities.Module module);

    List<Fonctionnalite> findByNomFonctionnaliteContainingIgnoreCase(String trim);

    List<Fonctionnalite> findByFonctionnaliteParentIsNull();

    List<Fonctionnalite>findByDisponibleBackoffice(Boolean disponibleBackoffice);

    List<Fonctionnalite> findByDisponiblePdv(Boolean disponiblePdv);

    List<Fonctionnalite> findByCodeFonctionnaliteContainingIgnoreCase(String code);
    Optional<Fonctionnalite> findByCodeFonctionnalite(String code);
}
