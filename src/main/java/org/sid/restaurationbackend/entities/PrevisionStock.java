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
public class PrevisionStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_prevision;

    private Double quantite_prevue;
    private String periode;
    private Boolean baseeSurVentes;
    private Date date_generation;

    @ManyToOne
    @JoinColumn(name = "id_ingredient")
    private Ingredient ingredient;
}
