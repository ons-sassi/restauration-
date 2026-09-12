package org.sid.restaurationbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Fonctionnalite;
import org.sid.restaurationbackend.entities.Role;
import org.sid.restaurationbackend.enums.InterfaceType;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_role_fonctionnalite_interface",
                columnNames = {
                        "id_role",
                        "id_fonctionnalite",
                        "interface_type"
                }
        )
)
public class RoleFonctionnalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_role_fonctionnalite;

    @ManyToOne
    @JoinColumn(name = "id_role", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Role role;

    @ManyToOne
    @JoinColumn(name = "id_fonctionnalite", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fonctionnalite fonctionnalite;

    @Enumerated(EnumType.STRING)
    private InterfaceType interfaceType;

    private Boolean autorise;

    private Date date_attribution;

    @ManyToOne
    @JoinColumn(name = "attribue_par")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee attribuePar;
}