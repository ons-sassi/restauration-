package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ClientNonAuthentifieDTO;
import org.sid.restaurationbackend.dtos.CommandeDTO;
import org.sid.restaurationbackend.dtos.EmployeeDTO;
import org.sid.restaurationbackend.dtos.LigneCommandeDTO;
import org.sid.restaurationbackend.dtos.ProduitDTO;
import org.sid.restaurationbackend.dtos.TableRestaurantDTO;
import org.sid.restaurationbackend.enums.StatutCommande;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.services.ClientNonAuthentifieService;
import org.sid.restaurationbackend.services.CommandeService;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.EmployeeService;
import org.sid.restaurationbackend.services.ProduitService;
import org.sid.restaurationbackend.services.TableService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;


/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — Lot 2.5
 * =========================================================
 * Commande n'a PAS de colonne restaurant en base : son restaurant est
 * déterminé INDIRECTEMENT, dans cet ordre de priorité :
 *   1) commande.table.restaurant       (le cas le plus courant en salle)
 *   2) commande.employee.restaurant    (commande créée/gérée par un employé)
 *   3) commande.clientNonAuthentifie.restaurant (session QR sans table)
 *   4) commande.client.restaurant       (commande passée par un client authentifié
 *                                      à emporter / en livraison)
 *
 * Toutes les lectures/écritures vérifient ce restaurant indirect avant
 * d'agir, et toutes les listes globales sont filtrées sur le restaurant
 * de l'employé connecté.
 *
 * ⚠️ Découverte sur ce lot (corrigée ici) : updateCommande acceptait un
 * CommandeDTO complet où table / employee / clientNonAuthentifie
 * pouvaient être remplacés par n'importe quel id envoyé par le frontend,
 * sans aucune vérification — un employé pouvait donc "déplacer" une
 * commande vers la table (ou l'employé) d'un AUTRE restaurant. Les
 * références fournies dans le corps de la requête sont désormais
 * rechargées en base et vérifiées avant toute mise à jour, exactement
 * comme pour les DTO de PointDeVente dans SessionCaisseController.
 *
 * Ce contrôleur ne gère PAS la création de commande (pas d'endpoint
 * POST /api/commandes) : la création passe par ClientCommandeController /
 * PublicCommandeController, qui seront traités au Lot 2.6.
 */
@RestController
@RequestMapping("/api/commandes")
@AllArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;
    private final TableService tableService;
    private final EmployeeService employeeService;
    private final ClientNonAuthentifieService clientNonAuthentifieService;
    private final ProduitService produitService;
    private final CurrentUserService currentUserService;




    @GetMapping
    public ResponseEntity<List<CommandeDTO>> getAllCommandes()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                commandeService.getAllCommandes()
                        .stream()
                        .filter(c -> estDuRestaurant(c, restaurantId))
                        .toList()
        );
    }




    @GetMapping("/{id}")
    public ResponseEntity<CommandeDTO> getCommande(
            @PathVariable Long id)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        CommandeDTO commande = commandeService.getCommande(id);

        verifierCommandeDuRestaurantConnecte(commande);

        return ResponseEntity.ok(commande);
    }




    @PutMapping("/{id}")
    public ResponseEntity<CommandeDTO> updateCommande(
            @PathVariable Long id,
            @RequestBody CommandeDTO commandeDTO)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException, TableNotFoundException,
            ClientNotFoundException {

        // La commande existante doit appartenir au restaurant connecté.
        verifierCommandeDuRestaurantConnecte(
                commandeService.getCommande(id)
        );

        // Toute nouvelle référence (table / employee / session) envoyée
        // dans le corps de la requête doit elle aussi appartenir au
        // restaurant connecté — jamais suivie aveuglément.
        verifierReferencesDto(commandeDTO);

        return ResponseEntity.ok(
                commandeService.updateCommande(
                        id,
                        commandeDTO
                )
        );
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommande(
            @PathVariable Long id)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierCommandeDuRestaurantConnecte(
                commandeService.getCommande(id)
        );

        commandeService.deleteCommande(id);

        return ResponseEntity.noContent().build();
    }



    @GetMapping("/search")
    public ResponseEntity<List<CommandeDTO>> searchCommandes(

            @RequestParam(required = false)
            Long clientId,

            @RequestParam(required = false)
            StatutCommande statut,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            Instant dateDebut,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            Instant dateFin

    ) throws ClientNotFoundException, EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                commandeService.searchCommandes(
                                clientId,
                                statut,
                                dateDebut != null ? Date.from(dateDebut) : null,
                                dateFin != null ? Date.from(dateFin) : null
                        )
                        .stream()
                        .filter(c -> estDuRestaurant(c, restaurantId))
                        .toList()
        );
    }



    // clientId est un ClientAuthentifie, qui n'est PAS rattaché à un seul
    // restaurant (un même compte client peut commander dans plusieurs
    // restaurants de l'enseigne) : on ne peut donc pas vérifier
    // "ownership" du client lui-même, seulement filtrer ses commandes sur
    // celles qui appartiennent au restaurant connecté.
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeDTO>>
    getCommandesByClient(
            @PathVariable Long clientId)
            throws ClientNotFoundException, EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                commandeService.getCommandesByClient(
                                clientId
                        )
                        .stream()
                        .filter(c -> estDuRestaurant(c, restaurantId))
                        .toList()
        );
    }




    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<CommandeDTO>>
    getCommandesBySession(
            @PathVariable Long sessionId)
            throws ClientNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        ClientNonAuthentifieDTO session =
                clientNonAuthentifieService
                        .getClientNonAuthentifieById(sessionId);

        currentUserService.verifierAccesRestaurant(
                session.getRestaurant() != null
                        ? session.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                commandeService
                        .getCommandesByClientNonAuthentifie(
                                sessionId
                        )
        );
    }





    @GetMapping("/table/{tableId}")
    public ResponseEntity<List<CommandeDTO>>
    getCommandesByTable(
            @PathVariable Long tableId)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO table =
                tableService.getTable(tableId);

        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                commandeService.getCommandesByTable(
                        tableId
                )
        );
    }



    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<CommandeDTO>>
    getCommandesByEmployee(
            @PathVariable Long employeeId)
            throws EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        EmployeeDTO employee =
                employeeService.getEmployee(employeeId);

        currentUserService.verifierAccesRestaurant(
                employee.getRestaurant() != null
                        ? employee.getRestaurant().getId_restaurant()
                        : null
        );

        return ResponseEntity.ok(
                commandeService.getCommandesByEmployee(
                        employeeId
                )
        );
    }




    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<CommandeDTO>>
    getCommandesByStatut(
            @PathVariable StatutCommande statut)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                commandeService.getCommandesByStatut(
                                statut
                        )
                        .stream()
                        .filter(c -> estDuRestaurant(c, restaurantId))
                        .toList()
        );
    }




    @PatchMapping("/{id}/statut")
    public ResponseEntity<CommandeDTO> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutCommande statut)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierCommandeDuRestaurantConnecte(
                commandeService.getCommande(id)
        );

        return ResponseEntity.ok(
                commandeService.changerStatut(
                        id,
                        statut
                )
        );
    }




    @PatchMapping("/{id}/annuler")
    public ResponseEntity<CommandeDTO> annulerCommande(
            @PathVariable Long id)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierCommandeDuRestaurantConnecte(
                commandeService.getCommande(id)
        );

        return ResponseEntity.ok(
                commandeService.annulerCommande(id)
        );
    }



    @GetMapping("/date")
    public ResponseEntity<List<CommandeDTO>>
    getCommandesByDate(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            Instant dateDebut,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            Instant dateFin

    ) throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return ResponseEntity.ok(
                commandeService.getCommandesByDate(
                                Date.from(dateDebut),
                                Date.from(dateFin)
                        )
                        .stream()
                        .filter(c -> estDuRestaurant(c, restaurantId))
                        .toList()
        );
    }




    @GetMapping("/{id}/lignes")
    public ResponseEntity<List<LigneCommandeDTO>>
    getLignesCommande(
            @PathVariable Long id)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierCommandeDuRestaurantConnecte(
                commandeService.getCommande(id)
        );

        return ResponseEntity.ok(
                commandeService.getLignesCommande(id)
        );
    }





    @PostMapping("/{id}/lignes")
    public ResponseEntity<LigneCommandeDTO>
    ajouterLigne(
            @PathVariable Long id,
            @RequestBody LigneCommandeDTO ligne)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException, ProduitNotFoundException {

        Long restaurantId = restaurantIdDeCommande(
                commandeService.getCommande(id)
        );

        currentUserService.verifierAccesRestaurant(restaurantId);

        // Le produit ajouté à la ligne doit lui aussi appartenir au
        // même restaurant que la commande (sinon on pourrait injecter
        // un produit d'un autre restaurant, avec son prix, dans une
        // commande qui n'est pas la sienne).
        if (ligne != null
                && ligne.getProduit() != null
                && ligne.getProduit().getId_element() != null) {

            ProduitDTO produit =
                    produitService.getProduit(
                            ligne.getProduit().getId_element()
                    );

            currentUserService.verifierAccesRestaurant(
                    produit.getRestaurant() != null
                            ? produit.getRestaurant().getId_restaurant()
                            : null
            );
        }

        return ResponseEntity.status(201)
                .body(
                        commandeService.ajouterLigne(
                                id,
                                ligne
                        )
                );
    }




    // Les endpoints ci-dessous agissent sur une ligne via son PROPRE id
    // (pas via l'id de la commande) : il n'existait aucun moyen de
    // recharger la ligne réelle avant modification/suppression sans
    // toucher au service. Un getter minimal (getLigneCommande) a été
    // ajouté à CommandeService pour permettre cette vérification, sans
    // changer le comportement des méthodes existantes.
    @PutMapping("/lignes/{id}")
    public ResponseEntity<LigneCommandeDTO>
    modifierLigne(
            @PathVariable Long id,
            @RequestBody LigneCommandeDTO ligne)
            throws LigneCommandeNotFoundException,
            CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        LigneCommandeDTO ligneExistante =
                commandeService.getLigneCommande(id);

        currentUserService.verifierAccesRestaurant(
                restaurantIdDeCommande(ligneExistante.getCommande())
        );

        return ResponseEntity.ok(
                commandeService.modifierLigne(
                        id,
                        ligne
                )
        );
    }




    @DeleteMapping("/lignes/{id}")
    public ResponseEntity<Void>
    supprimerLigne(
            @PathVariable Long id)
            throws LigneCommandeNotFoundException,
            CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        LigneCommandeDTO ligneExistante =
                commandeService.getLigneCommande(id);

        currentUserService.verifierAccesRestaurant(
                restaurantIdDeCommande(ligneExistante.getCommande())
        );

        commandeService.supprimerLigne(id);

        return ResponseEntity.noContent().build();
    }




    @GetMapping("/{id}/montant")
    public ResponseEntity<Double>
    calculerMontantTotal(
            @PathVariable Long id)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierCommandeDuRestaurantConnecte(
                commandeService.getCommande(id)
        );

        return ResponseEntity.ok(
                commandeService.calculerMontantTotal(id)
        );
    }




    @PatchMapping("/{id}/recalculer-montant")
    public ResponseEntity<CommandeDTO>
    recalculerMontantTotal(
            @PathVariable Long id)
            throws CommandeNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierCommandeDuRestaurantConnecte(
                commandeService.getCommande(id)
        );

        return ResponseEntity.ok(
                commandeService.recalculerMontantTotal(id)
        );
    }


    // =========================================================
    // HELPERS INTERNES DE VÉRIFICATION RESTAURANT
    // =========================================================

    // Détermine le restaurant "réel" d'une commande, dans l'ordre :
    // table > employee > clientNonAuthentifie (session) > client authentifié.
    // Les relations sont nullable individuellement. Une commande passée
    // par un client authentifié à emporter ou en livraison n'a ni table,
    // ni employee, ni session anonyme : son restaurant vient alors de
    // commande.client.restaurant.
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
        // il n'y a pas de table, d'employé ou de session anonyme. Le
        // restaurant doit donc être récupéré depuis le client.
        if (commande.getClient() != null
                && commande.getClient().getRestaurant() != null) {

            return commande.getClient()
                    .getRestaurant()
                    .getId_restaurant();
        }

        return null;
    }


    private void verifierCommandeDuRestaurantConnecte(
            CommandeDTO commande)
            throws EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                restaurantIdDeCommande(commande)
        );
    }


    private boolean estDuRestaurant(
            CommandeDTO commande,
            Long restaurantId) {

        Long restaurantDeLaCommande = restaurantIdDeCommande(commande);

        return restaurantDeLaCommande != null
                && restaurantDeLaCommande.equals(restaurantId);
    }


    // Vérifie que les nouvelles références (table / employee / session)
    // envoyées dans un CommandeDTO du frontend appartiennent bien au
    // restaurant connecté, en rechargeant chaque entité réelle en base
    // (jamais celle envoyée telle quelle par le frontend).
    private void verifierReferencesDto(CommandeDTO dto)
            throws EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException, TableNotFoundException,
            ClientNotFoundException {

        if (dto == null) {
            return;
        }

        if (dto.getTable() != null
                && dto.getTable().getId_table() != null) {

            TableRestaurantDTO table =
                    tableService.getTable(
                            dto.getTable().getId_table()
                    );

            currentUserService.verifierAccesRestaurant(
                    table.getRestaurant() != null
                            ? table.getRestaurant().getId_restaurant()
                            : null
            );
        }

        if (dto.getEmployee() != null
                && dto.getEmployee().getId_utilisateur() != null) {

            EmployeeDTO employee =
                    employeeService.getEmployee(
                            dto.getEmployee().getId_utilisateur()
                    );

            currentUserService.verifierAccesRestaurant(
                    employee.getRestaurant() != null
                            ? employee.getRestaurant().getId_restaurant()
                            : null
            );
        }

        if (dto.getClientNonAuthentifie() != null
                && dto.getClientNonAuthentifie().getId_session() != null) {

            ClientNonAuthentifieDTO session =
                    clientNonAuthentifieService
                            .getClientNonAuthentifieById(
                                    dto.getClientNonAuthentifie()
                                            .getId_session()
                            );

            currentUserService.verifierAccesRestaurant(
                    session.getRestaurant() != null
                            ? session.getRestaurant().getId_restaurant()
                            : null
            );
        }
    }
}