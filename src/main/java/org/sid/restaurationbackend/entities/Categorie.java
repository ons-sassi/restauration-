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
public class Categorie extends ElementMenu {
    private String description_categorie;

    @OneToMany(mappedBy = "categorieParent", orphanRemoval = true)
    private List<ElementMenu> elements;
}
