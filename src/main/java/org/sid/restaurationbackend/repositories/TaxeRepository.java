package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Taxe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface TaxeRepository extends JpaRepository<Taxe, Long> {

    Collection<Taxe> findByStatutIgnoreCase(String statut);
    @Query
            (""" 
SELECT t FROM Taxe t WHERE (:nom IS NULL OR LOWER(t.nomTaxe)
                    LIKE LOWER(CONCAT('%', :nom, '%'))) AND (:applicableA IS NULL OR LOWER(t.applicableA)
                    LIKE LOWER(CONCAT('%', :applicableA, '%'))) AND (:statut IS NULL OR LOWER(t.statut) = LOWER(:statut))
                    AND (:tauxMin IS NULL OR t.taux >= :tauxMin) AND (:tauxMax IS NULL OR t.taux <= :tauxMax)
                     ORDER BY t.dateCreation DESC""")
    List<Taxe> search(@Param("nom") String nom,
                      @Param("applicableA") String applicableA,
                      @Param("statut") String statut,
                      @Param("tauxMin") Double tauxMin,
                      @Param("tauxMax") Double tauxMax );
}
