package org.sid.restaurationbackend.dtos;

import lombok.Data;
import org.sid.restaurationbackend.enums.StatutReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Data
public class ReservationDTO {

    private Long id_reservation;
    private Integer nombre_personnes;
    private LocalDate dateReservation;
    private LocalTime heureReservation;
    private StatutReservation statut;
    private Date date_creation;
    private String commentaire_client;

    // Relation ManyToOne
    private ClientAuthentifieDTO client;

    // Relation ManyToOne
    private RestaurantDTO restaurant;

    // Relation ManyToOne
    private TableRestaurantDTO table;

    // Relation ManyToOne
    private EmployeeDTO confirmePar;
}
