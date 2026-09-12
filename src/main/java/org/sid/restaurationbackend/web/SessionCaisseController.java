package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.EmployeeDTO;
import org.sid.restaurationbackend.dtos.PointDeVenteDTO;
import org.sid.restaurationbackend.dtos.SessionCaisseDTO;
import org.sid.restaurationbackend.dtos.SessionCaisseResumeDTO;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.PointDeVenteNotFoundException;
import org.sid.restaurationbackend.exceptions.SessionCaisseNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.PointDeVenteService;
import org.sid.restaurationbackend.services.SessionCaisseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * SessionCaisse n'a PAS de colonne restaurant en base : son
 * restaurant est déterminé INDIRECTEMENT via son PointDeVente
 * (SessionCaisse.pointDeVente.restaurant). Toutes les méthodes
 * de ce contrôleur vérifient désormais ce restaurant indirect
 * avant de lire/écrire, ou filtrent les listes renvoyées sur
 * le restaurant de l'employé connecté.
 *
 * Un PointDeVenteDTO envoyé par le frontend n'est jamais pris
 * pour argent comptant : on recharge le vrai PointDeVente en
 * base via PointDeVenteService avant de vérifier son
 * restaurant, pour ne pas se faire piéger par un DTO forgé
 * (id correct mais restaurant modifié côté client, par ex.).
 */
@RestController
@RequestMapping("/api/sessionCaisses")
@AllArgsConstructor
public class SessionCaisseController {

    private final SessionCaisseService sessionCaisseService;
    private final PointDeVenteService pointDeVenteService;
    private final CurrentUserService currentUserService;


    /**
     * Créer une session de caisse.
     *
     * Le point de vente fourni doit appartenir au restaurant connecté.
     */
    @PostMapping
    public SessionCaisseDTO saveSessionCaisse(
            @RequestBody SessionCaisseDTO sessionCaisseDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException,
            PointDeVenteNotFoundException {

        verifierPdvDuDto(
                sessionCaisseDTO.getPointDeVente() != null
                        ? sessionCaisseDTO.getPointDeVente().getId_pdv()
                        : null
        );

        return sessionCaisseService.saveSessionCaisse(
                sessionCaisseDTO
        );
    }


    /**
     * Ouvrir une caisse.
     *
     * L'employé NE DOIT PAS être envoyé par Angular.
     *
     * Le backend doit récupérer l'employé connecté
     * à partir du JWT / SecurityContext.
     *
     * Le point de vente fourni doit appartenir au restaurant connecté
     * (avant : un employé pouvait ouvrir une caisse sur un PDV d'un
     * autre restaurant en changeant l'id envoyé).
     */
    @PostMapping("/ouvrir")
    public SessionCaisseDTO ouvrirCaisse(
            @RequestParam Double montantOuverture,
            @RequestBody PointDeVenteDTO pointDeVente)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException,
            PointDeVenteNotFoundException {

        verifierPdvDuDto(
                pointDeVente != null
                        ? pointDeVente.getId_pdv()
                        : null
        );

        return sessionCaisseService.ouvrirCaisse(
                montantOuverture,
                pointDeVente
        );
    }


    /**
     * Fermer une caisse.
     */
    @PutMapping("/{id}/fermer")
    public SessionCaisseDTO fermerCaisse(
            @PathVariable Long id,
            @RequestParam Double montantFermeture)
            throws SessionCaisseNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierSessionDuRestaurantConnecte(
                sessionCaisseService.getSessionCaisse(id)
        );

        return sessionCaisseService.fermerCaisse(
                id,
                montantFermeture
        );
    }


    /**
     * Supprimer une session.
     */
    @DeleteMapping("/{id}")
    public void deleteSessionCaisse(
            @PathVariable Long id)
            throws SessionCaisseNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierSessionDuRestaurantConnecte(
                sessionCaisseService.getSessionCaisse(id)
        );

