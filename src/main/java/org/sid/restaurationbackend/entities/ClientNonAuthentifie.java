package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.OrigineSession;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientNonAuthentifie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_session;


    private String allergie;

    @Enumerated(EnumType.STRING)
    private OrigineSession origineSession;
    private Date date_debut_session;
    private Date date_fin_session;

    @ManyToOne
    @JoinColumn(name = "id_restaurant")
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "id_table_scannee")
    private TableRestaurant tableScannee;

    @OneToMany(mappedBy = "clientNonAuthentifie")
    private List<Commande> commandes;
}
