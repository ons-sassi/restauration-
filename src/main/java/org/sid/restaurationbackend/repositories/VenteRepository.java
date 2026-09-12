package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {
    Collection<Vente> findByPointDeVente(PointDeVente pointDeVente);

    Collection<Vente> findByCommande(Commande commande);

    Collection<Vente> findByEmployee(Employee employee);

    Collection<Vente> findByModePaiement(ModePaiement modePaiement);


    Optional<Vente> findByRecu(Recu recu);


    @Query("""
    SELECT COALESCE(SUM(v.montantTtc), 0)
    FROM Vente v
    WHERE v.pointDeVente = :pdv
    AND v.dateVente >= :dateDebut
    AND v.dateVente <= :dateFin
""")
    Double calculerTotalVentes(
            @Param("pdv") PointDeVente pdv,
            @Param("dateDebut") Date dateDebut,
            @Param("dateFin") Date dateFin
    );


    @Query("""
    SELECT COALESCE(SUM(v.montantHt), 0)
    FROM Vente v
    WHERE v.pointDeVente = :pdv
    AND v.dateVente >= :dateDebut
    AND v.dateVente <= :dateFin
""")
    Double calculerTotalVentesHt(
            @Param("pdv") PointDeVente pdv,
            @Param("dateDebut") Date dateDebut,
            @Param("dateFin") Date dateFin
    );


    @Query("""
 SELECT v FROM Vente v WHERE (:pointDeVente IS NULL OR v.pointDeVente = :pointDeVente)
 AND (:employee IS NULL OR v.employee = :employee) AND (:modePaiement IS NULL
  OR v.modePaiement = :modePaiement) AND (:dateDebut IS NULL OR v.dateVente >= :dateDebut)
   AND (:dateFin IS NULL OR v.dateVente <= :dateFin) AND
    (:montantHtMin IS NULL OR v.montantHt >= :montantHtMin) AND
     (:montantHtMax IS NULL OR v.montantHt <= :montantHtMax) AND
      (:montantTtcMin IS NULL OR v.montantTtc >= :montantTtcMin) AND
       (:montantTtcMax IS NULL OR v.montantTtc <= :montantTtcMax)
        ORDER BY v.dateVente DESC""")
    List<Vente> search( @Param("pointDeVente") PointDeVente pointDeVente,
                        @Param("employee") Employee employee,
                        @Param("modePaiement") ModePaiement modePaiement,
                        @Param("dateDebut") Date dateDebut, @Param("dateFin") Date dateFin,
                        @Param("montantHtMin") Double montantHtMin,
                        @Param("montantHtMax") Double montantHtMax,
                        @Param("montantTtcMin") Double montantTtcMin,
                        @Param("montantTtcMax") Double montantTtcMax );

    Collection<Vente> findByDateVenteBetween(Date dateDebut, Date dateFin);

    Collection<Vente> findByPointDeVenteAndDateVenteBetween(PointDeVente pointDeVente, Date dateDebut, Date dateFin);
// ============================================================
// RECHERCHE DES VENTES PAR RESTAURANT
// ============================================================

    @Query("""
    SELECT v
    FROM Vente v
    WHERE v.pointDeVente.restaurant.id_restaurant = :restaurantId

      AND (
          :pointDeVenteId IS NULL
          OR v.pointDeVente.id_pdv = :pointDeVenteId
      )

      AND (
          :dateDebut IS NULL
          OR v.dateVente >= :dateDebut
      )

      AND (
          :dateFin IS NULL
          OR v.dateVente <= :dateFin
      )

    ORDER BY v.dateVente DESC
""")
    List<Vente> searchByRestaurant(
            @Param("restaurantId")
            Long restaurantId,

            @Param("pointDeVenteId")
            Long pointDeVenteId,

            @Param("dateDebut")
            Date dateDebut,

            @Param("dateFin")
            Date dateFin
    );

    @Query("""
    SELECT
        p.id_element,
        p.nom,
        c.nom,
        SUM(l.quantite),
        SUM(l.quantite * l.prix_unitaire),
        SUM(l.quantite * COALESCE(p.coutUnitaire, 0))
    FROM Vente v
    JOIN v.commande cmd
    JOIN cmd.lignes l
    JOIN l.produit p
    LEFT JOIN p.categorieParent c
    WHERE v.pointDeVente.restaurant.id_restaurant = :restaurantId
      AND (:pointDeVenteId IS NULL
           OR v.pointDeVente.id_pdv = :pointDeVenteId)
      AND (:dateDebut IS NULL
           OR v.dateVente >= :dateDebut)
      AND (:dateFin IS NULL
           OR v.dateVente <= :dateFin)
    GROUP BY
        p.id_element,
        p.nom,
        c.nom
    ORDER BY
        SUM(l.quantite * l.prix_unitaire) DESC
""")
    List<Object[]> getVentesParArticle(
            @Param("restaurantId") Long restaurantId,
            @Param("pointDeVenteId") Long pointDeVenteId,
            @Param("dateDebut") Date dateDebut,
            @Param("dateFin") Date dateFin
    );


    @Query("""
    SELECT
        c.id_element,
        c.nom,
        SUM(l.quantite),
        SUM(l.quantite * l.prix_unitaire),
        SUM(l.quantite * COALESCE(p.coutUnitaire, 0))
    FROM Vente v
    JOIN v.commande cmd
    JOIN cmd.lignes l
    JOIN l.produit p
    LEFT JOIN p.categorieParent c
    WHERE v.pointDeVente.restaurant.id_restaurant = :restaurantId

      AND (
          :pointDeVenteId IS NULL
          OR v.pointDeVente.id_pdv = :pointDeVenteId
      )

      AND (
          :dateDebut IS NULL
          OR v.dateVente >= :dateDebut
      )

      AND (
          :dateFin IS NULL
          OR v.dateVente <= :dateFin
      )

    GROUP BY
        c.id_element,
        c.nom

    ORDER BY
        SUM(l.quantite * l.prix_unitaire) DESC
""")
    List<Object[]> getVentesParCategorie(
            @Param("restaurantId")
            Long restaurantId,

            @Param("pointDeVenteId")
            Long pointDeVenteId,

            @Param("dateDebut")
            Date dateDebut,

            @Param("dateFin")
            Date dateFin
    );

    // ============================================================
    // VENTES PAR MODIFICATEUR
    // ============================================================

    @Query("""
    SELECT
        m.id_modificateur,
        m.nom_modificateur,
        SUM(lm.quantite),
        SUM(lm.quantite * m.prix_supplementaire)
    FROM Vente v
    JOIN v.commande cmd
    JOIN cmd.lignes l
    JOIN l.modificateursSelectionnes lm
    JOIN lm.modificateur m
    WHERE v.pointDeVente.restaurant.id_restaurant = :restaurantId

      AND (
          :pointDeVenteId IS NULL
          OR v.pointDeVente.id_pdv = :pointDeVenteId
      )

      AND (
          :dateDebut IS NULL
          OR v.dateVente >= :dateDebut
      )

      AND (
          :dateFin IS NULL
          OR v.dateVente <= :dateFin
      )

    GROUP BY
        m.id_modificateur,
        m.nom_modificateur

    ORDER BY
        SUM(lm.quantite * m.prix_supplementaire) DESC
""")
    List<Object[]> getVentesParModificateur(
            @Param("restaurantId")
            Long restaurantId,

            @Param("pointDeVenteId")
            Long pointDeVenteId,

            @Param("dateDebut")
            Date dateDebut,

            @Param("dateFin")
            Date dateFin
    );

    // ============================================================
    // VENTES PAR REDUCTION
    // ============================================================

    @Query("""
    SELECT
        r.id_reduction,
        r.nom_reduction,
        r.type,
        r.valeur,
        COUNT(v),
        COALESCE(SUM(v.montantReduction), 0),
        COALESCE(SUM(v.montantTtc), 0)
    FROM Vente v
    JOIN v.reduction r
    WHERE v.pointDeVente.restaurant.id_restaurant = :restaurantId

      AND (
          :pointDeVenteId IS NULL
          OR v.pointDeVente.id_pdv = :pointDeVenteId
      )

      AND (
          :dateDebut IS NULL
          OR v.dateVente >= :dateDebut
      )

      AND (
          :dateFin IS NULL
          OR v.dateVente <= :dateFin
      )

    GROUP BY
        r.id_reduction,
        r.nom_reduction,
        r.type,
        r.valeur

    ORDER BY
        SUM(v.montantReduction) DESC
""")
    List<Object[]> getVentesParReduction(
            @Param("restaurantId")
            Long restaurantId,

            @Param("pointDeVenteId")
            Long pointDeVenteId,

            @Param("dateDebut")
            Date dateDebut,

            @Param("dateFin")
            Date dateFin
    );

    // ============================================================
// VENTES PAR MODE DE PAIEMENT
// ============================================================

    @Query("""
    SELECT
        mp.id_mode_paiement,
        mp.libelle,
        COUNT(v),
        COALESCE(SUM(v.montantTtc), 0)
    FROM Vente v
    JOIN v.modePaiement mp
    WHERE v.pointDeVente.restaurant.id_restaurant = :restaurantId

      AND (
          :pointDeVenteId IS NULL
          OR v.pointDeVente.id_pdv = :pointDeVenteId
      )

      AND (
          :dateDebut IS NULL
          OR v.dateVente >= :dateDebut
      )

      AND (
          :dateFin IS NULL
          OR v.dateVente <= :dateFin
      )

    GROUP BY
        mp.id_mode_paiement,
        mp.libelle

    ORDER BY
        SUM(v.montantTtc) DESC
""")
    List<Object[]> getVentesParModePaiement(
            @Param("restaurantId")
            Long restaurantId,

            @Param("pointDeVenteId")
            Long pointDeVenteId,

            @Param("dateDebut")
            Date dateDebut,

            @Param("dateFin")
            Date dateFin
    );

    // ============================================================
    // VENTES PAR EMPLOYE (SERVEUR AYANT PRIS LA COMMANDE)
    // ============================================================

    @Query("""
    SELECT
        e.id_utilisateur,
        CONCAT(e.prenom, ' ', e.nom),
        e.matricule,
        COUNT(v),
        COALESCE(SUM(v.montantTtc), 0)
    FROM Vente v
    JOIN v.commande cmd
    JOIN cmd.employee e
    WHERE v.pointDeVente.restaurant.id_restaurant = :restaurantId

      AND (
          :pointDeVenteId IS NULL
          OR v.pointDeVente.id_pdv = :pointDeVenteId
      )

      AND (
          :dateDebut IS NULL
          OR v.dateVente >= :dateDebut
      )

      AND (
          :dateFin IS NULL
          OR v.dateVente <= :dateFin
      )

    GROUP BY
        e.id_utilisateur,
        e.prenom,
        e.nom,
        e.matricule

    ORDER BY
        SUM(v.montantTtc) DESC
""")
    List<Object[]> getVentesParEmployee(
            @Param("restaurantId")
            Long restaurantId,

            @Param("pointDeVenteId")
            Long pointDeVenteId,

            @Param("dateDebut")
            Date dateDebut,

            @Param("dateFin")
            Date dateFin
    );

    // ============================================================
    // VENTES PAR REÇU
    // ============================================================

    @Query("""
    SELECT
        v.id_vente,
        r.numeroRecu,
        r.date_emission,
        v.dateVente,
        v.montantTtc,
        mp.libelle,
        pdv.nomPdv
    FROM Vente v
    JOIN v.recu r
    LEFT JOIN v.modePaiement mp
    LEFT JOIN v.pointDeVente pdv
    WHERE v.pointDeVente.restaurant.id_restaurant = :restaurantId

      AND (
          :pointDeVenteId IS NULL
          OR v.pointDeVente.id_pdv = :pointDeVenteId
      )

      AND (
          :dateDebut IS NULL
          OR v.dateVente >= :dateDebut
      )

      AND (
          :dateFin IS NULL
          OR v.dateVente <= :dateFin
      )

    ORDER BY
        v.dateVente DESC
""")
    List<Object[]> getVentesParRecu(
            @Param("restaurantId")
            Long restaurantId,

            @Param("pointDeVenteId")
            Long pointDeVenteId,

            @Param("dateDebut")
            Date dateDebut,

            @Param("dateFin")
            Date dateFin
    );

}