package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.EmployeeDTO;
import org.sid.restaurationbackend.dtos.PointDeVenteDTO;
import org.sid.restaurationbackend.dtos.SessionCaisseDTO;
import org.sid.restaurationbackend.dtos.SessionCaisseResumeDTO;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.PointDeVente;
import org.sid.restaurationbackend.entities.SessionCaisse;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.SessionCaisseNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.EmployeeRepository;
import org.sid.restaurationbackend.repositories.SessionCaisseRepository;
import org.sid.restaurationbackend.repositories.VenteRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class SessionCaisseServiceImpl
        implements SessionCaisseService {


    private final RestaurantMapper dtoMapper;

    private final SessionCaisseRepository sessionCaisseRepository;

    private final VenteRepository venteRepository;

    /*
     * Permet de récupérer l'employé connecté
     * à partir de son email.
     */
    private final EmployeeRepository employeeRepository;


    // ============================================================
    // SAVE SESSION CAISSE
    // ============================================================

    @Override
    public SessionCaisseDTO saveSessionCaisse(
            SessionCaisseDTO sessionCaisseDTO) {

        SessionCaisse sessionCaisse =
                dtoMapper.fromSessionCaisseDTO(
                        sessionCaisseDTO
                );

        /*
         * Si aucune date d'ouverture n'est fournie,
         * on utilise la date actuelle.
         */
        if (sessionCaisse.getDateOuverture() == null) {
            sessionCaisse.setDateOuverture(new Date());
        }

        SessionCaisse savedSession =
                sessionCaisseRepository.save(
                        sessionCaisse
                );

        return dtoMapper.fromSessionCaisse(
                savedSession
        );
    }


    // ============================================================
    // OUVRIR CAISSE
    // ============================================================

    @Override
    public SessionCaisseDTO ouvrirCaisse(
            Double montantOuverture,
            PointDeVenteDTO pointDeVente) throws EmployeeNotFoundException {

        /*
         * --------------------------------------------------------
         * 1. Vérifier que l'utilisateur est authentifié
         * --------------------------------------------------------
         */

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Utilisateur non authentifié"
            );
        }


        /*
         * --------------------------------------------------------
         * 2. Récupérer l'identité de l'utilisateur connecté
         * --------------------------------------------------------
         *
         * authentication.getName()
         * correspond à l'identifiant utilisé par Spring Security.
         *
         * Dans ton application, nous utilisons l'email.
         */

        String email = authentication.getName();


        /*
         * --------------------------------------------------------
         * 3. Récupérer l'employé connecté
         * --------------------------------------------------------
         *
         * IMPORTANT :
         *
         * L'employé ne vient plus du frontend.
         *
         * Angular ne peut donc plus choisir un autre employé.
         */

        Employee employee =
                employeeRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(
                                        "Employee not found"
                                )
                        );


        /*
         * --------------------------------------------------------
         * 4. Vérifier le point de vente
         * --------------------------------------------------------
         */

        if (pointDeVente == null ||
                pointDeVente.getId_pdv() == null) {

            throw new IllegalArgumentException(
                    "Le point de vente est obligatoire"
            );
        }


        PointDeVente pdv =
                dtoMapper.fromPointDeVenteDTO(
                        pointDeVente
                );


        /*
         * --------------------------------------------------------
         * 5. Vérifier le montant d'ouverture
         * --------------------------------------------------------
         */

        if (montantOuverture == null ||
                montantOuverture < 0) {

            throw new IllegalArgumentException(
                    "Le montant d'ouverture doit être "
                            + "supérieur ou égal à 0"
            );
        }


        /*
         * --------------------------------------------------------
         * 6. Vérifier si une caisse est déjà ouverte
         *    pour ce point de vente
         * --------------------------------------------------------
         */

        boolean sessionOuverte =
                sessionCaisseRepository
                        .existsByPointDeVenteAndDateFermetureIsNull(
                                pdv
                        );

        if (sessionOuverte) {

            throw new IllegalStateException(
                    "Une session de caisse est déjà ouverte "
                            + "pour ce point de vente"
            );
        }


        /*
         * --------------------------------------------------------
         * 7. Créer une nouvelle session
         * --------------------------------------------------------
         */

        SessionCaisse sessionCaisse =
                new SessionCaisse();


        /*
         * Montant initial de la caisse.
         */

        sessionCaisse.setMontantOuverture(
                montantOuverture
        );


        /*
         * Date d'ouverture.
         */

        sessionCaisse.setDateOuverture(
                new Date()
        );


        /*
         * Une nouvelle caisse n'est pas encore fermée.
         */

        sessionCaisse.setMontantFermeture(
                null
        );

        sessionCaisse.setDateFermeture(
                null
        );

        sessionCaisse.setEcartCaisse(
                null
        );


        /*
         * --------------------------------------------------------
         * 8. Affecter le point de vente
         * --------------------------------------------------------
         */

        sessionCaisse.setPointDeVente(
                pdv
        );


        /*
         * --------------------------------------------------------
         * 9. Affecter l'employé connecté
         * --------------------------------------------------------
         */

        sessionCaisse.setEmployee(
                employee
        );


        /*
         * --------------------------------------------------------
         * 10. Sauvegarder
         * --------------------------------------------------------
         */

        SessionCaisse savedSession =
                sessionCaisseRepository.save(
                        sessionCaisse
                );


        log.info(
                "Ouverture caisse - Employé: {}, PDV: {}, Montant: {}",
                employee.getId_utilisateur(),
                pdv.getId_pdv(),
                montantOuverture
        );


        /*
         * --------------------------------------------------------
         * 11. Retourner le DTO
         * --------------------------------------------------------
         */

        return dtoMapper.fromSessionCaisse(
                savedSession
        );
    }


    // ============================================================
    // FERMER CAISSE
    // ============================================================

    @Override
    public SessionCaisseDTO fermerCaisse(
            Long id,
            Double montantFermeture)
            throws SessionCaisseNotFoundException {

        /*
         * --------------------------------------------------------
         * 1. Chercher la session
         * --------------------------------------------------------
         */

        SessionCaisse sessionCaisse =
                sessionCaisseRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new SessionCaisseNotFoundException(
                                        "SessionCaisse not found"
                                )
                        );


        /*
         * --------------------------------------------------------
         * 2. Vérifier que la caisse n'est pas déjà fermée
         * --------------------------------------------------------
         */

        if (sessionCaisse.getDateFermeture() != null) {

            throw new IllegalStateException(
                    "Cette session de caisse est déjà fermée"
            );
        }


        /*
         * --------------------------------------------------------
         * 3. Vérifier le montant de fermeture
         * --------------------------------------------------------
         */

        if (montantFermeture == null ||
                montantFermeture < 0) {

            throw new IllegalArgumentException(
                    "Le montant de fermeture doit être "
                            + "supérieur ou égal à 0"
            );
        }


        /*
         * --------------------------------------------------------
         * 4. Vérifier le PDV
         * --------------------------------------------------------
         */

        if (sessionCaisse.getPointDeVente() == null) {

            throw new IllegalStateException(
                    "Le point de vente de la session est obligatoire"
            );
        }


        /*
         * --------------------------------------------------------
         * 5. Vérifier la date d'ouverture
         * --------------------------------------------------------
         */

        if (sessionCaisse.getDateOuverture() == null) {

            throw new IllegalStateException(
                    "La date d'ouverture de la session est obligatoire"
            );
        }


        /*
         * --------------------------------------------------------
         * 6. Date réelle de fermeture
         * --------------------------------------------------------
         */

        Date dateFermeture = new Date();


        sessionCaisse.setMontantFermeture(
                montantFermeture
        );

        sessionCaisse.setDateFermeture(
                dateFermeture
        );


        /*
         * --------------------------------------------------------
         * 7. Calculer le total des ventes
         * --------------------------------------------------------
         *
         * Ce calcul doit rester dans le backend.
         */

        Double totalVentes =
                venteRepository.calculerTotalVentes(
                        sessionCaisse.getPointDeVente(),
                        sessionCaisse.getDateOuverture(),
                        dateFermeture
                );


        if (totalVentes == null) {
            totalVentes = 0.0;
        }


        /*
         * --------------------------------------------------------
         * 8. Récupérer le montant d'ouverture
         * --------------------------------------------------------
         */

        Double montantOuverture =
                sessionCaisse.getMontantOuverture();

        if (montantOuverture == null) {
            montantOuverture = 0.0;
        }


        /*
         * --------------------------------------------------------
         * 9. Calcul du montant théorique
         * --------------------------------------------------------
         */

        Double montantTheorique =
                montantOuverture + totalVentes;


        /*
         * --------------------------------------------------------
         * 10. Calcul de l'écart
         * --------------------------------------------------------
         */

        Double ecart =
                montantFermeture - montantTheorique;


        sessionCaisse.setEcartCaisse(
                ecart
        );


        /*
         * --------------------------------------------------------
         * 11. Sauvegarder
         * --------------------------------------------------------
         */

        SessionCaisse updatedSession =
                sessionCaisseRepository.save(
                        sessionCaisse
                );


        log.info(
                "Fermeture caisse - Session: {}, PDV: {}, "
                        + "Montant ouverture: {}, Total ventes: {}, "
                        + "Montant théorique: {}, Montant fermeture: {}, "
                        + "Écart: {}",

                sessionCaisse.getId_session_caisse(),

                sessionCaisse.getPointDeVente()
                        .getId_pdv(),

                montantOuverture,

                totalVentes,

                montantTheorique,

                montantFermeture,

                ecart
        );


        return dtoMapper.fromSessionCaisse(
                updatedSession
        );
    }


    // ============================================================
    // DELETE
    // ============================================================

    @Override
    public void deleteSessionCaisse(Long id)
            throws SessionCaisseNotFoundException {

        SessionCaisse sessionCaisse =
                sessionCaisseRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new SessionCaisseNotFoundException(
                                        "SessionCaisse not found"
                                )
                        );


        sessionCaisseRepository.delete(
                sessionCaisse
        );
    }


    // ============================================================
    // GET SESSION BY ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public SessionCaisseDTO getSessionCaisse(
            Long id)
            throws SessionCaisseNotFoundException {

        SessionCaisse sessionCaisse =
                sessionCaisseRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new SessionCaisseNotFoundException(
                                        "SessionCaisse not found"
                                )
                        );


        return dtoMapper.fromSessionCaisse(
                sessionCaisse
        );
    }


    // ============================================================
    // GET ALL
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<SessionCaisseDTO> getAllSessionCaisses() {

        return sessionCaisseRepository
                .findAll()
                .stream()
                .map(dtoMapper::fromSessionCaisse)
                .toList();
    }


    // ============================================================
    // GET BY EMPLOYEE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<SessionCaisseDTO> getSessionCaissesByEmployee(
            EmployeeDTO employee) {

        Employee employeeEntity =
                dtoMapper.fromEmployeeDTO(
                        employee
                );


        return sessionCaisseRepository
                .findByEmployee(employeeEntity)
                .stream()
                .map(dtoMapper::fromSessionCaisse)
                .toList();
    }


    // ============================================================
    // GET BY POINT DE VENTE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<SessionCaisseDTO> getSessionCaissesByPointDeVente(
            PointDeVenteDTO pointDeVente) {

        PointDeVente pdv =
                dtoMapper.fromPointDeVenteDTO(
                        pointDeVente
                );


        return sessionCaisseRepository
                .findByPointDeVente(pdv)
                .stream()
                .map(dtoMapper::fromSessionCaisse)
                .toList();
    }


    // ============================================================
    // GET SESSION OUVERTE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public SessionCaisseDTO getSessionOuverte(
            PointDeVenteDTO pointDeVente)
            throws SessionCaisseNotFoundException {

        PointDeVente pdv =
                dtoMapper.fromPointDeVenteDTO(
                        pointDeVente
                );


        SessionCaisse sessionCaisse =
                sessionCaisseRepository
                        .findByPointDeVenteAndDateFermetureIsNull(
                                pdv
                        )
                        .orElseThrow(() ->
                                new SessionCaisseNotFoundException(
                                        "Aucune session de caisse "
                                                + "ouverte pour ce point de vente"
                                )
                        );


        return dtoMapper.fromSessionCaisse(
                sessionCaisse
        );
    }


    // ============================================================
    // GET BY DATE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<SessionCaisseDTO> getSessionCaissesByDate(
            Date dateDebut,
            Date dateFin) {

        return sessionCaisseRepository
                .findByDateOuvertureBetween(
                        dateDebut,
                        dateFin
                )
                .stream()
                .map(dtoMapper::fromSessionCaisse)
                .toList();
    }


    // ============================================================
    // RESUME SESSION CAISSE (total ventes + écart)
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public SessionCaisseResumeDTO getResumeSessionCaisse(
            Long id,
            Double montantCompte)
            throws SessionCaisseNotFoundException {

        /*
         * --------------------------------------------------------
         * 1. Chercher la session
         * --------------------------------------------------------
         */

        SessionCaisse sessionCaisse =
                sessionCaisseRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new SessionCaisseNotFoundException(
                                        "SessionCaisse not found"
                                )
                        );


        /*
         * --------------------------------------------------------
         * 2. Déterminer la borne de fin du calcul
         * --------------------------------------------------------
         *
         * Si la session est déjà fermée, on utilise sa date de
         * fermeture réelle. Sinon, on calcule les ventes jusqu'à
         * l'instant présent (prévisualisation en temps réel).
         */

        boolean sessionOuverte =
                sessionCaisse.getDateFermeture() == null;

        Date dateFinCalcul =
                sessionOuverte
                        ? new Date()
                        : sessionCaisse.getDateFermeture();


        /*
         * --------------------------------------------------------
         * 3. Calculer le total des ventes de la session
         * --------------------------------------------------------
         */

        Double totalVentes =
                venteRepository.calculerTotalVentes(
                        sessionCaisse.getPointDeVente(),
                        sessionCaisse.getDateOuverture(),
                        dateFinCalcul
                );

        if (totalVentes == null) {
            totalVentes = 0.0;
        }


        /*
         * --------------------------------------------------------
         * 4. Montant théorique attendu en caisse
         * --------------------------------------------------------
         */

        Double montantOuverture =
                sessionCaisse.getMontantOuverture();

        if (montantOuverture == null) {
            montantOuverture = 0.0;
        }

        Double montantTheorique =
                montantOuverture + totalVentes;


        /*
         * --------------------------------------------------------
         * 5. Écart par rapport au montant compté
         * --------------------------------------------------------
         *
         * Si aucun montant compté n'est fourni en paramètre, on
         * réutilise le montant de fermeture déjà enregistré
         * (session fermée) le cas échéant.
         */

        Double montantCompteResolu =
                montantCompte != null
                        ? montantCompte
                        : sessionCaisse.getMontantFermeture();

        Double ecart =
                montantCompteResolu != null
                        ? montantCompteResolu - montantTheorique
                        : null;


        /*
         * --------------------------------------------------------
         * 6. Construire le DTO
         * --------------------------------------------------------
         */

        SessionCaisseResumeDTO resume =
                new SessionCaisseResumeDTO();

        resume.setId_session_caisse(
                sessionCaisse.getId_session_caisse()
        );

        resume.setMontantOuverture(montantOuverture);
        resume.setTotalVentes(totalVentes);
        resume.setMontantTheorique(montantTheorique);
        resume.setMontantCompte(montantCompteResolu);
        resume.setEcart(ecart);
        resume.setDateOuverture(sessionCaisse.getDateOuverture());
        resume.setDateFermeture(sessionCaisse.getDateFermeture());
        resume.setSessionOuverte(sessionOuverte);

        return resume;
    }
}