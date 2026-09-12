package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.services.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Date;
import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ MULTI-RESTAURANT (voir CurrentUserService) — Lot 2.5
 * =========================================================
 * Vente n'a PAS de colonne restaurant en base : son restaurant est
 * déterminé INDIRECTEMENT, dans cet ordre de priorité :
 *   1) vente.pointDeVente.restaurant
 *   2) vente.employee.restaurant
 *
 * Les endpoints "recapitulatif / par-article / par-categorie / ..." qui
 * reçoivent déjà `Authentication` étaient DÉJÀ scopés côté service
 * (VenteServiceImpl résout le restaurant à partir de l'email connecté,
 * pattern dupliqué indépendamment de CurrentUserService) : ils n'ont pas
 * été touchés.
 *
 * Tout le reste — CRUD ventes, recherche, listes par pdv/employé/mode de
 * paiement/commande, et les reçus — ne faisait AUCUNE vérification et a
 * été corrigé ici.
 *
 * ⚠️ ModePaiement (comme Reduction) n'a pas de colonne restaurant : c'est
 * un référentiel global par conception. getVentesByModePaiement ne peut
 * donc pas vérifier "l'appartenance" du mode de paiement lui-même ; la
 * liste de ventes renvoyée est filtrée après coup sur le restaurant
 * connecté.
 *
 * ⚠️ Point sensible (paiements/transactions) : saveVente/updateVente ne
 * vérifiaient aucune des références (commande / pointDeVente / employee)
 * envoyées par le frontend. Elles sont désormais rechargées en base et
 * vérifiées avant tout enregistrement.
 */
@RestController
@RequestMapping("/api/ventes")
@AllArgsConstructor
@Slf4j
public class VenteController {

    private final VenteService venteService;
    private final CommandeService commandeService;
    private final PointDeVenteService pointDeVenteService;
    private final EmployeeService employeeService;
    private final ModePaiementService modePaiementService;
    private final CurrentUserService currentUserService;


    // ============================================================
    // VENTES
    // ============================================================

    @PostMapping
    public VenteDTO saveVente(
            @RequestBody VenteDTO venteDTO)
            throws ReductionNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException, PointDeVenteNotFoundException,
            CommandeNotFoundException {

        verifierReferencesVenteDto(venteDTO);

        return venteService.saveVente(venteDTO);
    }


