package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Reservation;
import org.sid.restaurationbackend.entities.TableRestaurant;
import org.sid.restaurationbackend.enums.StatutReservation;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.ReservationNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.ReservationRepository;
import org.sid.restaurationbackend.repositories.TableRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final EmployeeService employeeService;
    private RestaurantMapper dtotMapper;
    private ReservationRepository reservationRepository;
    private TableRepository tableRepository;

    @Override
    public ReservationDTO saveReservation(ReservationDTO reservationDTO) {
        Reservation reservation = dtotMapper.fromReservationDTO(reservationDTO);
        Reservation savedReservation = reservationRepository.save(reservation);
        return dtotMapper.fromReservation(savedReservation);
    }

    @Override
    public ReservationDTO updateReservation(Long id, ReservationDTO reservationDTO) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        dtotMapper.updateReservationFromDto(reservationDTO, reservation);

        Reservation updatedReservation = reservationRepository.save(reservation);
        return dtotMapper.fromReservation(updatedReservation);
    }

    @Override
    public void deleteReservation(Long id) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
        reservationRepository.deleteById(id);
    }

    @Override
    public ReservationDTO getReservation(Long id) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
        return dtotMapper.fromReservation(reservation);
    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(dtotMapper::fromReservation)
                .toList();
    }

    @Override
    public List<ReservationDTO> getReservationsByClient(ClientAuthentifieDTO client) {
        return reservationRepository.findByClient(dtotMapper.fromClientAuthentifieDTO(client)).stream()
                .map(dtotMapper::fromReservation)
                .toList();
    }

    @Override
    public List<ReservationDTO> getReservationsByRestaurant(RestaurantDTO restaurant) {
        return reservationRepository.findByRestaurant(dtotMapper.fromRestaurantDTO(restaurant)).stream()
                .map(dtotMapper::fromReservation)
                .toList();
    }

    @Override
    public List<ReservationDTO> getReservationsByDateAndTime(LocalDate date, LocalTime time) {
        return reservationRepository.findByDateReservationAndHeureReservation(date,time).stream()
                .map(dtotMapper::fromReservation)
                .toList();
    }

    @Override
    public List<ReservationDTO> getReservationsByStatus(StatutReservation status) {
        return reservationRepository.findByStatut(status).stream()
                .map(dtotMapper::fromReservation)
                .toList();
    }

    @Override
    public List<ReservationDTO> getReservationsByEmploye(EmployeeDTO employee) {
        return reservationRepository.findByConfirmePar(dtotMapper.fromEmployeeDTO(employee)).stream()
                .map(dtotMapper::fromReservation)
                .toList();
    }

    @Override
    public List<ReservationDTO> getReservationsByTable(TableRestaurantDTO table) {
        return reservationRepository.findByTable(dtotMapper.fromTableRestaurantDTO(table)).stream()
                .map(dtotMapper::fromReservation)
                .toList();
    }

    @Override
    public List<ReservationDTO> getReservationsBetweenDates(
            LocalDate dateDebut,
            LocalDate dateFin) {

        return reservationRepository
                .findByDateReservationBetween(dateDebut, dateFin)
                .stream()
                .map(dtotMapper::fromReservation)
                .toList();
    }

    @Override
    public List<ReservationDTO> getReservationsByDate(LocalDate date) {

        return reservationRepository
                .findByDateReservation(date)
                .stream()
                .map(dtotMapper::fromReservation)
                .toList();
    }

    @Override
    public ReservationDTO annulerReservation(Long id)
            throws ReservationNotFoundException {

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

        reservation.setStatut(StatutReservation.ANNULEE);

        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return dtotMapper.fromReservation(updatedReservation);
    }

    @Override
    public ReservationDTO confirmerReservation(
            Long reservationId,
            Long employeeId,
            Long tableId)
            throws ReservationNotFoundException, EmployeeNotFoundException, TableNotFoundException {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

        // Le SUPERADMIN n'est pas un Employee (voir CurrentUserService) :
        // employeeId est alors null et la réservation est confirmée sans
        // employé attribué, plutôt que de tenter un lookup voué à échouer
        // avec "Employee not found".
        Employee employee = employeeId != null
                ? dtotMapper.fromEmployeeDTO(employeeService.getEmployee(employeeId))
                : null;

        // La table est désormais obligatoire pour confirmer (voir
        // ReservationController.confirmerReservation, qui vérifie déjà
        // qu'elle appartient au restaurant connecté).
        if (tableId == null) {
            throw new IllegalArgumentException(
                    "Veuillez assigner une table pour confirmer cette réservation."
            );
        }

        TableRestaurant table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException(
                        "Table introuvable avec l'id : " + tableId));

        reservation.setTable(table);
        reservation.setConfirmePar(employee);
        reservation.setStatut(StatutReservation.CONFIRMEE);

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return dtotMapper.fromReservation(savedReservation);
    }



}