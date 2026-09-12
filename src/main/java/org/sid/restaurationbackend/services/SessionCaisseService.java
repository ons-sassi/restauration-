package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.EmployeeDTO;
import org.sid.restaurationbackend.dtos.PointDeVenteDTO;
import org.sid.restaurationbackend.dtos.PresenceDTO;
import org.sid.restaurationbackend.dtos.SessionCaisseDTO;
import org.sid.restaurationbackend.dtos.SessionCaisseResumeDTO;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.SessionCaisseNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface SessionCaisseService {
    SessionCaisseDTO saveSessionCaisse(SessionCaisseDTO sessionCaisseDTO);

    SessionCaisseDTO ouvrirCaisse(
            Double montantOuverture,
            PointDeVenteDTO pointDeVente) throws EmployeeNotFoundException;

    SessionCaisseDTO fermerCaisse(
            Long id,
            Double montantFermeture)
            throws SessionCaisseNotFoundException;


    void deleteSessionCaisse(Long id) throws SessionCaisseNotFoundException;
    SessionCaisseDTO getSessionCaisse(Long id) throws SessionCaisseNotFoundException;
    List<SessionCaisseDTO> getAllSessionCaisses();
    List<SessionCaisseDTO> getSessionCaissesByEmployee(EmployeeDTO employee);
    List<SessionCaisseDTO> getSessionCaissesByPointDeVente (PointDeVenteDTO pointDeVenteDTO);

    @Transactional(readOnly = true)
    SessionCaisseDTO getSessionOuverte(
            PointDeVenteDTO pointDeVente)
            throws SessionCaisseNotFoundException;

    @Transactional(readOnly = true)
    List<SessionCaisseDTO> getSessionCaissesByDate(
            Date dateDebut,
            Date dateFin);

    /**
     * Calcule le résumé financier d'une session de caisse :
     * total des ventes réalisées, montant théorique attendu,
     * et écart par rapport à un montant compté optionnel.
     *
     * @param id             identifiant de la session de caisse
     * @param montantCompte  montant réellement compté en caisse
     *                       (optionnel, peut être null pour une
     *                       simple prévisualisation avant saisie)
     */
    @Transactional(readOnly = true)
    SessionCaisseResumeDTO getResumeSessionCaisse(
            Long id,
            Double montantCompte)
            throws SessionCaisseNotFoundException;
}