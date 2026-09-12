package org.sid.restaurationbackend.dtos;

import lombok.Data;

/**
 * Corps de PATCH /api/reservations/{id}/confirmer/{employeeId}.
 *
 * La table est désormais obligatoire pour confirmer une réservation
 * (voir ReservationController.confirmerReservation / ReservationServiceImpl) :
 * on ne peut plus confirmer "à l'aveugle" sans savoir où asseoir le client.
 */
@Data
public class ConfirmerReservationRequestDTO {
    private Long tableId;
}