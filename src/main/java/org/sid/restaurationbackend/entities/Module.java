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
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_module;

    private String nomModule;
    private String icone;
    private Integer ordre_affichage;
    private Boolean disponiblePdv;
    private Boolean disponible_backoffice;

    @OneToMany(mappedBy = "module")
    private List<Fonctionnalite> fonctionnalites;


}
