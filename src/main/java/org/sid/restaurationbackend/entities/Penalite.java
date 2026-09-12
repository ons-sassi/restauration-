package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Penalite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_penalite;

    private String motif;
    private Double montant_deduit;
    private Date date;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private Employee employee;
}
