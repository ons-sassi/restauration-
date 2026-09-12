package org.sid.restaurationbackend.entities;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =============================================================
 * SUPER-ADMIN PLATEFORME (solution 1 — RestaurantController)
 * =============================================================
 *
 * Contexte : avant ce type de compte, RestaurantController n'avait
 * AUCUNE restriction — n'importe quel Employee authentifié pouvait
 * créer/lister/modifier/supprimer n'importe quel restaurant, y
 * compris ceux dont il n'était pas membre.
 *
 * Le modèle existant ne permettait pas de distinguer "qui a le
 * droit d'administrer des restaurants en général" : un ROLE_ADMIN
 * (Employee.role.nom_role == "ADMIN") est TOUJOURS rattaché à un
 * restaurant précis (Employee.restaurant, non nullable) — il n'y
 * avait pas de notion de "propriétaire de la plateforme" au-dessus.
 *
 * SuperAdmin comble ce trou : comme ClientAuthentifie, c'est un
 * sous-type de Utilisateur (même mécanisme d'héritage JOINED) mais
 * qui n'est rattaché à AUCUN restaurant. Il n'a pas de rôle
 * (Role/RoleFonctionnalite), pas de PDV, pas de matricule — ce
 * n'est PAS un Employee : il a son propre mécanisme d'auth dédié
 * (voir AuthService.loginSuperAdmin, generateSuperAdminToken dans
 * JwtUtil) qui produit un JWT avec le rôle "SUPERADMIN"
 * (authority Spring Security ROLE_SUPERADMIN), sans restaurantId
 * ni pointDeVenteId.
 *
 * Seul un SuperAdmin peut créer/lister-tous/rechercher/supprimer
 * des restaurants (voir RestaurantController). Un Employee (y
 * compris ADMIN de restaurant) ne peut plus lire/modifier que SON
 * propre restaurant, vérifié via CurrentUserService — comme tous
 * les autres contrôleurs du chantier de sécurité multi-tenant.
 */
@Entity
@Data
@NoArgsConstructor
public class SuperAdmin extends Utilisateur {
    // Aucun champ supplémentaire pour l'instant : SuperAdmin ne
    // fait qu'hériter de email / mot_de_passe / statut / etc. depuis
    // Utilisateur. Un flag ou des champs propres (ex : niveau
    // d'habilitation) pourront être ajoutés ici plus tard sans
    // impacter Employee ni ClientAuthentifie.
}