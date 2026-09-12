package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {


    Collection<Suggestion> findByClient(ClientAuthentifie client);

    Collection<Suggestion> findByDateCreationBetween(Date dateDebut, Date dateFin);

    Collection<Suggestion> findByContenuContainingIgnoreCase(String contenu);

    Collection<Suggestion> findByPriseEnCompte(Boolean priseEnCompte);
}
