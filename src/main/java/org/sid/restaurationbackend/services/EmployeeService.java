package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.EmployeeDTO;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.Reservation;
import org.sid.restaurationbackend.entities.RoleFonctionnalite;
import org.sid.restaurationbackend.entities.VersementSalaire;
import org.sid.restaurationbackend.enums.StatutPresence;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;

import java.util.List;

public interface EmployeeService {
    EmployeeDTO saveEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO updateEmployee(Long id ,EmployeeDTO employeeDTO) throws EmployeeNotFoundException;
    void deleteEmployee(Long id) throws EmployeeNotFoundException;
    EmployeeDTO getEmployee(Long id) throws EmployeeNotFoundException;
    EmployeeDTO getEmployeeByMatricule(String matricule) throws EmployeeNotFoundException;
    EmployeeDTO getEmployeeByEmail(String email) throws EmployeeNotFoundException;
    List<EmployeeDTO> searchEmployees(String keyword);

    List<EmployeeDTO> getEmployeesByStatutPresence(
            StatutPresence statutPresence);

    EmployeeDTO updateStatutPresence(
            Long id,
            StatutPresence statutPresence)
            throws EmployeeNotFoundException;

    List<EmployeeDTO> getAllEmployees();



    List<EmployeeDTO> getEmployeesByRole(RoleDTO role);

    List<EmployeeDTO> getEmployeeByPdvAffecte(PointDeVenteDTO pdv);

    EmployeeDTO getEmployeeByCodePin(String code) throws EmployeeNotFoundException;


    // =========================================================
    // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
    // =========================================================

    /*
     * Active/désactive l'éligibilité d'un employé à recevoir une
     * prise en charge (automatique ou manuelle) — §5/§26 de la spec.
     * Réservé au responsable (contrôlé côté controller, comme
     * updateStatutPresence n'est pas restreint mais celle-ci le sera
     * via @PreAuthorize côté EmployeeController).
     */
    EmployeeDTO updateEligibiliteAttributionAutomatique(
            Long id,
            Boolean eligible)
            throws EmployeeNotFoundException;

    /*
     * Liste des employés actuellement éligibles, pour la case à
     * cocher du responsable (§5) et pour les écrans de statistiques.
     */
    List<EmployeeDTO> getEmployesEligiblesAttributionAutomatique();

}
