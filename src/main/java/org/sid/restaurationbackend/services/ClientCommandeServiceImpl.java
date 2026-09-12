package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;

import org.sid.restaurationbackend.dtos.ClientCommandeConfirmationDTO;
import org.sid.restaurationbackend.dtos.ClientCommandeRequestDTO;
import org.sid.restaurationbackend.dtos.ClientLigneCommandeConfirmationDTO;
import org.sid.restaurationbackend.dtos.ClientLigneCommandeRequestDTO;
import org.sid.restaurationbackend.dtos.ModeleRecuDTO;
import org.sid.restaurationbackend.dtos.TableRestaurantDTO;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Categorie;
import org.sid.restaurationbackend.entities.Commande;
import org.sid.restaurationbackend.entities.LigneCommande;
import org.sid.restaurationbackend.entities.LigneCommandeModificateur;
import org.sid.restaurationbackend.entities.Modificateur;
import org.sid.restaurationbackend.entities.Produit;
import org.sid.restaurationbackend.entities.Restaurant;
import org.sid.restaurationbackend.entities.TableRestaurant;

import org.sid.restaurationbackend.enums.ModeCommande;
import org.sid.restaurationbackend.enums.StatutCommande;
import org.sid.restaurationbackend.enums.StatutTable;

import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.exceptions.CommandeNotFoundException;
import org.sid.restaurationbackend.exceptions.ModificateurNotFoundException;
import org.sid.restaurationbackend.exceptions.ProduitNotFoundException;
import org.sid.restaurationbackend.exceptions.TableNotFoundException;

import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.sid.restaurationbackend.repositories.CommandeRepository;
import org.sid.restaurationbackend.repositories.LigneCommandeModificateurRepository;
import org.sid.restaurationbackend.repositories.LigneCommandeRepository;
import org.sid.restaurationbackend.repositories.ModeleRecuRepository;
import org.sid.restaurationbackend.repositories.ModificateurRepository;
import org.sid.restaurationbackend.repositories.ProduitRepository;
import org.sid.restaurationbackend.repositories.TableRepository;
import org.sid.restaurationbackend.mappers.RestaurantMapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Voir ClientCommandeService pour le pourquoi de cette séparation.
 *
 * ⚠️ SÉCURITÉ — DÉCOUVERTE AVANT CETTE ÉTAPE :
 * L'ancien POST /api/client/commandes (CommandeService.creerCommandeClient
 * + CommandeDTO brut) était en réalité inexploitable pour créer une vraie
 * commande : CommandeDTO n'a pas de champ "lignes", et l'unique endpoint
 * d'ajout de ligne (POST /api/commandes/{id}/lignes) suppose un Employee
 * connecté (currentUserService.verifierAccesRestaurant) — un client
 * authentifié y aurait provoqué une EmployeeNotFoundException, exactement
 * le même bug que celui déjà corrigé pour ProduitController à l'étape 3.
 * Cette implémentation ne réutilise donc PAS ce chemin : le panier entier
 * (lignes + modificateurs) est soumis en une seule requête, et le prix de
 * chaque ligne est intégralement recalculé ici à partir des vraies
 * entités Produit/Modificateur — jamais depuis une valeur envoyée par
 * Angular.
 */
@Service
@Transactional
@AllArgsConstructor
public class ClientCommandeServiceImpl implements ClientCommandeService {

    // Seuls ces modes ont un sens pour une commande passée depuis
    // l'espace client authentifié :
    //  - A_EMPORTER          : aucune référence supplémentaire requise
    //  - LIVRAISON           : adresseLivraison requise
    //  - SAISIE_MANUELLE_NUMERO_TABLE : numeroTable requis, résolu en
    //                          une vraie TableRestaurant du restaurant
    // SUR_PLACE / SCAN_QR_TABLE / EN_LIGNE restent hors périmètre client
    // authentifié (flux PDV / session anonyme / héritage).
    private static final Set<ModeCommande> MODES_AUTORISES = EnumSet.of(
            ModeCommande.A_EMPORTER,
            ModeCommande.LIVRAISON,
            ModeCommande.SAISIE_MANUELLE_NUMERO_TABLE
    );