    @GetMapping
    public List<VenteDTO> getAllVentes()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return venteService.getAllVentes()
                .stream()
                .filter(v -> estDuRestaurant(v, restaurantId))
                .toList();
    }


    // ============================================================
    // RECAPITULATIF DES VENTES
    // IMPORTANT : avant /{id}
    // Déjà scopé côté service via l'email de l'employé connecté —
    // non modifié.
    // ============================================================

    @GetMapping("/recapitulatif")
    public ResponseEntity<VenteRecapitulatifDTO>
    getRecapitulatifVentes(

            @RequestParam(
                    required = false
            )
            Long pointDeVenteId,

            @RequestParam(
                    required = false
            )
            @DateTimeFormat(
                    pattern = "yyyy-MM-dd"
            )
            Date dateDebut,

            @RequestParam(
                    required = false
            )
            @DateTimeFormat(
                    pattern = "yyyy-MM-dd"
            )
            Date dateFin,

            @RequestParam(
                    required = false,
                    defaultValue = "JOUR"
            )
            String periode,

            Authentication authentication) {


        String emailUtilisateur =
                authentication.getName();


        VenteRecapitulatifDTO resultat =
                venteService.getRecapitulatifVentes(
                        emailUtilisateur,
                        pointDeVenteId,
                        dateDebut,
                        dateFin,
                        periode
                );


        return ResponseEntity.ok(
                resultat
        );
    }

    // ============================================================
    // VENTES DU RECAPITULATIF — déjà scopé côté service, non modifié.
    // ============================================================

    @GetMapping("/recapitulatif/ventes")
    public ResponseEntity<List<VenteDTO>> getVentesRecapitulatif(
            @RequestParam(required = false) Long pointDeVenteId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin,
            Authentication authentication) {

        List<VenteDTO> ventes = venteService.getVentesRecapitulatif(
                authentication.getName(),
                pointDeVenteId,
                dateDebut,
                dateFin
        );

        return ResponseEntity.ok(ventes);
    }


    // ============================================================
    // VENTE PAR ID
    // ============================================================

    @GetMapping("/{id}")
    public VenteDTO getVente(
            @PathVariable Long id)
            throws VenteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        VenteDTO vente = venteService.getVente(id);

        verifierVenteDuRestaurantConnecte(vente);

        return vente;
    }


    @PutMapping("/{id}")
    public VenteDTO updateVente(
            @PathVariable Long id,
            @RequestBody VenteDTO venteDTO)
            throws VenteNotFoundException, ReductionNotFoundException,
            EmployeeNotFoundException, AccesRestaurantNonAutoriseException,
            PointDeVenteNotFoundException, CommandeNotFoundException {

        // La vente existante doit appartenir au restaurant connecté.
        verifierVenteDuRestaurantConnecte(
                venteService.getVente(id)
        );

        // Toute nouvelle référence envoyée dans le corps doit elle aussi
        // appartenir au restaurant connecté.
        verifierReferencesVenteDto(venteDTO);

        return venteService.updateVente(
                id,
                venteDTO
        );
    }


    @DeleteMapping("/{id}")
    public void deleteVente(
            @PathVariable Long id)
            throws VenteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierVenteDuRestaurantConnecte(
                venteService.getVente(id)
        );

        venteService.deleteVente(id);
    }


    // ============================================================
    // RECHERCHE DES VENTES
    // ============================================================

    @GetMapping("/search")
    public List<VenteDTO> searchVentes(

            @RequestParam(required = false)
            Long pointDeVenteId,

            @RequestParam(required = false)
            Long employeeId,

            @RequestParam(required = false)
            Long modePaiementId,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date dateDebut,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date dateFin,

            @RequestParam(required = false)
            Double montantHtMin,

            @RequestParam(required = false)
            Double montantHtMax,

            @RequestParam(required = false)
            Double montantTtcMin,

            @RequestParam(required = false)
            Double montantTtcMax
    )
            throws ModePaiementNotFoundException,
            EmployeeNotFoundException,
            PointDeVenteNotFoundException,
            AccesRestaurantNonAutoriseException {

        PointDeVenteDTO pointDeVente = null;
        EmployeeDTO employee = null;
        ModePaiementDTO modePaiement = null;


        if (pointDeVenteId != null) {

            pointDeVente =
                    pointDeVenteService.getPointDeVente(
                            pointDeVenteId
                    );

            currentUserService.verifierAccesRestaurant(
                    pointDeVente.getRestaurant() != null
                            ? pointDeVente.getRestaurant().getId_restaurant()
                            : null
            );
        }


        if (employeeId != null) {

            employee =
                    employeeService.getEmployee(
                            employeeId
                    );

            currentUserService.verifierAccesRestaurant(
                    employee.getRestaurant() != null
                            ? employee.getRestaurant().getId_restaurant()
                            : null
            );
        }


        if (modePaiementId != null) {

            modePaiement =
                    modePaiementService.getModePaiement(
                            modePaiementId
                    );
        }

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return venteService.searchVentes(
                        pointDeVente,
                        employee,
                        modePaiement,
                        dateDebut,
                        dateFin,
                        montantHtMin,
                        montantHtMax,
                        montantTtcMin,
                        montantTtcMax
                )
                .stream()
                .filter(v -> estDuRestaurant(v, restaurantId))
                .toList();
    }


    // ============================================================
    // VENTES PAR DATE
    // ============================================================

    @GetMapping("/date")
    public List<VenteDTO> getVentesByDate(

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date dateDebut,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date dateFin
    ) throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return venteService.getVentesByDate(
                        dateDebut,
                        dateFin
                )
                .stream()
                .filter(v -> estDuRestaurant(v, restaurantId))
                .toList();
    }


    // ============================================================
    // VENTES PAR COMMANDE
    // ============================================================

    @GetMapping("/commande/{commandeId}")
    public List<VenteDTO> getVentesByCommande(
            @PathVariable Long commandeId)
            throws Exception {

        CommandeDTO commande =
                commandeService.getCommande(
                        commandeId
                );

        currentUserService.verifierAccesRestaurant(
                restaurantIdDeCommande(commande)
        );

        return venteService.getVentesByCommande(
                commande
        );
    }


    // ============================================================
    // VENTES PAR POINT DE VENTE
    // ============================================================

    @GetMapping("/pdv/{pointDeVenteId}")
    public List<VenteDTO> getVentesByPointDeVente(
            @PathVariable Long pointDeVenteId)
            throws Exception {

        PointDeVenteDTO pointDeVente =
                pointDeVenteService.getPointDeVente(
                        pointDeVenteId
                );

        currentUserService.verifierAccesRestaurant(
                pointDeVente.getRestaurant() != null
                        ? pointDeVente.getRestaurant().getId_restaurant()
                        : null
        );

        return venteService.getVentesByPointDeVente(
                pointDeVente
        );
    }


    // ============================================================
    // VENTES PAR EMPLOYE
    // ============================================================

    @GetMapping("/employee/{employeeId}")
    public List<VenteDTO> getVentesByEmployee(
            @PathVariable Long employeeId)
            throws Exception {

        EmployeeDTO employee =
                employeeService.getEmployee(
                        employeeId
                );

        currentUserService.verifierAccesRestaurant(
                employee.getRestaurant() != null
                        ? employee.getRestaurant().getId_restaurant()
                        : null
        );

        return venteService.getVentesByEmployee(
                employee
        );
    }


    // ============================================================
    // VENTES PAR MODE DE PAIEMENT
    //
    // ModePaiement est un référentiel GLOBAL (pas de colonne restaurant,
    // par conception, comme Taxe/Role) : impossible de vérifier son
    // "appartenance". On filtre donc après coup la liste des ventes
    // renvoyées sur le restaurant connecté.
    // ============================================================

    @GetMapping("/mode-paiement/{modePaiementId}")
    public List<VenteDTO> getVentesByModePaiement(
            @PathVariable Long modePaiementId)
            throws Exception {

        ModePaiementDTO modePaiement =
                modePaiementService.getModePaiement(
                        modePaiementId
                );

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return venteService.getVentesByModePaiement(
                        modePaiement
                )
                .stream()
                .filter(v -> estDuRestaurant(v, restaurantId))
                .toList();
    }


    // ============================================================
    // RECUS
    // ============================================================

    @PostMapping("/recus")
    public RecuDTO saveRecu(
            @RequestBody RecuDTO recuDTO)
            throws VenteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        verifierReferenceVenteDuRecu(recuDTO);

        return venteService.saveRecu(
                recuDTO
        );
    }


    @GetMapping("/recus")
    public List<RecuDTO> getAllRecus()
            throws EmployeeNotFoundException {

        Long restaurantId = currentUserService.getRestaurantIdConnecte();

        return venteService.getAllRecus()
                .stream()
                .filter(r -> estDuRestaurant(r.getVente(), restaurantId))
                .toList();
    }


    @GetMapping("/recus/{id}")
    public RecuDTO getRecu(
            @PathVariable Long id)
            throws RecuNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        RecuDTO recu = venteService.getRecu(id);

        currentUserService.verifierAccesRestaurant(
                restaurantIdDeVente(recu.getVente())
        );

        return recu;
    }


    @PutMapping("/recus/{id}")
    public RecuDTO updateRecu(
            @PathVariable Long id,
            @RequestBody RecuDTO recuDTO)
            throws RecuNotFoundException, VenteNotFoundException,
            EmployeeNotFoundException, AccesRestaurantNonAutoriseException {

        RecuDTO recuExistant = venteService.getRecu(id);

        currentUserService.verifierAccesRestaurant(
                restaurantIdDeVente(recuExistant.getVente())
        );

        verifierReferenceVenteDuRecu(recuDTO);

        return venteService.updateRecu(
                id,
                recuDTO
        );
    }


    @DeleteMapping("/recus/{id}")
    public void deleteRecu(
            @PathVariable Long id)
            throws RecuNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        RecuDTO recuExistant = venteService.getRecu(id);

        currentUserService.verifierAccesRestaurant(
                restaurantIdDeVente(recuExistant.getVente())
        );

        venteService.deleteRecu(id);
    }


    // ============================================================
    // RECU D'UNE VENTE
    // ============================================================

    @GetMapping("/{venteId}/recu")
    public RecuDTO getRecuByVente(
            @PathVariable Long venteId)
            throws VenteNotFoundException,
            RecuNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        VenteDTO vente =
                venteService.getVente(
                        venteId
                );

        verifierVenteDuRestaurantConnecte(vente);

        return venteService.getRecusByVente(
                vente
        );
    }

    // ============================================================
    // Les endpoints par-article / par-categorie / ventes-par-* /
    // par-employe / par-modificateur / par-reduction reçoivent tous
    // `Authentication` et sont déjà scopés côté service via l'email de
    // l'employé connecté (même pattern que getRecapitulatifVentes) :
    // non modifiés dans ce lot.
    // ============================================================

    @GetMapping("/par-article")
    public List<VenteParArticleDTO> getVentesParArticle(
            @RequestParam(required = false)
            Long pointDeVenteId,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date dateDebut,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date dateFin,

            Authentication authentication) {

        return venteService.getVentesParArticle(
                authentication.getName(),
                pointDeVenteId,
                dateDebut,
                dateFin
        );
    }

    @GetMapping("/par-categorie")
    public List<VenteParCategorieDTO> getVentesParCategorie(

            @RequestParam(required = false)
            Long pointDeVenteId,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date dateDebut,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date dateFin,

            Authentication authentication) {

        return venteService.getVentesParCategorie(
                authentication.getName(),
                pointDeVenteId,
                dateDebut,
                dateFin
        );
    }

    @GetMapping("/ventes-par-mode-paiement")
    public List<VenteParModePaiementDTO> getVentesParModePaiement(
            Authentication authentication,
            @RequestParam(required = false) Long pointDeVenteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateFin) {

        return venteService.getVentesParModePaiement(
                authentication.getName(),
                pointDeVenteId,
                dateDebut,
                dateFin
        );
    }

    @GetMapping("/ventes-par-recu")
    public List<VenteParRecuDTO> getVentesParRecu(
            Authentication authentication,
            @RequestParam(required = false) Long pointDeVenteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateFin) {

        return venteService.getVentesParRecu(
                authentication.getName(),
                pointDeVenteId,
                dateDebut,
                dateFin
        );
    }

    @GetMapping("/par-employe")
    public List<VenteParEmployeeDTO> getVentesParEmployee(
            Authentication authentication,
            @RequestParam(required = false) Long pointDeVenteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateFin) {

        return venteService.getVentesParEmployee(
                authentication.getName(),
                pointDeVenteId,
                dateDebut,
                dateFin
        );
    }

    @GetMapping("/par-modificateur")
    public List<VenteParModificateurDTO> getVentesParModificateur(
            Authentication authentication,
            @RequestParam(required = false) Long pointDeVenteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateFin) {

        return venteService.getVentesParModificateur(
                authentication.getName(),
                pointDeVenteId,
                dateDebut,
                dateFin
        );
    }

    @GetMapping("/par-reduction")
    public List<VenteParReductionDTO> getVentesParReduction(
            Authentication authentication,
            @RequestParam(required = false) Long pointDeVenteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Date dateFin) {

        return venteService.getVentesParReduction(
                authentication.getName(),
                pointDeVenteId,
                dateDebut,
                dateFin
        );
    }


    // =========================================================
    // HELPERS INTERNES DE VÉRIFICATION RESTAURANT
    // =========================================================

    // Détermine le restaurant "réel" d'une vente : pointDeVente en
    // priorité, sinon employee.
    private Long restaurantIdDeVente(VenteDTO vente) {

        if (vente == null) {
            return null;
        }

        if (vente.getPointDeVente() != null
                && vente.getPointDeVente().getRestaurant() != null) {

            return vente.getPointDeVente()
                    .getRestaurant()
                    .getId_restaurant();
        }

        if (vente.getEmployee() != null
                && vente.getEmployee().getRestaurant() != null) {

            return vente.getEmployee()
                    .getRestaurant()
                    .getId_restaurant();
        }

        return null;
    }


    // Même logique que dans CommandeController, dupliquée ici pour
    // déterminer le restaurant d'une commande référencée par une vente
    // (getVentesByCommande) sans dépendance croisée entre contrôleurs.
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


    private void verifierVenteDuRestaurantConnecte(VenteDTO vente)
            throws EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        currentUserService.verifierAccesRestaurant(
                restaurantIdDeVente(vente)
        );
    }


    private boolean estDuRestaurant(VenteDTO vente, Long restaurantId) {

        Long restaurantDeLaVente = restaurantIdDeVente(vente);

        return restaurantDeLaVente != null
                && restaurantDeLaVente.equals(restaurantId);
    }


    // Vérifie que les références (commande / pointDeVente / employee)
    // envoyées dans un VenteDTO du frontend appartiennent bien au
    // restaurant connecté, en rechargeant chaque entité réelle en base.
    // Si AUCUNE des trois n'est fournie, l'accès est refusé par défaut
    // (verifierAccesRestaurant(null) échoue toujours) : on ne peut pas
    // enregistrer une vente sans pouvoir déterminer son restaurant.
    private void verifierReferencesVenteDto(VenteDTO dto)
            throws EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException,
            PointDeVenteNotFoundException, CommandeNotFoundException {

        if (dto == null) {
            currentUserService.verifierAccesRestaurant(null);
            return;
        }

        Long pdvId =
                dto.getPointDeVente() != null
                        ? dto.getPointDeVente().getId_pdv()
                        : null;

        Long employeeId =
                dto.getEmployee() != null
                        ? dto.getEmployee().getId_utilisateur()
                        : null;

        Long commandeId =
                dto.getCommande() != null
                        ? dto.getCommande().getId_commande()
                        : null;

        if (pdvId == null && employeeId == null && commandeId == null) {
            currentUserService.verifierAccesRestaurant(null);
            return;
        }

        if (pdvId != null) {

            PointDeVenteDTO pdvReel =
                    pointDeVenteService.getPointDeVente(pdvId);

            currentUserService.verifierAccesRestaurant(
                    pdvReel.getRestaurant() != null
                            ? pdvReel.getRestaurant().getId_restaurant()
                            : null
            );
        }

        if (employeeId != null) {

            EmployeeDTO employeeReel =
                    employeeService.getEmployee(employeeId);

            currentUserService.verifierAccesRestaurant(
                    employeeReel.getRestaurant() != null
                            ? employeeReel.getRestaurant().getId_restaurant()
                            : null
            );
        }

        if (commandeId != null) {

            CommandeDTO commandeReelle =
                    commandeService.getCommande(commandeId);

            currentUserService.verifierAccesRestaurant(
                    restaurantIdDeCommande(commandeReelle)
            );
        }
    }


    // Vérifie que la vente référencée par un RecuDTO envoyé par le
    // frontend appartient bien au restaurant connecté.
    private void verifierReferenceVenteDuRecu(RecuDTO recuDTO)
            throws VenteNotFoundException, EmployeeNotFoundException,
            AccesRestaurantNonAutoriseException {

        if (recuDTO == null
                || recuDTO.getVente() == null
                || recuDTO.getVente().getId_vente() == null) {

            currentUserService.verifierAccesRestaurant(null);
            return;
        }

        VenteDTO venteReelle =
                venteService.getVente(
                        recuDTO.getVente().getId_vente()
                );

        verifierVenteDuRestaurantConnecte(venteReelle);
    }
}