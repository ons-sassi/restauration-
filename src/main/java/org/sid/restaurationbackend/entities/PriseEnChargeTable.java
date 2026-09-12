package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sid.restaurationbackend.enums.StatutPriseEnCharge;

import java.util.Date;

/*
 * =============================================================
 * PRISE EN CHARGE TEMPORAIRE D'UNE TABLE
 * =============================================================
 *
 * Distincte de l'affectation PERMANENTE d'un responsable
 * (TableRestaurant.serveurAttribue) :
 *
 * - serveurAttribue = "cet employé est responsable de cette
 *   table en général" (relation durable).
 *
 * - PriseEnChargeTable = "cet employé s'occupe ACTUELLEMENT du
 *   client de cette table, à cet instant précis" (relation
 *   ponctuelle, avec un début et éventuellement une fin).
 *
 * Le responsable permanent d'une table n'est pas forcément celui
 * qui la prend en charge à un instant donné (pause, absence,
 * dépannage par un collègue, etc.), d'où la nécessité de cette
 * entité séparée plutôt que de surcharger serveurAttribue.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriseEnChargeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_prise_en_charge;

    @ManyToOne
    @JoinColumn(name = "id_table", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TableRestaurant table;

    @ManyToOne
    @JoinColumn(name = "id_employee", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee employee;

    private Date dateDebut;
    private Date dateFin;

    @Enumerated(EnumType.STRING)
    private StatutPriseEnCharge statut;
}
