package org.sid.restaurationbackend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sid.restaurationbackend.dtos.PriseEnChargeTableDTO;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.PriseEnChargeTable;
import org.sid.restaurationbackend.entities.TableRestaurant;
import org.sid.restaurationbackend.enums.StatutPriseEnCharge;
import org.sid.restaurationbackend.enums.StatutTable;
import org.sid.restaurationbackend.exceptions.AucunEmployeeDisponibleException;
import org.sid.restaurationbackend.exceptions.EmployeeNonEligibleException;
import org.sid.restaurationbackend.exceptions.PriseEnChargeTableEnUtilisationException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.EmployeeRepository;
import org.sid.restaurationbackend.repositories.PriseEnChargeTableRepository;
import org.sid.restaurationbackend.repositories.TableRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * Phase 3C — tests de l'algorithme d'attribution automatique/manuelle
 * des prises en charge (§29/§30 de la spec).
 *
 * Les repositories sont mockés (Mockito) : ces tests vérifient
 * uniquement la LOGIQUE de choix de l'employé, pas le comportement
 * réel du verrou PESSIMISTIC_WRITE en base (qui nécessiterait un test
 * d'intégration avec une vraie base de données concurrente).
 *
 * RestaurantMapper est une classe concrète simple (BeanUtils), on
 * utilise donc une vraie instance plutôt qu'un mock.
 */
@ExtendWith(MockitoExtension.class)
class PriseEnChargeAttributionAutomatiqueServiceTest {

    @Mock
    private PriseEnChargeTableRepository priseEnChargeTableRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private RestaurantMapper dtotMapper;

    private PriseEnChargeTableServiceImpl service;

    private TableRestaurant table;
    private Employee ahmed;
    private Employee sarah;
    private Employee yassine;

    @BeforeEach
    void setUp() {
        dtotMapper = new RestaurantMapper();

        service = new PriseEnChargeTableServiceImpl(
                priseEnChargeTableRepository,
                tableRepository,
                employeeRepository,
                dtotMapper
        );

        table = new TableRestaurant();
        table.setId_table(5L);
        table.setStatut(StatutTable.LIBRE);

        ahmed = new Employee();
        ahmed.setId_utilisateur(1L);
        ahmed.setNom("Trabelsi");
        ahmed.setPrenom("Ahmed");
        ahmed.setEligibleAttributionAutomatique(true);

        sarah = new Employee();
        sarah.setId_utilisateur(2L);
        sarah.setNom("Gharbi");
        sarah.setPrenom("Sarah");
        sarah.setEligibleAttributionAutomatique(true);

        yassine = new Employee();
        yassine.setId_utilisateur(3L);
        yassine.setNom("Ben Ali");
        yassine.setPrenom("Yassine");
        yassine.setEligibleAttributionAutomatique(true);

        // Aucune prise en charge active sur la table par défaut.
        when(tableRepository.findById(5L))
                .thenReturn(Optional.of(table));
        when(priseEnChargeTableRepository.findByTableAndStatut(
                eq(table), eq(StatutPriseEnCharge.ACTIVE)))
                .thenReturn(Optional.empty());
        when(priseEnChargeTableRepository.save(any(PriseEnChargeTable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // =========================================================
    // TEST 1 — Ahmed=3, Sarah=2, Yassine=1 -> Yassine choisi
    // =========================================================
    @Test
    void test1_choisitEmployeeAvecMoinsDePrisesEnCharge()
            throws Exception {

        when(employeeRepository.findEligiblesForAttributionAutomatiquePourMiseAJour())
                .thenReturn(List.of(ahmed, sarah, yassine));

        when(priseEnChargeTableRepository.countByEmployeeAndStatut(
                ahmed, StatutPriseEnCharge.ACTIVE)).thenReturn(3L);
        when(priseEnChargeTableRepository.countByEmployeeAndStatut(
                sarah, StatutPriseEnCharge.ACTIVE)).thenReturn(2L);
        when(priseEnChargeTableRepository.countByEmployeeAndStatut(
                yassine, StatutPriseEnCharge.ACTIVE)).thenReturn(1L);

        PriseEnChargeTableDTO resultat =
                service.attribuerAutomatiquement(5L);

        assertEquals("Yassine", resultat.getEmployee().getPrenom());
        assertEquals(StatutPriseEnCharge.ACTIVE, resultat.getStatut());
        assertEquals(StatutTable.OCCUPEE, table.getStatut());
    }

    // =========================================================
    // TEST 2 — égalité parfaite (0 = 0 = 0) -> règle déterministe :
    // le plus petit identifiant (Ahmed, id=1) est choisi.
    // =========================================================
    @Test
    void test2_egaliteChoisitPlusPetitIdentifiant() throws Exception {

        when(employeeRepository.findEligiblesForAttributionAutomatiquePourMiseAJour())
                .thenReturn(List.of(ahmed, sarah, yassine));

        when(priseEnChargeTableRepository.countByEmployeeAndStatut(
                any(Employee.class), eq(StatutPriseEnCharge.ACTIVE)))
                .thenReturn(0L);

        PriseEnChargeTableDTO resultat =
                service.attribuerAutomatiquement(5L);

        assertEquals("Ahmed", resultat.getEmployee().getPrenom());
    }

    // =========================================================
    // TEST 3 — un employé non éligible (ex: Mohamed) n'est jamais
    // pris en compte : simulé ici par le fait que le repository
    // (filtré par eligibleAttributionAutomatique = true) ne renvoie
    // que Ahmed et Sarah.
    // =========================================================
    @Test
    void test3_employeNonEligibleExclu() throws Exception {

        when(employeeRepository.findEligiblesForAttributionAutomatiquePourMiseAJour())
                .thenReturn(List.of(ahmed, sarah));

        when(priseEnChargeTableRepository.countByEmployeeAndStatut(
                ahmed, StatutPriseEnCharge.ACTIVE)).thenReturn(2L);
        when(priseEnChargeTableRepository.countByEmployeeAndStatut(
                sarah, StatutPriseEnCharge.ACTIVE)).thenReturn(1L);

        PriseEnChargeTableDTO resultat =
                service.attribuerAutomatiquement(5L);

        assertEquals("Sarah", resultat.getEmployee().getPrenom());
    }

    // =========================================================
    // TEST 4 — aucun employé éligible -> erreur métier, aucune
    // prise en charge créée.
    // =========================================================
    @Test
    void test4_aucunEmployeeEligible_leveErreurMetier() {

        when(employeeRepository.findEligiblesForAttributionAutomatiquePourMiseAJour())
                .thenReturn(List.of());

        assertThrows(
                AucunEmployeeDisponibleException.class,
                () -> service.attribuerAutomatiquement(5L)
        );

        verify(priseEnChargeTableRepository, never())
                .save(any(PriseEnChargeTable.class));
    }

    // =========================================================
    // TEST 5 — après qu'une prise en charge soit TERMINEE, le
    // compte actif diminue et l'employé redevient un candidat
    // prioritaire (simulé en faisant évoluer le mock entre deux
    // appels, comme le ferait une vraie base de données).
    // =========================================================
    @Test
    void test5_compteBaisseApresPriseEnChargeTerminee() throws Exception {

        when(employeeRepository.findEligiblesForAttributionAutomatiquePourMiseAJour())
                .thenReturn(List.of(ahmed, yassine));

        // Avant : Yassine a 2 prises actives (donc >= Ahmed).
        when(priseEnChargeTableRepository.countByEmployeeAndStatut(
                ahmed, StatutPriseEnCharge.ACTIVE)).thenReturn(1L);
        when(priseEnChargeTableRepository.countByEmployeeAndStatut(
                yassine, StatutPriseEnCharge.ACTIVE))
                .thenReturn(2L) // premier appel : avant "client parti"
                .thenReturn(0L); // second appel : après "client parti"

        PriseEnChargeTableDTO premierResultat =
                service.attribuerAutomatiquement(5L);
        assertEquals("Ahmed", premierResultat.getEmployee().getPrenom());

        // Simule une deuxième table libre + le fait que la prise en
        // charge de Yassine s'est terminée entre-temps (son compte
        // repasse à 0, cf. mock ci-dessus).
        TableRestaurant table2 = new TableRestaurant();
        table2.setId_table(6L);
        table2.setStatut(StatutTable.LIBRE);
        when(tableRepository.findById(6L)).thenReturn(Optional.of(table2));
        when(priseEnChargeTableRepository.findByTableAndStatut(
                eq(table2), eq(StatutPriseEnCharge.ACTIVE)))
                .thenReturn(Optional.empty());

        PriseEnChargeTableDTO deuxiemeResultat =
                service.attribuerAutomatiquement(6L);
        assertEquals("Yassine", deuxiemeResultat.getEmployee().getPrenom());
    }

    // =========================================================
    // Table déjà occupée par une prise en charge active : refusé,
    // que l'attribution soit automatique ou manuelle.
    // =========================================================
    @Test
    void refuseAttributionAutomatique_siPriseActiveExisteDeja() {

        PriseEnChargeTable priseExistante = new PriseEnChargeTable();
        priseExistante.setStatut(StatutPriseEnCharge.ACTIVE);

        when(priseEnChargeTableRepository.findByTableAndStatut(
                eq(table), eq(StatutPriseEnCharge.ACTIVE)))
                .thenReturn(Optional.of(priseExistante));

        assertThrows(
                PriseEnChargeTableEnUtilisationException.class,
                () -> service.attribuerAutomatiquement(5L)
        );

        verifyNoInteractions(employeeRepository);
    }

    // =========================================================
    // Attribution MANUELLE : succès pour un employé éligible.
    // =========================================================
    @Test
    void attributionManuelle_succesPourEmployeEligible() throws Exception {

        when(employeeRepository.findById(3L))
                .thenReturn(Optional.of(yassine));

        PriseEnChargeTableDTO resultat =
                service.attribuerManuellement(5L, 3L);

        assertEquals("Yassine", resultat.getEmployee().getPrenom());
        assertEquals(StatutTable.OCCUPEE, table.getStatut());
    }

    // =========================================================
    // Attribution MANUELLE : refusée pour un employé non éligible
    // (§21 de la spec — ex. Mohamed).
    // =========================================================
    @Test
    void attributionManuelle_refuseePourEmployeNonEligible() {

        Employee mohamed = new Employee();
        mohamed.setId_utilisateur(4L);
        mohamed.setNom("Ali");
        mohamed.setPrenom("Mohamed");
        mohamed.setEligibleAttributionAutomatique(false);

        when(employeeRepository.findById(4L))
                .thenReturn(Optional.of(mohamed));

        assertThrows(
                EmployeeNonEligibleException.class,
                () -> service.attribuerManuellement(5L, 4L)
        );

        verify(priseEnChargeTableRepository, never())
                .save(any(PriseEnChargeTable.class));
    }

    // =========================================================
    // Employé créé avant la phase 3C : champ eligibleAttribution
    // Automatique encore null -> traité comme non éligible, pas de
    // NullPointerException.
    // =========================================================
    @Test
    void attributionManuelle_refuseeSiChampEligibiliteNull() {

        Employee employeAncien = new Employee();
        employeAncien.setId_utilisateur(5L);
        employeAncien.setEligibleAttributionAutomatique(null);

        when(employeeRepository.findById(5L))
                .thenReturn(Optional.of(employeAncien));

        assertThrows(
                EmployeeNonEligibleException.class,
                () -> service.attribuerManuellement(5L, 5L)
        );
    }
}
