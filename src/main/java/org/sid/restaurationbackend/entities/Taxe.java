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
public class Taxe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_taxe;

    private String nomTaxe;
    private Double taux;
    private String applicableA;
    private Date dateCreation;
    private String statut;
}
