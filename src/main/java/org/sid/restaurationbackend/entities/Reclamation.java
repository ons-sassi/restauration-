package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.StatutReclamation;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reclamation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_reclamation;

    private String sujet;
    private String description;
    private Date date_creation;
    @Enumerated(EnumType.STRING)
    private StatutReclamation statut;
    private String reponse_employee;
    private Date date_reponse;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private ClientAuthentifie client;

    @ManyToOne
    @JoinColumn(name = "id_commande", nullable = true)
    private Commande commande;
}
