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
public class Suggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_suggestion;

    private String contenu;
    private Date dateCreation;
    private Boolean priseEnCompte;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private ClientAuthentifie client;

    // SÉCURITÉ MULTI-RESTAURANT — famille "Stock/Fidélité" :
    // colonne DIRECTE (et non indirecte via "client"), car "client"
    // reste nullable ici — une
    // suggestion pourrait exister sans client rattaché. Consommée
    // dans un écran back-office mono-restaurant ("Conseils clients",
    // permission CLIENTS_CONSEILS) : même pattern que Reduction.
    @ManyToOne
    @JoinColumn(name = "id_restaurant", nullable = false)
    private Restaurant restaurant;
}
