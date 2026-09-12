package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametreCompte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_parametre;

    private String langue;
    private String devise;
    private String logo;
    private Boolean afficher_infos_client_sur_recu;
    private Boolean afficher_commentaire;
    private String option_restauration;

    @ManyToOne
    @JoinColumn(name = "id_restaurant")
    private Restaurant restaurant;
}
