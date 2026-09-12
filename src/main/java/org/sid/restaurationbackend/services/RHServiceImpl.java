package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.enums.StatutPresence;
import org.sid.restaurationbackend.enums.StatutVersement;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class RHServiceImpl implements RHService {

    private final EmployeeService employeeService;
    private RestaurantMapper dtotMapper;

    private PresenceRepository presenceRepository;
    private PenaliteRepository penaliteRepository;
    private VersementSalaireRepository versementSalaireRepository;
    private PerformanceRepository performanceRepository;




    @Override
    public PresenceDTO savePresence(PresenceDTO presenceDTO) {

        Presence presence = dtotMapper.fromPresenceDTO(presenceDTO);

        Presence savedPresence = presenceRepository.save(presence);

        return dtotMapper.fromPresence(savedPresence);
    }

    @Override
    public PresenceDTO updatePresence(
            Long id,
            PresenceDTO presenceDTO)
            throws PresenceNotFoundException {

        Presence presence = presenceRepository.findById(id)
                .orElseThrow(() ->
                        new PresenceNotFoundException(
                                "Presence not found"));

        dtotMapper.updatePresenceFromDto(
                presenceDTO,
                presence);

        Presence updatedPresence =
                presenceRepository.save(presence);

        return dtotMapper.fromPresence(updatedPresence);
    }

    @Override
    public void deletePresence(Long id)
            throws PresenceNotFoundException {

        Presence presence = presenceRepository.findById(id)
                .orElseThrow(() ->
                        new PresenceNotFoundException(
                                "Presence not found"));

        presenceRepository.delete(presence);
    }

    @Override
    public PresenceDTO getPresence(Long id)
            throws PresenceNotFoundException {

        Presence presence = presenceRepository.findById(id)
                .orElseThrow(() ->
                        new PresenceNotFoundException(
                                "Presence not found"));

        return dtotMapper.fromPresence(presence);
    }

    @Override
    public List<PresenceDTO> getAllPresences() {

        return presenceRepository.findAll()
                .stream()
                .map(dtotMapper::fromPresence)
                .toList();
    }

    @Override
    public List<PresenceDTO> getPresencesByEmployee(
            Long id) throws EmployeeNotFoundException {
        EmployeeDTO employeeDTO=employeeService.getEmployee(id);

        return presenceRepository
                .findByEmployee(
                        dtotMapper.fromEmployeeDTO(employeeDTO))
                .stream()
                .map(dtotMapper::fromPresence)
                .toList();
    }





    @Override
    public List<PresenceDTO> getPresencesByDateBetween(
            Date dateDebut,
            Date dateFin) {

        return presenceRepository
                .findByDateBetween(dateDebut, dateFin)
                .stream()
                .map(dtotMapper::fromPresence)
                .toList();
    }


    @Override
    public List<PresenceDTO> getPresencesByEmployeeAndDateBetween(
            Long employeeId,
            Date dateDebut,
            Date dateFin) throws EmployeeNotFoundException {
        EmployeeDTO employeeDTO=employeeService.getEmployee(employeeId);

        return presenceRepository
                .findByEmployeeAndDateBetween(
                        dtotMapper.fromEmployeeDTO(employeeDTO),
                        dateDebut,
                        dateFin)
                .stream()
                .map(dtotMapper::fromPresence)
                .toList();
    }


    // =========================================================
    // PAGE "PRESENCE" — pointage par le responsable
    // =========================================================

    @Override
    public PresenceDTO marquerPresence(
            Long employeeId,
            Date date,
            StatutPresence statut) throws EmployeeNotFoundException {

        EmployeeDTO employeeDTO =
                employeeService.getEmployee(employeeId);

        Employee employee =
                dtotMapper.fromEmployeeDTO(employeeDTO);

        Date debutJour = debutDeJournee(date);
        Date finJour = finDeJournee(date);

        Presence presence = presenceRepository
                .findFirstByEmployeeAndDateBetween(
                        employee,
                        debutJour,
                        finJour)
                .orElseGet(() -> {
                    Presence nouvelle = new Presence();
                    nouvelle.setEmployee(employee);
                    nouvelle.setDate(debutJour);
                    return nouvelle;
                });

        presence.setStatut(statut);

        Presence savedPresence =
                presenceRepository.save(presence);

        return dtotMapper.fromPresence(savedPresence);
    }

    @Override
    public List<FeuillePresenceDTO> getFeuillePresence(Date date) {

        Date debutJour = debutDeJournee(date);
        Date finJour = finDeJournee(date);

        Date debutMois = debutDuMois(date);
        Date finMois = finDuMois(date);

        Date debutAnnee = debutDeLAnnee(date);
        Date finAnnee = finDeLAnnee(date);

        return employeeService.getAllEmployees()
                .stream()
                .map(employeeDTO -> {

                    Employee employee =
                            dtotMapper.fromEmployeeDTO(employeeDTO);

                    PresenceDTO presenceDuJour = presenceRepository
                            .findFirstByEmployeeAndDateBetween(
                                    employee,
                                    debutJour,
                                    finJour)
                            .map(dtotMapper::fromPresence)
                            .orElse(null);

                    long absencesMoisCourant = presenceRepository
                            .countByEmployeeAndStatutAndDateBetween(
                                    employee,
                                    StatutPresence.ABSENT,
                                    debutMois,
                                    finMois);

                    long absencesAnneeCourante = presenceRepository
                            .countByEmployeeAndStatutAndDateBetween(
                                    employee,
                                    StatutPresence.ABSENT,
                                    debutAnnee,
                                    finAnnee);

                    return new FeuillePresenceDTO(
                            employeeDTO,
                            presenceDuJour,
                            absencesMoisCourant,
                            absencesAnneeCourante);
                })
                .toList();
    }


    // ---------------------------------------------------------------
    // BORNES DE DATES (jour / mois / année contenant la date fournie)
    // ---------------------------------------------------------------

    private Date debutDeJournee(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private Date finDeJournee(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    private Date debutDuMois(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private Date finDuMois(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(
                Calendar.DAY_OF_MONTH,
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    private Date debutDeLAnnee(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private Date finDeLAnnee(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(
                Calendar.DAY_OF_YEAR,
                calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }



    @Override
    public PenaliteDTO savePenalite(PenaliteDTO penaliteDTO) {

        Penalite penalite =
                dtotMapper.fromPenaliteDTO(penaliteDTO);

        Penalite savedPenalite =
                penaliteRepository.save(penalite);

        return dtotMapper.fromPenalite(savedPenalite);
    }

    @Override
    public PenaliteDTO updatePenalite(
            Long id,
            PenaliteDTO penaliteDTO)
            throws PenaliteNotFoundException {

        Penalite penalite =
                penaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new PenaliteNotFoundException(
                                        "Penalite not found"));

        dtotMapper.updatePenaliteFromDto(
                penaliteDTO,
                penalite);

        Penalite updatedPenalite =
                penaliteRepository.save(penalite);

        return dtotMapper.fromPenalite(updatedPenalite);
    }

    @Override
    public void deletePenalite(Long id)
            throws PenaliteNotFoundException {

        Penalite penalite =
                penaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new PenaliteNotFoundException(
                                        "Penalite not found"));

        penaliteRepository.delete(penalite);
    }

    @Override
    public PenaliteDTO getPenalite(Long id)
            throws PenaliteNotFoundException {

        Penalite penalite =
                penaliteRepository.findById(id)
                        .orElseThrow(() ->
                                new PenaliteNotFoundException(
                                        "Penalite not found"));

        return dtotMapper.fromPenalite(penalite);
    }

    @Override
    public List<PenaliteDTO> getAllPenalites() {

        return penaliteRepository.findAll()
                .stream()
                .map(dtotMapper::fromPenalite)
                .toList();
    }

    @Override
    public List<PenaliteDTO> getPenalitesByEmployee(
            Long id) throws EmployeeNotFoundException {
        EmployeeDTO employeeDTO=employeeService.getEmployee(id);

        return penaliteRepository
                .findByEmployee(
                        dtotMapper.fromEmployeeDTO(employeeDTO))
                .stream()
                .map(dtotMapper::fromPenalite)
                .toList();
    }




    @Override
    public List<PenaliteDTO> getPenalitesByDateBetween(
            Date dateDebut,
            Date dateFin) {

        return penaliteRepository
                .findByDateBetween(dateDebut, dateFin)
                .stream()
                .map(dtotMapper::fromPenalite)
                .toList();
    }


    @Override
    public List<PenaliteDTO> getPenalitesByEmployeeAndDateBetween(
            Long employeeId,
            Date dateDebut,
            Date dateFin) throws EmployeeNotFoundException {
        EmployeeDTO employeeDTO=employeeService.getEmployee(employeeId);

        return penaliteRepository
                .findByEmployeeAndDateBetween(
                        dtotMapper.fromEmployeeDTO(employeeDTO) ,
                        dateDebut,
                        dateFin)
                .stream()
                .map(dtotMapper::fromPenalite)
                .toList();
    }



    @Override
    public VersementSalaireDTO saveVersement(
            VersementSalaireDTO versementDTO) {

        VersementSalaire versement =
                dtotMapper.fromVersementSalaireDTO(
                        versementDTO);

        VersementSalaire savedVersement =
                versementSalaireRepository.save(versement);

        return dtotMapper.fromVersementSalaire(savedVersement);
    }

    @Override
    public VersementSalaireDTO updateVersement(
            Long id,
            VersementSalaireDTO versementDTO)
            throws VersementSalaireNotFoundException {

        VersementSalaire versement =
                versementSalaireRepository.findById(id)
                        .orElseThrow(() ->
                                new VersementSalaireNotFoundException(
                                        "VersementSalaire not found"));

        dtotMapper.updateVersementSalaireFromDto(
                versementDTO,
                versement);

        VersementSalaire updatedVersement =
                versementSalaireRepository.save(versement);

        return dtotMapper.fromVersementSalaire(
                updatedVersement);
    }

    @Override
    public void deleteVersement(Long id)
            throws VersementSalaireNotFoundException {

        VersementSalaire versement =
                versementSalaireRepository.findById(id)
                        .orElseThrow(() ->
                                new VersementSalaireNotFoundException(
                                        "VersementSalaire not found"));

        versementSalaireRepository.delete(versement);
    }

    @Override
    public VersementSalaireDTO getVersement(Long id)
            throws VersementSalaireNotFoundException {

        VersementSalaire versement =
                versementSalaireRepository.findById(id)
                        .orElseThrow(() ->
                                new VersementSalaireNotFoundException(
                                        "VersementSalaire not found"));

        return dtotMapper.fromVersementSalaire(versement);
    }

    @Override
    public List<VersementSalaireDTO> getAllVersements() {

        return versementSalaireRepository.findAll()
                .stream()
                .map(dtotMapper::fromVersementSalaire)
                .toList();
    }

    @Override
    public List<VersementSalaireDTO> getVersementsByEmployee(
            Long id) throws EmployeeNotFoundException {
        EmployeeDTO employeeDTO=employeeService.getEmployee(id);

        return versementSalaireRepository
                .findByEmployee(
                        dtotMapper.fromEmployeeDTO(employeeDTO))
                .stream()
                .map(dtotMapper::fromVersementSalaire)
                .toList();
    }



    @Override
    public List<VersementSalaireDTO> getVersementsByStatut(
            StatutVersement statut) {

        return versementSalaireRepository
                .findByStatut(statut)
                .stream()
                .map(dtotMapper::fromVersementSalaire)
                .toList();
    }


    @Override
    public List<VersementSalaireDTO> getVersementsByPeriode(
            String periode) {

        return versementSalaireRepository
                .findByPeriode(periode)
                .stream()
                .map(dtotMapper::fromVersementSalaire)
                .toList();
    }


    @Override
    public List<VersementSalaireDTO> getVersementsByEmployeeAndStatut(
            Long employeeId,
            StatutVersement statut) throws EmployeeNotFoundException {
        EmployeeDTO employeeDTO=employeeService.getEmployee(employeeId);

        return versementSalaireRepository
                .findByEmployeeAndStatut(dtotMapper.fromEmployeeDTO(employeeDTO),
                        statut)
                .stream()
                .map(dtotMapper::fromVersementSalaire)
                .toList();
    }



    @Override
    public PerformanceDTO savePerformance(
            PerformanceDTO performanceDTO) {

        Performance performance =
                dtotMapper.fromPerformanceDTO(performanceDTO);

        Performance savedPerformance =
                performanceRepository.save(performance);

        return dtotMapper.fromPerformance(savedPerformance);
    }

    @Override
    public PerformanceDTO updatePerformance(
            Long id,
            PerformanceDTO performanceDTO)
            throws PerformanceNotFoundException {

        Performance performance =
                performanceRepository.findById(id)
                        .orElseThrow(() ->
                                new PerformanceNotFoundException(
                                        "Performance not found"));

        dtotMapper.updatePerformanceFromDto(
                performanceDTO,
                performance);

        Performance updatedPerformance =
                performanceRepository.save(performance);

        return dtotMapper.fromPerformance(
                updatedPerformance);
    }

    @Override
    public void deletePerformance(Long id)
            throws PerformanceNotFoundException {

        Performance performance =
                performanceRepository.findById(id)
                        .orElseThrow(() ->
                                new PerformanceNotFoundException(
                                        "Performance not found"));

        performanceRepository.delete(performance);
    }

    @Override
    public PerformanceDTO getPerformance(Long id)
            throws PerformanceNotFoundException {

        Performance performance =
                performanceRepository.findById(id)
                        .orElseThrow(() ->
                                new PerformanceNotFoundException(
                                        "Performance not found"));

        return dtotMapper.fromPerformance(performance);
    }

    @Override
    public List<PerformanceDTO> getAllPerformances() {

        return performanceRepository.findAll()
                .stream()
                .map(dtotMapper::fromPerformance)
                .toList();
    }

    @Override
    public List<PerformanceDTO> getPerformancesByEmployee(
            Long id) throws EmployeeNotFoundException {
        EmployeeDTO employeeDTO=employeeService.getEmployee(id);

        return performanceRepository
                .findByEmployee(
                        dtotMapper.fromEmployeeDTO(employeeDTO))
                .stream()
                .map(dtotMapper::fromPerformance)
                .toList();
    }




    @Override
    public List<PerformanceDTO> getPerformancesByPeriode(
            String periode) {

        return performanceRepository
                .findByPeriode(periode)
                .stream()
                .map(dtotMapper::fromPerformance)
                .toList();
    }


    @Override
    public List<PerformanceDTO> getPerformancesByEmployeeAndPeriode(
            Long employeeId,
            String periode) throws EmployeeNotFoundException {
        EmployeeDTO employeeDTO=employeeService.getEmployee(employeeId);

        return performanceRepository
                .findByEmployeeAndPeriode(
                        dtotMapper.fromEmployeeDTO(employeeDTO),
                        periode)
                .stream()
                .map(dtotMapper::fromPerformance)
                .toList();
    }
}