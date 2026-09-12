package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.PointDeVente;
import org.sid.restaurationbackend.entities.Role;
import org.sid.restaurationbackend.enums.StatutPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByMatricule(String matricule);
    Optional<Employee> findByCodePin(String codePin);
    List<Employee> findByRole(Role roleId);
    List<Employee> findByPdvAffecte(PointDeVente pdv);
    Optional<Employee> findByEmail(String email);

    /*
     * Tous les employés d'un restaurant donné, quel que soit leur
     * rôle. Utilisée par AuthServiceImpl.impersonateRestaurant
     * (SUPERADMIN — Option B) pour retrouver l'Admin du restaurant
     * ciblé ; le filtrage par rôle "Admin" se fait ensuite en
     * mémoire (même approche que EmployeeController pour les autres
     * filtres, cf. javadoc de classe).
     */
    @Query("SELECT e FROM Employee e WHERE e.restaurant.id_restaurant = :idRestaurant")
    List<Employee> findByRestaurant_Id_restaurant(@Param("idRestaurant") Long idRestaurant);
    List<Employee> findByStatutPresence(StatutPresence statutPresence);
    List<Employee>
    findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrEmailContainingIgnoreCaseOrMatriculeContainingIgnoreCaseOrTelephoneContainingIgnoreCase(
            String nom,
            String prenom,
            String email,
            String matricule,
            String telephone
    );

    /*
     * Employés dont le rôle possède, pour au moins une interface,
     * une fonctionnalité autorisée (RoleFonctionnalite.autorise = true)
     * dont le code correspond à celui fourni. Réutilise le système de
     * permissions existant (Role -> RoleFonctionnalite -> Fonctionnalite)
     * plutôt que de créer un nouveau mécanisme d'éligibilité.
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
            "JOIN e.role r " +
            "JOIN r.fonctionnalites rf " +
            "JOIN rf.fonctionnalite f " +
            "WHERE f.codeFonctionnalite = :code " +
            "AND rf.autorise = true")
    List<Employee> findEmployeesEligiblesParFonctionnalite(
            @Param("code") String code
    );


    // =========================================================
    // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
    // =========================================================

    /*
     * Employés éligibles à l'attribution automatique/manuelle d'une
     * prise en charge (§2 et §26 de la spec). Simple lecture, sans
     * verrou : utilisée pour l'affichage (statistiques, cases à
     * cocher côté responsable), jamais pour décider quel employé
     * recevra une nouvelle prise en charge.
     */
    List<Employee> findByEligibleAttributionAutomatiqueTrue();

    /*
     * Même liste, mais triée par identifiant croissant ET verrouillée
     * en écriture (SELECT ... FOR UPDATE) pour la durée de la
     * transaction appelante.
     *
     * Utilisée UNIQUEMENT par l'algorithme d'attribution automatique
     * (§10 de la spec : éviter les attributions concurrentes). Deux
     * requêtes d'attribution simultanées tentent de verrouiller les
     * mêmes lignes Employee, dans le même ordre (id croissant, ce qui
     * évite les deadlocks) : la seconde transaction attend que la
     * première ait validé (COMMIT) sa nouvelle PriseEnChargeTable
     * avant de pouvoir lire à son tour un compte de charge à jour.
     * Le tri par id croissant sert aussi de règle de départage
     * déterministe en cas d'égalité (§9 de la spec) : à charge égale,
     * l'algorithme retient le premier employé rencontré, donc celui
     * avec le plus petit identifiant.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Employee e " +
            "WHERE e.eligibleAttributionAutomatique = true " +
            "ORDER BY e.id_utilisateur ASC")
    List<Employee> findEligiblesForAttributionAutomatiquePourMiseAJour();

}