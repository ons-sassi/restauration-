package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteParEmployeeDTO {

    private Long employeeId;

    private String employee;

    private String matricule;

    private Integer nombreVentes;

    private Double chiffreAffaires;

    /**
     * Pourcentage de productivité de l'employé.
     *
     * Correspond à la part du chiffre d'affaires généré
     * par cet employé (en tant que serveur ayant pris la
     * commande) par rapport au chiffre d'affaires total
     * de la période sélectionnée.
     */
    private Double pourcentageProductivite;
}
