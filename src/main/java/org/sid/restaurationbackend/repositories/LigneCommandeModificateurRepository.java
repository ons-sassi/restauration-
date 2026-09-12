package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.LigneCommande;
import org.sid.restaurationbackend.entities.LigneCommandeModificateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneCommandeModificateurRepository
        extends JpaRepository<LigneCommandeModificateur, Long> {

    List<LigneCommandeModificateur> findByLigneCommande(LigneCommande ligneCommande);
}
