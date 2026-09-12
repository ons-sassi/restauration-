package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.entities.Employee;
import org.sid.restaurationbackend.entities.Utilisateur;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.UtilisateurNotFoundException;

/**
 * =============================================================
 * CENTRALISATION DE L'IDENTITÉ / DU RESTAURANT DE L'UTILISATEUR
 * RÉELLEMENT CONNECTÉ (BACK OFFICE / PDV)
 * =============================================================
 *
 * Contexte :
 * Le JWT ne contient PAS l'id du restaurant sélectionné ; celui-ci
 * n'était jusqu'ici transmis que par le FRONTEND (localStorage
 * "selectedRestaurantId"), à chaque requête. Aucun contrôleur ne
 * vérifiait que l'employé authentifié appartenait réellement à ce
 * restaurant : un employé du restaurant A pouvait donc voir/modifier
 * les données du restaurant B simplement en changeant cette valeur
 * côté client (localStorage, requête Postman, etc.).
 *
 * Ce service devient la SEULE source de vérité côté serveur pour
 * "quel est le restaurant de l'utilisateur connecté ?" : il relit
 * l'email authentifié depuis le SecurityContext (rempli par le JWT,
 * donc infalsifiable côté client) et recharge l'Employee correspondant
 * en base, avec son restaurant réel.
 *
 * Ce pattern existait déjà, dupliqué, dans TableServiceImpl
 * (getTablesDuServeurConnecte), SessionCaisseServiceImpl,
 * PriseEnChargeTableServiceImpl et CommandeServiceImpl : il est
 * centralisé ici pour être réutilisé dans tous les contrôleurs/services
 * qui doivent filtrer par restaurant.
 *
 * Ne concerne PAS les clients (ClientAuthentifie / ClientNonAuthentifie),
 * qui ne sont pas rattachés à un seul restaurant de la même façon —
 * à l'exception de {@link #getUtilisateurConnecte()} ci-dessous, ajouté
 * pour NotificationController (Lot 2.6), qui doit identifier N'IMPORTE
 * QUEL type d'utilisateur connecté (Employee, ClientAuthentifie ou
 * SuperAdmin), pas seulement les employés.
 */
public interface CurrentUserService {

    /**
     * @return l'Employee correspondant au token JWT actuellement
     * authentifié (email extrait du SecurityContext).
     * @throws EmployeeNotFoundException si aucun utilisateur n'est
     * authentifié, ou si l'email du token ne correspond à aucun
     * employé en base.
     */
    Employee getEmployeeConnecte() throws EmployeeNotFoundException;

    /**
     * @return le VRAI Utilisateur connecté, sous son sous-type concret
     * (Employee, ClientAuthentifie ou SuperAdmin — héritage JOINED),
     * rechargé par l'email authentifié depuis le SecurityContext.
     * Contrairement à {@link #getEmployeeConnecte()}, ne suppose pas
     * que l'appelant est un employé : à utiliser dans les contrôleurs
     * accessibles à plusieurs types d'utilisateurs (ex :
     * NotificationController, ReclamationController), quand il faut
     * distinguer le type réel de l'appelant (via {@code instanceof})
     * pour choisir la bonne règle de scoping.
     * @throws UtilisateurNotFoundException si aucun utilisateur n'est
     * authentifié, ou si l'email du token ne correspond à aucun
     * utilisateur en base.
     */
    Utilisateur getUtilisateurConnecte() throws UtilisateurNotFoundException, EmployeeNotFoundException;

    /**
     * @return l'id du restaurant réel de l'employé connecté
     * (employeeConnecte.getRestaurant().getId_restaurant()).
     * C'est cette valeur qui doit servir de filtre côté serveur —
     * jamais un id de restaurant fourni par le frontend.
     * @throws EmployeeNotFoundException si aucun utilisateur n'est
     * authentifié, ou si l'employé connecté n'a pas de restaurant
     * rattaché.
     */
    Long getRestaurantIdConnecte() throws EmployeeNotFoundException;

    /**
     * Vérifie que l'id de restaurant fourni (ex : reçu dans une
     * requête du frontend) correspond bien au restaurant réel de
     * l'employé connecté.
     *
     * @param restaurantId l'id à vérifier (peut être null : dans ce
     *                      cas la vérification échoue).
     * @return true si et seulement si restaurantId correspond au
     * restaurant réel de l'employé connecté.
     */
    boolean estRestaurantDeLEmployeConnecte(Long restaurantId) throws EmployeeNotFoundException;

    /**
     * Même vérification que {@link #estRestaurantDeLEmployeConnecte}, mais
     * lève directement une exception (mappée en HTTP 403) au lieu de
     * renvoyer un booléen — pour appeler ça en une ligne en début de
     * contrôleur/service sans dupliquer le if/throw à chaque fois.
     *
     * @param restaurantId l'id de restaurant reçu du frontend (ex :
     *                      RestaurantDTO.getId_restaurant(), un paramètre
     *                      restaurantId, ou le restaurant d'une entité
     *                      chargée par son id).
     * @throws EmployeeNotFoundException si l'employé connecté est
     * introuvable ou n'a pas de restaurant rattaché.
     * @throws AccesRestaurantNonAutoriseException si restaurantId ne
     * correspond pas au restaurant réel de l'employé connecté.
     */
    void verifierAccesRestaurant(Long restaurantId)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException;
}
