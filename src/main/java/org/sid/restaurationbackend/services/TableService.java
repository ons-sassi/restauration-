package org.sid.restaurationbackend.services;


import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.enums.StatutTable;
import org.sid.restaurationbackend.exceptions.*;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

public interface TableService {
    TableRestaurantDTO saveTable(TableRestaurantDTO tableRestaurantDTO);
    TableRestaurantDTO updateTable(Long id ,TableRestaurantDTO tableRestaurantDTO) throws TableNotFoundException;
    void deleteTable(Long id) throws TableNotFoundException;
    TableRestaurantDTO getTable(Long id) throws TableNotFoundException;
    TableRestaurantDTO getTableByNumeroTable(Integer numero_table) throws TableNotFoundException;
    TableRestaurantDTO getTableByCodeQr(String code_qr) throws TableNotFoundException;
    TableRestaurantDTO getTableByUrlQr(String url_qr) throws TableNotFoundException;
    List<TableRestaurantDTO> getAllTable();
    List<TableRestaurantDTO> getTabByStatut(StatutTable statut);
    List<TableRestaurantDTO> getTabByRestaurant(RestaurantDTO restaurant) throws RestaurantNotFoundException;
    List<TableRestaurantDTO> getTabByServeurAttribue(EmployeeDTO employeeId) throws EmployeeNotFoundException;
    List<TableRestaurantDTO> getTabByGenerePar(EmployeeDTO employee) throws EmployeeNotFoundException;

    // =========================================================
    // MES TABLES (phase 3A)
    // =========================================================
    // Tables de l'employé actuellement authentifié (déduit du JWT,
    // jamais d'un identifiant fourni par le frontend).
    @Transactional(readOnly = true)
    List<TableRestaurantDTO> getTablesDuServeurConnecte() throws EmployeeNotFoundException;

    // =========================================================
    // AFFECTATION PERMANENTE D'UN RESPONSABLE (serveurAttribue)
    // =========================================================

    TableRestaurantDTO assignerServeurResponsable(Long tableId, Long employeeId)
            throws TableNotFoundException, EmployeeNotFoundException;

    TableRestaurantDTO retirerServeurResponsable(Long tableId)
            throws TableNotFoundException;

    @Transactional(readOnly = true)
    List<EmployeeDTO> getEmployesEligiblesResponsablesTables();


    TableRestaurantDTO activerQr(Long id)
            throws TableNotFoundException;

    TableRestaurantDTO desactiverQr(Long id)
            throws TableNotFoundException;

    @Transactional(readOnly = true)
    TableRestaurantDTO getTableByQrActif(
            Long id)
            throws TableNotFoundException;

    @Transactional(readOnly = true)
    List<TableRestaurantDTO> getTablesDisponiblesByRestaurant(
            RestaurantDTO restaurant)
            throws RestaurantNotFoundException;

    @Transactional(readOnly = true)
    List<TableRestaurantDTO> getTablesDisponibles();
}