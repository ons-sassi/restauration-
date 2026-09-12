package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    @Query("""
        SELECT f
        FROM Fournisseur f
        WHERE (:nom IS NULL OR LOWER(f.nom) LIKE LOWER(CONCAT('%', :nom, '%')))
          AND (:adresse IS NULL OR LOWER(f.adresse) LIKE LOWER(CONCAT('%', :adresse, '%')))
          AND (:email IS NULL OR LOWER(f.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:num IS NULL OR LOWER(f.numTel) LIKE LOWER(CONCAT('%', :num, '%')))
    """)
    List<Fournisseur> search(
            @Param("nom") String nom,
            @Param("adresse") String adresse,
            @Param("email") String email,
            @Param("num") String num);

}
