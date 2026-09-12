package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Restaurant;
import org.sid.restaurationbackend.entities.TableRestaurant;
import org.sid.restaurationbackend.enums.StatutTable;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.EmployeeRepository;
import org.sid.restaurationbackend.repositories.TableRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TableServiceImpl implements TableService {

    private final RestaurantMapper dtotMapper;
    private final TableRepository tableRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    /*
     * Code de la fonctionnalité (déjà définie dans le catalogue de
     * permissions / DataInitializer) donnant le droit d'attribuer un
     * responsable à une table. Réutilisé ici pour lister les employés
     * éligibles, sans dupliquer le système de permissions existant.
     */
    private static final String CODE_PERMISSION_ATTRIBUTION_TABLES = "EMPLOYES_TABLES";




    @Override
    public TableRestaurantDTO saveTable(
            TableRestaurantDTO tableRestaurantDTO) {

        TableRestaurant table =
                dtotMapper.fromTableRestaurantDTO(tableRestaurantDTO);

        // Si aucune date de génération du QR n'est fournie
        if (table.getDateGenerationQr() == null
                && table.getCodeQr() != null) {

            table.setDateGenerationQr(new Date());
        }

        TableRestaurant savedTable =
                tableRepository.save(table);

        return dtotMapper.fromTableRestaurant(savedTable);
    }


    @Override
    public TableRestaurantDTO updateTable(
            Long id,
            TableRestaurantDTO tableRestaurantDTO)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findById(id)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        dtotMapper.updateTableRestaurantFromDto(
                tableRestaurantDTO,
                table);

        TableRestaurant updatedTable =
                tableRepository.save(table);

        return dtotMapper.fromTableRestaurant(updatedTable);
    }


    @Override
    public void deleteTable(Long id)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findById(id)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        tableRepository.delete(table);
    }


    @Override
    @Transactional(readOnly = true)
    public TableRestaurantDTO getTable(Long id)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findById(id)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        return dtotMapper.fromTableRestaurant(table);
    }


    @Override
    @Transactional(readOnly = true)
    public List<TableRestaurantDTO> getAllTable() {

        return tableRepository.findAll()
                .stream()
                .map(dtotMapper::fromTableRestaurant)
                .toList();
    }




    @Override
    @Transactional(readOnly = true)
    public TableRestaurantDTO getTableByNumeroTable(
            Integer numeroTable)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findByNumeroTable(numeroTable)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        return dtotMapper.fromTableRestaurant(table);
    }


    @Override
    @Transactional(readOnly = true)
    public TableRestaurantDTO getTableByCodeQr(
            String codeQr)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findByCodeQr(codeQr)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        return dtotMapper.fromTableRestaurant(table);
    }


    @Override
    @Transactional(readOnly = true)
    public TableRestaurantDTO getTableByUrlQr(
            String urlQr)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findByUrlQr(urlQr)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        return dtotMapper.fromTableRestaurant(table);
    }


    @Override
    @Transactional(readOnly = true)
    public List<TableRestaurantDTO> getTabByStatut(
            StatutTable statut) {

        return tableRepository.findByStatut(statut)
                .stream()
                .map(dtotMapper::fromTableRestaurant)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<TableRestaurantDTO> getTabByRestaurant(
            RestaurantDTO restaurant)
            throws RestaurantNotFoundException {

        Restaurant restaurantEntity =
                dtotMapper.fromRestaurantDTO(restaurant);

        return tableRepository
                .findByRestaurant(restaurantEntity)
                .stream()
                .map(dtotMapper::fromTableRestaurant)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<TableRestaurantDTO> getTabByServeurAttribue(
            EmployeeDTO employeeDTO)
            throws EmployeeNotFoundException {

        Employee employee =
                dtotMapper.fromEmployeeDTO(employeeDTO);

        return tableRepository
                .findByServeurAttribue(employee)
                .stream()
                .map(dtotMapper::fromTableRestaurant)
                .toList();
    }


    // =========================================================
    // MES TABLES (phase 3A)
    // =========================================================
    // L'employé connecté ne peut voir QUE les tables dont il est
    // responsable. L'identité de l'employé est déduite du token
    // JWT (email porté par l'Authentication), jamais d'un paramètre
    // envoyé par le frontend : impossible de consulter les tables
    // d'un autre employé en modifiant une URL ou une requête.
    @Override
    @Transactional(readOnly = true)
    public List<TableRestaurantDTO> getTablesDuServeurConnecte()
            throws EmployeeNotFoundException {

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

        // Le SUPERADMIN n'est pas un Employee : il n'est donc jamais
        // "responsable" d'une table à proprement parler. Plutôt que de
        // renvoyer une erreur (comportement précédent), on lui affiche
        // TOUTES les tables du restaurant actuellement sélectionné —
        // même restaurant que le reste du Back Office pour ce compte
        // (voir CurrentUserService.getRestaurantIdConnecte, qui lit le
        // restaurant choisi depuis le JWT pour un SUPERADMIN).
        boolean superAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPERADMIN".equalsIgnoreCase(a.getAuthority()));

        if (superAdmin) {

            Long restaurantId = currentUserService.getRestaurantIdConnecte();

            return tableRepository.findAll().stream()
                    .filter(t -> t.getRestaurant() != null
                            && restaurantId.equals(t.getRestaurant().getId_restaurant()))
                    .map(dtotMapper::fromTableRestaurant)
                    .toList();
        }

        String email = authentication.getName();

        Employee employee =
                employeeRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(
                                        "Employé introuvable avec l'email : " + email
                                ));

        return tableRepository
                .findByServeurAttribue(employee)
                .stream()
                .map(dtotMapper::fromTableRestaurant)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<TableRestaurantDTO> getTabByGenerePar(
            EmployeeDTO employee)
            throws EmployeeNotFoundException {

        Employee employeeEntity =
                dtotMapper.fromEmployeeDTO(employee);

        return tableRepository
                .findByGenerePar(employeeEntity)
                .stream()
                .map(dtotMapper::fromTableRestaurant)
                .toList();
    }




    @Override
    public TableRestaurantDTO activerQr(Long id)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findById(id)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        table.setQrActif(true);

        if (table.getDateGenerationQr() == null) {
            table.setDateGenerationQr(new Date());
        }

        TableRestaurant updatedTable =
                tableRepository.save(table);

        return dtotMapper.fromTableRestaurant(updatedTable);
    }


    @Override
    public TableRestaurantDTO desactiverQr(Long id)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findById(id)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        table.setQrActif(false);

        TableRestaurant updatedTable =
                tableRepository.save(table);

        return dtotMapper.fromTableRestaurant(updatedTable);
    }


    @Transactional(readOnly = true)
    @Override
    public TableRestaurantDTO getTableByQrActif(
            Long id)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findById(id)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        if (!Boolean.TRUE.equals(table.getQrActif())) {
            throw new TableNotFoundException(
                    "Le QR code de cette table est désactivé");
        }

        return dtotMapper.fromTableRestaurant(table);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TableRestaurantDTO> getTablesDisponiblesByRestaurant(
            RestaurantDTO restaurant)
            throws RestaurantNotFoundException {

        Restaurant restaurantEntity =
                dtotMapper.fromRestaurantDTO(restaurant);

        return tableRepository
                .findByRestaurantAndStatut(
                        restaurantEntity,
                        StatutTable.LIBRE)
                .stream()
                .map(dtotMapper::fromTableRestaurant)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TableRestaurantDTO> getTablesDisponibles() {

        return tableRepository
                .findByStatut(StatutTable.LIBRE)
                .stream()
                .map(dtotMapper::fromTableRestaurant)
                .toList();
    }


    // =========================================================
    // AFFECTATION PERMANENTE D'UN RESPONSABLE (serveurAttribue)
    // =========================================================

    @Override
    public TableRestaurantDTO assignerServeurResponsable(
            Long tableId,
            Long employeeId)
            throws TableNotFoundException, EmployeeNotFoundException {

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

        table.setServeurAttribue(employee);

        TableRestaurant updatedTable =
                tableRepository.save(table);

        return dtotMapper.fromTableRestaurant(updatedTable);
    }


    @Override
    public TableRestaurantDTO retirerServeurResponsable(
            Long tableId)
            throws TableNotFoundException {

        TableRestaurant table =
                tableRepository.findById(tableId)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"));

        table.setServeurAttribue(null);

        TableRestaurant updatedTable =
                tableRepository.save(table);

        return dtotMapper.fromTableRestaurant(updatedTable);
    }


    /*
     * Ancienne restriction (retirée à la demande) : seuls les
     * employés dont le rôle possédait la permission EMPLOYES_TABLES
     * apparaissaient dans le sélecteur "Employé responsable" de la
     * page "Attribution des tables" — via
     * findEmployeesEligiblesParFonctionnalite(CODE_PERMISSION_ATTRIBUTION_TABLES).
     *
     * On renvoie désormais TOUS les employés : le filtrage par
     * restaurant reste appliqué juste après, côté TableController
     * (getEmployesEligiblesResponsablesTables), donc aucune fuite
     * multi-restaurant n'est introduite ici.
     */
    @Transactional(readOnly = true)
    @Override
    public List<EmployeeDTO> getEmployesEligiblesResponsablesTables() {

        return employeeRepository
                .findAll()
                .stream()
                .map(dtotMapper::fromEmployee)
                .toList();
    }


}