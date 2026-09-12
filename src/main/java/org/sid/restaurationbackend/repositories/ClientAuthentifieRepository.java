package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.BonDeCommandeStock;
import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Fournisseur;
import org.sid.restaurationbackend.enums.StatutBonCommande;
import org.sid.restaurationbackend.enums.StatutUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientAuthentifieRepository extends JpaRepository<ClientAuthentifie, Long> {
   
    Optional<ClientAuthentifie> findByEmail(String email);

    Collection<ClientAuthentifie> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelephoneContainingIgnoreCase(String keyword, String keyword1, String keyword2, String keyword3);

    Collection<ClientAuthentifie> findByStatut(StatutUtilisateur statut);
    Optional<ClientAuthentifie> findByCodeParrainage(String codeParrainage);
}
