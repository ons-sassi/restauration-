package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.enums.ModeCommande;
import org.sid.restaurationbackend.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByClient(ClientAuthentifie client);

    List<Commande> findByClientNonAuthentifie(ClientNonAuthentifie clientNonAuthentifie);

    List<Commande> findByTable(TableRestaurant table);

    List<Commande> findByEmployee(Employee employee);

    List<Commande> findByStatut(StatutCommande statut);

    List<Commande> findByModeCommande(ModeCommande modeCommande);

    List<Commande> findByDateCommandeBetween(
            Date dateDebut,
            Date dateFin
    );

    @Query("""
        SELECT c FROM Commande c
        WHERE (:client IS NULL OR c.client = :client)
        AND (:statut IS NULL OR c.statut = :statut)
        AND (:dateDebut IS NULL OR c.dateCommande >= :dateDebut)
        AND (:dateFin IS NULL OR c.dateCommande <= :dateFin)
        ORDER BY c.dateCommande DESC
    """)
    List<Commande> search(
            @Param("client") ClientAuthentifie client,
            @Param("statut") StatutCommande statut,
            @Param("dateDebut") Date dateDebut,
            @Param("dateFin") Date dateFin
    );
}