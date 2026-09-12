package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_reservation;

    private Integer nombre_personnes;
    private LocalDate dateReservation;
    private LocalTime heureReservation;
    @Enumerated(EnumType.STRING)
    private StatutReservation statut;
    private Date date_creation;
    private String commentaire_client;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private ClientAuthentifie client;

    @ManyToOne
    @JoinColumn(name = "restaurant")
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "id_table", nullable = true)
    private TableRestaurant table;

    @ManyToOne
    @JoinColumn(name = "confirmee_par", nullable = true)
    private Employee confirmePar;
}
