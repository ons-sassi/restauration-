package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * Vue "client" d'une réservation — séparée de ReservationDTO (partagé
 * avec le back-office) pour ne jamais exposer plus que ce dont
 * l'espace client a besoin : pas de ClientAuthentifieDTO ni de
 * RestaurantDTO imbriqués (le client connaît déjà sa propre identité
 * et son restaurant), et seulement le numéro de la table assignée
 * (jamais un TableRestaurantDTO complet) — null tant que le
 * restaurant n'a pas encore assigné de table précise (voir
 * Reservation.table, nullable).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientReservationDTO {

    private Long id_reservation;
    private Integer nombre_personnes;
    private LocalDate dateReservation;
    private LocalTime heureReservation;
    private StatutReservation statut;
    private Date date_creation;
    private String commentaire_client;

    private Integer numeroTable;
}