package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.CommandeDTO;
import org.sid.restaurationbackend.dtos.ReclamationDTO;
import org.sid.restaurationbackend.enums.StatutReclamation;
import org.sid.restaurationbackend.exceptions.AccesRestaurantNonAutoriseException;
import org.sid.restaurationbackend.exceptions.CommandeNotFoundException;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.ReclamationNotFoundException;
import org.sid.restaurationbackend.services.CommandeService;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.ReclamationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — Lot 2.6
 * =========================================================
 * Reclamation n'a PAS de colonne restaurant en base. Son restaurant
 * est déterminé INDIRECTEMENT via sa Commande (elle-même scopée par
 * table > employee > clientNonAuthentifie — même logique que
 * CommandeController, dupliquée ici volontairement pour rester
 * cohérent avec le reste du fichier).
 *
 * ⚠️ CAS LIMITE (commande == null) : le champ Reclamation.commande
 * est nullable, et Reclamation.client (ClientAuthentifie) n'est pas
 * rattaché à un seul restaurant. Sans commande, il n'existe donc
 * AUCUN moyen de déterminer quel restaurant est légitime pour agir
 * sur cette réclamation. Décision retenue : on ferme par défaut
 * (403) pour un employé de restaurant classique — seul le
 * SUPERADMIN plateforme (voir RestaurantController) peut consulter/
 * gérer ces réclamations "orphelines". C'est un choix conservateur,
 * pas une solution définitive ; si ce cas s'avère fréquent en
 * pratique, il faudra une vraie décision produit (comme pour la
 * famille Stock, Lot 2.7).
 *
 * AUTRE PARTICULARITÉ : ce contrôleur est accessible à des employés
 * ET à des clients authentifiés (pas de @PreAuthorize d'interface,
 * cf. SecurityConfig : "/api/reclamations/**" tombe sous
 * anyRequest().authenticated()). CurrentUserService ne fonctionne
 * QUE pour les employés (il recharge un Employee par email). Les
 * endpoints ci-dessous qui font de la vérification de restaurant
 * (lecture/modification/suppression/réponse — typiquement des
 * actions de back-office) supposent donc un appelant Employee ou
 * SuperAdmin. La création (POST, sans vérification de restaurant)
 * reste ouverte aux deux : voir javadoc de saveReclamation.
 */
@RestController
@RequestMapping("/api/reclamations")
@AllArgsConstructor
public class ReclamationController {

    private final ReclamationService reclamationService;
    private final CommandeService commandeService;
    private final CurrentUserService currentUserService;


    // ---------------------------------------------------------------
    // Utilitaires internes de scoping restaurant (voir javadoc classe)
    // ---------------------------------------------------------------

