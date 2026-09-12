package org.sid.restaurationbackend.dtos;

import lombok.Data;

/**
 * Corps de POST /api/client/suggestions. Volontairement minimal :
 * contenu uniquement — le restaurant est toujours dérivé du client
 * connecté (voir ClientSuggestionServiceImpl), jamais fourni par le
 * frontend.
 */
@Data
public class ClientSuggestionRequestDTO {

    private String contenu;
}