    private final ClientAuthentifieRepository clientAuthentifieRepository;
    private final ProduitRepository produitRepository;
    private final ModificateurRepository modificateurRepository;
    private final TableRepository tableRepository;
    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final LigneCommandeModificateurRepository ligneCommandeModificateurRepository;
    private final ModeleRecuRepository modeleRecuRepository;
    private final RestaurantMapper dtotMapper;

    @Override
    public ClientCommandeConfirmationDTO creerCommandeDepuisPanier(
            ClientCommandeRequestDTO requestDTO)
            throws ClientNotFoundException, ProduitNotFoundException,
            ModificateurNotFoundException, TableNotFoundException {

        ClientAuthentifie client = getClientConnecte();
        Restaurant restaurant = client.getRestaurant();

        if (requestDTO == null
                || requestDTO.getLignes() == null
                || requestDTO.getLignes().isEmpty()) {

            throw new IllegalArgumentException(
                    "Le panier est vide"
            );
        }

        ModeCommande mode = requestDTO.getModeCommande();

        if (mode == null || !MODES_AUTORISES.contains(mode)) {

            throw new IllegalArgumentException(
                    "Mode de commande non autorisé pour l'espace client : "
                            + mode
            );
        }

        // =====================================================
        // RÉFÉRENCE SELON LE MODE (jamais suivie aveuglément : on
        // recharge/valide la vraie entité correspondante)
        // =====================================================

        TableRestaurant table = null;
        String adresseLivraison = null;

        if (mode == ModeCommande.SAISIE_MANUELLE_NUMERO_TABLE) {

            if (requestDTO.getNumeroTable() == null) {
                throw new IllegalArgumentException(
                        "Le numéro de table est requis pour ce mode de commande"
                );
            }

            table = tableRepository
                    .findByRestaurantAndNumeroTable(restaurant, requestDTO.getNumeroTable())
                    .orElseThrow(() -> new TableNotFoundException(
                            "Aucune table numéro " + requestDTO.getNumeroTable()
                                    + " pour ce restaurant"
                    ));

            // Confirmation automatique de la prise de table : dès que la
            // commande est passée sur cette table, elle passe à OCCUPÉE
            // et disparaît immédiatement de la liste des tables
            // disponibles (getTablesDisponibles ci-dessous, filtrée sur
            // LIBRE). Elle ne redeviendra disponible que lorsqu'un
            // employé la marque explicitement "vidée" (remise à LIBRE,
            // voir TableController / TableRestaurantService.updateStatut
            // côté Back Office).
            table.setStatut(StatutTable.OCCUPEE);
            tableRepository.save(table);

        } else if (mode == ModeCommande.LIVRAISON) {

            if (requestDTO.getAdresseLivraison() == null
                    || requestDTO.getAdresseLivraison().isBlank()) {

                throw new IllegalArgumentException(
                        "L'adresse de livraison est requise pour ce mode de commande"
                );
            }

            adresseLivraison = requestDTO.getAdresseLivraison();
        }

        // =====================================================
        // COMMANDE (en-tête, sauvegardée en premier pour obtenir un id
        // avant de rattacher les lignes)
        // =====================================================

        Commande commande = new Commande();
        commande.setDateCommande(new Date());
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setModeCommande(mode);
        commande.setClient(client);
        commande.setClientNonAuthentifie(null);
        commande.setTable(table);
        commande.setAdresseLivraison(adresseLivraison);
        commande.setMontant_total(0.0);

        Commande commandeSauvegardee = commandeRepository.save(commande);

        // =====================================================
        // LIGNES (prix recalculé serveur, jamais celui du frontend)
        // =====================================================

        List<ClientLigneCommandeConfirmationDTO> lignesConfirmation = new ArrayList<>();
        double montantTotal = 0.0;

        for (ClientLigneCommandeRequestDTO ligneRequest : requestDTO.getLignes()) {

            if (ligneRequest.getProduitId() == null
                    || ligneRequest.getQuantite() == null
                    || ligneRequest.getQuantite() <= 0) {

                throw new IllegalArgumentException(
                        "Ligne de panier invalide (produit ou quantité manquant)"
                );
            }

            Produit produit = getProduitDuRestaurant(ligneRequest.getProduitId(), restaurant);

            if (!Boolean.TRUE.equals(produit.getDisponible())) {
                throw new ProduitNotFoundException(
                        "Produit introuvable avec l'id : " + ligneRequest.getProduitId()
                );
            }

            List<Modificateur> modificateurs = resolveModificateurs(
                    ligneRequest.getModificateurIds(), produit
            );

            double prixModificateurs = modificateurs.stream()
                    .mapToDouble(m -> m.getPrix_supplementaire() != null
                            ? m.getPrix_supplementaire() : 0.0)
                    .sum();

            double prixUnitaire = (produit.getPrix() != null ? produit.getPrix() : 0.0)
                    + prixModificateurs;

            LigneCommande ligne = new LigneCommande();
            ligne.setCommande(commandeSauvegardee);
            ligne.setProduit(produit);
            ligne.setQuantite(ligneRequest.getQuantite());
            ligne.setPrix_unitaire(prixUnitaire);
            ligne.setRemarque(ligneRequest.getRemarque());

            LigneCommande ligneSauvegardee = ligneCommandeRepository.save(ligne);

            // LigneCommandeModificateur n'est pas en cascade depuis
            // LigneCommande (voir l'entité) : sauvegarde explicite,
            // nécessaire une fois que la ligne a un id.
            for (Modificateur modificateur : modificateurs) {

                LigneCommandeModificateur lcm = new LigneCommandeModificateur();
                lcm.setLigneCommande(ligneSauvegardee);
                lcm.setModificateur(modificateur);
                lcm.setQuantite(1);

                ligneCommandeModificateurRepository.save(lcm);
            }

            montantTotal += prixUnitaire * ligneRequest.getQuantite();

            lignesConfirmation.add(new ClientLigneCommandeConfirmationDTO(
                    ligneSauvegardee.getId_ligne_commande(),
                    produit.getNom(),
                    ligneRequest.getQuantite(),
                    prixUnitaire,
                    modificateurs.stream()
                            .map(Modificateur::getNom_modificateur)
                            .collect(Collectors.toList()),
                    getCategorieNom(produit),
                    getSousCategorieNom(produit)
            ));
        }

        commandeSauvegardee.setMontant_total(montantTotal);
        commandeRepository.save(commandeSauvegardee);

        return new ClientCommandeConfirmationDTO(
                commandeSauvegardee.getId_commande(),
                commandeSauvegardee.getDateCommande(),
                commandeSauvegardee.getStatut(),
                commandeSauvegardee.getModeCommande(),
                montantTotal,
                table != null ? table.getNumeroTable() : null,
                adresseLivraison,
                lignesConfirmation
        );
    }

