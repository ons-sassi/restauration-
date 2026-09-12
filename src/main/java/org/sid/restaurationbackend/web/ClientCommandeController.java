package org.sid.restaurationbackend.web;

import lombok.AllArgsConstructor;
import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.dtos.ClientCommandeConfirmationDTO;
import org.sid.restaurationbackend.dtos.ClientCommandeRequestDTO;
import org.sid.restaurationbackend.dtos.ClientLigneCommandeConfirmationDTO;
import org.sid.restaurationbackend.dtos.CommandeDTO;
import org.sid.restaurationbackend.dtos.ModeleRecuDTO;
import org.sid.restaurationbackend.dtos.TableRestaurantDTO;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.CommandeNotFoundException;
import org.sid.restaurationbackend.exceptions.ModificateurNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;
import org.sid.restaurationbackend.services.ClientAuthentifieService;
import org.sid.restaurationbackend.services.ClientCommandeService;
import org.sid.restaurationbackend.services.CommandeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================
 * SÉCURITÉ — Lot 2.6 (IDOR client, pas un scoping restaurant)
 * =========================================================
 * ⚠️ Découverte critique : getMaCommande(id) appelait
 * commandeService.getCommande(id), qui renvoie N'IMPORTE QUELLE
 * commande sans vérifier son propriétaire. N'importe quel client
 * authentifié pouvait donc lire le détail de la commande de
 * N'IMPORTE QUEL AUTRE CLIENT (tous restaurants confondus) en
 * devinant/incrémentant un id dans l'URL.
 *
 * Le correctif était déjà à moitié en place : CommandeService expose
 * depuis le début une méthode dédiée, getCommandeClient(commandeId,
 * clientId), qui vérifie que la commande appartient bien au client
 * fourni — mais ce contrôleur ne l'appelait pas. Il l'appelle
 * maintenant avec l'id du client réellement connecté (jamais un id
 * fourni par le frontend).
 *
 * =========================================================
 * ÉTAPE 5 (panier + finalisation) — nouveau POST
 * =========================================================
 * L'ancien POST prenait un CommandeDTO brut et appelait
 * commandeService.creerCommandeClient(...), un chemin en réalité non
 * exploitable : CommandeDTO n'a pas de champ "lignes", donc aucune
 * commande créée par ce biais n'a jamais pu contenir de produits (voir
 * ClientCommandeServiceImpl pour le détail). Cet endpoint n'était
 * appelé par aucun écran Angular existant (le panier n'avait pas
 * encore d'écran de finalisation) : le remplacer est donc sans risque
 * de régression. Le nouveau flux passe par ClientCommandeService, qui
 * reçoit le panier complet (lignes + modificateurs) en une seule
 * requête et recalcule tous les prix côté serveur.
 */
@RestController
@RequestMapping("/api/client/commandes")
@AllArgsConstructor
public class ClientCommandeController {

    private final CommandeService commandeService;
    private final ClientAuthentifieService clientAuthentifieService;
    private final ClientCommandeService clientCommandeService;


    /**
     * Finaliser le panier du client connecté en une vraie commande
     * (avec ses lignes et modificateurs).
     */
    @PostMapping
    public ResponseEntity<ClientCommandeConfirmationDTO> creerCommande(
            @RequestBody ClientCommandeRequestDTO requestDTO)
            throws ClientNotFoundException, ProduitNotFoundException,
            ModificateurNotFoundException, TableNotFoundException {

        ClientCommandeConfirmationDTO commande =
                clientCommandeService.creerCommandeDepuisPanier(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commande);
    }


    /**
     * Récupérer les commandes du client connecté.
     */
    @GetMapping
    public ResponseEntity<List<CommandeDTO>> getMesCommandes()
            throws ClientNotFoundException {

        ClientAuthentifieDTO client =
                clientAuthentifieService.getClientConnecte();

        List<CommandeDTO> commandes =
                commandeService.getCommandesByClient(
                        client.getId_utilisateur()
                );

        return ResponseEntity.ok(commandes);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CommandeDTO> getMaCommande(
            @PathVariable Long id)
            throws CommandeNotFoundException, ClientNotFoundException {

        ClientAuthentifieDTO client =
                clientAuthentifieService.getClientConnecte();

        return ResponseEntity.ok(
                commandeService.getCommandeClient(
                        id,
                        client.getId_utilisateur()
                )
        );
    }


    /**
     * Détail des lignes d'une commande du client connecté (historique).
     *
     * CommandeDTO n'a pas de champ "lignes" (voir ClientCommandeService) :
     * ce endpoint dédié réutilise ClientLigneCommandeConfirmationDTO, déjà
     * exploité par la confirmation de commande (étape 5), pour renvoyer
     * des noms déjà résolus (produit, modificateurs).
     */
    @GetMapping("/{id}/lignes")
    public ResponseEntity<List<ClientLigneCommandeConfirmationDTO>> getMesLignesCommande(
            @PathVariable Long id)
            throws ClientNotFoundException, CommandeNotFoundException {

        return ResponseEntity.ok(
                clientCommandeService.getMesLignesCommande(id)
        );
    }


    /**
     * Tables actuellement disponibles (LIBRE) du restaurant du client
     * connecté — alimente le sélecteur de table du mode "sur place"
     * (SAISIE_MANUELLE_NUMERO_TABLE) à l'étape de finalisation du
     * panier, à la place d'un simple champ numérique en saisie libre.
     */
    @GetMapping("/modele-recu")
    public ResponseEntity<ModeleRecuDTO> getModeleRecuClient()
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientCommandeService.getModeleRecuClient()
        );
    }


    @GetMapping("/tables-disponibles")
    public ResponseEntity<List<TableRestaurantDTO>> getTablesDisponibles()
            throws ClientNotFoundException {

        return ResponseEntity.ok(
                clientCommandeService.getTablesDisponibles()
        );
    }
}