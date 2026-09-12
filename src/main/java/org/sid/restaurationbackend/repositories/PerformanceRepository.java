package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {



    List<Performance> findByEmployee(Employee employee);

    List<Performance> findByPeriode(String periode);

    List<Performance> findByEmployeeAndPeriode(Employee employee, String periode);
}
