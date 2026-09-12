package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_role;

    private String nom_role;
    private String description;
    private Boolean acces_pdv;
    private Boolean acces_backoffice;
    private Date date_creation;

    // =========================================================
    // Exclu de toString()/equals()/hashCode() : Employee.role
    // pointe vers ce Role, qui pointe vers cet Employee via
    // attribuePar -> boucle infinie sans cette exclusion.
    // =========================================================
    @ManyToOne
    @JoinColumn(name = "attribue_par")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee attribuePar;

    @OneToMany(mappedBy = "role")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Employee> employees;


    @OneToMany(mappedBy = "role")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RoleFonctionnalite> fonctionnalites;
}