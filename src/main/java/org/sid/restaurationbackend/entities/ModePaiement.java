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
public class ModePaiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_mode_paiement;

    private String libelle;
    private Boolean actif;

    @OneToMany(mappedBy = "modePaiement")
    private List<Vente> ventes;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private ClientAuthentifie client;
}
