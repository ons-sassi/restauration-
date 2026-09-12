package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCommandeDTO {

    private Long id;

    private String client;

    private String table;

    private Double montant;

    private String statut;
}