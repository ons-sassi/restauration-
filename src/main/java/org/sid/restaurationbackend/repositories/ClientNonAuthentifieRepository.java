package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.dtos.TableRestaurantDTO;
import org.sid.restaurationbackend.entities.ClientNonAuthentifie;
import org.sid.restaurationbackend.entities.Restaurant;
import org.sid.restaurationbackend.entities.TableRestaurant;
import org.sid.restaurationbackend.enums.OrigineSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClientNonAuthentifieRepository extends JpaRepository<ClientNonAuthentifie, Long> {
    List<ClientNonAuthentifie> findByRestaurant(Restaurant restaurant);

    Collection<ClientNonAuthentifie> findByTableScannee(TableRestaurant tableRestaurant);
    List<ClientNonAuthentifie> findByOrigineSession(OrigineSession origineSession);

    List<ClientNonAuthentifie> findByRestaurantAndTableScannee(
            Restaurant restaurant,
            TableRestaurant tableRestaurant
    );
}
