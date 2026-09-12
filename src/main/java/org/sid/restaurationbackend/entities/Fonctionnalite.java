package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fonctionnalite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_fonctionnalite;
    private String nomFonctionnalite;
    private String codeFonctionnalite;
    private Boolean disponiblePdv;
    private Boolean disponibleBackoffice;
    private Integer ordre_affichage;

    @ManyToOne
    @JoinColumn(name = "id_module")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Module module;

    // =========================================================
    // Relation auto-référencée (parent / sous-fonctionnalités) :
    // sans exclusion, Lombok génère un toString()/equals() qui
    // boucle indéfiniment entre une fonctionnalité et ses
    // sous-fonctionnalités (StackOverflowError garanti dès
    // qu'un parent a au moins un enfant).
    // =========================================================
    @ManyToOne
    @JoinColumn(name = "id_fonctionnalite_parent", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fonctionnalite fonctionnaliteParent;

    @OneToMany(mappedBy = "fonctionnaliteParent")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Fonctionnalite> sous_fonctionnalites;

    @OneToMany(mappedBy = "fonctionnalite",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RoleFonctionnalite> roles;
}