    // Copié de CommandeController (même ordre de priorité) : pas de
    // helper partagé dans ce projet, chaque contrôleur duplique son
    // propre utilitaire de scoping — convention déjà en place.
    private boolean estSuperAdminConnecte() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_SUPERADMIN".equals(
                                authority.getAuthority()
                        )
                );
    }

    private Long restaurantIdDeCommande(CommandeDTO commande) {

        if (commande == null) {
            return null;
        }

        if (commande.getTable() != null
                && commande.getTable().getRestaurant() != null) {

            return commande.getTable()
                    .getRestaurant()
                    .getId_restaurant();
        }

        if (commande.getEmployee() != null
                && commande.getEmployee().getRestaurant() != null) {

            return commande.getEmployee()
                    .getRestaurant()
                    .getId_restaurant();
        }

        if (commande.getClientNonAuthentifie() != null
                && commande.getClientNonAuthentifie().getRestaurant() != null) {

            return commande.getClientNonAuthentifie()
                    .getRestaurant()
                    .getId_restaurant();
        }

        // Commande passée par un client authentifié (à emporter / livraison) :
        // ni table, ni employee, ni clientNonAuthentifie ne sont renseignés
        // dans ce cas (voir ClientCommandeServiceImpl.creerCommandeDepuisPanier).
        // Même correctif que CommandeController.restaurantIdDeCommande.
        if (commande.getClient() != null
                && commande.getClient().getRestaurant() != null) {

            return commande.getClient()
                    .getRestaurant()
                    .getId_restaurant();
        }

        return null;
    }

    // À utiliser sur une ReclamationDTO déjà rechargée depuis le
    // service (jamais sur un objet envoyé tel quel par le frontend).
    // Bypass total pour le SUPERADMIN. Ferme (403) si la commande
    // est absente : voir javadoc de la classe (cas limite).
    private void verifierAccesReclamation(ReclamationDTO reclamation)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        if (estSuperAdminConnecte()) {
            return;
        }

        Long restaurantId = reclamation != null
                ? restaurantIdDeCommande(reclamation.getCommande())
                : null;

        if (restaurantId == null) {
            throw new AccesRestaurantNonAutoriseException(
                    "Réclamation non rattachée à un restaurant "
                            + "déterminable (commande absente) : "
                            + "réservé au super-admin.");
        }

        currentUserService.verifierAccesRestaurant(restaurantId);
    }

    // Recharge la VRAIE commande depuis son id (jamais depuis le
    // sous-objet "commande" envoyé dans le corps d'une requête par le
    // frontend, qui pourrait être falsifié), vérifie son restaurant,
    // et la renvoie pour être utilisée à la place de l'objet fourni.
    private CommandeDTO chargerCommandeAutorisee(Long commandeId)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        if (commandeId == null) {
            throw new CommandeNotFoundException("Commande not found");
        }

        CommandeDTO commande = commandeService.getCommande(commandeId);

        if (!estSuperAdminConnecte()) {
            currentUserService.verifierAccesRestaurant(
                    restaurantIdDeCommande(commande));
        }

        return commande;
    }


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Créer une réclamation.
     *
     * Pas de vérification de restaurant ici : l'appelant peut être un
     * client authentifié (qui n'est pas rattaché à un restaurant au
     * sens de CurrentUserService) déposant sa propre réclamation, ou
     * un employé le faisant pour son compte. On recharge toutefois la
     * VRAIE commande depuis son id si elle est fournie, pour ne
     * jamais persister un sous-objet "commande" (table/employee/...)
     * fourni tel quel par le frontend.
     *
     * Non couvert par ce lot (hors périmètre "restaurant") : la
     * vérification que le client fourni est bien le client connecté
     * — même famille de bug que l'ancien getMaCommande, mais aucun
     * équivalent de CurrentUserService n'existe pour les clients
     * dans ce projet à ce jour.
     */
    @PostMapping
    public ResponseEntity<ReclamationDTO> saveReclamation(
            @RequestBody ReclamationDTO reclamationDTO)
            throws CommandeNotFoundException {

        if (reclamationDTO.getCommande() != null
                && reclamationDTO.getCommande().getId_commande() != null) {

            reclamationDTO.setCommande(
                    commandeService.getCommande(
                            reclamationDTO.getCommande().getId_commande()));
        }

        ReclamationDTO savedReclamation =
                reclamationService.saveReclamation(
                        reclamationDTO
                );

        return new ResponseEntity<>(
                savedReclamation,
                HttpStatus.CREATED
        );
    }



    @GetMapping("/{id}")
    public ResponseEntity<ReclamationDTO> getReclamation(
            @PathVariable Long id)
            throws ReclamationNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReclamationDTO reclamation =
                reclamationService.getReclamation(id);

        verifierAccesReclamation(reclamation);

        return ResponseEntity.ok(reclamation);
    }



    // Toutes les réclamations DU RESTAURANT CONNECTÉ (ou toutes, pour
    // le SUPERADMIN). Avant : renvoyait les réclamations de tous les
    // restaurants confondus, y compris les orphelines sans commande.
    @GetMapping
    public ResponseEntity<List<ReclamationDTO>>
    getAllReclamations()
            throws EmployeeNotFoundException {

        List<ReclamationDTO> toutes =
                reclamationService.getAllReclamations();

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(toutes);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                toutes.stream()
                        .filter(r -> restaurantId.equals(
                                restaurantIdDeCommande(r.getCommande())))
                        .toList()
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ReclamationDTO> updateReclamation(
            @PathVariable Long id,
            @RequestBody ReclamationDTO reclamationDTO)
            throws ReclamationNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException, CommandeNotFoundException {

        ReclamationDTO existante = reclamationService.getReclamation(id);
        verifierAccesReclamation(existante);

        // Si le corps de la requête rattache la réclamation à une
        // AUTRE commande, cette nouvelle commande doit elle aussi
        // appartenir au restaurant connecté (jamais suivie
        // aveuglément).
        Long ancienCommandeId = existante.getCommande() != null
                ? existante.getCommande().getId_commande()
                : null;

        if (reclamationDTO.getCommande() != null
                && reclamationDTO.getCommande().getId_commande() != null
                && !Objects.equals(
                reclamationDTO.getCommande().getId_commande(),
                ancienCommandeId)) {

            reclamationDTO.setCommande(
                    chargerCommandeAutorisee(
                            reclamationDTO.getCommande().getId_commande()));
        }

        return ResponseEntity.ok(
                reclamationService.updateReclamation(
                        id,
                        reclamationDTO
                )
        );
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReclamation(
            @PathVariable Long id)
            throws ReclamationNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReclamationDTO existante = reclamationService.getReclamation(id);
        verifierAccesReclamation(existante);

        reclamationService.deleteReclamation(id);

        return ResponseEntity.noContent().build();
    }




    // client est un ClientAuthentifie, qui n'est PAS rattaché à un
    // seul restaurant (même remarque que CommandeController
    // .getCommandesByClient) : on filtre donc sur les réclamations
    // dont la commande appartient au restaurant connecté, plutôt que
    // de vérifier une "ownership" du client lui-même.
    @PostMapping("/client")
    public ResponseEntity<List<ReclamationDTO>>
    getReclamationsByClient(
            @RequestBody ClientAuthentifieDTO client)
            throws EmployeeNotFoundException {

        List<ReclamationDTO> resultats =
                reclamationService.getReclamationsByClient(client);

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(r -> restaurantId.equals(
                                restaurantIdDeCommande(r.getCommande())))
                        .toList()
        );
    }




    // IDOR corrigé : avant, le CommandeDTO envoyé par le frontend
    // (id_commande arbitraire) était utilisé tel quel pour la
    // requête, sans vérifier que cette commande appartenait au
    // restaurant connecté. La vraie commande est maintenant
    // rechargée depuis son id et vérifiée avant d'être utilisée.
    @PostMapping("/commande")
    public ResponseEntity<List<ReclamationDTO>>
    getReclamationsByCommande(
            @RequestBody CommandeDTO commande)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException,
            CommandeNotFoundException {

        CommandeDTO commandeAutorisee = chargerCommandeAutorisee(
                commande != null ? commande.getId_commande() : null);

        return ResponseEntity.ok(
                reclamationService.getReclamationsByCommande(
                        commandeAutorisee
                )
        );
    }



    // Globale (tous restaurants confondus côté service) : filtrée ici
    // par restaurant connecté, sauf pour le SUPERADMIN.
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<ReclamationDTO>>
    getReclamationsByStatut(
            @PathVariable StatutReclamation statut)
            throws EmployeeNotFoundException {

        List<ReclamationDTO> resultats =
                reclamationService.getReclamationsByStatut(statut);

        if (estSuperAdminConnecte()) {
            return ResponseEntity.ok(resultats);
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                resultats.stream()
                        .filter(r -> restaurantId.equals(
                                restaurantIdDeCommande(r.getCommande())))
                        .toList()
        );
    }



    @PatchMapping("/{id}/repondre")
    public ResponseEntity<ReclamationDTO>
    repondreReclamation(
            @PathVariable Long id,
            @RequestParam String reponse)
            throws ReclamationNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ReclamationDTO existante = reclamationService.getReclamation(id);
        verifierAccesReclamation(existante);

        return ResponseEntity.ok(
                reclamationService.repondreReclamation(
                        id,
                        reponse
                )
        );
    }
}