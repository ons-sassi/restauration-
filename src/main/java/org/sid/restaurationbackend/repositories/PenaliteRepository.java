package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Penalite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface PenaliteRepository extends JpaRepository<Penalite, Long> {


    List<Penalite> findByEmployee(Employee employee);

    List<Penalite> findByDateBetween(Date dateDebut, Date dateFin);

    Collection<Penalite> findByEmployeeAndDateBetween(Employee employee, Date dateAfter, Date dateAfter1);
}
