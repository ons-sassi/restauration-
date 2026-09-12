
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
public class PlanDeSalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_plan;

    private String disposition_tables;

    private Date dateMiseAJour;

    @ManyToOne
    @JoinColumn(name = "id_restaurant")
    private Restaurant restaurant;
}

