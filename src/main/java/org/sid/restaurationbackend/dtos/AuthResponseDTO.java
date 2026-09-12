package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.restaurationbackend.enums.InterfaceType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {

    private String token;

    private String type = "Bearer";

    private Long userId;

    private String email;

    private String role;

    private InterfaceType interfaceType;

    private Long pointDeVenteId;

    private Long restaurantId;

    private List<String> permissions;

    // =========================================================
    // IMPERSONATION (Option B — SUPERADMIN = supervision complète)
    // =========================================================
    //
    // true uniquement pour un token émis par
    // AuthService.impersonateRestaurant (le SUPERADMIN "devient"
    // temporairement l'Admin du restaurant ciblé). false pour un
    // login normal (back-office, PDV, client, super-admin).
    // Le frontend s'appuie sur ce champ pour afficher le bandeau
    // "mode supervision" et proposer de revenir à la session
    // SUPERADMIN d'origine.
    private boolean impersonating;

    // Nom du restaurant supervisé, uniquement renseigné quand
    // impersonating = true (affichage du bandeau côté frontend,
    // sans requête supplémentaire).
    private String impersonatedRestaurantName;
}