        sessionCaisseService.deleteSessionCaisse(id);
    }


    /**
     * Récupérer une session.
     */
    @GetMapping("/{id}")
    public SessionCaisseDTO getSessionCaisse(
            @PathVariable Long id)
            throws SessionCaisseNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        SessionCaisseDTO session = sessionCaisseService.getSessionCaisse(id);

        verifierSessionDuRestaurantConnecte(session);

        return session;
    }


    /**
     * Récupérer toutes les sessions DU RESTAURANT CONNECTÉ.
     */
    @GetMapping
    public List<SessionCaisseDTO> getAllSessionCaisses()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return sessionCaisseService
                .getAllSessionCaisses()
                .stream()
                .filter(s -> estDuRestaurant(s, restaurantId))
                .toList();
    }


    /**
     * Récupérer les sessions d'un employé, DU RESTAURANT CONNECTÉ.
     *
     * Cette méthode peut rester pour l'administration.
     */
    @PostMapping("/employee")
    public List<SessionCaisseDTO> getSessionCaissesByEmployee(
            @RequestBody EmployeeDTO employee)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return sessionCaisseService
                .getSessionCaissesByEmployee(employee)
                .stream()
                .filter(s -> estDuRestaurant(s, restaurantId))
                .toList();
    }


    /**
     * Récupérer les sessions d'un point de vente.
     *
     * Le point de vente fourni doit appartenir au restaurant connecté.
     */
    @PostMapping("/pdv")
    public List<SessionCaisseDTO> getSessionCaissesByPointDeVente(
            @RequestBody PointDeVenteDTO pointDeVente)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException,
            PointDeVenteNotFoundException {

        verifierPdvDuDto(
                pointDeVente != null
                        ? pointDeVente.getId_pdv()
                        : null
        );

        return sessionCaisseService
                .getSessionCaissesByPointDeVente(
                        pointDeVente
                );
    }


    /**
     * Récupérer la session actuellement ouverte
     * pour un point de vente.
     *
     * Le point de vente fourni doit appartenir au restaurant connecté.
     */
    @PostMapping("/ouverte")
    public SessionCaisseDTO getSessionOuverte(
            @RequestBody PointDeVenteDTO pointDeVente)
            throws SessionCaisseNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException, PointDeVenteNotFoundException {

        verifierPdvDuDto(
                pointDeVente != null
                        ? pointDeVente.getId_pdv()
                        : null
        );

        return sessionCaisseService
                .getSessionOuverte(pointDeVente);
    }


    /**
     * Récupérer les sessions entre deux dates, DU RESTAURANT CONNECTÉ.
     */
    @GetMapping("/date")
    public List<SessionCaisseDTO> getSessionCaissesByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Date dateDebut,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Date dateFin)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return sessionCaisseService
                .getSessionCaissesByDate(
                        dateDebut,
                        dateFin
                )
                .stream()
                .filter(s -> estDuRestaurant(s, restaurantId))
                .toList();
    }


    /**
     * Résumé financier d'une session de caisse :
     *
     *   - montant total vendu pendant la session,
     *   - montant théorique attendu en caisse,
     *   - écart (surplus / manque) par rapport à un montant
     *     compté (optionnel : permet une prévisualisation en
     *     temps réel avant de confirmer la fermeture).
     */
    @GetMapping("/{id}/resume")
    public SessionCaisseResumeDTO getResumeSessionCaisse(
            @PathVariable Long id,
            @RequestParam(required = false) Double montantCompte)
            throws SessionCaisseNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierSessionDuRestaurantConnecte(
                sessionCaisseService.getSessionCaisse(id)
        );

        return sessionCaisseService
                .getResumeSessionCaisse(id, montantCompte);
    }


    // =========================================================
    // HELPERS INTERNES DE VÉRIFICATION RESTAURANT
    // =========================================================

    // Recharge le vrai PointDeVente en base (jamais celui envoyé par
    // le frontend) et vérifie que son restaurant est bien celui de
    // l'employé connecté.
    private void verifierPdvDuDto(Long pdvId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException,
            PointDeVenteNotFoundException {

        if (pdvId == null) {
            currentUserService.verifierAccesRestaurant(null);
            return;
        }

        PointDeVenteDTO pdvReel = pointDeVenteService.getPointDeVente(pdvId);

        currentUserService.verifierAccesRestaurant(
                pdvReel.getRestaurant() != null
                        ? pdvReel.getRestaurant().getId_restaurant()
                        : null
        );
    }


    // Vérifie qu'une session de caisse existante appartient (via son
    // point de vente) au restaurant de l'employé connecté.
    private void verifierSessionDuRestaurantConnecte(
            SessionCaisseDTO session)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                session.getPointDeVente() != null
                        && session.getPointDeVente().getRestaurant() != null
                        ? session.getPointDeVente().getRestaurant().getId_restaurant()
                        : null
        );
    }


    // Utilisé pour filtrer une liste de sessions sur le restaurant
    // connecté (via pointDeVente.restaurant), sans lever d'exception
    // pour chaque élément ignoré.
    private boolean estDuRestaurant(
            SessionCaisseDTO session,
            Long restaurantId) {

        return session.getPointDeVente() != null
                && session.getPointDeVente().getRestaurant() != null
                && restaurantId.equals(
                session.getPointDeVente().getRestaurant().getId_restaurant()
        );
    }
}