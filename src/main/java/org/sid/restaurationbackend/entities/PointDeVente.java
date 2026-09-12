package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointDeVente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_pdv;

    private String nomPdv;
    private String appareil_pos;
    private String statutConnexion;

    @ManyToOne
    @JoinColumn(name = "id_restaurant")
    private Restaurant restaurant;

    @OneToMany(mappedBy = "pdvAffecte")
    private List<Employee> employees;

    @OneToMany(mappedBy = "pointDeVente")
    private List<Vente> ventes;

    @OneToMany(mappedBy = "pointDeVente")
    private List<SessionCaisse> sessions;


}
