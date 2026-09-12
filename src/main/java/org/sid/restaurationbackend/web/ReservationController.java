package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.enums.StatutReservation;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.ReservationNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.ReservationService;
import org.sid.restaurationbackend.services.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * Reservation est rattachée directement à un restaurant
 * (Reservation.restaurant). Toutes les méthodes de ce
 * contrôleur vérifient désormais que ce restaurant est bien
 * celui de l'employé connecté, au lieu de faire confiance à un
 * restaurantId fourni par le frontend, ou de renvoyer les
 * réservations de tous les restaurants confondus.
 *
 * Les recherches par client/table/employé/date/statut ne
 * prennent pas de restaurantId en paramètre côté service : le
 * filtrage est fait ici, en mémoire, sur le résultat — même
 * approche que ProduitController/EmployeeController.
 */
@RestController
@RequestMapping("/api/reservations")
@AllArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final CurrentUserService currentUserService;
    private final TableService tableService;

    // Même pattern que ReductionController : le SUPERADMIN n'est pas un
    // Employee (voir CurrentUserService), donc aucun id d'employé ne peut
    // lui être associé.
    private boolean estSuperAdminConnecte() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_SUPERADMIN".equals(
                                authority.getAuthority()
                        )
                );
    }


    // =========================
    // CREATE
    // =========================

    // Le restaurant fourni dans le DTO doit être celui de l'employé
    // connecté.
    @PostMapping
    public ResponseEntity<ReservationDTO> saveReservation(
            @RequestBody ReservationDTO reservationDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                reservationDTO.getRestaurant() != null
                        ? reservationDTO.getRestaurant().getId_restaurant()
                        : null
        );

        ReservationDTO savedReservation =
                reservationService.saveReservation(reservationDTO);

        return new ResponseEntity<>(
                savedReservation,
                HttpStatus.CREATED
        );
    }


    // Récupérer une réservation par ID
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservation(
            @PathVariable Long id)
            throws ReservationNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReservationDTO reservation = reservationService.getReservation(id);

        currentUserService.verifierAccesRestaurant(
                reservation.getRestaurant() != null
                        ? reservation.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(reservation);
    }


    // Récupérer toutes les réservations DU RESTAURANT CONNECTÉ
    @GetMapping
    public ResponseEntity<List<ReservationDTO>> getAllReservations()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ReservationDTO> reservations = reservationService.getAllReservations()
                .stream()
                .filter(r -> r.getRestaurant() != null
                        && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(reservations);
    }


    // Modifier une réservation
    //
    // La réservation existante doit appartenir au restaurant connecté,
    // et on ne doit pas pouvoir la "déplacer" vers un autre restaurant.
    @PutMapping("/{id}")
    public ResponseEntity<ReservationDTO> updateReservation(
            @PathVariable Long id,
            @RequestBody ReservationDTO reservationDTO)
            throws ReservationNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReservationDTO existante = reservationService.getReservation(id);
        currentUserService.verifierAccesRestaurant(
                existante.getRestaurant() != null
                        ? existante.getRestaurant().getId_restaurant()
                        : null
        );

        if (reservationDTO.getRestaurant() != null) {
            currentUserService.verifierAccesRestaurant(
                    reservationDTO.getRestaurant().getId_restaurant()
            );
        }

        return ResponseEntity.ok(
                reservationService.updateReservation(
                        id,
                        reservationDTO
                )
        );
    }


    // Supprimer une réservation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id)
            throws ReservationNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReservationDTO existante = reservationService.getReservation(id);
        currentUserService.verifierAccesRestaurant(
                existante.getRestaurant() != null
                        ? existante.getRestaurant().getId_restaurant()
                        : null
        );

        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }


    // Réservations par client, DU RESTAURANT CONNECTÉ
    @PostMapping("/client")
    public ResponseEntity<List<ReservationDTO>>
    getReservationsByClient(
            @RequestBody ClientAuthentifieDTO client)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ReservationDTO> reservations = reservationService
                .getReservationsByClient(client)
                .stream()
                .filter(r -> r.getRestaurant() != null
                        && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(reservations);
    }


    // Réservations par restaurant
    //
    // Le restaurant demandé DOIT être celui de l'employé connecté.
    @PostMapping("/restaurant")
    public ResponseEntity<List<ReservationDTO>>
    getReservationsByRestaurant(
            @RequestBody RestaurantDTO restaurant)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                restaurant != null
                        ? restaurant.getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                reservationService.getReservationsByRestaurant(restaurant)
        );
    }


    // Réservations par table, DU RESTAURANT CONNECTÉ
    @PostMapping("/table")
    public ResponseEntity<List<ReservationDTO>>
    getReservationsByTable(
            @RequestBody TableRestaurantDTO table)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ReservationDTO> reservations = reservationService
                .getReservationsByTable(table)
                .stream()
                .filter(r -> r.getRestaurant() != null
                        && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(reservations);
    }


    // Réservations par employé, DU RESTAURANT CONNECTÉ
    @PostMapping("/employee")
    public ResponseEntity<List<ReservationDTO>>
    getReservationsByEmploye(
            @RequestBody EmployeeDTO employee)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ReservationDTO> reservations = reservationService
                .getReservationsByEmploye(employee)
                .stream()
                .filter(r -> r.getRestaurant() != null
                        && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(reservations);
    }


    // Réservations par date, DU RESTAURANT CONNECTÉ
    @GetMapping("/date")
    public ResponseEntity<List<ReservationDTO>>
    getReservationsByDate(
            @RequestParam LocalDate date)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ReservationDTO> reservations = reservationService
                .getReservationsByDate(date)
                .stream()
                .filter(r -> r.getRestaurant() != null
                        && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(reservations);
    }


    // Réservations par date et heure, DU RESTAURANT CONNECTÉ
    @GetMapping("/date-time")
    public ResponseEntity<List<ReservationDTO>>
    getReservationsByDateAndTime(
            @RequestParam LocalDate date,
            @RequestParam LocalTime time)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ReservationDTO> reservations = reservationService
                .getReservationsByDateAndTime(
                        date,
                        time
                )
                .stream()
                .filter(r -> r.getRestaurant() != null
                        && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(reservations);
    }


    // Réservations entre deux dates, DU RESTAURANT CONNECTÉ
    @GetMapping("/between-dates")
    public ResponseEntity<List<ReservationDTO>>
    getReservationsBetweenDates(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ReservationDTO> reservations = reservationService
                .getReservationsBetweenDates(
                        dateDebut,
                        dateFin
                )
                .stream()
                .filter(r -> r.getRestaurant() != null
                        && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(reservations);
    }


    // Réservations par statut, DU RESTAURANT CONNECTÉ
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<ReservationDTO>>
    getReservationsByStatus(
            @PathVariable StatutReservation statut)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        List<ReservationDTO> reservations = reservationService
                .getReservationsByStatus(statut)
                .stream()
                .filter(r -> r.getRestaurant() != null
                        && restaurantId.equals(r.getRestaurant().getId_restaurant()))
                .toList();

        return ResponseEntity.ok(reservations);
    }


    // Annuler une réservation
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<ReservationDTO>
    annulerReservation(
            @PathVariable Long id)
            throws ReservationNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReservationDTO existante = reservationService.getReservation(id);
        currentUserService.verifierAccesRestaurant(
                existante.getRestaurant() != null
                        ? existante.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                reservationService.annulerReservation(id)
        );
    }


    // Confirmer une réservation
    //
    // L'employé qui confirme n'est plus fourni par le frontend (voir
    // CORRECTIF ci-dessous) : il est déterminé côté serveur, comme le
    // restaurant connecté (voir CurrentUserService). La réservation ET
    // l'employé doivent tous les deux appartenir au restaurant connecté.
    // Une table doit désormais être assignée pour confirmer (obligatoire,
    // voir ConfirmerReservationRequestDTO) — elle aussi doit appartenir
    // au restaurant connecté.
    //
    // CORRECTIF : l'ancienne route "/confirmer/{employeeId}" faisait
    // confiance à un id d'employé fourni par le frontend
    // (AuthService.getUserId(), donc l'id de l'utilisateur connecté quel
    // qu'il soit). Pour un SUPERADMIN — qui n'est PAS un Employee, voir
    // CurrentUserService — cet id ne correspond à aucune ligne de la
    // table Employee, d'où l'erreur "Employee not found" en confirmant
    // une réservation en tant que SUPERADMIN. L'employé est maintenant
    // résolu ici (null pour un SUPERADMIN, confirmePar restera vide ;
    // l'employé réellement connecté sinon), sans jamais faire confiance
    // à un id venu du frontend.
    @PatchMapping("/{reservationId}/confirmer")
    public ResponseEntity<ReservationDTO>
    confirmerReservation(
            @PathVariable Long reservationId,
            @RequestBody ConfirmerReservationRequestDTO requestDTO)
            throws ReservationNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException, TableNotFoundException {

        ReservationDTO reservation = reservationService.getReservation(reservationId);
        currentUserService.verifierAccesRestaurant(
                reservation.getRestaurant() != null
                        ? reservation.getRestaurant().getId_restaurant()
                        : null
        );

        Long employeeId = estSuperAdminConnecte()
                ? null
                : currentUserService.getEmployeeConnecte().getId_utilisateur();

        if (requestDTO == null || requestDTO.getTableId() == null) {
            throw new IllegalArgumentException(
                    "Veuillez assigner une table pour confirmer cette réservation."
            );
        }

        TableRestaurantDTO table = tableService.getTable(requestDTO.getTableId());
        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                reservationService.confirmerReservation(
                        reservationId,
                        employeeId,
                        requestDTO.getTableId()
                )
        );
    }
}