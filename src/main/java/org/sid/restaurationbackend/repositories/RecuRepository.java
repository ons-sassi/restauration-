package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.ModeleRecu;
import org.sid.restaurationbackend.entities.Recu;
import org.sid.restaurationbackend.entities.Vente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;


import java.util.Collection;
import java.util.Optional;

@Repository
public interface RecuRepository extends JpaRepository<Recu, Long> {


    Optional<Recu> findByVente(Vente vente);

    Collection<Recu> findByModele(ModeleRecu modelRecu);
}
