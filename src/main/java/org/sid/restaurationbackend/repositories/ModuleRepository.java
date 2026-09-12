package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Collection;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {


    Collection<Module> findByNomModuleContainingIgnoreCase(String trim);

    Optional<Module> findByNomModuleIgnoreCase(String nom);
}
