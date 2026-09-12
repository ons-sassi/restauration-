package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Commande;
import org.sid.restaurationbackend.entities.Reclamation;
import org.sid.restaurationbackend.enums.StatutReclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    List<Reclamation> findByClient(ClientAuthentifie client);

    List<Reclamation> findByCommande(Commande commande);

    List<Reclamation> findByStatut(StatutReclamation statut);
}
