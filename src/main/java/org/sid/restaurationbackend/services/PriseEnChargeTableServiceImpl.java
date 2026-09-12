package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.AttributionStatsDTO;
import org.sid.restaurationbackend.dtos.PriseEnChargeTableDTO;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.PriseEnChargeTable;
import org.sid.restaurationbackend.entities.TableRestaurant;
import org.sid.restaurationbackend.enums.StatutPriseEnCharge;
import org.sid.restaurationbackend.enums.StatutTable;
import org.sid.restaurationbackend.exceptions.AucunEmployeeDisponibleException;
import org.sid.restaurationbackend.exceptions.EmployeeNonEligibleException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeNonAutoriseeException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeTableEnUtilisationException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeTableNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.EmployeeRepository;
import org.sid.restaurationbackend.repositories.PriseEnChargeTableRepository;
import org.sid.restaurationbackend.repositories.TableRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class PriseEnChargeTableServiceImpl implements PriseEnChargeTableService {

    private final PriseEnChargeTableRepository priseEnChargeTableRepository;
    private final TableRepository tableRepository;
    private final EmployeeRepository employeeRepository;
    private final RestaurantMapper dtotMapper;


    // =========================================================
    // Déduit l'employé connecté depuis le token JWT (email porté
    // par l'Authentication), exactement comme
    // TableServiceImpl.getTablesDuServeurConnecte(). Jamais un
    // employeeId envoyé par le frontend : impossible de démarrer ou
    // terminer une prise en charge au nom d'un collègue en modifiant
    // une requête.
    // =========================================================
    private Employee getEmployeeConnecte() throws EmployeeNotFoundException {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new EmployeeNotFoundException(
                    "Employé non authentifié"
            );
        }

        String email = authentication.getName();

        return employeeRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employé introuvable avec l'email : " + email
                        ));
    }


    @Override
    public PriseEnChargeTableDTO commencerPriseEnCharge(
            Long tableId)
            throws TableNotFoundException,
            EmployeeNotFoundException,
            PriseEnChargeTableEnUtilisationException,
            PriseEnChargeNonAutoriseeException {

        TableRestaurant table =
                tableRepository.findById(tableId)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        Employee employee = getEmployeeConnecte();

        // Un employé ne peut démarrer une prise en charge que sur une
        // table dont il est le responsable PERMANENT (serveurAttribue).
        // Cette règle est indépendante de ce que le frontend affiche.
        if (table.getServeurAttribue() == null ||
                !table.getServeurAttribue().getId_utilisateur().equals(
                        employee.getId_utilisateur())) {

            throw new PriseEnChargeNonAutoriseeException(
                    "Vous n'êtes pas responsable de cette table.");
        }

        Optional<PriseEnChargeTable> priseActiveExistante =
                priseEnChargeTableRepository
                        .findByTableAndStatut(
                                table,
                                StatutPriseEnCharge.ACTIVE);

        if (priseActiveExistante.isPresent()) {

            throw new PriseEnChargeTableEnUtilisationException(
                    "Cette table a déjà une prise en charge active. " +
                            "Terminez-la avant d'en démarrer une nouvelle.");
        }

        PriseEnChargeTable prise = new PriseEnChargeTable();

        prise.setTable(table);
        prise.setEmployee(employee);
        prise.setDateDebut(new Date());
        prise.setDateFin(null);
        prise.setStatut(StatutPriseEnCharge.ACTIVE);

        PriseEnChargeTable savedPrise =
                priseEnChargeTableRepository.save(prise);

        // Cohérence table / prise en charge : le client est maintenant
        // présent à la table, elle passe donc OCCUPÉE si elle ne
        // l'était pas déjà (ex. table LIBRE prise en charge directement).
        if (table.getStatut() != StatutTable.OCCUPEE) {
            table.setStatut(StatutTable.OCCUPEE);
            tableRepository.save(table);
        }

        return dtotMapper.fromPriseEnChargeTable(savedPrise);
    }


    @Override
    public PriseEnChargeTableDTO terminerPriseEnCharge(
            Long tableId)
            throws TableNotFoundException,
            PriseEnChargeTableNotFoundException,
            EmployeeNotFoundException,
            PriseEnChargeNonAutoriseeException {

        TableRestaurant table =
                tableRepository.findById(tableId)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        PriseEnChargeTable prise =
                priseEnChargeTableRepository
                        .findByTableAndStatut(
                                table,
                                StatutPriseEnCharge.ACTIVE)
                        .orElseThrow(() ->
                                new PriseEnChargeTableNotFoundException(
                                        "Aucune prise en charge active pour cette table"));

        Employee employee = getEmployeeConnecte();

        // Seul l'employé qui a démarré la prise en charge peut la
        // terminer ("Client parti"). Un collègue ne peut pas terminer
        // la prise en charge d'un autre, même s'il est responsable
        // permanent de la table.
        if (!prise.getEmployee().getId_utilisateur().equals(
                employee.getId_utilisateur())) {

            throw new PriseEnChargeNonAutoriseeException(
                    "Vous n'êtes pas à l'origine de cette prise en charge.");
        }

        prise.setStatut(StatutPriseEnCharge.TERMINEE);
        prise.setDateFin(new Date());

        PriseEnChargeTable updatedPrise =
                priseEnChargeTableRepository.save(prise);

        // Cohérence table / prise en charge : le client est parti, la
        // table redevient disponible selon les règles métier actuelles
        // (pas de nouveaux statuts, on réutilise LIBRE/OCCUPÉE).
        table.setStatut(StatutTable.LIBRE);
        tableRepository.save(table);

        return dtotMapper.fromPriseEnChargeTable(updatedPrise);
    }


    @Override
    @Transactional(readOnly = true)
    public PriseEnChargeTableDTO getPriseEnChargeActiveByTable(
            Long tableId)
            throws TableNotFoundException, PriseEnChargeTableNotFoundException {

        TableRestaurant table =
                tableRepository.findById(tableId)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        PriseEnChargeTable prise =
                priseEnChargeTableRepository
                        .findByTableAndStatut(
                                table,
                                StatutPriseEnCharge.ACTIVE)
                        .orElseThrow(() ->
                                new PriseEnChargeTableNotFoundException(
                                        "Aucune prise en charge active pour cette table"));

        return dtotMapper.fromPriseEnChargeTable(prise);
    }


    @Override
    @Transactional(readOnly = true)
    public List<PriseEnChargeTableDTO> getPrisesEnChargeActivesByEmployee(
            Long employeeId)
            throws EmployeeNotFoundException {

        Employee employee =
                employeeRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(
                                        "Employee not found"));

        return priseEnChargeTableRepository
                .findByEmployeeAndStatut(
                        employee,
                        StatutPriseEnCharge.ACTIVE)
                .stream()
                .map(dtotMapper::fromPriseEnChargeTable)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public long getChargeActiveEmployee(
            Long employeeId)
            throws EmployeeNotFoundException {

        Employee employee =
                employeeRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(
                                        "Employee not found"));

        return priseEnChargeTableRepository
                .countByEmployeeAndStatut(
                        employee,
                        StatutPriseEnCharge.ACTIVE);
    }


    // =========================================================
    // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
    // =========================================================

    /*
     * Factorise la création d'une PriseEnChargeTable et le passage de
     * la table à OCCUPÉE, commun à l'attribution automatique et à
     * l'attribution manuelle par le responsable. Contrairement à
     * commencerPriseEnCharge (§3B, self-service par l'employé
     * connecté), ici l'employé est fourni par l'appelant (algorithme
     * ou choix du responsable) et n'a pas besoin d'être le
     * responsable permanent de la table.
     */
    private PriseEnChargeTableDTO creerPriseEnChargePourEmployee(
            TableRestaurant table,
            Employee employee) {

        PriseEnChargeTable prise = new PriseEnChargeTable();

        prise.setTable(table);
        prise.setEmployee(employee);
        prise.setDateDebut(new Date());
        prise.setDateFin(null);
        prise.setStatut(StatutPriseEnCharge.ACTIVE);

        PriseEnChargeTable savedPrise =
                priseEnChargeTableRepository.save(prise);

        // Cohérence table / prise en charge (identique à
        // commencerPriseEnCharge) : le client est désormais présent.
        if (table.getStatut() != StatutTable.OCCUPEE) {
            table.setStatut(StatutTable.OCCUPEE);
            tableRepository.save(table);
        }

        return dtotMapper.fromPriseEnChargeTable(savedPrise);
    }


    /*
     * Vérifie qu'aucune prise en charge ACTIVE n'existe déjà pour
     * cette table (§ règle réutilisée de commencerPriseEnCharge) :
     * qu'elle soit démarrée automatiquement, manuellement ou par
     * l'employé lui-même, une table ne peut avoir qu'une seule prise
     * en charge active à la fois.
     */
    private void verifierAucunePriseActive(TableRestaurant table)
            throws PriseEnChargeTableEnUtilisationException {

        Optional<PriseEnChargeTable> priseActiveExistante =
                priseEnChargeTableRepository
                        .findByTableAndStatut(
                                table,
                                StatutPriseEnCharge.ACTIVE);

        if (priseActiveExistante.isPresent()) {
            throw new PriseEnChargeTableEnUtilisationException(
                    "Cette table a déjà une prise en charge active. " +
                            "Terminez-la avant d'en démarrer une nouvelle.");
        }
    }


    @Override
    @Transactional
    public PriseEnChargeTableDTO attribuerAutomatiquement(
            Long tableId)
            throws TableNotFoundException,
            PriseEnChargeTableEnUtilisationException,
            AucunEmployeeDisponibleException {

        TableRestaurant table =
                tableRepository.findById(tableId)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        verifierAucunePriseActive(table);

        // =====================================================
        // §10 de la spec — ÉVITER LES ATTRIBUTIONS CONCURRENTES
        // =====================================================
        // Verrouille (SELECT ... FOR UPDATE) toutes les lignes
        // Employee éligibles, triées par id croissant. Si deux
        // requêtes d'attribution automatique arrivent en même temps,
        // la seconde est bloquée par la base de données jusqu'à ce
        // que la transaction de la première (qui insère la nouvelle
        // PriseEnChargeTable puis COMMIT) soit terminée. La seconde
        // transaction recompte alors les charges actives à jour et
        // ne peut donc pas choisir le même employé sur la base d'un
        // compte périmé.
        List<Employee> eligibles =
                employeeRepository
                        .findEligiblesForAttributionAutomatiquePourMiseAJour();

        if (eligibles.isEmpty()) {
            throw new AucunEmployeeDisponibleException(
                    "Aucun employé n'est actuellement disponible " +
                            "pour l'attribution automatique.");
        }

        // §28 — Choisir l'employé éligible ayant le moins de prises
        // en charge ACTIVES au moment de l'attribution.
        // §9 — Règle de départage en cas d'égalité : la liste est
        // triée par id croissant (voir la requête ci-dessus) et l'on
        // ne remplace le meilleur candidat qu'en cas de compte
        // STRICTEMENT inférieur ("<", jamais "<="). Ainsi, à charge
        // égale, c'est toujours le premier rencontré dans cet ordre
        // -- donc celui avec le plus petit identifiant -- qui est
        // retenu. Règle simple, déterministe et stable.
        Employee employeeChoisi = null;
        long chargeMinimale = Long.MAX_VALUE;

        for (Employee candidat : eligibles) {

            long chargeActuelle =
                    priseEnChargeTableRepository
                            .countByEmployeeAndStatut(
                                    candidat,
                                    StatutPriseEnCharge.ACTIVE);

            if (chargeActuelle < chargeMinimale) {
                chargeMinimale = chargeActuelle;
                employeeChoisi = candidat;
            }
        }

        return creerPriseEnChargePourEmployee(table, employeeChoisi);
    }


    @Override
    @Transactional
    public PriseEnChargeTableDTO attribuerManuellement(
            Long tableId,
            Long employeeId)
            throws TableNotFoundException,
            EmployeeNotFoundException,
            PriseEnChargeTableEnUtilisationException,
            EmployeeNonEligibleException {

        TableRestaurant table =
                tableRepository.findById(tableId)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        Employee employee =
                employeeRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(
                                        "Employee not found"));

        // §21 — Un employé non éligible ne peut pas recevoir de
        // prise en charge, même choisi explicitement par le
        // responsable. Boolean.TRUE.equals(...) gère aussi le cas
        // où le champ est encore null (employés créés avant la
        // phase 3C) comme "non éligible", sans lever de NPE.
        if (!Boolean.TRUE.equals(employee.getEligibleAttributionAutomatique())) {
            throw new EmployeeNonEligibleException(
                    employee.getPrenom() + " " + employee.getNom() +
                            " n'est pas éligible pour recevoir des " +
                            "prises en charge.");
        }

        verifierAucunePriseActive(table);

        return creerPriseEnChargePourEmployee(table, employee);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AttributionStatsDTO> getStatistiquesAttribution() {

        List<Employee> eligibles =
                employeeRepository.findByEligibleAttributionAutomatiqueTrue();

        List<AttributionStatsDTO> stats = new ArrayList<>();

        for (Employee employee : eligibles) {

            long chargeActuelle =
                    priseEnChargeTableRepository
                            .countByEmployeeAndStatut(
                                    employee,
                                    StatutPriseEnCharge.ACTIVE);

            stats.add(new AttributionStatsDTO(
                    dtotMapper.fromEmployee(employee),
                    chargeActuelle,
                    true));
        }

        return stats;
    }
}
