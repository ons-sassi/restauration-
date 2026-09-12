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
public class SessionCaisse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_session_caisse;

    private Double montantOuverture;
    private Double montantFermeture;
    private Date dateOuverture;
    private Date dateFermeture;
    private Double ecartCaisse;

    @ManyToOne
    @JoinColumn(name = "id_pdv")
    private PointDeVente pointDeVente;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private Employee employee;
}
