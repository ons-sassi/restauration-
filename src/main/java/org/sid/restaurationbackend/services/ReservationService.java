package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.enums.StatutReservation;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.ReservationNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService {
    ReservationDTO saveReservation(ReservationDTO reservationDTO);
    ReservationDTO updateReservation(Long id, ReservationDTO reservationDTO) throws ReservationNotFoundException;
    void deleteReservation(Long id) throws ReservationNotFoundException;
    ReservationDTO getReservation(Long id) throws ReservationNotFoundException;
    List<ReservationDTO> getAllReservations();
    List<ReservationDTO> getReservationsByClient(ClientAuthentifieDTO client);
    List<ReservationDTO> getReservationsByRestaurant(RestaurantDTO restaurant);
    List<ReservationDTO> getReservationsByDateAndTime( LocalDate date, LocalTime time);

    List<ReservationDTO> getReservationsByStatus(StatutReservation status);
    List<ReservationDTO> getReservationsByEmploye(EmployeeDTO employeeDTO);
    List<ReservationDTO> getReservationsByTable(TableRestaurantDTO table);

    List<ReservationDTO> getReservationsBetweenDates(
            LocalDate dateDebut,
            LocalDate dateFin);

    List<ReservationDTO> getReservationsByDate(LocalDate date);

    ReservationDTO annulerReservation(Long id)
            throws ReservationNotFoundException;

    ReservationDTO confirmerReservation(
            Long reservationId,
            Long employeeId,
            Long tableId)
            throws ReservationNotFoundException, EmployeeNotFoundException, TableNotFoundException;
}