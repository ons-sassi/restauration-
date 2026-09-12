package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.VersementSalaire;
import org.sid.restaurationbackend.enums.StatutVersement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VersementSalaireRepository extends JpaRepository<VersementSalaire, Long> {

    List<VersementSalaire> findByPeriode(String periode);

    List<VersementSalaire> findByEmployee(Employee employee);
    List<VersementSalaire>findByStatut(StatutVersement statut);

    List<VersementSalaire> findByEmployeeAndStatut(Employee employee, StatutVersement statut);
}
