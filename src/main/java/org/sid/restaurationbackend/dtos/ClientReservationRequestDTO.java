package org.sid.restaurationbackend.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Corps de POST /api/client/reservations.
 *
 * Volontairement minimal, même logique que
 * ClientReclamationRequestDTO : le client ne fournit que les
 * informations de SA demande de réservation, jamais un restaurant ou
 * un client falsifiable — le restaurant est toujours celui du client
 * connecté (ClientAuthentifie.restaurant, colonne NOT NULL, voir
 * ClientReservationServiceImpl), et le statut initial est toujours
 * EN_ATTENTE (jamais choisi par le client). Aucune table n'est
 * choisie ici non plus : c'est le restaurant qui l'assigne ensuite
 * via ReservationController (back-office).
 */
@Data
public class ClientReservationRequestDTO {

    private Integer nombrePersonnes;
    private LocalDate dateReservation;
    private LocalTime heureReservation;

    /**
     * Optionnel : message libre du client au restaurant (ex.
     * "table près de la fenêtre").
     */
    private String commentaireClient;
}