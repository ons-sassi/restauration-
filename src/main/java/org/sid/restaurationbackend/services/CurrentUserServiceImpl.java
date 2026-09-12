package org.sid.restaurationbackend.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Utilisateur;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.UtilisateurNotFoundException;
import org.sid.restaurationbackend.repositories.EmployeeRepository;
import org.sid.restaurationbackend.repositories.UtilisateurRepository;
import org.sid.restaurationbackend.security.jwt.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final EmployeeRepository employeeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    @Override
    public Employee getEmployeeConnecte() throws EmployeeNotFoundException {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();

        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employé introuvable avec l'email : " + email));
    }

    @Override
    public Utilisateur getUtilisateurConnecte() throws UtilisateurNotFoundException, EmployeeNotFoundException {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UtilisateurNotFoundException(
                        "Utilisateur introuvable avec l'email : " + email));
    }

    @Override
    public Long getRestaurantIdConnecte() throws EmployeeNotFoundException {
        Authentication authentication = getAuthentication();

        // Le SUPERADMIN n'est pas un Employee.
        // Son restaurant courant est porté par le JWT après sélection.
        boolean superAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPERADMIN".equalsIgnoreCase(a.getAuthority()));

        if (superAdmin) {
            String token = extractBearerToken();
            if (token == null) {
                throw new EmployeeNotFoundException("Token Bearer introuvable");
            }

            Long restaurantId = jwtUtil.extractRestaurantId(token);
            if (restaurantId == null) {
                throw new EmployeeNotFoundException(
                        "Aucun restaurant n'est sélectionné pour le Super Admin");
            }

            return restaurantId;
        }

        Employee employee = getEmployeeConnecte();

        if (employee.getRestaurant() == null) {
            throw new EmployeeNotFoundException(
                    "L'employé connecté (" + employee.getEmail()
                            + ") n'est rattaché à aucun restaurant");
        }

        return employee.getRestaurant().getId_restaurant();
    }

    @Override
    public boolean estRestaurantDeLEmployeConnecte(Long restaurantId)
            throws EmployeeNotFoundException {
        if (restaurantId == null) return false;
        return restaurantId.equals(getRestaurantIdConnecte());
    }

    @Override
    public void verifierAccesRestaurant(Long restaurantId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {
        Long restaurantIdConnecte = getRestaurantIdConnecte();

        if (restaurantId == null || !restaurantId.equals(restaurantIdConnecte)) {
            throw new AccesRestaurantNonAutoriseException(
                    "Accès refusé : le restaurant demandé (id=" + restaurantId
                            + ") ne correspond pas au restaurant sélectionné (id="
                            + restaurantIdConnecte + ")");
        }
    }

    private Authentication getAuthentication() throws EmployeeNotFoundException {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new EmployeeNotFoundException("Utilisateur non authentifié");
        }

        return authentication;
    }

    private String extractBearerToken() {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7).trim();
        return token.isBlank() ? null : token;
    }
}
