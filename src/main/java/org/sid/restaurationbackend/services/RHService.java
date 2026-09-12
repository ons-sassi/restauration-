package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.enums.StatutPresence;
import org.sid.restaurationbackend.enums.StatutVersement;
import org.sid.restaurationbackend.exceptions.*;

import java.util.Date;
import java.util.List;

public interface RHService {
    PresenceDTO savePresence(PresenceDTO presenceDTO);
    PresenceDTO updatePresence(Long id, PresenceDTO presenceDTO) throws PresenceNotFoundException;
    void deletePresence(Long id) throws PresenceNotFoundException;
    PresenceDTO getPresence(Long id) throws PresenceNotFoundException;
    List<PresenceDTO> getAllPresences();


    List<PresenceDTO> getPresencesByEmployee(
            Long id) throws EmployeeNotFoundException;

    List<PresenceDTO> getPresencesByDateBetween(
            Date dateDebut,
            Date dateFin);

    List<PresenceDTO> getPresencesByEmployeeAndDateBetween(
            Long employeeId,
            Date dateDebut,
            Date dateFin) throws EmployeeNotFoundException;


    // =========================================================
    // PAGE "PRESENCE" — pointage par le responsable
    // =========================================================

    /*
     * Marque le statut (PRESENT / ABSENT / CONGE) d'un employé pour
     * un jour donné. Fait un upsert : si un enregistrement existe
     * déjà pour ce jour, il est mis à jour ; sinon, un nouveau
     * Presence est créé.
     */
    PresenceDTO marquerPresence(
            Long employeeId,
            Date date,
            StatutPresence statut) throws EmployeeNotFoundException;

    /*
     * Feuille de présence d'un jour donné : tous les employés, leur
     * statut pour ce jour (peut être absent/non renseigné) et leurs
     * compteurs d'absences pour le mois et l'année contenant cette
     * date.
     */
    List<FeuillePresenceDTO> getFeuillePresence(Date date);

    PenaliteDTO savePenalite(PenaliteDTO penaliteDTO);
    PenaliteDTO updatePenalite(Long id, PenaliteDTO penaliteDTO) throws PenaliteNotFoundException;
    void deletePenalite(Long id) throws PenaliteNotFoundException;
    PenaliteDTO getPenalite(Long id) throws PenaliteNotFoundException;
    List<PenaliteDTO> getAllPenalites();

    List<PenaliteDTO> getPenalitesByEmployee(
            Long id) throws EmployeeNotFoundException;

    List<PenaliteDTO> getPenalitesByDateBetween(
            Date dateDebut,
            Date dateFin);

    List<PenaliteDTO> getPenalitesByEmployeeAndDateBetween(
            Long employeeId,
            Date dateDebut,
            Date dateFin) throws EmployeeNotFoundException;

    VersementSalaireDTO saveVersement(VersementSalaireDTO versementDTO);
    VersementSalaireDTO updateVersement(Long id, VersementSalaireDTO versementDTO) throws VersementSalaireNotFoundException;
    void deleteVersement(Long id) throws VersementSalaireNotFoundException;
    VersementSalaireDTO getVersement(Long id) throws VersementSalaireNotFoundException;
    List<VersementSalaireDTO> getAllVersements();

    List<VersementSalaireDTO> getVersementsByEmployee(
            Long id) throws EmployeeNotFoundException;

    List<VersementSalaireDTO> getVersementsByStatut(
            StatutVersement statut);

    List<VersementSalaireDTO> getVersementsByPeriode(
            String periode);

    List<VersementSalaireDTO> getVersementsByEmployeeAndStatut(
            Long employeeId,
            StatutVersement statut) throws EmployeeNotFoundException;

    PerformanceDTO savePerformance(PerformanceDTO performanceDTO);
    PerformanceDTO updatePerformance(Long id, PerformanceDTO performanceDTO) throws PerformanceNotFoundException;
    void deletePerformance(Long id) throws PerformanceNotFoundException;
    PerformanceDTO getPerformance(Long id) throws PerformanceNotFoundException;
    List<PerformanceDTO> getAllPerformances();

    List<PerformanceDTO> getPerformancesByEmployee(
            Long id) throws EmployeeNotFoundException;

    List<PerformanceDTO> getPerformancesByPeriode(
            String periode);

    List<PerformanceDTO> getPerformancesByEmployeeAndPeriode(
            Long employeeId,
            String periode) throws EmployeeNotFoundException;
}
