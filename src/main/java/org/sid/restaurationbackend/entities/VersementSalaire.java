package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutVersement;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VersementSalaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_versement;

    private String periode;
    private Double montant;
    private Date date_versement;
    @Enumerated(EnumType.STRING)
    private StatutVersement statut;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private Employee employee;
}
