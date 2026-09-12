package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Vue "client" d'une suggestion — séparée de SuggestionDTO (partagé
 * avec le back-office) : pas de ClientAuthentifieDTO ni de
 * RestaurantDTO imbriqués, le client connaît déjà les deux.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientSuggestionDTO {

    private Long id_suggestion;
    private String contenu;
    private Date dateCreation;
    private Boolean priseEnCompte;
}
