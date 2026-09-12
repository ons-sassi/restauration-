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
public class Modificateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_modificateur;

    private String nom_modificateur;
    private Double prix_supplementaire;

    // Un modificateur peut désormais être associé à plusieurs produits.
    @ManyToMany
    @JoinTable(
            name = "modificateur_produit",
            joinColumns = @JoinColumn(name = "id_modificateur"),
            inverseJoinColumns = @JoinColumn(name = "id_produit")
    )
    private List<Produit> produits;
}
