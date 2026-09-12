package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Reduction;
import org.sid.restaurationbackend.enums.TypeReduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReductionRepository extends JpaRepository<Reduction, Long> {

    List<Reduction> findByType(TypeReduction type);

    List<Reduction> findByDateDebutBetween(Date dateDebut, Date dateFin);

    List<Reduction> findByDateDebutLessThanEqualAndDateFinGreaterThanEqual(Date maintenant, Date maintenant1);
}
        