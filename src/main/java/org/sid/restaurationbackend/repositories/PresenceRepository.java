package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Presence;
import org.sid.restaurationbackend.enums.StatutPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {


    Collection<Presence> findByEmployee(Employee employee);


    List<Presence> findByDateBetween(Date dateDebut, Date dateFin);

    List <Presence> findByEmployeeAndDateBetween(Employee employee, Date dateDebut, Date dateFin);

    // ---------------------------------------------------------------
    // PAGE "PRESENCE" — un seul enregistrement par employé et par jour
    // ---------------------------------------------------------------

    /*
     * Le presence du jour pour un employé donné (bornes début/fin de
     * journée). Utilisé à la fois pour afficher la feuille de
     * présence du jour et pour l'upsert lors du marquage
     * (Présent / Absent / Congé) : s'il existe déjà un enregistrement
     * pour ce jour, on le met à jour plutôt que d'en créer un second.
     */
    Optional<Presence> findFirstByEmployeeAndDateBetween(
            Employee employee,
            Date dateDebut,
            Date dateFin
    );

    /*
     * Nombre de jours où l'employé a été marqué avec un statut donné
     * (typiquement ABSENT) sur une période donnée (mois courant,
     * année courante...).
     */
    long countByEmployeeAndStatutAndDateBetween(
            Employee employee,
            StatutPresence statut,
            Date dateDebut,
            Date dateFin
    );
}
