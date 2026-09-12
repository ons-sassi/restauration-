package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.dtos.EmployeeDTO;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


    Collection<Role> findByAttribuePar(Employee employee);
}
