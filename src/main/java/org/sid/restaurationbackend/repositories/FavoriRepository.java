package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Favori;
import org.sid.restaurationbackend.entities.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long> {

    List<Favori> findByClient(ClientAuthentifie client);

    Optional<Favori> findByClientAndProduit(ClientAuthentifie client, Produit produit);
}
