package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;

import org.sid.restaurationbackend.entities.*;

import org.sid.restaurationbackend.enums.StatutPresence;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;
import java.util.List;
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {
    private RestaurantMapper dtotMapper;
    private EmployeeRepository employeeRepository;
    private PasswordEncoder passwordEncoder;
    private final RestaurantRepository restaurantRepository;


    @Override
    public EmployeeDTO saveEmployee(EmployeeDTO employeeDTO) {

        Employee employee = dtotMapper.fromEmployeeDTO(employeeDTO);

        // Encoder le mot de passe avant la sauvegarde
        if (employee.getMot_de_passe() != null &&
                !employee.getMot_de_passe().isBlank()) {

            employee.setMot_de_passe(
                    passwordEncoder.encode(
                            employee.getMot_de_passe()
                    )
            );
        }

        // Encoder le PIN avant la sauvegarde
        if (employee.getCodePin() != null &&
                !employee.getCodePin().isBlank()) {

            employee.setCodePin(
                    passwordEncoder.encode(
                            employee.getCodePin()
                    )
            );
        }
        employee.setDate_creation(new Date());

        if (employeeDTO.getRestaurant() == null
                || employeeDTO.getRestaurant().getId_restaurant() == null) {

            throw new IllegalArgumentException(
                    "Le restaurant de l'employé est obligatoire"
            );
        }

        Long restaurantId =
                employeeDTO
                        .getRestaurant()
                        .getId_restaurant();

        Restaurant restaurant =
                restaurantRepository
                        .findById(restaurantId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Restaurant introuvable : "
                                                + restaurantId
                                )
                        );

        employee.setRestaurant(restaurant);

        Employee savedEmployee =
                employeeRepository.save(employee);

        return dtotMapper.fromEmployee(savedEmployee);
    }

    @Override
    public EmployeeDTO updateEmployee(Long id ,EmployeeDTO employeeDTO) throws EmployeeNotFoundException {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        // Mémoriser le mot de passe et le code PIN actuellement stockés
        // (déjà hachés) AVANT le mapping, pour pouvoir détecter si la
        // valeur envoyée par le front est réellement nouvelle.
        String motDePasseActuel = employee.getMot_de_passe();
        String codePinActuel = employee.getCodePin();

        dtotMapper.updateEmployeeFromDto(employeeDTO, employee);

        // Le front renvoie soit le hash existant tel quel (champ non
        // modifié), soit un NOUVEAU mot de passe en clair saisi par
        // l'utilisateur. Si la valeur a changé par rapport au hash
        // stocké, c'est donc un mot de passe en clair : il faut le
        // hacher avant sauvegarde (sinon il est stocké en clair et
        // l'employé ne peut plus se connecter, ni avec l'ancien ni
        // avec le nouveau mot de passe). S'il est identique, on ne
        // le ré-encode pas (sinon on le hacherait deux fois).
        String motDePasseEnvoye = employee.getMot_de_passe();
        if (motDePasseEnvoye != null && !motDePasseEnvoye.isBlank()
                && !motDePasseEnvoye.equals(motDePasseActuel)) {

            employee.setMot_de_passe(passwordEncoder.encode(motDePasseEnvoye));
        }

        String codePinEnvoye = employee.getCodePin();
        if (codePinEnvoye != null && !codePinEnvoye.isBlank()
                && !codePinEnvoye.equals(codePinActuel)) {

            employee.setCodePin(passwordEncoder.encode(codePinEnvoye));
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return dtotMapper.fromEmployee(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) throws EmployeeNotFoundException {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        employeeRepository.delete(employee);
    }

    @Override
    public EmployeeDTO getEmployee(Long id) throws EmployeeNotFoundException {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return dtotMapper.fromEmployee(employee);
    }

    @Override
    public EmployeeDTO getEmployeeByMatricule(String matricule) throws EmployeeNotFoundException {
        Employee employee = employeeRepository.findByMatricule(matricule)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return dtotMapper.fromEmployee(employee);
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) throws EmployeeNotFoundException {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return dtotMapper.fromEmployee(employee);
    }



    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .filter(employee -> !isAdminRole(employee))
                .map(dtotMapper::fromEmployee)
                .toList();
    }

    /**
     * Un employé ayant le rôle "Admin" ne doit jamais apparaître
     * dans les listes d'employés du Back Office (il n'est pas un
     * employé "normal" mais le compte d'administration du système).
     */
    private boolean isAdminRole(Employee employee) {
        return employee.getRole() != null
                && employee.getRole().getNom_role() != null
                && "Admin".equalsIgnoreCase(employee.getRole().getNom_role());
    }

    @Override
    public List<EmployeeDTO> getEmployeesByRole(RoleDTO role) {
        return employeeRepository.findByRole(dtotMapper.fromRoleDTO(role)).stream()
                .map(dtotMapper::fromEmployee)
                .toList();
    }

    @Override
    public List<EmployeeDTO> getEmployeeByPdvAffecte(PointDeVenteDTO pdv) {
        return employeeRepository.findByPdvAffecte(dtotMapper.fromPointDeVenteDTO(pdv)).stream()
                .map(dtotMapper::fromEmployee)
                .toList();
    }

    @Override
    public EmployeeDTO getEmployeeByCodePin(String code) throws EmployeeNotFoundException {
        Employee employee = employeeRepository.findByCodePin(code)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return dtotMapper.fromEmployee(employee);
    }

    @Override
    public List<EmployeeDTO> searchEmployees(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllEmployees();
        }

        keyword = keyword.trim();

        return employeeRepository
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrEmailContainingIgnoreCaseOrMatriculeContainingIgnoreCaseOrTelephoneContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword,
                        keyword,
                        keyword
                )
                .stream()
                .filter(employee -> !isAdminRole(employee))
                .map(dtotMapper::fromEmployee)
                .toList();
    }
    @Override
    public List<EmployeeDTO> getEmployeesByStatutPresence(
            StatutPresence statutPresence) {

        return employeeRepository
                .findByStatutPresence(statutPresence)
                .stream()
                .map(dtotMapper::fromEmployee)
                .toList();
    }

    @Override
    public EmployeeDTO updateStatutPresence(
            Long id,
            StatutPresence statutPresence)
            throws EmployeeNotFoundException {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found"));

        employee.setStatutPresence(statutPresence);

        Employee updatedEmployee =
                employeeRepository.save(employee);

        return dtotMapper.fromEmployee(updatedEmployee);
    }


    // =========================================================
    // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
    // =========================================================

    @Override
    public EmployeeDTO updateEligibiliteAttributionAutomatique(
            Long id,
            Boolean eligible)
            throws EmployeeNotFoundException {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException("Employee not found"));

        employee.setEligibleAttributionAutomatique(
                Boolean.TRUE.equals(eligible));

        Employee updatedEmployee =
                employeeRepository.save(employee);

        return dtotMapper.fromEmployee(updatedEmployee);
    }


    @Override
    public List<EmployeeDTO> getEmployesEligiblesAttributionAutomatique() {

        return employeeRepository
                .findByEligibleAttributionAutomatiqueTrue()
                .stream()
                .map(dtotMapper::fromEmployee)
                .toList();
    }

}