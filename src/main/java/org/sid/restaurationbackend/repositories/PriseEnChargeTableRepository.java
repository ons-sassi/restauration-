package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.PriseEnChargeTable;
import org.sid.restaurationbackend.entities.TableRestaurant;
import org.sid.restaurationbackend.enums.StatutPriseEnCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriseEnChargeTableRepository extends JpaRepository<PriseEnChargeTable, Long> {

    Optional<PriseEnChargeTable> findByTableAndStatut(
            TableRestaurant table,
            StatutPriseEnCharge statut
    );

    List<PriseEnChargeTable> findByEmployeeAndStatut(
            Employee employee,
            StatutPriseEnCharge statut
    );

    List<PriseEnChargeTable> findByTable(TableRestaurant table);

    List<PriseEnChargeTable> findByEmployee(Employee employee);

    long countByEmployeeAndStatut(
            Employee employee,
            StatutPriseEnCharge statut
    );
}
