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
public class Recu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_recu;

    private String numeroRecu;
    private Date date_emission;
    private Boolean logo_affiche;
    private String entete_personnalise;
    private String pied_de_page_personnalise;
    private String commentaire_client;

    @OneToOne
    @JoinColumn(name = "id_vente")
    private Vente vente;

    @ManyToOne
    @JoinColumn(name = "id_modele")
    private ModeleRecu modele;
}
