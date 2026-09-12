package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutPresence;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_presence;

    private Date date;
    private String heure_arrivee;
    private String heure_depart;
    private Double heures_travaillees_total;

    // Statut du jour marqué par le responsable : PRESENT / ABSENT /
    // CONGE (page "Présence" du Back Office). Nullable en base pour
    // ne pas casser les éventuels enregistrements déjà existants
    // (pointages sans statut) : traité comme non renseigné partout
    // où il est lu.
    @Enumerated(EnumType.STRING)
    private StatutPresence statut;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private Employee employee;
}
