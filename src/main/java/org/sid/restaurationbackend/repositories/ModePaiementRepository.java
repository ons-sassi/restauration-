package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.ModePaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ModePaiementRepository extends JpaRepository<ModePaiement, Long> {


    Collection<ModePaiement> findByClient (ClientAuthentifie clientAuthentifie);

    Collection<ModePaiement> findByActifTrue();


}
