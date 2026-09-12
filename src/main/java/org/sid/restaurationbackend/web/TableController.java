package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.enums.StatutTable;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.services.CurrentUserService;
import org.sid.restaurationbackend.services.EmployeeService;
import org.sid.restaurationbackend.services.TableService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService)
 * =========================================================
 * TableRestaurant est rattachée directement à un restaurant.
 * Toutes les méthodes de ce contrôleur vérifient désormais que
 * ce restaurant est bien celui de l'employé connecté — au lieu
 * de faire confiance à un restaurantId/employeeId fourni par
 * le frontend, ou de renvoyer les tables de tous les
 * restaurants confondus.
 *
 * "/my-tables" (getTablesDuServeurConnecte) utilise le pattern
 * centralisé dans
 * CurrentUserService (identité déduite du JWT, jamais du
 * frontend).
 *
 * ⚠️ Découverte indépendante (non corrigée) : getTableByNumero
 * cherche une table par numéro sans distinction de restaurant
 * en base (TableRepository.findByNumeroTable) — si deux
 * restaurants réutilisent le même numéro de table, seule la
 * première trouvée sera retournée. Le correctif ici se limite
 * à vérifier que la table renvoyée appartient bien au
 * restaurant connecté (donc à refuser l'accès si ce n'est pas
 * le cas), pas à corriger la recherche elle-même.
 */
@RestController
@RequestMapping("/api/tables")
@AllArgsConstructor
public class TableController {

    private final TableService tableService;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;


    // Ajouter une table
    @PostMapping
    public TableRestaurantDTO saveTable(
            @RequestBody TableRestaurantDTO tableRestaurantDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                tableRestaurantDTO.getRestaurant() != null
                        ? tableRestaurantDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return tableService.saveTable(tableRestaurantDTO);
    }


    // Modifier une table
    //
    // La table existante doit appartenir au restaurant connecté, et on
    // ne doit pas pouvoir la "déplacer" vers un autre restaurant.
    @PutMapping("/{id}")
    public TableRestaurantDTO updateTable(
            @PathVariable Long id,
            @RequestBody TableRestaurantDTO tableRestaurantDTO)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO existante = tableService.getTable(id);
        currentUserService.verifierAccesRestaurant(
                existante.getRestaurant() != null
                        ? existante.getRestaurant().getId_restaurant()
                        : null
        );

        if (tableRestaurantDTO.getRestaurant() != null) {
            currentUserService.verifierAccesRestaurant(
                    tableRestaurantDTO.getRestaurant().getId_restaurant()
            );
        }

