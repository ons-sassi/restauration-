package org.sid.restaurationbackend.repositories;


import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.PointDeVente;
import org.sid.restaurationbackend.entities.SessionCaisse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionCaisseRepository extends JpaRepository<SessionCaisse, Long> {

    List<SessionCaisse> findByEmployee(Employee employee);

    List<SessionCaisse> findByPointDeVente(PointDeVente pointDeVente);

    List<SessionCaisse> findByDateOuvertureBetween(Date dateDebut, Date dateFin);

    Optional<SessionCaisse> findByPointDeVenteAndDateFermetureIsNull(PointDeVente pdv);

    boolean existsByPointDeVenteAndDateFermetureIsNull(PointDeVente pdv);
}
