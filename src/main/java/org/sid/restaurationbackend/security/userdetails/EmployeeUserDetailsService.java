package org.sid.restaurationbackend.security.userdetails;

import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.repositories.EmployeeRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public EmployeeUserDetailsService(
            EmployeeRepository employeeRepository
    ) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Employee employee = employeeRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Employé introuvable avec l'email : " + email
                        )
                );

        String roleName = employee.getRole()
                .getNom_role();

        return User.builder()
                .username(employee.getEmail())
                .password(employee.getMot_de_passe())
                .authorities(
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + roleName.toUpperCase()
                                )
                        )
                )
                .build();
    }
}