        return tableService.updateTable(
                id,
                tableRestaurantDTO);
    }


    // Supprimer une table
    @DeleteMapping("/{id}")
    public void deleteTable(
            @PathVariable Long id)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO existante = tableService.getTable(id);
        currentUserService.verifierAccesRestaurant(
                existante.getRestaurant() != null
                        ? existante.getRestaurant().getId_restaurant()
                        : null
        );

        tableService.deleteTable(id);
    }


    // Récupérer une table par ID
    @GetMapping("/{id}")
    public TableRestaurantDTO getTable(
            @PathVariable Long id)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO table = tableService.getTable(id);

        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );

        return table;
    }


    // Récupérer toutes les tables DU RESTAURANT CONNECTÉ
    @GetMapping
    public List<TableRestaurantDTO> getAllTables()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return tableService.getAllTable()
                .stream()
                .filter(t -> t.getRestaurant() != null
                        && restaurantId.equals(t.getRestaurant().getId_restaurant()))
                .toList();
    }


    // Récupérer une table par numéro (voir avertissement en en-tête)
    @GetMapping("/numero/{numeroTable}")
    public TableRestaurantDTO getTableByNumero(
            @PathVariable Integer numeroTable)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO table = tableService.getTableByNumeroTable(
                numeroTable);

        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );

        return table;
    }


    // Récupérer une table par code QR
    @GetMapping("/qr/code/{codeQr}")
    public TableRestaurantDTO getTableByCodeQr(
            @PathVariable String codeQr)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO table = tableService.getTableByCodeQr(codeQr);

        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );

        return table;
    }


    // Récupérer une table par URL QR
    @GetMapping("/qr/url")
    public TableRestaurantDTO getTableByUrlQr(
            @RequestParam String urlQr)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO table = tableService.getTableByUrlQr(urlQr);

        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );

        return table;
    }


    // Tables par statut, DU RESTAURANT CONNECTÉ
    @GetMapping("/statut/{statut}")
    public List<TableRestaurantDTO> getTablesByStatut(
            @PathVariable StatutTable statut)
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return tableService.getTabByStatut(statut)
                .stream()
                .filter(t -> t.getRestaurant() != null
                        && restaurantId.equals(t.getRestaurant().getId_restaurant()))
                .toList();
    }


    // Tables par restaurant
    //
    // Le restaurant demandé DOIT être celui de l'employé connecté.
    @PostMapping("/by-restaurant")
    public List<TableRestaurantDTO> getTablesByRestaurant(
            @RequestBody RestaurantDTO restaurantDTO)
            throws RestaurantNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                restaurantDTO != null
                        ? restaurantDTO.getId_restaurant()
                        : null
        );

        return tableService.getTabByRestaurant(
                restaurantDTO);
    }


    // Tables par serveur attribué
    //
    // L'employé fourni doit appartenir au restaurant connecté, sinon
    // on pourrait lister les tables d'un employé d'un autre restaurant.
    @PostMapping("/by-serveur")
    public List<TableRestaurantDTO> getTablesByServeur(
            @RequestBody EmployeeDTO employeeDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                employeeDTO.getRestaurant() != null
                        ? employeeDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return tableService.getTabByServeurAttribue(
                employeeDTO);
    }


    // =========================================================
    // MES TABLES (phase 3A)
    // =========================================================
    /*
     * Tables dont l'employé CONNECTÉ est responsable ("Mes tables").
     * Contrairement à /by-serveur, ici l'identité de l'employé n'est
     * jamais lue depuis le corps de la requête : elle est déduite du
     * token JWT côté service (SecurityContextHolder). Un employé ne
     * peut donc jamais voir les tables d'un collègue en modifiant un
     * employeeId côté frontend. L'accès à cette page Back Office est
     * réservé aux utilisateurs qui possèdent la fonctionnalité
     * EMPLOYES_TABLES.
     */
    // Protégé par EMPLOYES_MES_TABLES : accès à sa propre liste de
    // tables (PDV et Back Office), distinct de EMPLOYES_TABLES qui
    // gouverne l'attribution des responsables (voir plus bas).
    @PreAuthorize("hasAuthority('EMPLOYES_MES_TABLES')")
    @GetMapping("/my-tables")
    public List<TableRestaurantDTO> getMesTables()
            throws EmployeeNotFoundException {

        return tableService.getTablesDuServeurConnecte();
    }


    // Tables par générateur (employé)
    //
    // Même vérification que /by-serveur : l'employé fourni doit
    // appartenir au restaurant connecté.
    @PostMapping("/by-generateur")
    public List<TableRestaurantDTO> getTablesByGenerateur(
            @RequestBody EmployeeDTO employeeDTO)
            throws EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                employeeDTO.getRestaurant() != null
                        ? employeeDTO.getRestaurant().getId_restaurant()
                        : null
        );

        return tableService.getTabByGenerePar(
                employeeDTO);
    }


    // Activer le QR d'une table
    @PatchMapping("/{id}/qr/activer")
    public TableRestaurantDTO activerQr(
            @PathVariable Long id)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO existante = tableService.getTable(id);
        currentUserService.verifierAccesRestaurant(
                existante.getRestaurant() != null
                        ? existante.getRestaurant().getId_restaurant()
                        : null
        );

        return tableService.activerQr(id);
    }


    // Désactiver le QR d'une table
    @PatchMapping("/{id}/qr/desactiver")
    public TableRestaurantDTO desactiverQr(
            @PathVariable Long id)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO existante = tableService.getTable(id);
        currentUserService.verifierAccesRestaurant(
                existante.getRestaurant() != null
                        ? existante.getRestaurant().getId_restaurant()
                        : null
        );

        return tableService.desactiverQr(id);
    }


    // Récupérer une table par QR actif
    @GetMapping("/{id}/qr")
    public TableRestaurantDTO getTableByQrActif(
            @PathVariable Long id)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO table = tableService.getTableByQrActif(id);

        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );

        return table;
    }


    // Tables disponibles, DU RESTAURANT CONNECTÉ
    @GetMapping("/disponibles")
    public List<TableRestaurantDTO> getTablesDisponibles()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return tableService.getTablesDisponibles()
                .stream()
                .filter(t -> t.getRestaurant() != null
                        && restaurantId.equals(t.getRestaurant().getId_restaurant()))
                .toList();
    }


    // Tables disponibles par restaurant
    //
    // Le restaurant demandé DOIT être celui de l'employé connecté.
    @PostMapping("/disponibles/by-restaurant")
    public List<TableRestaurantDTO> getTablesDisponiblesByRestaurant(
            @RequestBody RestaurantDTO restaurantDTO)
            throws RestaurantNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                restaurantDTO != null
                        ? restaurantDTO.getId_restaurant()
                        : null
        );

        return tableService.getTablesDisponiblesByRestaurant(
                restaurantDTO);
    }


    // =========================================================
    // AFFECTATION PERMANENTE D'UN RESPONSABLE (serveurAttribue)
    // =========================================================

    /*
     * Protégé par la fonctionnalité existante EMPLOYES_TABLES
     * ("Attribuer des serveurs aux tables"). Le backend vérifie la
     * permission indépendamment de ce que montre/cache le frontend :
     * seuls les employés dont le rôle possède cette permission (ex.
     * Admin, Serveur) peuvent attribuer ou retirer un responsable.
     * Sert aussi bien pour attribuer une table non affectée que pour
     * changer le responsable d'une table déjà affectée.
     *
     * Sécurité multi-restaurant : la table ET l'employé désigné
     * doivent tous les deux appartenir au restaurant connecté —
     * impossible d'attribuer une table d'un autre restaurant, ou d'y
     * affecter un employé d'un autre restaurant.
     */
    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @PatchMapping("/{id}/serveur/{employeeId}")
    public TableRestaurantDTO assignerServeurResponsable(
            @PathVariable Long id,
            @PathVariable Long employeeId)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO table = tableService.getTable(id);
        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );

        EmployeeDTO employee = employeeService.getEmployee(employeeId);
        currentUserService.verifierAccesRestaurant(
                employee.getRestaurant() != null
                        ? employee.getRestaurant().getId_restaurant()
                        : null
        );

        return tableService.assignerServeurResponsable(id, employeeId);
    }


    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @DeleteMapping("/{id}/serveur")
    public TableRestaurantDTO retirerServeurResponsable(
            @PathVariable Long id)
            throws TableNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        TableRestaurantDTO table = tableService.getTable(id);
        currentUserService.verifierAccesRestaurant(
                table.getRestaurant() != null
                        ? table.getRestaurant().getId_restaurant()
                        : null
        );

        return tableService.retirerServeurResponsable(id);
    }


    @PreAuthorize("hasAuthority('EMPLOYES_TABLES')")
    @GetMapping("/employes-eligibles")
    public List<EmployeeDTO> getEmployesEligiblesResponsablesTables()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return tableService.getEmployesEligiblesResponsablesTables()
                .stream()
                .filter(e -> e.getRestaurant() != null
                        && restaurantId.equals(e.getRestaurant().getId_restaurant()))
                .toList();
    }
}