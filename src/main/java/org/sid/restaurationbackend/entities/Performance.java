package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Performance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_performance;

    private String periode;
    private Integer nombre_ventes;
    private Double chiffre_affaire_genere;
    private Double note_evaluation;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private Employee employee;
}
