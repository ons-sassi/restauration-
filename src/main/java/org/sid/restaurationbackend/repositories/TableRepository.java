package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Restaurant;
import org.sid.restaurationbackend.entities.TableRestaurant;
import org.sid.restaurationbackend.enums.StatutTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<TableRestaurant, Long> {
    List<TableRestaurant> findByRestaurant(Restaurant restaurant);

    List<TableRestaurant> findByStatut(StatutTable statut);

    Optional<TableRestaurant> findByNumeroTable(Integer numeroTable);

    // Ajouté à l'étape 5 (panier client, mode SAISIE_MANUELLE_NUMERO_TABLE) :
    // findByNumeroTable seul n'est pas fiable, un même numéro de table
    // pouvant exister dans plusieurs restaurants différents.
    Optional<TableRestaurant> findByRestaurantAndNumeroTable(Restaurant restaurant, Integer numeroTable);

    Optional<TableRestaurant> findByCodeQr(String codeQr);

    Optional<TableRestaurant> findByUrlQr(String urlQr);

    List<TableRestaurant> findByServeurAttribue(Employee employee);

    List<TableRestaurant> findByGenerePar(Employee employee);

    List<TableRestaurant> findByRestaurantAndStatut(Restaurant restaurantEntity, StatutTable statutTable);

}
