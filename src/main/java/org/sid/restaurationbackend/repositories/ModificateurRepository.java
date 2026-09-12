package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Modificateur;
import org.sid.restaurationbackend.entities.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModificateurRepository extends JpaRepository<Modificateur, Long> {
    List<Modificateur> findByProduitsContains(Produit produit);
}
