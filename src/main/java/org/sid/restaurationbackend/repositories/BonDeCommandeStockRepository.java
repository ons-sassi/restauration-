package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.BonDeCommandeStock;
import org.sid.restaurationbackend.entities.Fournisseur;
import org.sid.restaurationbackend.enums.StatutBonCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface BonDeCommandeStockRepository extends JpaRepository<BonDeCommandeStock, Long> {


    Collection<BonDeCommandeStock> findByFournisseur(Fournisseur fournisseur);

    Collection<BonDeCommandeStock> findByStatut(StatutBonCommande statut);

    List<BonDeCommandeStock> findByDateCommandeBetween(LocalDate dateDebut, LocalDate dateFin);

    @Query("""
SELECT b FROM BonDeCommandeStock b
WHERE (:fournisseur IS NULL OR b.fournisseur = :fournisseur)
AND (:statut IS NULL OR b.statut = :statut)
AND (:dateDebut IS NULL OR :dateFin IS NULL
     OR b.dateCommande BETWEEN :dateDebut AND :dateFin)
""")
    List<BonDeCommandeStock> search(
            @Param("fournisseur") Fournisseur fournisseur,
            @Param("statut") StatutBonCommande statut,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);
}
