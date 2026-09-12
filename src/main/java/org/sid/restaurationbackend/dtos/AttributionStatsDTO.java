package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Phase 3C — vue "statistiques d'attribution" côté responsable
 * (§22/§23 de la spec) : pour chaque employé, son nombre de prises en
 * charge ACTIVES actuellement et s'il est éligible à l'attribution
 * automatique/manuelle. Sert à faire comprendre au responsable
 * pourquoi tel employé serait choisi par l'automatisation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributionStatsDTO {
    private EmployeeDTO employee;
    private long nombrePrisesEnChargeActives;
    private boolean eligible;
}
