package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElementMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_element;

    private String nom;
    private Integer ordre_affichage;

    /**
     * Peut contenir soit une URL, soit une image encodée en base64
     * (upload depuis le poste local). NVARCHAR(MAX) pour éviter
     * toute troncature du contenu base64.
     */
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String image;

    @ManyToOne
    @JoinColumn(name = "id_categorie_parent", nullable = true)
    private Categorie categorieParent;

    @ManyToOne
    @JoinColumn(name = "id_restaurant")
    private Restaurant restaurant;
}