    @Override
    public List<ClientLigneCommandeConfirmationDTO> getMesLignesCommande(
            Long commandeId)
            throws ClientNotFoundException, CommandeNotFoundException {

        ClientAuthentifie client = getClientConnecte();

        Commande commande = getCommandeDuClient(commandeId, client);

        return ligneCommandeRepository.findByCommande(commande).stream()
                .map(this::toLigneConfirmationDto)
                .toList();
    }

    /*
     * Tables affichées dans le sélecteur "sur place" du checkout
     * client :
     *
     * - les tables LIBRES du restaurant (comme avant) ;
     * - EN PLUS, la ou les tables où CE client a déjà une commande
     *   active (EN_ATTENTE ou SERVIE) — même si leur statut est
     *   désormais OCCUPEE. Sans ça, un client déjà installé à une
     *   table (qui vient donc de passer à OCCUPEE, cf.
     *   creerCommandeDepuisPanier ci-dessus) ne pouvait plus
     *   re-sélectionner sa propre table pour une commande
     *   supplémentaire (ex. recommander un dessert).
     *
     * On ne se base jamais sur un numéro de table fourni par le
     * frontend pour ça : uniquement sur les commandes déjà
     * enregistrées de ce client, retrouvé via le token JWT
     * (getClientConnecte), comme partout ailleurs dans ce service.
     */
    @Override
    public List<TableRestaurantDTO> getTablesDisponibles()
            throws ClientNotFoundException {

        ClientAuthentifie client = getClientConnecte();
        Restaurant restaurant = client.getRestaurant();

        List<TableRestaurant> tablesLibres =
                tableRepository.findByRestaurantAndStatut(restaurant, StatutTable.LIBRE);

        List<TableRestaurant> mesTablesActives = commandeRepository
                .findByClient(client)
                .stream()
                .filter(commande ->
                        commande.getStatut() == StatutCommande.EN_ATTENTE
                                || commande.getStatut() == StatutCommande.SERVIE)
                .map(Commande::getTable)
                .filter(table -> table != null
                        && table.getRestaurant() != null
                        && table.getRestaurant().getId_restaurant()
                        .equals(restaurant.getId_restaurant()))
                .toList();

        List<TableRestaurant> tablesAAfficher = new java.util.ArrayList<>(tablesLibres);

        for (TableRestaurant maTable : mesTablesActives) {
            boolean dejaPresente = tablesAAfficher.stream()
                    .anyMatch(t -> t.getId_table().equals(maTable.getId_table()));

            if (!dejaPresente) {
                tablesAAfficher.add(maTable);
            }
        }

        return tablesAAfficher
                .stream()
                .map(dtotMapper::fromTableRestaurant)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ModeleRecuDTO getModeleRecuClient() throws ClientNotFoundException {
        ClientAuthentifie client = getClientConnecte();
        Restaurant restaurant = client.getRestaurant();

        if (restaurant == null || restaurant.getId_restaurant() == null) {
            throw new ClientNotFoundException("Le client n'est rattaché à aucun restaurant");
        }

        return modeleRecuRepository
                .findTopByRestaurantOrderByDateModificationDescIdModeleDesc(restaurant)
                .map(dtotMapper::fromModeleRecu)
                .orElseGet(() -> {
                    ModeleRecuDTO defaut = new ModeleRecuDTO();
                    defaut.setNomModele("Modèle par défaut");
                    defaut.setAfficher_logo(true);
                    defaut.setAfficher_infos_client(false);
                    defaut.setAfficher_commentaire_client(false);
                    defaut.setAfficher_modificateurs_commande(false);
                    defaut.setAfficher_categorie_article(false);
                    defaut.setAfficher_allergies_client(false);
                    defaut.setOrdre_elements("LOGO,RESTAURANT,ENTETE,ARTICLES,TOTAL,CLIENT,ALLERGIES,COMMENTAIRE,PIED_PAGE");
                    defaut.setLargeur_ticket(280);
                    defaut.setTaille_police(12);
                    defaut.setFamille_police("Courier New");
                    defaut.setAlignement("center");
                    defaut.setAfficher_ligne_separation(true);
                    defaut.setEntete_personnalisee("");
                    defaut.setPied_de_page_personnalise("Merci de votre visite !");
                    defaut.setRestaurant(dtotMapper.fromRestaurant(restaurant));
                    return defaut;
                });
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private ClientAuthentifie getClientConnecte() throws ClientNotFoundException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new ClientNotFoundException("Client non authentifié");
        }

        return clientAuthentifieRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new ClientNotFoundException(
                        "Client authentifié introuvable"
                ));
    }

    private Produit getProduitDuRestaurant(Long produitId, Restaurant restaurant)
            throws ProduitNotFoundException {

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ProduitNotFoundException(
                        "Produit introuvable avec l'id : " + produitId));

        if (produit.getRestaurant() == null
                || !restaurant.getId_restaurant().equals(produit.getRestaurant().getId_restaurant())) {
            // 404 plutôt que 403, comme dans ClientMenuServiceImpl : ne
            // pas confirmer à un client l'existence d'un produit d'un
            // autre restaurant.
            throw new ProduitNotFoundException(
                    "Produit introuvable avec l'id : " + produitId);
        }

        return produit;
    }

    private List<Modificateur> resolveModificateurs(List<Long> modificateurIds, Produit produit)
            throws ModificateurNotFoundException {

        if (modificateurIds == null || modificateurIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Modificateur> modificateursDuProduit =
                modificateurRepository.findByProduitsContains(produit);

        List<Modificateur> resolus = new ArrayList<>();

        for (Long modificateurId : modificateurIds) {

            Modificateur modificateur = modificateursDuProduit.stream()
                    .filter(m -> m.getId_modificateur().equals(modificateurId))
                    .findFirst()
                    // N'appartient pas à ce produit (ou n'existe pas du
                    // tout) : même traitement dans les deux cas, pour ne
                    // pas laisser un client deviner les ids d'un autre
                    // restaurant/produit.
                    .orElseThrow(() -> new ModificateurNotFoundException(
                            "Modificateur introuvable pour ce produit : " + modificateurId));

            resolus.add(modificateur);
        }

        return resolus;
    }

    // Recharge une commande et vérifie qu'elle appartient bien au
    // client connecté. 404 (pas 403) si ce n'est pas le cas, même
    // principe que getProduitDuRestaurant / ClientReclamationServiceImpl
    // : ne jamais confirmer à un client l'existence de la commande
    // d'un autre client.
    private Commande getCommandeDuClient(Long commandeId, ClientAuthentifie client)
            throws CommandeNotFoundException {

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new CommandeNotFoundException(
                        "Commande introuvable avec l'id : " + commandeId));

        if (commande.getClient() == null
                || !client.getId_utilisateur().equals(commande.getClient().getId_utilisateur())) {

            throw new CommandeNotFoundException(
                    "Commande introuvable avec l'id : " + commandeId);
        }

        return commande;
    }

    private String getCategorieNom(Produit produit) {
        Categorie direct = produit != null ? produit.getCategorieParent() : null;
        if (direct == null) {
            return null;
        }
        Categorie parent = direct.getCategorieParent();
        return parent != null ? parent.getNom() : direct.getNom();
    }

    private String getSousCategorieNom(Produit produit) {
        Categorie direct = produit != null ? produit.getCategorieParent() : null;
        if (direct == null || direct.getCategorieParent() == null) {
            return null;
        }
        return direct.getNom();
    }

    private ClientLigneCommandeConfirmationDTO toLigneConfirmationDto(LigneCommande ligne) {

        List<String> modificateurs = ligne.getModificateursSelectionnes() == null
                ? Collections.emptyList()
                : ligne.getModificateursSelectionnes().stream()
                .map(lcm -> lcm.getModificateur() != null
                        ? lcm.getModificateur().getNom_modificateur()
                        : null)
                .filter(nom -> nom != null)
                .toList();

        return new ClientLigneCommandeConfirmationDTO(
                ligne.getId_ligne_commande(),
                ligne.getProduit() != null ? ligne.getProduit().getNom() : null,
                ligne.getQuantite(),
                ligne.getPrix_unitaire(),
                modificateurs,
                ligne.getProduit() != null ? getCategorieNom(ligne.getProduit()) : null,
                ligne.getProduit() != null ? getSousCategorieNom(ligne.getProduit()) : null
        );
    }
}