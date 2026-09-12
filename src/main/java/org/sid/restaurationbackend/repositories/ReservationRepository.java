package org.sid.restaurationbackend.repositories;

import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    List<Reservation> findByClient(ClientAuthentifie client);

    List<Reservation> findByRestaurant(Restaurant restaurant);

    List<Reservation> findByDateReservationAndHeureReservation(LocalDate localDate, LocalTime time);

    List<Reservation> findByHeureReservation(LocalTime localTime);

    List<Reservation> findByStatut(StatutReservation status);

    List<Reservation> findByConfirmePar(Employee employee);

    List<Reservation> findByTable(TableRestaurant table);

    List<Reservation> findByDateReservationBetween(LocalDate dateDebut, LocalDate dateFin);

    List<Reservation>findByDateReservation(LocalDate date);
}
