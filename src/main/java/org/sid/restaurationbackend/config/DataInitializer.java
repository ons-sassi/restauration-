package org.sid.restaurationbackend.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.entities.Module;
import org.sid.restaurationbackend.enums.*;
import org.sid.restaurationbackend.repositories.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    // =========================================================
    // REPOSITORIES
    // =========================================================

    private final RestaurantRepository restaurantRepository;
    private final ModuleRepository moduleRepository;
    private final FonctionnaliteRepository fonctionnaliteRepository;
    private final RoleRepository roleRepository;
    private final RoleFonctionnaliteRepository roleFonctionnaliteRepository;

    private final EmployeeRepository employeeRepository;
    private final ClientAuthentifieRepository clientAuthentifieRepository;
    private final SuperAdminRepository superAdminRepository;

    private final PointDeVenteRepository pointDeVenteRepository;
    private final TableRepository tableRepository;
    private final PriseEnChargeTableRepository priseEnChargeTableRepository;

    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;

    private final FournisseurRepository fournisseurRepository;
    private final IngredientRepository ingredientRepository;

    private final ModificateurRepository modificateurRepository;
    private final ModePaiementRepository modePaiementRepository;

    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final LigneCommandeModificateurRepository ligneCommandeModificateurRepository;

    private final ReservationRepository reservationRepository;
    private final ReclamationRepository reclamationRepository;
    private final SuggestionRepository suggestionRepository;
    private final NotificationRepository notificationRepository;

    private final TaxeRepository taxeRepository;
    private final ReductionRepository reductionRepository;

    private final VenteRepository venteRepository;
    private final RecuRepository recuRepository;

    private final SessionCaisseRepository sessionCaisseRepository;

    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // INITIALISATION
    // =========================================================

    @Override
    @Transactional
    public void run(String... args) {

        /*
         * ---------------------------------------------------------
         * IMPORTANT
         * ---------------------------------------------------------
         *
         * On conserve le comportement actuel :
         *
         * - si la base contient déjà un restaurant :
         *   aucune donnée de test n'est recréée.
         *
         * - sur une base vide :
         *   toutes les données sont créées.
         *
         * Le catalogue des fonctionnalités est donc créé en même
         * temps que les données initiales.
         */

        if (restaurantRepository.count() > 0) {

            log.info(
                    "DataInitializer : des données existent déjà, insertion ignorée."
            );

            return;
        }

        log.info("========================================================");
        log.info("DataInitializer : insertion des données de test...");
        log.info("========================================================");


        // =========================================================
        // RESTAURANT
        // =========================================================

        Restaurant restaurant = new Restaurant();

        restaurant.setNomRestaurant("Le Gourmet");

        restaurant.setAdresse(
                "12 Avenue Habib Bourguiba, Tunis"
        );

        restaurant.setDevise(
                Devise.DINAR
        );

        restaurant.setLangue_par_defaut(
                LangueParDefaut.FRANCAIS
        );

        restaurant.setStatut(
                StatutRestaurant.ACTIF
        );

        restaurant.setHoraires_ouverture(
                LocalTime.of(9, 0)
        );

        restaurant.setHoraires_fermeture(
                LocalTime.of(23, 0)
        );

        restaurant = restaurantRepository.save(restaurant);


        // =========================================================
        // CATALOGUE COMPLET DES MODULES / FONCTIONNALITES
        // =========================================================

        /*
         * Structure :
         *
         * MODULE
         *   |
         *   +--- FONCTIONNALITE PRINCIPALE
         *          |
         *          +--- SOUS-FONCTIONNALITE
         *
         * Une fonctionnalité parent représente généralement
         * l'accès à une page / section.
         *
         * Les enfants représentent les actions :
         *
         * - voir
         * - ajouter
         * - modifier
         * - supprimer
         * - etc.
         */

        // =========================================================
        // GESTION DES VENTES
        // =========================================================

        Module moduleVentes = createModule(
                "Gestion des ventes",
                "receipt_long",
                1,
                true,
                true
        );

        Fonctionnalite ventesRecapitulatif = createFeature(
                moduleVentes,
                "Récapitulation des ventes",
                "VENTES_RECAPITULATIF",
                true,
                true,
                1
        );

        Fonctionnalite ventesParArticle = createFeature(
                moduleVentes,
                "Ventes par article",
                "VENTES_PAR_ARTICLE",
                false,
                true,
                2
        );

        Fonctionnalite ventesParCategorie = createFeature(
                moduleVentes,
                "Ventes par catégorie",
                "VENTES_PAR_CATEGORIE",
                false,
                true,
                3
        );

        Fonctionnalite ventesParModePaiement = createFeature(
                moduleVentes,
                "Ventes par mode de paiement",
                "VENTES_PAR_MODE_PAIEMENT",
                false,
                true,
                4
        );

        Fonctionnalite ventesParRecu = createFeature(
                moduleVentes,
                "Ventes par reçu",
                "VENTES_PAR_RECU",
                false,
                true,
                5
        );

        Fonctionnalite ventesParEmploye = createFeature(
                moduleVentes,
                "Ventes par employé",
                "VENTES_PAR_EMPLOYE",
                false,
                true,
                6
        );

        Fonctionnalite ventesParModificateur = createFeature(
                moduleVentes,
                "Ventes par modificateur",
                "VENTES_PAR_MODIFICATEUR",
                false,
                true,
                7
        );

        Fonctionnalite ventesReductions = createFeature(
                moduleVentes,
                "Réductions",
                "VENTES_REDUCTIONS",
                false,
                true,
                8
        );

        Fonctionnalite gestionCaisse = createFeature(
                moduleVentes,
                "Gestion caisse",
                "GESTION_CAISSE",
                true,
                true,
                9
        );

        Fonctionnalite accepterPaiements = createFeature(
                moduleVentes,
                "Accepter les paiements",
                "VENTES_ACCEPTER_PAIEMENTS",
                true,
                true,
                10
        );


        // =========================================================
        // GESTION DU STOCK
        // =========================================================

        Module moduleStock = createModule(
                "Gestion du stock",
                "inventory_2",
                2,
                true,
                true
        );

        Fonctionnalite stockEtat = createFeature(
                moduleStock,
                "Voir état des stocks",
                "STOCK_ETAT",
                true,
                true,
                1
        );

        Fonctionnalite stockCommandes = createFeature(
                moduleStock,
                "Historique des commandes et état de livraison",
                "STOCK_COMMANDES",
                true,
                true,
                2
        );

        createChildFeature(
                stockCommandes,
                "Ajouter bon de commande",
                "STOCK_COMMANDES_AJOUTER",
                true,
                true,
                1
        );

        Fonctionnalite stockIngredients = createFeature(
                moduleStock,
                "Ajouter ingrédients au magasin",
                "STOCK_INGREDIENTS",
                false,
                true,
                3
        );

        Fonctionnalite stockFournisseurs = createFeature(
                moduleStock,
                "Ajouter fournisseur",
                "STOCK_FOURNISSEURS",
                false,
                true,
                4
        );


        // =========================================================
        // GESTION DES TAXES
        // =========================================================

        Module moduleTaxes = createModule(
                "Gestion des taxes",
                "percent",
                3,
                false,
                true
        );

        Fonctionnalite taxeAjouter = createFeature(
                moduleTaxes,
                "Ajouter taxe",
                "TAXE_AJOUTER",
                false,
                true,
                1
        );

        Fonctionnalite taxeListe = createFeature(
                moduleTaxes,
                "Voir liste des taxes et détails",
                "TAXE_LISTE",
                false,
                true,
                2
        );

        createChildFeature(
                taxeListe,
                "Modifier taxe",
                "TAXE_MODIFIER",
                false,
                true,
                1
        );

        createChildFeature(
                taxeListe,
                "Supprimer taxe",
                "TAXE_SUPPRIMER",
                false,
                true,
                2
        );

        Fonctionnalite taxeStatistiques = createFeature(
                moduleTaxes,
                "Voir les statistiques",
                "TAXE_STATISTIQUES",
                false,
                true,
                3
        );


        // =========================================================
        // GESTION DU MENU
        // =========================================================

        Module moduleMenu = createModule(
                "Gestion du menu",
                "restaurant_menu",
                4,
                true,
                true
        );


        // ---------------------------------------------------------
        // PRODUITS
        // ---------------------------------------------------------

        Fonctionnalite menuProduits = createFeature(
                moduleMenu,
                "Produits",
                "MENU_PRODUITS",
                true,
                true,
                1
        );

        Fonctionnalite menuProduitsVoir = createChildFeature(
                menuProduits,
                "Voir liste des produits",
                "MENU_PRODUITS_VOIR",
                true,
                true,
                1
        );

        createChildFeature(
                menuProduits,
                "Ajouter produit",
                "MENU_PRODUITS_AJOUTER",
                true,
                true,
                2
        );

        createChildFeature(
                menuProduits,
                "Modifier produit",
                "MENU_PRODUITS_MODIFIER",
                true,
                true,
                3
        );

        createChildFeature(
                menuProduits,
                "Supprimer produit",
                "MENU_PRODUITS_SUPPRIMER",
                true,
                true,
                4
        );


        // ---------------------------------------------------------
        // CATEGORIES
        // ---------------------------------------------------------

        Fonctionnalite menuCategories = createFeature(
                moduleMenu,
                "Catégories",
                "MENU_CATEGORIES",
                true,
                true,
                2
        );

        Fonctionnalite menuCategoriesVoir = createChildFeature(
                menuCategories,
                "Voir catégories",
                "MENU_CATEGORIES_VOIR",
                true,
                true,
                1
        );

        createChildFeature(
                menuCategories,
                "Ajouter catégorie",
                "MENU_CATEGORIES_AJOUTER",
                true,
                true,
                2
        );

        createChildFeature(
                menuCategories,
                "Modifier catégorie",
                "MENU_CATEGORIES_MODIFIER",
                true,
                true,
                3
        );

        createChildFeature(
                menuCategories,
                "Supprimer catégorie",
                "MENU_CATEGORIES_SUPPRIMER",
                true,
                true,
                4
        );


        // ---------------------------------------------------------
        // MODIFICATEURS
        // ---------------------------------------------------------

        Fonctionnalite menuModificateurs = createFeature(
                moduleMenu,
                "Modificateurs",
                "MENU_MODIFICATEURS",
                true,
                true,
                3
        );

        Fonctionnalite menuModificateursVoir = createChildFeature(
                menuModificateurs,
                "Voir modificateurs",
                "MENU_MODIFICATEURS_VOIR",
                true,
                true,
                1
        );

        createChildFeature(
                menuModificateurs,
                "Ajouter modificateur",
                "MENU_MODIFICATEURS_AJOUTER",
                true,
                true,
                2
        );

        createChildFeature(
                menuModificateurs,
                "Modifier modificateur",
                "MENU_MODIFICATEURS_MODIFIER",
                true,
                true,
                3
        );

        createChildFeature(
                menuModificateurs,
                "Supprimer modificateur",
                "MENU_MODIFICATEURS_SUPPRIMER",
                true,
                true,
                4
        );


        // ---------------------------------------------------------
        // REDUCTIONS
        // ---------------------------------------------------------

        Fonctionnalite menuReductions = createFeature(
                moduleMenu,
                "Réductions",
                "MENU_REDUCTIONS",
                true,
                true,
                4
        );

        Fonctionnalite menuReductionsVoir = createChildFeature(
                menuReductions,
                "Voir réductions",
                "MENU_REDUCTIONS_VOIR",
                true,
                true,
                1
        );

        createChildFeature(
                menuReductions,
                "Ajouter réduction",
                "MENU_REDUCTIONS_AJOUTER",
                true,
                true,
                2
        );

        createChildFeature(
                menuReductions,
                "Modifier réduction",
                "MENU_REDUCTIONS_MODIFIER",
                true,
                true,
                3
        );

        createChildFeature(
                menuReductions,
                "Supprimer réduction",
                "MENU_REDUCTIONS_SUPPRIMER",
                true,
                true,
                4
        );



        // =========================================================
        // GESTION DES EMPLOYES
        // =========================================================

        Module moduleEmployes = createModule(
                "Gestion des employés",
                "groups",
                5,
                true,
                true
        );

        Fonctionnalite employesListe = createFeature(
                moduleEmployes,
                "Voir liste des employés",
                "EMPLOYES_LISTE",
                true,
                true,
                1
        );

        createChildFeature(
                employesListe,
                "Ajouter un employé",
                "EMPLOYES_AJOUTER",
                false,
                true,
                1
        );

        createChildFeature(
                employesListe,
                "Modifier un employé",
                "EMPLOYES_MODIFIER",
                false,
                true,
                2
        );

        createChildFeature(
                employesListe,
                "Supprimer un employé",
                "EMPLOYES_SUPPRIMER",
                false,
                true,
                3
        );

        Fonctionnalite employesTables = createFeature(
                moduleEmployes,
                "Attribuer des serveurs aux tables",
                "EMPLOYES_TABLES",
                true,
                true,
                2
        );

        /*
         * "Mes tables" est volontairement une fonctionnalité DISTINCTE
         * de EMPLOYES_TABLES ci-dessus :
         *
         * - EMPLOYES_TABLES  : action d'ADMINISTRATION (attribuer /
         *   retirer le responsable permanent d'une table).
         * - EMPLOYES_MES_TABLES : accès à sa PROPRE liste de tables
         *   (consultation + cycle de prise en charge), disponible en
         *   PDV comme en Back Office.
         *
         * Cela permet d'accorder à un rôle l'accès à "Mes tables"
         * sans lui donner le droit d'attribuer des serveurs aux
         * tables, et inversement.
         */
        Fonctionnalite employesMesTables = createFeature(
                moduleEmployes,
                "Voir mes tables",
                "EMPLOYES_MES_TABLES",
                true,
                true,
                3
        );

        Fonctionnalite employesPresence = createFeature(
                moduleEmployes,
                "Modifier feuille de présence",
                "EMPLOYES_PRESENCE",
                true,
                true,
                4
        );

        createChildFeature(
                employesPresence,
                "Pénalité de retard automatique",
                "EMPLOYES_PRESENCE_PENALITE",
                true,
                true,
                1
        );


        // =========================================================
        // GESTION DES CLIENTS
        // =========================================================

        Module moduleClients = createModule(
                "Gestion des clients",
                "groups",
                6,
                true,
                true
        );


        // ---------------------------------------------------------
        // LISTE CLIENTS
        // ---------------------------------------------------------

        Fonctionnalite clientsListe = createFeature(
                moduleClients,
                "Voir liste des clients",
                "CLIENTS_LISTE",
                false,
                true,
                1
        );


        // ---------------------------------------------------------
        // PLAN DES TABLES / COMMANDES
        // ---------------------------------------------------------

        Fonctionnalite clientsPlanTables = createFeature(
                moduleClients,
                "Voir plan de table et état des commandes",
                "CLIENTS_PLAN_TABLES",
                true,
                true,
                2
        );

        createChildFeature(
                clientsPlanTables,
                "Annuler commande",
                "CLIENTS_COMMANDE_ANNULER",
                true,
                true,
                1
        );

        createChildFeature(
                clientsPlanTables,
                "Modifier commande",
                "CLIENTS_COMMANDE_MODIFIER",
                true,
                true,
                2
        );

        createChildFeature(
                clientsPlanTables,
                "Prendre en charge une table",
                "CLIENTS_TABLE_PRENDRE_EN_CHARGE",
                true,
                true,
                3
        );


        // ---------------------------------------------------------
        // RECLAMATIONS
        // ---------------------------------------------------------

        Fonctionnalite clientsReclamations = createFeature(
                moduleClients,
                "Réclamations",
                "CLIENTS_RECLAMATIONS",
                false,
                true,
                3
        );

        createChildFeature(
                clientsReclamations,
                "Répondre au client",
                "CLIENTS_RECLAMATIONS_REPONDRE",
                false,
                true,
                1
        );

        createChildFeature(
                clientsReclamations,
                "Proposer des solutions",
                "CLIENTS_RECLAMATIONS_SOLUTIONS",
                false,
                true,
                2
        );


        // ---------------------------------------------------------
        // CONSEILS CLIENT
        // ---------------------------------------------------------

        Fonctionnalite clientsConseils = createFeature(
                moduleClients,
                "Conseils client",
                "CLIENTS_CONSEILS",
                false,
                true,
                4
        );


        // ---------------------------------------------------------
        // AJOUTER COMMANDE
        // ---------------------------------------------------------

        Fonctionnalite clientsCommandeAjouter = createFeature(
                moduleClients,
                "Ajouter une commande",
                "CLIENTS_COMMANDE_AJOUTER",
                true,
                true,
                5
        );

        Fonctionnalite clientsCommandePaiement = createChildFeature(
                clientsCommandeAjouter,
                "Accepter paiement",
                "CLIENTS_COMMANDE_PAIEMENT",
                true,
                true,
                1
        );


        // ---------------------------------------------------------
        // LISTE COMMANDES
        // ---------------------------------------------------------

        Fonctionnalite clientsCommandesListe = createFeature(
                moduleClients,
                "Voir liste des commandes",
                "CLIENTS_COMMANDES",
                false,
                true,
                6
        );


        // ---------------------------------------------------------
        // LISTE RESERVATIONS
        // ---------------------------------------------------------

        Fonctionnalite reservationsListe = createFeature(
                moduleClients,
                "Voir liste des réservations",
                "RESERVATIONS_LISTE",
                false,
                true,
                7
        );

        createChildFeature(
                reservationsListe,
                "Modifier une réservation",
                "RESERVATIONS_MODIFIER",
                false,
                true,
                1
        );

        createChildFeature(
                reservationsListe,
                "Annuler une réservation",
                "RESERVATIONS_ANNULER",
                false,
                true,
                2
        );


        // =========================================================
        // PARAMETRES DU COMPTE
        // =========================================================

        Module moduleParametres = createModule(
                "Paramètres du compte",
                "settings",
                7,
                false,
                true
        );

        Fonctionnalite parametresPaiement = createFeature(
                moduleParametres,
                "Modifier méthode de paiement",
                "PARAMETRES_PAIEMENT",
                false,
                true,
                1
        );

        Fonctionnalite parametresRecu = createFeature(
                moduleParametres,
                "Gestion détail du reçu",
                "PARAMETRES_RECU",
                false,
                true,
                2
        );

        Fonctionnalite parametresLangue = createFeature(
                moduleParametres,
                "Modifier langue",
                "PARAMETRES_LANGUE",
                false,
                true,
                3
        );

        Fonctionnalite parametresRestauration = createFeature(
                moduleParametres,
                "Modifier options de restauration",
                "PARAMETRES_RESTAURATION",
                false,
                true,
                4
        );

        Fonctionnalite parametresAppareils = createFeature(
                moduleParametres,
                "Gestion appareils PDV",
                "PARAMETRES_APPAREILS_PDV",
                false,
                true,
                5
        );

        createChildFeature(
                parametresAppareils,
                "Ajouter appareil PDV",
                "PARAMETRES_APPAREILS_PDV_AJOUTER",
                false,
                true,
                1
        );

        createChildFeature(
                parametresAppareils,
                "Modifier appareil PDV",
                "PARAMETRES_APPAREILS_PDV_MODIFIER",
                false,
                true,
                2
        );

        createChildFeature(
                parametresAppareils,
                "Supprimer appareil PDV",
                "PARAMETRES_APPAREILS_PDV_SUPPRIMER",
                false,
                true,
                3
        );

        Fonctionnalite parametresCompte = createFeature(
                moduleParametres,
                "Modifier données du compte",
                "PARAMETRES_COMPTE",
                false,
                true,
                6
        );


        // =========================================================
        // GESTION GENERALE
        // =========================================================

        Module moduleGeneral = createModule(
                "Gestion générale",
                "dashboard",
                8,
                true,
                true
        );

        Fonctionnalite dashboard = createFeature(
                moduleGeneral,
                "Tableau de bord",
                "DASHBOARD",
                true,
                true,
                1
        );

        Fonctionnalite gestionCommandes = createFeature(
                moduleGeneral,
                "Gestion des commandes",
                "COMMANDES",
                true,
                true,
                2
        );


        // =========================================================
        // ROLES
        // =========================================================

        Role roleSuperAdmin = new Role();

        roleSuperAdmin.setNom_role(
                "SUPERADMIN"
        );

        roleSuperAdmin.setDescription(
                "Accès global à la plateforme et au Back Office"
        );

        roleSuperAdmin.setAcces_backoffice(
                true
        );

        roleSuperAdmin.setAcces_pdv(
                false
        );

        roleSuperAdmin.setDate_creation(
                new Date()
        );

        roleSuperAdmin = roleRepository.save(
                roleSuperAdmin
        );


        Role roleAdmin = new Role();

        roleAdmin.setNom_role(
                "ADMIN"
        );

        roleAdmin.setDescription(
                "Gérant du restaurant — accès complet au Back Office de son restaurant"
        );

        roleAdmin.setAcces_backoffice(
                true
        );

        roleAdmin.setAcces_pdv(
                false
        );

        roleAdmin.setDate_creation(
                new Date()
        );

        roleAdmin = roleRepository.save(
                roleAdmin
        );


        Role roleServeur = new Role();

        roleServeur.setNom_role(
                "Serveur"
        );

        roleServeur.setDescription(
                "Accès aux fonctionnalités opérationnelles du PDV"
        );

        roleServeur.setAcces_backoffice(
                false
        );

        roleServeur.setAcces_pdv(
                true
        );

        roleServeur.setDate_creation(
                new Date()
        );

        roleServeur = roleRepository.save(
                roleServeur
        );


        // =========================================================
        // ROLES SUPPLEMENTAIRES
        // =========================================================
        /*
         * MANAGER       : gestion opérationnelle du Back Office.
         * CAISSIER      : caisse, ventes et commandes.
         * CUISINIER     : préparation des commandes et stock au PDV.
         * RESPONSABLE_SALLE : tables, employés et relation client.
         *
         * Les rôles historiques SUPERADMIN / ADMIN / Serveur sont
         * conservés et restent disponibles.
         */

        Role roleManager = creerRole(
                "MANAGER",
                "Responsable opérationnel du restaurant",
                true,
                false
        );

        Role roleCaissier = creerRole(
                "CAISSIER",
                "Gestionnaire de caisse et des commandes",
                true,
                false
        );

        Role roleCuisinier = creerRole(
                "CUISINIER",
                "Préparation des commandes et consultation du stock",
                false,
                true
        );

        Role roleResponsableSalle = creerRole(
                "RESPONSABLE_SALLE",
                "Gestion de salle, tables, employés et clients",
                true,
                false
        );


        // =========================================================
        // PERMISSIONS SUPERADMIN
        // =========================================================

        /*
         * SUPERADMIN :
         *
         * Toutes les fonctionnalités disponibles dans le système
         * sont autorisées en Back Office.
         *
         * Pour le PDV, seules les fonctionnalités marquées
         * disponiblePdv = true sont attribuées.
         */

        List<Fonctionnalite> toutesFonctionnalites =
                fonctionnaliteRepository.findAll();

        for (Fonctionnalite fonctionnalite : toutesFonctionnalites) {

            if (Boolean.TRUE.equals(
                    fonctionnalite.getDisponibleBackoffice()
            )) {

                saveRoleFonctionnalite(
                        roleSuperAdmin,
                        fonctionnalite,
                        InterfaceType.BACKOFFICE
                );
            }

            // Le SUPERADMIN travaille dans le Back Office.
            // Il n'est pas un employé de PDV et ne reçoit donc
            // aucune permission PDV via ce rôle.
        }


        // =========================================================
        // PERMISSIONS ADMIN (gérant de restaurant)
        // =========================================================

        /*
         * ADMIN : toutes les fonctionnalités disponibles en
         * Back Office (comme SUPERADMIN), mais restreintes à SON
         * restaurant (Employee.restaurant), contrairement au
         * SUPERADMIN qui n'est rattaché à aucun restaurant.
         */

        for (Fonctionnalite fonctionnalite : toutesFonctionnalites) {

            if (Boolean.TRUE.equals(
                    fonctionnalite.getDisponibleBackoffice()
            )) {

                saveRoleFonctionnalite(
                        roleAdmin,
                        fonctionnalite,
                        InterfaceType.BACKOFFICE
                );
            }
        }


        // =========================================================
        // PERMISSIONS SERVEUR
        // =========================================================

        /*
         * Le serveur possède uniquement les fonctionnalités
         * opérationnelles du PDV.
         */

        saveRoleFonctionnalite(
                roleServeur,
                dashboard,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                gestionCommandes,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                gestionCaisse,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                accepterPaiements,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                stockEtat,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                stockCommandes,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                menuProduits,
                InterfaceType.PDV
        );

        // Code réellement vérifié par le frontend (route/menu produits).
        saveRoleFonctionnalite(
                roleServeur,
                menuProduitsVoir,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                menuCategories,
                InterfaceType.PDV
        );

        // Code réellement vérifié par le frontend (route/menu catégories).
        saveRoleFonctionnalite(
                roleServeur,
                menuCategoriesVoir,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                menuModificateurs,
                InterfaceType.PDV
        );

        // Code réellement vérifié par le frontend (route/menu modificateurs).
        saveRoleFonctionnalite(
                roleServeur,
                menuModificateursVoir,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                menuReductions,
                InterfaceType.PDV
        );

        // Code réellement vérifié par le frontend (route/menu réductions).
        saveRoleFonctionnalite(
                roleServeur,
                menuReductionsVoir,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                employesListe,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                employesTables,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                employesMesTables,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                employesPresence,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                clientsPlanTables,
                InterfaceType.PDV
        );

        saveRoleFonctionnalite(
                roleServeur,
                clientsCommandeAjouter,
                InterfaceType.PDV
        );


        // =========================================================
        // PERMISSIONS ROLES SUPPLEMENTAIRES
        // =========================================================

        // MANAGER : toutes les fonctionnalités Back Office
        // disponibles dans le restaurant, sauf la gestion technique
        // des appareils PDV.
        for (Fonctionnalite fonctionnalite : toutesFonctionnalites) {
            if (Boolean.TRUE.equals(fonctionnalite.getDisponibleBackoffice())
                    && !"PARAMETRES_APPAREILS_PDV".equals(fonctionnalite.getCodeFonctionnalite())) {
                saveRoleFonctionnalite(
                        roleManager,
                        fonctionnalite,
                        InterfaceType.BACKOFFICE
                );
            }
        }

        autoriser(
                roleCaissier,
                InterfaceType.BACKOFFICE,
                dashboard,
                gestionCommandes,
                ventesRecapitulatif,
                gestionCaisse,
                accepterPaiements,
                clientsPlanTables,
                clientsCommandeAjouter,
                clientsCommandesListe,
                reservationsListe,
                parametresRecu
        );

        autoriser(
                roleCaissier,
                InterfaceType.BACKOFFICE,
                clientsCommandePaiement
        );

        autoriser(
                roleCuisinier,
                InterfaceType.PDV,
                dashboard,
                gestionCommandes,
                stockEtat,
                stockCommandes,
                menuProduits,
                menuProduitsVoir
        );

        autoriser(
                roleResponsableSalle,
                InterfaceType.BACKOFFICE,
                dashboard,
                employesListe,
                employesTables,
                employesMesTables,
                employesPresence,
                clientsPlanTables,
                clientsCommandeAjouter,
                clientsCommandesListe,
                clientsListe,
                reservationsListe,
                clientsReclamations,
                clientsConseils,
                gestionCommandes
        );


        // =========================================================
        // POINT DE VENTE
        // =========================================================

        PointDeVente pdv = new PointDeVente();

        pdv.setNomPdv(
                "Caisse principale"
        );

        pdv.setAppareil_pos(
                "Tablette 1"
        );

        pdv.setStatutConnexion(
                "CONNECTE"
        );

        pdv.setRestaurant(
                restaurant
        );

        pdv = pointDeVenteRepository.save(
                pdv
        );


        // =========================================================
        // EMPLOYE ADMIN (gérant du restaurant)
        // =========================================================
        // Compte de test pour vérifier la connexion Back Office
        // d'un rôle autre que SUPERADMIN. Volontairement SANS
        // pdvAffecte : c'est le cas normal d'un gérant, et c'est
        // précisément le cas que le bug de restaurantId cassait.
        // =========================================================

        Employee admin = new Employee();

        admin.setNom(
                "Gharbi"
        );

        admin.setPrenom(
                "Mehdi"
        );

        admin.setEmail(
                "admin@restaurant.com"
        );

        admin.setTelephone(
                "+21620000001"
        );

        admin.setMot_de_passe(
                passwordEncoder.encode("admin123")
        );

        admin.setDate_creation(
                new Date()
        );

        admin.setStatut(
                StatutUtilisateur.ACTIF
        );

        admin.setMatricule(
                "EMP-0001"
        );

        admin.setDate_embauche(
                new Date()
        );

        admin.setCodePin(
                passwordEncoder.encode("1234")
        );

        admin.setSalaire_base(
                2500L
        );

        admin.setStatutPresence(
                StatutPresence.ABSENT
        );

        admin.setRole(
                roleAdmin
        );

        admin.setRestaurant(
                restaurant
        );

        // Pas de pdvAffecte : un gérant n'est pas rattaché à une
        // caisse précise.

        admin = employeeRepository.save(
                admin
        );


        // =========================================================
        // EMPLOYE SERVEUR
        // =========================================================

        Employee serveur = new Employee();

        serveur.setNom(
                "Ben Ali"
        );

        serveur.setPrenom(
                "Karim"
        );

        serveur.setEmail(
                "serveur@restaurant.com"
        );

        serveur.setTelephone(
                "+21620000002"
        );

        serveur.setMot_de_passe(
                passwordEncoder.encode("serveur123")
        );

        serveur.setDate_creation(
                new Date()
        );

        serveur.setStatut(
                StatutUtilisateur.ACTIF
        );

        serveur.setMatricule(
                "EMP-0002"
        );

        serveur.setDate_embauche(
                new Date()
        );

        serveur.setCodePin(
                passwordEncoder.encode("5678")
        );

        serveur.setSalaire_base(
                1200L
        );

        serveur.setStatutPresence(
                StatutPresence.ABSENT
        );

        serveur.setRole(
                roleServeur
        );

        serveur.setRestaurant(
                restaurant
        );

        serveur.setPdvAffecte(
                pdv
        );

        serveur = employeeRepository.save(
                serveur
        );


        // =========================================================
        // EMPLOYES RESPONSABLES DE TABLES (Ahmed, Sarah, Yassine)
        // =========================================================

        /*
         * Ces employés utilisent le rôle "Serveur" existant, qui
         * possède déjà la fonctionnalité EMPLOYES_TABLES
         * ("Attribuer des serveurs aux tables") en PDV et en
         * Back Office. Aucune nouvelle permission n'est créée :
         * le système de permissions existant est réutilisé tel quel.
         */

        Employee ahmed = creerEmployeeResponsableTable(
                "Ahmed",
                "Trabelsi",
                "ahmed@restaurant.com",
                "+21620000010",
                "EMP-0003",
                "1111",
                roleServeur,
                restaurant,
                pdv
        );

        Employee sarah = creerEmployeeResponsableTable(
                "Sarah",
                "Gharbi",
                "sarah@restaurant.com",
                "+21620000011",
                "EMP-0004",
                "2222",
                roleServeur,
                restaurant,
                pdv
        );

        Employee yassine = creerEmployeeResponsableTable(
                "Yassine",
                "Bouzid",
                "yassine@restaurant.com",
                "+21620000012",
                "EMP-0005",
                "3333",
                roleServeur,
                restaurant,
                pdv
        );


        // =========================================================
        // TABLES
        // =========================================================

        /*
         * L'affectation permanente d'un employé responsable à une
         * table réutilise le champ existant
         * TableRestaurant.serveurAttribue (aucune nouvelle relation
         * Employee/Table n'est créée).
         *
         * Positions (X, Y) : coordonnées initiales permettant de
         * tester le futur plan visuel du restaurant.
         */

        creerTable(1, 2, restaurant, ahmed, 100, 100);
        creerTable(2, 4, restaurant, ahmed, 300, 100);
        creerTable(3, 2, restaurant, sarah, 200, 250);
        creerTable(4, 4, restaurant, sarah, 100, 400);
        creerTable(5, 2, restaurant, yassine, 400, 400);
        creerTable(6, 6, restaurant, null, 600, 250);


        // =========================================================
        // PRISES EN CHARGE DE TEST (temporaires, distinctes de
        // l'affectation permanente serveurAttribue ci-dessus)
        // =========================================================

        /*
         * Ahmed est responsable permanent de la Table 1 ET s'en
         * occupe actuellement (prise en charge ACTIVE).
         */

        tableRepository.findByNumeroTable(1)
                .ifPresent(tableUn ->
                        creerPriseEnCharge(
                                tableUn,
                                ahmed,
                                new Date(),
                                null,
                                StatutPriseEnCharge.ACTIVE
                        )
                );

        /*
         * Sarah est responsable permanente de la Table 3, mais sa
         * prise en charge précédente est déjà TERMINEE (le client
         * de la table est parti) : permet de tester le calcul de
         * charge active (elle ne doit pas compter dans son total).
         */

        tableRepository.findByNumeroTable(3)
                .ifPresent(tableTrois ->
                        creerPriseEnCharge(
                                tableTrois,
                                sarah,
                                new Date(
                                        System.currentTimeMillis()
                                                - 2L * 60 * 60 * 1000
                                ),
                                new Date(
                                        System.currentTimeMillis()
                                                - 60L * 60 * 1000
                                ),
                                StatutPriseEnCharge.TERMINEE
                        )
                );


        // =========================================================
        // CATEGORIES
        // =========================================================

        Categorie pizzas = new Categorie();

        pizzas.setNom(
                "Pizzas"
        );

        pizzas.setOrdre_affichage(
                1
        );

        pizzas.setDescription_categorie(
                "Nos pizzas au four à bois"
        );

        pizzas.setRestaurant(
                restaurant
        );

        pizzas.setImage(
                "/images/categories/pizzas.jpg"
        );

        pizzas = categorieRepository.save(
                pizzas
        );


        Categorie boissons = new Categorie();

        boissons.setNom(
                "Boissons"
        );

        boissons.setOrdre_affichage(
                2
        );

        boissons.setDescription_categorie(
                "Boissons fraîches et chaudes"
        );

        boissons.setRestaurant(
                restaurant
        );

        boissons.setImage(
                "/images/categories/boissons.jpg"
        );

        boissons = categorieRepository.save(
                boissons
        );


        Categorie desserts = new Categorie();

        desserts.setNom(
                "Desserts"
        );

        desserts.setOrdre_affichage(
                3
        );

        desserts.setDescription_categorie(
                "Pâtisseries et douceurs maison"
        );

        desserts.setRestaurant(
                restaurant
        );

        desserts.setImage(
                "/images/categories/desserts.jpg"
        );

        desserts = categorieRepository.save(
                desserts
        );


        // =========================================================
        // PRODUITS
        // =========================================================

        Produit margherita =
                creerProduit(
                        "Pizza Margherita",
                        "Tomate, mozzarella, basilic",
                        18.5,
                        12,
                        pizzas,
                        restaurant,
                        6.50,
                        "/images/produits/pizza-margherita.jpg"
                );

        Produit quatreFromages =
                creerProduit(
                        "Pizza 4 Fromages",
                        "Mozzarella, gorgonzola, chèvre, parmesan",
                        22.0,
                        12,
                        pizzas,
                        restaurant,
                        8.20,
                        "/images/produits/pizza-4-fromages.jpg"
                );

        Produit pepperoni =
                creerProduit(
                        "Pizza Pepperoni",
                        "Tomate, mozzarella, pepperoni",
                        20.0,
                        12,
                        pizzas,
                        restaurant,
                        7.50,
                        "/images/produits/pizza-pepperoni.jpg"
                );

        Produit coca =
                creerProduit(
                        "Coca-Cola 33cl",
                        "Boisson gazeuse",
                        4.0,
                        1,
                        boissons,
                        restaurant,
                        1.20,
                        "/images/produits/coca-cola.jpg"
                );

        Produit eau =
                creerProduit(
                        "Eau minérale 50cl",
                        "Eau plate",
                        2.5,
                        1,
                        boissons,
                        restaurant,
                        0.50,
                        "/images/produits/eau-minerale.jpg"
                );

        Produit tiramisu =
                creerProduit(
                        "Tiramisu",
                        "Dessert italien au café et mascarpone",
                        9.0,
                        5,
                        desserts,
                        restaurant,
                        3.00,
                        "/images/produits/tiramisu.jpg"
                );


        // =========================================================
        // FOURNISSEUR
        // =========================================================

        Fournisseur fournisseur =
                new Fournisseur();

        fournisseur.setNom(
                "Grossiste Alimentaire Tunis"
        );

        fournisseur.setNumTel(
                "+21671000000"
        );

        fournisseur.setAdresse(
                "Zone Industrielle, Tunis"
        );

        fournisseur.setEmail(
                "contact@grossiste-tn.com"
        );

        fournisseur.setDelai_livraison_moyen(
                2
        );

        fournisseur.setRestaurant(
                restaurant
        );

        fournisseur =
                fournisseurRepository.save(
                        fournisseur
                );


        // =========================================================
        // INGREDIENTS
        // =========================================================

        creerIngredient(
                "Farine",
                "kg",
                50.0,
                10.0,
                fournisseur
        );

        creerIngredient(
                "Mozzarella",
                "kg",
                20.0,
                5.0,
                fournisseur
        );

        creerIngredient(
                "Tomate",
                "kg",
                30.0,
                8.0,
                fournisseur
        );

        creerIngredient(
                "Pepperoni",
                "kg",
                8.0,
                2.0,
                fournisseur
        );


        // =========================================================
        // MODIFICATEURS
        // =========================================================

        Modificateur extraFromage =
                creerModificateur(
                        "Extra fromage",
                        3.0,
                        margherita,
                        quatreFromages
                );

        Modificateur sansOignon =
                creerModificateur(
                        "Sans oignon",
                        0.0,
                        pepperoni
                );

        Modificateur pateFine =
                creerModificateur(
                        "Pâte fine",
                        0.0,
                        quatreFromages
                );


        // =========================================================
        // TAXE
        // =========================================================

        Taxe tva = new Taxe();

        tva.setNomTaxe(
                "TVA Standard"
        );

        tva.setTaux(
                19.0
        );

        tva.setApplicableA(
                "PRODUITS"
        );

        tva.setDateCreation(
                new Date()
        );

        tva.setStatut(
                "ACTIF"
        );

        taxeRepository.save(
                tva
        );


        // =========================================================
        // REDUCTION
        // =========================================================

        Reduction reduction =
                new Reduction();

        reduction.setNom_reduction(
                "Bienvenue -10%"
        );

        reduction.setType(
                TypeReduction.POURCENTAGE
        );

        reduction.setValeur(
                10.0
        );

        reduction.setDateDebut(
                new Date()
        );

        reduction.setDateFin(
                new Date(
                        System.currentTimeMillis()
                                + 30L * 24 * 3600 * 1000
                )
        );

        reduction.setConditions_application(
                "Première commande"
        );

        reduction.setRestaurant(
                restaurant
        );

        reductionRepository.save(
                reduction
        );


        // =========================================================
        // CLIENT
        // =========================================================

        ClientAuthentifie client =
                new ClientAuthentifie();

        client.setNom(
                "Trabelsi"
        );

        client.setPrenom(
                "Sami"
        );

        client.setEmail(
                "client@test.com"
        );

        client.setTelephone(
                "+21620000003"
        );

        client.setMot_de_passe(
                passwordEncoder.encode("client123")
        );

        client.setDate_creation(
                new Date()
        );

        client.setStatut(
                StatutUtilisateur.ACTIF
        );

        client.setAdresse(
                "5 Rue de Marseille, Tunis"
        );

        client.setRestaurant(
                restaurant
        );

        client =
                clientAuthentifieRepository.save(
                        client
                );


        // =========================================================
        // MODES DE PAIEMENT
        // =========================================================

        ModePaiement carte =
                new ModePaiement();

        carte.setLibelle(
                "Carte bancaire"
        );

        carte.setActif(
                true
        );

        carte.setClient(
                client
        );

        carte =
                modePaiementRepository.save(
                        carte
                );


        ModePaiement especes =
                new ModePaiement();

        especes.setLibelle(
                "Espèces"
        );

        especes.setActif(
                true
        );

        especes.setClient(
                client
        );

        especes =
                modePaiementRepository.save(
                        especes
                );


        // =========================================================
        // TABLES POUR LES COMMANDES
        // =========================================================

        List<TableRestaurant> tables =
                tableRepository.findByRestaurant(
                        restaurant
                );

        TableRestaurant table1 =
                tables.get(0);

        TableRestaurant table2 =
                tables.get(1);


        // =========================================================
        // COMMANDE 1
        // =========================================================

        Commande commande1 =
                new Commande();

        commande1.setDateCommande(
                new Date()
        );

        commande1.setStatut(
                StatutCommande.SERVIE
        );

        commande1.setModeCommande(
                ModeCommande.SUR_PLACE
        );

        commande1.setClient(
                client
        );

        commande1.setTable(
                table1
        );

        commande1.setEmployee(
                serveur
        );

        commande1.setMontant_total(
                margherita.getPrix()
                        + coca.getPrix()
        );

        commande1 =
                commandeRepository.save(
                        commande1
                );


        LigneCommande ligneMargheritaCmd1 =
                creerLigneCommande(
                        commande1,
                        margherita,
                        1,
                        "Extra fromage"
                );

        attacherModificateur(
                ligneMargheritaCmd1,
                extraFromage,
                1
        );

        creerLigneCommande(
                commande1,
                coca,
                1,
                ""
        );


        // =========================================================
        // COMMANDE 2
        // =========================================================

        Commande commande2 =
                new Commande();

        commande2.setDateCommande(
                new Date()
        );

        commande2.setStatut(
                StatutCommande.EN_ATTENTE
        );

        commande2.setModeCommande(
                ModeCommande.SUR_PLACE
        );

        commande2.setClient(
                client
        );

        commande2.setTable(
                table2
        );

        commande2.setEmployee(
                serveur
        );

        commande2.setMontant_total(
                quatreFromages.getPrix()
                        + pepperoni.getPrix()
                        + tiramisu.getPrix()
        );

        commande2 =
                commandeRepository.save(
                        commande2
                );


        LigneCommande ligneQuatreFromagesCmd2 =
                creerLigneCommande(
                        commande2,
                        quatreFromages,
                        1,
                        "Pâte fine"
                );

        attacherModificateur(
                ligneQuatreFromagesCmd2,
                pateFine,
                1
        );

        LigneCommande lignePepperoniCmd2 =
                creerLigneCommande(
                        commande2,
                        pepperoni,
                        1,
                        "Sans oignon"
                );

        attacherModificateur(
                lignePepperoniCmd2,
                sansOignon,
                1
        );

        creerLigneCommande(
                commande2,
                tiramisu,
                1,
                ""
        );


        // =========================================================
        // COMMANDE 3
        // =========================================================

        Commande commande3 =
                new Commande();

        commande3.setDateCommande(
                new Date()
        );

        commande3.setStatut(
                StatutCommande.PAYEE
        );

        commande3.setModeCommande(
                ModeCommande.A_EMPORTER
        );

        commande3.setClient(
                client
        );

        commande3.setEmployee(
                serveur
        );

        commande3.setMontant_total(
                pepperoni.getPrix()
                        + (eau.getPrix() * 2)
        );

        commande3 =
                commandeRepository.save(
                        commande3
                );


        LigneCommande lignePepperoniCmd3 =
                creerLigneCommande(
                        commande3,
                        pepperoni,
                        1,
                        "Sans oignon"
                );

        attacherModificateur(
                lignePepperoniCmd3,
                sansOignon,
                1
        );

        creerLigneCommande(
                commande3,
                eau,
                2,
                ""
        );


        // =========================================================
        // VENTES DE TEST
        // =========================================================

        Date maintenant =
                new Date();


        // =========================================================
        // VENTE 1
        // =========================================================

        Vente vente1 =
                new Vente();

        vente1.setDateVente(
                new Date(
                        maintenant.getTime()
                                - 3L * 60 * 60 * 1000
                )
        );

        vente1.setMontantTtc(
                22.50
        );

        vente1.setMontantHt(
                22.50 / 1.19
        );

        vente1.setReduction(
                reduction
        );

        vente1.setMontantReduction(
                2.50
        );

        vente1.setCommande(
                commande1
        );

        vente1.setPointDeVente(
                pdv
        );

        vente1.setEmployee(
                serveur
        );

        vente1.setModePaiement(
                carte
        );

        vente1 =
                venteRepository.save(
                        vente1
                );


        // =========================================================
        // RECU 1
        // =========================================================

        Recu recu1 =
                new Recu();

        recu1.setNumeroRecu(
                "REC-0001"
        );

        recu1.setDate_emission(
                vente1.getDateVente()
        );

        recu1.setLogo_affiche(
                true
        );

        recu1.setEntete_personnalise(
                "Le Gourmet"
        );

        recu1.setPied_de_page_personnalise(
                "Merci pour votre visite !"
        );

        recu1.setCommentaire_client(
                "Commande servie"
        );

        recu1.setVente(
                vente1
        );

        recuRepository.save(
                recu1
        );


        // =========================================================
        // VENTE 2
        // =========================================================

        Vente vente2 =
                new Vente();

        vente2.setDateVente(
                new Date(
                        maintenant.getTime()
                                - 2L * 60 * 60 * 1000
                )
        );

        vente2.setMontantTtc(
                25.00
        );

        vente2.setMontantHt(
                25.00 / 1.19
        );

        vente2.setReduction(
                reduction
        );

        vente2.setMontantReduction(
                5.00
        );

        vente2.setCommande(
                commande3
        );

        vente2.setPointDeVente(
                pdv
        );

        vente2.setEmployee(
                serveur
        );

        vente2.setModePaiement(
                especes
        );

        vente2 =
                venteRepository.save(
                        vente2
                );


        // =========================================================
        // RECU 2
        // =========================================================

        Recu recu2 =
                new Recu();

        recu2.setNumeroRecu(
                "REC-0002"
        );

        recu2.setDate_emission(
                vente2.getDateVente()
        );

        recu2.setLogo_affiche(
                true
        );

        recu2.setEntete_personnalise(
                "Le Gourmet"
        );

        recu2.setPied_de_page_personnalise(
                "Merci pour votre visite !"
        );

        recu2.setCommentaire_client(
                "Paiement espèces"
        );

        recu2.setVente(
                vente2
        );

        recuRepository.save(
                recu2
        );


        // =========================================================
        // VENTE 3
        // =========================================================

        Vente vente3 =
                new Vente();

        vente3.setDateVente(
                new Date(
                        maintenant.getTime()
                                - 30L * 60 * 1000
                )
        );

        vente3.setMontantTtc(
                51.00
        );

        vente3.setMontantHt(
                51.00 / 1.19
        );

        vente3.setReduction(
                null
        );

        vente3.setMontantReduction(
                0.0
        );

        vente3.setCommande(
                commande2
        );

        vente3.setPointDeVente(
                pdv
        );

        vente3.setEmployee(
                serveur
        );

        vente3.setModePaiement(
                carte
        );

        vente3 =
                venteRepository.save(
                        vente3
                );


        // =========================================================
        // RECU 3
        // =========================================================

        Recu recu3 =
                new Recu();

        recu3.setNumeroRecu(
                "REC-0003"
        );

        recu3.setDate_emission(
                vente3.getDateVente()
        );

        recu3.setLogo_affiche(
                true
        );

        recu3.setEntete_personnalise(
                "Le Gourmet"
        );

        recu3.setPied_de_page_personnalise(
                "Merci pour votre visite !"
        );

        recu3.setCommentaire_client(
                "Paiement par carte"
        );

        recu3.setVente(
                vente3
        );

        recuRepository.save(
                recu3
        );


        // =========================================================
        // SESSIONS DE CAISSE
        // =========================================================

        final long UN_JOUR =
                24L * 60 * 60 * 1000;

        final long UNE_HEURE =
                60L * 60 * 1000;


        // Session 1

        SessionCaisse sessionCaisse1 =
                new SessionCaisse();

        sessionCaisse1.setPointDeVente(
                pdv
        );

        sessionCaisse1.setEmployee(
                serveur
        );

        sessionCaisse1.setDateOuverture(
                new Date(
                        maintenant.getTime()
                                - 10 * UN_JOUR
                                - 12 * UNE_HEURE
                )
        );

        sessionCaisse1.setMontantOuverture(
                200.00
        );

        sessionCaisse1.setDateFermeture(
                new Date(
                        maintenant.getTime()
                                - 10 * UN_JOUR
                                - UNE_HEURE
                )
        );

        sessionCaisse1.setMontantFermeture(
                485.50
        );

        sessionCaisse1.setEcartCaisse(
                5.50
        );

        sessionCaisseRepository.save(
                sessionCaisse1
        );


        // Session 2

        SessionCaisse sessionCaisse2 =
                new SessionCaisse();

        sessionCaisse2.setPointDeVente(
                pdv
        );

        sessionCaisse2.setEmployee(
                serveur
        );

        sessionCaisse2.setDateOuverture(
                new Date(
                        maintenant.getTime()
                                - 9 * UN_JOUR
                                - 12 * UNE_HEURE
                )
        );

        sessionCaisse2.setMontantOuverture(
                150.00
        );

        sessionCaisse2.setDateFermeture(
                new Date(
                        maintenant.getTime()
                                - 9 * UN_JOUR
                                - UNE_HEURE
                )
        );

        sessionCaisse2.setMontantFermeture(
                429.00
        );

        sessionCaisse2.setEcartCaisse(
                -10.50
        );

        sessionCaisseRepository.save(
                sessionCaisse2
        );


        // Session 3

        SessionCaisse sessionCaisse3 =
                new SessionCaisse();

        sessionCaisse3.setPointDeVente(
                pdv
        );

        sessionCaisse3.setEmployee(
                serveur
        );

        sessionCaisse3.setDateOuverture(
                new Date(
                        maintenant.getTime()
                                - 6 * UN_JOUR
                                - 11 * UNE_HEURE
                )
        );

        sessionCaisse3.setMontantOuverture(
                250.00
        );

        sessionCaisse3.setDateFermeture(
                new Date(
                        maintenant.getTime()
                                - 6 * UN_JOUR
                                - UNE_HEURE
                )
        );

        sessionCaisse3.setMontantFermeture(
                583.00
        );

        sessionCaisse3.setEcartCaisse(
                0.00
        );

        sessionCaisseRepository.save(
                sessionCaisse3
        );


        // Session 4

        SessionCaisse sessionCaisse4 =
                new SessionCaisse();

        sessionCaisse4.setPointDeVente(
                pdv
        );

        sessionCaisse4.setEmployee(
                serveur
        );

        sessionCaisse4.setDateOuverture(
                new Date(
                        maintenant.getTime()
                                - 5 * UN_JOUR
                                - 11 * UNE_HEURE
                )
        );

        sessionCaisse4.setMontantOuverture(
                180.00
        );

        sessionCaisse4.setDateFermeture(
                new Date(
                        maintenant.getTime()
                                - 5 * UN_JOUR
                                - UNE_HEURE
                )
        );

        sessionCaisse4.setMontantFermeture(
                390.00
        );

        sessionCaisse4.setEcartCaisse(
                -50.00
        );

        sessionCaisseRepository.save(
                sessionCaisse4
        );


        // Session 5

        SessionCaisse sessionCaisse5 =
                new SessionCaisse();

        sessionCaisse5.setPointDeVente(
                pdv
        );

        sessionCaisse5.setEmployee(
                serveur
        );

        sessionCaisse5.setDateOuverture(
                new Date(
                        maintenant.getTime()
                                - 4 * UN_JOUR
                                - 11 * UNE_HEURE
                )
        );

        sessionCaisse5.setMontantOuverture(
                220.00
        );

        sessionCaisse5.setDateFermeture(
                new Date(
                        maintenant.getTime()
                                - 4 * UN_JOUR
                                - UNE_HEURE
                )
        );

        sessionCaisse5.setMontantFermeture(
                560.00
        );

        sessionCaisse5.setEcartCaisse(
                40.00
        );

        sessionCaisseRepository.save(
                sessionCaisse5
        );


        // Session 6

        SessionCaisse sessionCaisse6 =
                new SessionCaisse();

        sessionCaisse6.setPointDeVente(
                pdv
        );

        sessionCaisse6.setEmployee(
                serveur
        );

        sessionCaisse6.setDateOuverture(
                new Date(
                        maintenant.getTime()
                                - 3 * UN_JOUR
                                - 11 * UNE_HEURE
                )
        );

        sessionCaisse6.setMontantOuverture(
                200.00
        );

        sessionCaisse6.setDateFermeture(
                new Date(
                        maintenant.getTime()
                                - 3 * UN_JOUR
                                - UNE_HEURE
                )
        );

        sessionCaisse6.setMontantFermeture(
                452.75
        );

        sessionCaisse6.setEcartCaisse(
                2.75
        );

        sessionCaisseRepository.save(
                sessionCaisse6
        );


        // Session 7

        SessionCaisse sessionCaisse7 =
                new SessionCaisse();

        sessionCaisse7.setPointDeVente(
                pdv
        );

        sessionCaisse7.setEmployee(
                serveur
        );

        sessionCaisse7.setDateOuverture(
                new Date(
                        maintenant.getTime()
                                - 2 * UN_JOUR
                                - 11 * UNE_HEURE
                )
        );

        sessionCaisse7.setMontantOuverture(
                190.00
        );

        sessionCaisse7.setDateFermeture(
                new Date(
                        maintenant.getTime()
                                - 2 * UN_JOUR
                                - UNE_HEURE
                )
        );

        sessionCaisse7.setMontantFermeture(
                407.20
        );

        sessionCaisse7.setEcartCaisse(
                -2.80
        );

        sessionCaisseRepository.save(
                sessionCaisse7
        );


        // Session 8

        SessionCaisse sessionCaisse8 =
                new SessionCaisse();

        sessionCaisse8.setPointDeVente(
                pdv
        );

        sessionCaisse8.setEmployee(
                serveur
        );

        sessionCaisse8.setDateOuverture(
                new Date(
                        maintenant.getTime()
                                - UN_JOUR
                                - 11 * UNE_HEURE
                )
        );

        sessionCaisse8.setMontantOuverture(
                210.00
        );

        sessionCaisse8.setDateFermeture(
                new Date(
                        maintenant.getTime()
                                - UN_JOUR
                                - UNE_HEURE
                )
        );

        sessionCaisse8.setMontantFermeture(
                495.00
        );

        sessionCaisse8.setEcartCaisse(
                0.00
        );

        sessionCaisseRepository.save(
                sessionCaisse8
        );


        // Session 9 ouverte

        SessionCaisse sessionCaisse9 =
                new SessionCaisse();

        sessionCaisse9.setPointDeVente(
                pdv
        );

        sessionCaisse9.setEmployee(
                serveur
        );

        sessionCaisse9.setDateOuverture(
                new Date(
                        maintenant.getTime()
                                - 4 * UNE_HEURE
                )
        );

        sessionCaisse9.setMontantOuverture(
                200.00
        );

        sessionCaisse9.setMontantFermeture(
                null
        );

        sessionCaisse9.setDateFermeture(
                null
        );

        sessionCaisse9.setEcartCaisse(
                null
        );

        sessionCaisseRepository.save(
                sessionCaisse9
        );


        // =========================================================
        // RESERVATION 1
        // =========================================================

        Reservation reservation1 =
                new Reservation();

        reservation1.setDateReservation(
                java.time.LocalDate.now()
                        .plusDays(1)
        );

        reservation1.setHeureReservation(
                java.time.LocalTime.of(20, 0)
        );

        reservation1.setNombre_personnes(
                4
        );

        reservation1.setStatut(
                StatutReservation.CONFIRMEE
        );

        reservation1.setDate_creation(
                new Date()
        );

        reservation1.setClient(
                client
        );

        reservation1.setRestaurant(
                restaurant
        );

        reservation1.setTable(
                table1
        );

        reservation1.setConfirmePar(
                serveur
        );

        reservation1.setCommentaire_client(
                "Table près de la fenêtre si possible"
        );

        reservationRepository.save(
                reservation1
        );


        // =========================================================
        // RESERVATION 2
        // =========================================================

        Reservation reservation2 =
                new Reservation();

        reservation2.setDateReservation(
                java.time.LocalDate.now()
                        .plusDays(3)
        );

        reservation2.setHeureReservation(
                java.time.LocalTime.of(13, 30)
        );

        reservation2.setNombre_personnes(
                2
        );

        reservation2.setStatut(
                StatutReservation.EN_ATTENTE
        );

        reservation2.setDate_creation(
                new Date()
        );

        reservation2.setClient(
                client
        );

        reservation2.setRestaurant(
                restaurant
        );

        reservationRepository.save(
                reservation2
        );


        // =========================================================
        // RESERVATION 3 (ANNULEE)
        // =========================================================

        Reservation reservation3 =
                new Reservation();

        reservation3.setDateReservation(
                java.time.LocalDate.now()
                        .plusDays(2)
        );

        reservation3.setHeureReservation(
                java.time.LocalTime.of(19, 0)
        );

        reservation3.setNombre_personnes(
                6
        );

        reservation3.setStatut(
                StatutReservation.ANNULEE
        );

        reservation3.setDate_creation(
                new Date()
        );

        reservation3.setClient(
                client
        );

        reservation3.setRestaurant(
                restaurant
        );

        reservation3.setTable(
                table2
        );

        reservation3.setCommentaire_client(
                "Anniversaire, table calme si possible"
        );

        reservationRepository.save(
                reservation3
        );


        // =========================================================
        // RESERVATION 4 (HONOREE)
        // =========================================================

        Reservation reservation4 =
                new Reservation();

        reservation4.setDateReservation(
                java.time.LocalDate.now()
                        .minusDays(2)
        );

        reservation4.setHeureReservation(
                java.time.LocalTime.of(12, 30)
        );

        reservation4.setNombre_personnes(
                3
        );

        reservation4.setStatut(
                StatutReservation.HONOREE
        );

        reservation4.setDate_creation(
                new Date()
        );

        reservation4.setClient(
                client
        );

        reservation4.setRestaurant(
                restaurant
        );

        reservation4.setTable(
                table1
        );

        reservation4.setConfirmePar(
                ahmed
        );

        reservation4.setCommentaire_client(
                "Client fidèle, déjeuner d'affaires"
        );

        reservationRepository.save(
                reservation4
        );


        // =========================================================
        // RESERVATION 5 (NO_SHOW)
        // =========================================================

        Reservation reservation5 =
                new Reservation();

        reservation5.setDateReservation(
                java.time.LocalDate.now()
                        .minusDays(1)
        );

        reservation5.setHeureReservation(
                java.time.LocalTime.of(20, 30)
        );

        reservation5.setNombre_personnes(
                5
        );

        reservation5.setStatut(
                StatutReservation.NO_SHOW
        );

        reservation5.setDate_creation(
                new Date()
        );

        reservation5.setClient(
                client
        );

        reservation5.setRestaurant(
                restaurant
        );

        reservation5.setTable(
                table2
        );

        reservation5.setConfirmePar(
                sarah
        );

        reservation5.setCommentaire_client(
                "Aucune nouvelle du client le jour J"
        );

        reservationRepository.save(
                reservation5
        );


        // =========================================================
        // RECLAMATION
        // =========================================================

        Reclamation reclamation =
                new Reclamation();

        reclamation.setSujet(
                "Commande en retard"
        );

        reclamation.setDescription(
                "Ma commande a mis 40 minutes à arriver"
        );

        reclamation.setDate_creation(
                new Date()
        );

        reclamation.setStatut(
                StatutReclamation.OUVERTE
        );

        reclamation.setClient(
                client
        );

        reclamation.setCommande(
                commande2
        );

        reclamationRepository.save(
                reclamation
        );


        // =========================================================
        // SUGGESTION
        // =========================================================

        Suggestion suggestion =
                new Suggestion();

        suggestion.setContenu(
                "Ce serait bien d'avoir plus d'options végétariennes"
        );

        suggestion.setDateCreation(
                new Date()
        );

        suggestion.setPriseEnCompte(
                false
        );

        suggestion.setClient(
                client
        );

        suggestion.setRestaurant(
                restaurant
        );

        suggestionRepository.save(
                suggestion
        );


        // =========================================================
        // NOTIFICATION
        // =========================================================

        Notification notification =
                new Notification();

        notification.setEmetteur(
                serveur
        );

        notification.setDestinataire(
                client
        );

        notification.setType(
                TypeNotification.EMAIL
        );

        notification.setContenu(
                "Votre commande #"
                        + commande1.getId_commande()
                        + " a été servie, bon appétit !"
        );

        notification.setDateEnvoi(
                new Date()
        );

        notification.setStatutEnvoi(
                StatutEnvoi.ENVOYE
        );

        notification.setDeclencheur(
                "COMMANDE_SERVIE"
        );

        notificationRepository.save(
                notification
        );



        // =========================================================
        // DONNEES SUPPLEMENTAIRES — 3 RESTAURANTS / HISTORIQUE
        // =========================================================
        /*
         * Les données historiques du restaurant "Le Gourmet" restent
         * inchangées. Ce bloc ajoute des catégories, sous-catégories,
         * employés, clients, commandes, ventes et réservations.
         *
         * Les commandes sont réparties sur 14, 11, 8, 4 et 1 mois.
         * La période couvre donc plus d'une année.
         */

        // ---------------------------------------------------------
        // SOUS-CATEGORIES ET PRODUITS — RESTAURANT 1
        // ---------------------------------------------------------

        Categorie pizzasClassiques = creerCategorie(
                "Pizzas classiques",
                "Les recettes traditionnelles",
                1,
                restaurant,
                pizzas
        );

        Categorie pizzasGourmandes = creerCategorie(
                "Pizzas gourmandes",
                "Recettes généreuses et spéciales",
                2,
                restaurant,
                pizzas
        );

        Categorie boissonsFroides = creerCategorie(
                "Boissons froides",
                "Sodas, eaux et boissons fraîches",
                1,
                restaurant,
                boissons
        );

        Categorie boissonsChaudes = creerCategorie(
                "Boissons chaudes",
                "Café et boissons chaudes",
                2,
                restaurant,
                boissons
        );

        Categorie dessertsMaison = creerCategorie(
                "Desserts maison",
                "Desserts préparés sur place",
                1,
                restaurant,
                desserts
        );

        Categorie glaces = creerCategorie(
                "Glaces",
                "Glaces et coupes dessert",
                2,
                restaurant,
                desserts
        );

        Produit calzone = creerProduit(
                "Calzone",
                "Pizza fermée, mozzarella, jambon et tomate",
                21.0,
                14,
                pizzasClassiques,
                restaurant,
                7.8,
                "/images/produits/calzone.jpg"
        );

        Produit tunisienne = creerProduit(
                "Pizza Tunisienne",
                "Harissa, thon, olives, mozzarella",
                19.5,
                13,
                pizzasGourmandes,
                restaurant,
                7.1,
                "/images/produits/pizza-tunisienne.jpg"
        );

        Produit quatroSaisons = creerProduit(
                "Pizza 4 Saisons",
                "Champignons, artichauts, olives et mozzarella",
                23.5,
                14,
                pizzasGourmandes,
                restaurant,
                9.0,
                "/images/produits/pizza-4-saisons.jpg"
        );

        Produit citronnade = creerProduit(
                "Citronnade maison",
                "Citron frais et menthe",
                6.0,
                3,
                boissonsFroides,
                restaurant,
                1.5,
                "/images/produits/citronnade.jpg"
        );

        Produit cafe = creerProduit(
                "Café espresso",
                "Café espresso tunisien",
                3.0,
                2,
                boissonsChaudes,
                restaurant,
                0.8,
                "/images/produits/cafe.jpg"
        );

        Produit cheesecake = creerProduit(
                "Cheesecake",
                "Cheesecake maison aux fruits rouges",
                10.0,
                6,
                dessertsMaison,
                restaurant,
                3.4,
                "/images/produits/cheesecake.jpg"
        );

        Produit glaceVanille = creerProduit(
                "Glace vanille",
                "Deux boules de glace vanille",
                7.0,
                2,
                glaces,
                restaurant,
                2.2,
                "/images/produits/glace-vanille.jpg"
        );


        // ---------------------------------------------------------
        // EMPLOYES SUPPLEMENTAIRES — RESTAURANT 1
        // ---------------------------------------------------------

        Employee managerGourmet = creerEmployeeDemo(
                "Nadia", "Mansouri", "nadia@restaurant.com",
                "+21620000020", "EMP-0010", "4444",
                roleManager, restaurant, pdv, false
        );

        Employee caissierGourmet = creerEmployeeDemo(
                "Walid", "Ben Ali", "walid@restaurant.com",
                "+21620000021", "EMP-0011", "5555",
                roleCaissier, restaurant, pdv, false
        );

        Employee cuisinierGourmet = creerEmployeeDemo(
                "Karim", "Jaziri", "karim@restaurant.com",
                "+21620000022", "EMP-0012", "6666",
                roleCuisinier, restaurant, pdv, false
        );


        // ---------------------------------------------------------
        // CLIENTS SUPPLEMENTAIRES — RESTAURANT 1
        // ---------------------------------------------------------

        ClientAuthentifie clientGourmet2 = creerClient(
                "Ben Salem", "Amira", "amira.gourmet@test.com",
                "+21620000102", "12 Rue du Lac 1, Tunis", restaurant
        );

        ClientAuthentifie clientGourmet3 = creerClient(
                "Khelifi", "Omar", "omar.gourmet@test.com",
                "+21620000103", "8 Rue du Lac, Tunis", restaurant
        );

        creerModesPaiement(clientGourmet2);
        creerModesPaiement(clientGourmet3);


        // ---------------------------------------------------------
        // TABLES UTILISEES PAR LES COMMANDES HISTORIQUES — RESTAURANT 1
        // ---------------------------------------------------------
        // On récupère les tables du Gourmet ici afin de les utiliser dans
        // les commandes ci-dessous. Elles sont créées plus haut dans le
        // DataInitializer et restent donc compatibles avec le modèle existant.
        List<TableRestaurant> tablesGourmet = tableRepository.findByRestaurant(restaurant);

        TableRestaurant table3 = tablesGourmet.get(2);
        TableRestaurant table4 = tablesGourmet.get(3);


        // ---------------------------------------------------------
        // COMMANDES HISTORIQUES — RESTAURANT 1
        // ---------------------------------------------------------

        Commande gourmetAncienne = creerCommandeDemo(
                dateAgoMonths(14),
                StatutCommande.PAYEE,
                ModeCommande.EN_LIGNE,
                clientGourmet2,
                table1,
                caissierGourmet,
                new Produit[]{margherita, cheesecake},
                new int[]{2, 1}
        );

        Commande gourmetOnzeMois = creerCommandeDemo(
                dateAgoMonths(11),
                StatutCommande.SERVIE,
                ModeCommande.SUR_PLACE,
                clientGourmet3,
                table2,
                ahmed,
                new Produit[]{calzone, coca},
                new int[]{1, 2}
        );

        Commande gourmetHuitMois = creerCommandeDemo(
                dateAgoMonths(8),
                StatutCommande.PAYEE,
                ModeCommande.A_EMPORTER,
                client,
                null,
                serveur,
                new Produit[]{tunisienne, citronnade},
                new int[]{1, 2}
        );

        Commande gourmetQuatreMois = creerCommandeDemo(
                dateAgoMonths(4),
                StatutCommande.SERVIE,
                ModeCommande.SCAN_QR_TABLE,
                clientGourmet2,
                table3,
                sarah,
                new Produit[]{quatroSaisons, glaceVanille},
                new int[]{1, 2}
        );

        Commande gourmetRecente = creerCommandeDemo(
                dateAgoMonths(1),
                StatutCommande.ANNULEE,
                ModeCommande.SUR_PLACE,
                clientGourmet3,
                table4,
                yassine,
                new Produit[]{margherita, cafe},
                new int[]{1, 1}
        );

        creerVenteDemo(
                gourmetAncienne, dateAgoMonths(14), caissierGourmet,
                pdv, carte, "GOURMET"
        );

        creerVenteDemo(
                gourmetOnzeMois, dateAgoMonths(11), ahmed,
                pdv, especes, "GOURMET"
        );

        creerVenteDemo(
                gourmetHuitMois, dateAgoMonths(8), serveur,
                pdv, carte, "GOURMET"
        );

        creerVenteDemo(
                gourmetQuatreMois, dateAgoMonths(4), sarah,
                pdv, especes, "GOURMET"
        );


        // ---------------------------------------------------------
        // RESTAURANT 2 : LA PIAZZA
        // ---------------------------------------------------------

        Restaurant restaurant2 = creerRestaurant(
                "La Piazza",
                "25 Avenue de la Liberté, Tunis",
                Devise.DINAR,
                LangueParDefaut.FRANCAIS,
                LocalTime.of(10, 0),
                LocalTime.of(23, 30)
        );

        PointDeVente pdv2 = creerPdv(
                "Caisse principale",
                "Tablette 2",
                restaurant2
        );

        Employee managerPiazza = creerEmployeeDemo(
                "Meriem", "Chaabane", "meriem@lapiazza.com",
                "+21620000201", "EMP-0201", "1212",
                roleManager, restaurant2, pdv2, false
        );

        Employee caissierPiazza = creerEmployeeDemo(
                "Hatem", "Karray", "hatem@lapiazza.com",
                "+21620000202", "EMP-0202", "1313",
                roleCaissier, restaurant2, pdv2, false
        );

        Employee cuisinierPiazza = creerEmployeeDemo(
                "Sonia", "Mrad", "sonia@lapiazza.com",
                "+21620000203", "EMP-0203", "1414",
                roleCuisinier, restaurant2, pdv2, false
        );

        Employee serveurPiazza = creerEmployeeDemo(
                "Youssef", "Ayari", "youssef@lapiazza.com",
                "+21620000204", "EMP-0204", "1515",
                roleServeur, restaurant2, pdv2, true
        );

        TableRestaurant p2t1 = creerTable(1, 2, restaurant2, serveurPiazza, 100, 100);
        TableRestaurant p2t2 = creerTable(2, 4, restaurant2, serveurPiazza, 300, 100);
        TableRestaurant p2t3 = creerTable(3, 4, restaurant2, null, 200, 250);
        TableRestaurant p2t4 = creerTable(4, 6, restaurant2, null, 500, 250);
        TableRestaurant p2t5 = creerTable(5, 2, restaurant2, serveurPiazza, 400, 400);
        TableRestaurant p2t6 = creerTable(6, 6, restaurant2, null, 600, 400);

        Categorie p2Pizzas = creerCategorie(
                "Pizzas", "Pizzas italiennes au feu de bois", 1, restaurant2, null
        );
        Categorie p2PizzasClassiques = creerCategorie(
                "Classiques", "Les incontournables", 1, restaurant2, p2Pizzas
        );
        Categorie p2PizzasSpeciales = creerCategorie(
                "Spéciales", "Recettes signatures", 2, restaurant2, p2Pizzas
        );

        Categorie p2Burgers = creerCategorie(
                "Burgers", "Burgers maison", 2, restaurant2, null
        );
        Categorie p2Boeuf = creerCategorie(
                "Boeuf", "Burgers au boeuf", 1, restaurant2, p2Burgers
        );
        Categorie p2Poulet = creerCategorie(
                "Poulet", "Burgers au poulet", 2, restaurant2, p2Burgers
        );

        Categorie p2Boissons = creerCategorie(
                "Boissons", "Boissons fraîches et chaudes", 3, restaurant2, null
        );
        Categorie p2Froides = creerCategorie(
                "Froides", "Boissons froides", 1, restaurant2, p2Boissons
        );
        Categorie p2Chaudes = creerCategorie(
                "Chaudes", "Boissons chaudes", 2, restaurant2, p2Boissons
        );

        Categorie p2Desserts = creerCategorie(
                "Desserts", "Desserts maison", 4, restaurant2, null
        );
        Categorie p2Maison = creerCategorie(
                "Maison", "Desserts faits maison", 1, restaurant2, p2Desserts
        );
        Categorie p2Glaces = creerCategorie(
                "Glaces", "Glaces et sorbets", 2, restaurant2, p2Desserts
        );

        Produit p2Margherita = creerProduit(
                "Margherita", "Tomate, mozzarella, basilic", 19.0, 12,
                p2PizzasClassiques, restaurant2, 6.7, "/images/produits/p2-margherita.jpg"
        );
        Produit p2Regina = creerProduit(
                "Regina", "Tomate, mozzarella, jambon, champignons", 22.0, 13,
                p2PizzasClassiques, restaurant2, 8.0, "/images/produits/p2-regina.jpg"
        );
        Produit p2Signature = creerProduit(
                "Piazza Signature", "Tomate, burrata, roquette et jambon cru", 28.0, 15,
                p2PizzasSpeciales, restaurant2, 11.0, "/images/produits/p2-signature.jpg"
        );
        Produit p2BurgerClassic = creerProduit(
                "Classic Burger", "Steak haché, cheddar, salade", 24.0, 12,
                p2Boeuf, restaurant2, 10.0, "/images/produits/p2-burger.jpg"
        );
        Produit p2BurgerPoulet = creerProduit(
                "Chicken Burger", "Poulet pané, cheddar, sauce maison", 23.0, 12,
                p2Poulet, restaurant2, 9.0, "/images/produits/p2-chicken.jpg"
        );
        Produit p2Cola = creerProduit(
                "Cola 33cl", "Boisson gazeuse", 4.0, 1,
                p2Froides, restaurant2, 1.2, "/images/produits/p2-cola.jpg"
        );
        Produit p2Cafe = creerProduit(
                "Café", "Espresso", 3.0, 2,
                p2Chaudes, restaurant2, 0.8, "/images/produits/p2-cafe.jpg"
        );
        Produit p2Tiramisu = creerProduit(
                "Tiramisu maison", "Mascarpone, café et cacao", 9.0, 5,
                p2Maison, restaurant2, 3.1, "/images/produits/p2-tiramisu.jpg"
        );
        Produit p2Glace = creerProduit(
                "Glace pistache", "Deux boules de glace pistache", 8.0, 2,
                p2Glaces, restaurant2, 2.5, "/images/produits/p2-glace.jpg"
        );

        Modificateur p2ExtraFromage = creerModificateur(
                "Extra fromage", 3.0, p2Margherita, p2Regina, p2Signature
        );
        Modificateur p2Sauce = creerModificateur(
                "Sauce maison", 1.0, p2BurgerClassic, p2BurgerPoulet
        );

        Fournisseur p2Fournisseur = creerFournisseur(
                "Fournisseur La Piazza",
                "+21671000201",
                "Zone industrielle Tunis",
                "contact@fournisseur-piazza.tn",
                2,
                restaurant2
        );
        creerIngredient("Farine", "kg", 70, 15, p2Fournisseur);
        creerIngredient("Mozzarella", "kg", 25, 6, p2Fournisseur);
        creerIngredient("Steak haché", "kg", 18, 5, p2Fournisseur);

        ClientAuthentifie p2Client1 = creerClient(
                "Guesmi", "Nour", "nour@lapiazza.com",
                "+21620000301", "15 Rue Carthage, Tunis", restaurant2
        );
        ClientAuthentifie p2Client2 = creerClient(
                "Kacem", "Ali", "ali@lapiazza.com",
                "+21620000302", "7 Rue du Lac, Tunis", restaurant2
        );
        ClientAuthentifie p2Client3 = creerClient(
                "Jlassi", "Ines", "ines@lapiazza.com",
                "+21620000303", "9 Rue de Marseille, Tunis", restaurant2
        );

        ModePaiement p2Carte = creerModePaiement("Carte bancaire", p2Client1);
        ModePaiement p2Especes = creerModePaiement("Espèces", p2Client2);

        Commande p2Commande1 = creerCommandeDemo(
                dateAgoMonths(14), StatutCommande.PAYEE, ModeCommande.EN_LIGNE,
                p2Client1, p2t1, serveurPiazza,
                new Produit[]{p2Margherita, p2Tiramisu}, new int[]{2, 1}
        );
        Commande p2Commande2 = creerCommandeDemo(
                dateAgoMonths(11), StatutCommande.SERVIE, ModeCommande.SUR_PLACE,
                p2Client2, p2t2, serveurPiazza,
                new Produit[]{p2Signature, p2Cola}, new int[]{1, 2}
        );
        Commande p2Commande3 = creerCommandeDemo(
                dateAgoMonths(8), StatutCommande.PAYEE, ModeCommande.A_EMPORTER,
                p2Client3, null, caissierPiazza,
                new Produit[]{p2BurgerClassic, p2Cafe}, new int[]{1, 1}
        );
        Commande p2Commande4 = creerCommandeDemo(
                dateAgoMonths(4), StatutCommande.SERVIE, ModeCommande.SCAN_QR_TABLE,
                p2Client1, p2t3, serveurPiazza,
                new Produit[]{p2BurgerPoulet, p2Glace}, new int[]{2, 2}
        );
        Commande p2Commande5 = creerCommandeDemo(
                dateAgoMonths(1), StatutCommande.EN_ATTENTE, ModeCommande.SUR_PLACE,
                p2Client2, p2t4, serveurPiazza,
                new Produit[]{p2Regina, p2Cola}, new int[]{1, 1}
        );

        creerVenteDemo(p2Commande1, dateAgoMonths(14), caissierPiazza, pdv2, p2Carte, "PIAZZA");
        creerVenteDemo(p2Commande2, dateAgoMonths(11), serveurPiazza, pdv2, p2Especes, "PIAZZA");
        creerVenteDemo(p2Commande3, dateAgoMonths(8), caissierPiazza, pdv2, p2Carte, "PIAZZA");
        creerVenteDemo(p2Commande4, dateAgoMonths(4), serveurPiazza, pdv2, p2Especes, "PIAZZA");

        creerReservationDemo(
                p2Client1, restaurant2, p2t1,
                java.time.LocalDate.now().plusDays(5),
                java.time.LocalTime.of(20, 0),
                4, StatutReservation.CONFIRMEE, serveurPiazza
        );
        creerReservationDemo(
                p2Client2, restaurant2, p2t2,
                java.time.LocalDate.now().minusMonths(6),
                java.time.LocalTime.of(13, 0),
                2, StatutReservation.HONOREE, managerPiazza
        );

        creerReclamationDemo(
                p2Client3, restaurant2, p2Commande2,
                "Temps d'attente",
                "Le temps d'attente a été un peu long.",
                StatutReclamation.OUVERTE
        );


        // ---------------------------------------------------------
        // RESTAURANT 3 : CAFE MEDINA
        // ---------------------------------------------------------

        Restaurant restaurant3 = creerRestaurant(
                "Café Medina",
                "8 Rue de la Kasbah, Tunis",
                Devise.DINAR,
                LangueParDefaut.FRANCAIS,
                LocalTime.of(8, 0),
                LocalTime.of(22, 0)
        );

        PointDeVente pdv3 = creerPdv(
                "Caisse principale",
                "Tablette 3",
                restaurant3
        );

        Employee managerMedina = creerEmployeeDemo(
                "Leila", "Ben Amor", "leila@cafemedina.com",
                "+21620000401", "EMP-0301", "2121",
                roleManager, restaurant3, pdv3, false
        );
        Employee caissierMedina = creerEmployeeDemo(
                "Fares", "Khalfallah", "fares@cafemedina.com",
                "+21620000402", "EMP-0302", "2222",
                roleCaissier, restaurant3, pdv3, false
        );
        Employee cuisinierMedina = creerEmployeeDemo(
                "Aymen", "Trabelsi", "aymen@cafemedina.com",
                "+21620000403", "EMP-0303", "2323",
                roleCuisinier, restaurant3, pdv3, false
        );
        Employee serveurMedina = creerEmployeeDemo(
                "Hichem", "Mabrouk", "hichem@cafemedina.com",
                "+21620000404", "EMP-0304", "2424",
                roleServeur, restaurant3, pdv3, true
        );

        TableRestaurant p3t1 = creerTable(1, 2, restaurant3, serveurMedina, 120, 100);
        TableRestaurant p3t2 = creerTable(2, 4, restaurant3, serveurMedina, 320, 100);
        TableRestaurant p3t3 = creerTable(3, 4, restaurant3, null, 220, 250);
        TableRestaurant p3t4 = creerTable(4, 6, restaurant3, null, 520, 250);
        TableRestaurant p3t5 = creerTable(5, 2, restaurant3, serveurMedina, 420, 420);
        TableRestaurant p3t6 = creerTable(6, 8, restaurant3, null, 620, 420);

        Categorie p3PetitDej = creerCategorie(
                "Petit déjeuner", "Petit déjeuner tunisien et continental", 1, restaurant3, null
        );
        Categorie p3Tunisien = creerCategorie(
                "Tunisien", "Petit déjeuner traditionnel", 1, restaurant3, p3PetitDej
        );
        Categorie p3Continental = creerCategorie(
                "Continental", "Petit déjeuner continental", 2, restaurant3, p3PetitDej
        );

        Categorie p3Plats = creerCategorie(
                "Plats", "Plats et spécialités", 2, restaurant3, null
        );
        Categorie p3Traditionnels = creerCategorie(
                "Traditionnels", "Spécialités tunisiennes", 1, restaurant3, p3Plats
        );
        Categorie p3Legers = creerCategorie(
                "Légers", "Salades et plats légers", 2, restaurant3, p3Plats
        );

        Categorie p3Boissons = creerCategorie(
                "Boissons", "Boissons chaudes et fraîches", 3, restaurant3, null
        );
        Categorie p3Chaudes = creerCategorie(
                "Chaudes", "Cafés et thés", 1, restaurant3, p3Boissons
        );
        Categorie p3Froides = creerCategorie(
                "Froides", "Jus et boissons fraîches", 2, restaurant3, p3Boissons
        );

        Categorie p3Desserts = creerCategorie(
                "Desserts", "Desserts et pâtisseries", 4, restaurant3, null
        );
        Categorie p3Gateaux = creerCategorie(
                "Pâtisseries", "Gâteaux et pâtisseries maison", 1, restaurant3, p3Desserts
        );
        Categorie p3Glaces = creerCategorie(
                "Glaces", "Glaces et sorbets", 2, restaurant3, p3Desserts
        );

        Produit p3Mlawi = creerProduit(
                "Mlawi œuf", "Mlawi, œuf et fromage", 8.0, 6,
                p3Tunisien, restaurant3, 3.0, "/images/produits/p3-mlawi.jpg"
        );
        Produit p3Ojja = creerProduit(
                "Ojja merguez", "Œufs, tomate et merguez", 18.0, 12,
                p3Traditionnels, restaurant3, 7.0, "/images/produits/p3-ojja.jpg"
        );
        Produit p3Salade = creerProduit(
                "Salade César", "Salade, poulet grillé et parmesan", 16.0, 8,
                p3Legers, restaurant3, 6.0, "/images/produits/p3-salade.jpg"
        );
        Produit p3Cappuccino = creerProduit(
                "Cappuccino", "Café et lait mousseux", 6.0, 4,
                p3Chaudes, restaurant3, 1.6, "/images/produits/p3-cappuccino.jpg"
        );
        Produit p3Jus = creerProduit(
                "Jus d'orange frais", "Oranges pressées minute", 7.0, 4,
                p3Froides, restaurant3, 2.0, "/images/produits/p3-jus.jpg"
        );
        Produit p3Croissant = creerProduit(
                "Croissant", "Croissant au beurre", 3.5, 3,
                p3Continental, restaurant3, 1.2, "/images/produits/p3-croissant.jpg"
        );
        Produit p3Baklava = creerProduit(
                "Baklava", "Baklava aux fruits secs", 7.5, 5,
                p3Gateaux, restaurant3, 2.8, "/images/produits/p3-baklava.jpg"
        );
        Produit p3Glace = creerProduit(
                "Glace fraise", "Deux boules de glace fraise", 7.0, 2,
                p3Glaces, restaurant3, 2.3, "/images/produits/p3-glace.jpg"
        );

        Modificateur p3ExtraFromage = creerModificateur(
                "Extra fromage", 2.5, p3Ojja, p3Salade
        );
        Modificateur p3SansSucre = creerModificateur(
                "Sans sucre", 0.0, p3Cappuccino, p3Jus
        );

        Fournisseur p3Fournisseur = creerFournisseur(
                "Fournisseur Café Medina",
                "+21671000301",
                "Ariana, Tunisie",
                "contact@fournisseur-medina.tn",
                1,
                restaurant3
        );
        creerIngredient("Œufs", "unité", 120, 30, p3Fournisseur);
        creerIngredient("Café", "kg", 15, 4, p3Fournisseur);
        creerIngredient("Oranges", "kg", 40, 10, p3Fournisseur);

        ClientAuthentifie p3Client1 = creerClient(
                "Mami", "Sarra", "sarra@cafemedina.com",
                "+21620000501", "4 Rue de la Kasbah, Tunis", restaurant3
        );
        ClientAuthentifie p3Client2 = creerClient(
                "Gharbi", "Nabil", "nabil@cafemedina.com",
                "+21620000502", "22 Rue Sidi Bou Said, Tunis", restaurant3
        );
        ClientAuthentifie p3Client3 = creerClient(
                "Jebali", "Rania", "rania@cafemedina.com",
                "+21620000503", "11 Rue de Carthage, Tunis", restaurant3
        );

        ModePaiement p3Carte = creerModePaiement("Carte bancaire", p3Client1);
        ModePaiement p3Especes = creerModePaiement("Espèces", p3Client2);

        Commande p3Commande1 = creerCommandeDemo(
                dateAgoMonths(14), StatutCommande.PAYEE, ModeCommande.EN_LIGNE,
                p3Client1, p3t1, serveurMedina,
                new Produit[]{p3Ojja, p3Jus}, new int[]{2, 2}
        );
        Commande p3Commande2 = creerCommandeDemo(
                dateAgoMonths(11), StatutCommande.SERVIE, ModeCommande.SUR_PLACE,
                p3Client2, p3t2, serveurMedina,
                new Produit[]{p3Salade, p3Cappuccino}, new int[]{1, 2}
        );
        Commande p3Commande3 = creerCommandeDemo(
                dateAgoMonths(8), StatutCommande.PAYEE, ModeCommande.A_EMPORTER,
                p3Client3, null, caissierMedina,
                new Produit[]{p3Mlawi, p3Croissant}, new int[]{2, 2}
        );
        Commande p3Commande4 = creerCommandeDemo(
                dateAgoMonths(4), StatutCommande.SERVIE, ModeCommande.SCAN_QR_TABLE,
                p3Client1, p3t3, serveurMedina,
                new Produit[]{p3Cappuccino, p3Baklava, p3Glace}, new int[]{2, 1, 1}
        );
        Commande p3Commande5 = creerCommandeDemo(
                dateAgoMonths(1), StatutCommande.ANNULEE, ModeCommande.SUR_PLACE,
                p3Client2, p3t4, serveurMedina,
                new Produit[]{p3Ojja, p3Jus}, new int[]{1, 1}
        );

        creerVenteDemo(p3Commande1, dateAgoMonths(14), caissierMedina, pdv3, p3Carte, "MEDINA");
        creerVenteDemo(p3Commande2, dateAgoMonths(11), serveurMedina, pdv3, p3Especes, "MEDINA");
        creerVenteDemo(p3Commande3, dateAgoMonths(8), caissierMedina, pdv3, p3Carte, "MEDINA");
        creerVenteDemo(p3Commande4, dateAgoMonths(4), serveurMedina, pdv3, p3Especes, "MEDINA");


        // =========================================================
        // SÉRIE DE VENTES POUR LES GRAPHIQUES — 3 RESTAURANTS
        // =========================================================
        //
        // 12 mois supplémentaires par restaurant.
        // Les montants sont volontairement variés et réalistes afin
        // d'obtenir un graphique d'évolution lisible :
        // - creux et pics selon les mois
        // - croissance progressive
        // - plusieurs ventes dans l'historique, dont > 1 an déjà
        //   présentes plus haut.
        //
        // Les commandes sont PAYEE/SERVIE afin qu'elles soient
        // comptabilisées comme ventes dans les statistiques.

        int[] moisGraph = {13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        // ---------------------------------------------------------
        // LE GOURMET — évolution ~ 120 à 420 DT / mois
        // ---------------------------------------------------------
        Produit[] produitsGourmetGraph = {
                margherita, quatroSaisons, calzone, tunisienne,
                cafe, citronnade, glaceVanille, cheesecake
        };
        int[][] quantitesGourmetGraph = {
                {3, 2, 1, 2, 3, 2, 2, 1},
                {4, 2, 2, 2, 4, 2, 2, 1},
                {3, 4, 2, 3, 4, 3, 2, 2},
                {4, 3, 3, 3, 5, 3, 3, 2},
                {5, 4, 3, 3, 5, 4, 3, 2},
                {4, 5, 4, 4, 6, 4, 4, 2},
                {6, 4, 4, 4, 6, 5, 4, 3},
                {5, 6, 5, 5, 7, 5, 5, 3},
                {6, 6, 5, 6, 8, 6, 5, 3},
                {7, 6, 6, 6, 8, 7, 6, 4},
                {8, 7, 6, 7, 9, 7, 6, 4},
                {9, 8, 7, 8, 10, 8, 7, 5},
                {10, 9, 8, 9, 12, 9, 8, 5}
        };
        for (int i = 0; i < moisGraph.length; i++) {
            Commande c = creerCommandeDemo(
                    dateAgoMonths(moisGraph[i]),
                    i % 4 == 0 ? StatutCommande.SERVIE : StatutCommande.PAYEE,
                    i % 3 == 0 ? ModeCommande.SUR_PLACE :
                            (i % 3 == 1 ? ModeCommande.A_EMPORTER : ModeCommande.EN_LIGNE),
                    i % 2 == 0 ? clientGourmet2 : clientGourmet3,
                    i % 3 == 0 ? table1 : (i % 3 == 1 ? table2 : null),
                    i % 2 == 0 ? serveur : ahmed,
                    produitsGourmetGraph,
                    quantitesGourmetGraph[i]
            );
            creerVenteDemo(
                    c, dateAgoMonths(moisGraph[i]),
                    i % 2 == 0 ? caissierGourmet : serveur,
                    pdv,
                    i % 3 == 0 ? carte : especes,
                    "GOURMET"
            );
        }


        // ---------------------------------------------------------
        // LA PIAZZA — évolution ~ 160 à 520 DT / mois
        // ---------------------------------------------------------
        Produit[] produitsPiazzaGraph = {
                p2Margherita, p2Regina, p2Signature, p2BurgerClassic,
                p2BurgerPoulet, p2Cola, p2Cafe, p2Tiramisu, p2Glace
        };
        int[][] quantitesPiazzaGraph = {
                {3, 2, 1, 2, 1, 3, 2, 1, 1},
                {4, 3, 1, 2, 2, 4, 3, 2, 1},
                {4, 4, 2, 3, 2, 5, 3, 2, 2},
                {5, 4, 2, 4, 3, 6, 4, 2, 2},
                {6, 5, 2, 4, 3, 7, 5, 3, 2},
                {5, 6, 3, 5, 4, 8, 5, 3, 3},
                {7, 6, 3, 5, 4, 9, 6, 4, 3},
                {7, 7, 4, 6, 5, 10, 7, 4, 3},
                {8, 7, 4, 7, 5, 11, 8, 5, 4},
                {9, 8, 5, 7, 6, 12, 9, 5, 4},
                {10, 9, 5, 8, 7, 13, 10, 6, 4},
                {11, 10, 6, 9, 8, 14, 11, 6, 5},
                {12, 11, 7, 10, 9, 15, 12, 7, 5}
        };
        for (int i = 0; i < moisGraph.length; i++) {
            Commande c = creerCommandeDemo(
                    dateAgoMonths(moisGraph[i]),
                    i % 4 == 0 ? StatutCommande.SERVIE : StatutCommande.PAYEE,
                    i % 3 == 0 ? ModeCommande.SUR_PLACE :
                            (i % 3 == 1 ? ModeCommande.A_EMPORTER : ModeCommande.EN_LIGNE),
                    i % 2 == 0 ? p2Client1 : p2Client2,
                    i % 3 == 0 ? p2t1 : (i % 3 == 1 ? p2t2 : null),
                    i % 2 == 0 ? serveurPiazza : caissierPiazza,
                    produitsPiazzaGraph,
                    quantitesPiazzaGraph[i]
            );
            creerVenteDemo(
                    c, dateAgoMonths(moisGraph[i]),
                    i % 2 == 0 ? caissierPiazza : serveurPiazza,
                    pdv2,
                    i % 3 == 0 ? p2Carte : p2Especes,
                    "PIAZZA"
            );
        }


        // ---------------------------------------------------------
        // CAFÉ MEDINA — évolution ~ 100 à 360 DT / mois
        // ---------------------------------------------------------
        Produit[] produitsMedinaGraph = {
                p3Mlawi, p3Ojja, p3Salade, p3Cappuccino,
                p3Jus, p3Croissant, p3Baklava, p3Glace
        };
        int[][] quantitesMedinaGraph = {
                {4, 2, 2, 3, 3, 3, 2, 1},
                {5, 3, 2, 4, 4, 4, 2, 1},
                {6, 4, 3, 5, 5, 5, 3, 2},
                {7, 4, 4, 6, 6, 6, 3, 2},
                {8, 5, 4, 7, 7, 7, 4, 3},
                {9, 6, 5, 8, 8, 8, 4, 3},
                {10, 6, 6, 9, 9, 9, 5, 4},
                {11, 7, 6, 10, 10, 10, 5, 4},
                {12, 8, 7, 11, 11, 11, 6, 5},
                {13, 8, 8, 12, 12, 12, 6, 5},
                {14, 9, 9, 13, 13, 13, 7, 6},
                {15, 10, 10, 14, 14, 14, 8, 6},
                {16, 11, 11, 15, 15, 15, 9, 7}
        };
        for (int i = 0; i < moisGraph.length; i++) {
            Commande c = creerCommandeDemo(
                    dateAgoMonths(moisGraph[i]),
                    i % 4 == 0 ? StatutCommande.SERVIE : StatutCommande.PAYEE,
                    i % 3 == 0 ? ModeCommande.SUR_PLACE :
                            (i % 3 == 1 ? ModeCommande.A_EMPORTER : ModeCommande.EN_LIGNE),
                    i % 2 == 0 ? p3Client1 : p3Client2,
                    i % 3 == 0 ? p3t1 : (i % 3 == 1 ? p3t2 : null),
                    i % 2 == 0 ? serveurMedina : caissierMedina,
                    produitsMedinaGraph,
                    quantitesMedinaGraph[i]
            );
            creerVenteDemo(
                    c, dateAgoMonths(moisGraph[i]),
                    i % 2 == 0 ? caissierMedina : serveurMedina,
                    pdv3,
                    i % 3 == 0 ? p3Carte : p3Especes,
                    "MEDINA"
            );
        }

        log.info("Ventes de démonstration supplémentaires créées pour les 3 restaurants : 39 commandes/ventes.");

        creerReservationDemo(
                p3Client1, restaurant3, p3t1,
                java.time.LocalDate.now().plusDays(2),
                java.time.LocalTime.of(19, 30),
                3, StatutReservation.CONFIRMEE, serveurMedina
        );
        creerReservationDemo(
                p3Client2, restaurant3, p3t2,
                java.time.LocalDate.now().minusMonths(8),
                java.time.LocalTime.of(12, 30),
                4, StatutReservation.HONOREE, managerMedina
        );

        creerReclamationDemo(
                p3Client1, restaurant3, p3Commande2,
                "Qualité du service",
                "Le client demande un suivi sur le service.",
                StatutReclamation.OUVERTE
        );


        // =========================================================
        // FIN DES DONNEES SUPPLEMENTAIRES
        // =========================================================


        // =========================================================
        // SUPER-ADMIN PLATEFORME (solution 1 — RestaurantController)
        // =========================================================
        //
        // Compte de démo, indépendant de tout restaurant (SuperAdmin
        // n'a pas de champ restaurant). Seul habilité à créer/lister
        // tous/rechercher/supprimer des restaurants — voir
        // RestaurantController et AuthService.loginSuperAdmin.
        //
        // En production, ce premier compte serait créé par un
        // processus manuel hors application (migration, script),
        // pas par DataInitializer — mais comme ddl-auto=create
        // recrée le schéma à chaque démarrage ici, on le sème comme
        // les autres comptes de démo.

        SuperAdmin superAdmin = new SuperAdmin();

        superAdmin.setNom(
                "Plateforme"
        );

        superAdmin.setPrenom(
                "Super Admin"
        );

        superAdmin.setEmail(
                "superadmin@plateforme.com"
        );

        superAdmin.setTelephone(
                "+21620000000"
        );

        superAdmin.setMot_de_passe(
                passwordEncoder.encode("superadmin123")
        );

        superAdmin.setDate_creation(
                new Date()
        );

        superAdmin.setStatut(
                StatutUtilisateur.ACTIF
        );

        superAdminRepository.save(
                superAdmin
        );


        // =========================================================
        // FIN
        // =========================================================

        log.info("========================================================");

        log.info(
                "DataInitializer : données de test insérées avec succès"
        );

        log.info(
                "Catalogue des fonctionnalités : {} fonctionnalités",
                fonctionnaliteRepository.count()
        );

        log.info(
                "Back office : accès avec le compte SUPERADMIN après sélection du restaurant"
        );

        log.info(
                "Super-admin : superadmin@plateforme.com / superadmin123"
        );

        log.info(
                "PDV         : PIN 5678 (Serveur)"
        );

        log.info(
                "Client      : client@test.com / client123"
        );

        log.info(
                "Ventes      : 3"
        );

        log.info(
                "CA test     : 98.50 DT"
        );

        log.info("========================================================");
    }


    // =========================================================
    // CREER UN MODULE
    // =========================================================

    private Module createModule(
            String nom,
            String icone,
            int ordre,
            boolean disponiblePdv,
            boolean disponibleBackoffice) {

        Module module =
                new Module();

        module.setNomModule(
                nom
        );

        module.setIcone(
                icone
        );

        module.setOrdre_affichage(
                ordre
        );

        module.setDisponiblePdv(
                disponiblePdv
        );

        module.setDisponible_backoffice(
                disponibleBackoffice
        );

        return moduleRepository.save(
                module
        );
    }


    // =========================================================
    // CREER UNE FONCTIONNALITE PRINCIPALE
    // =========================================================

    private Fonctionnalite createFeature(
            Module module,
            String nom,
            String code,
            boolean disponiblePdv,
            boolean disponibleBackoffice,
            int ordre) {

        Fonctionnalite fonctionnalite =
                new Fonctionnalite();

        fonctionnalite.setNomFonctionnalite(
                nom
        );

        fonctionnalite.setCodeFonctionnalite(
                code
        );

        fonctionnalite.setDisponiblePdv(
                disponiblePdv
        );

        fonctionnalite.setDisponibleBackoffice(
                disponibleBackoffice
        );

        fonctionnalite.setOrdre_affichage(
                ordre
        );

        fonctionnalite.setModule(
                module
        );

        fonctionnalite.setFonctionnaliteParent(
                null
        );

        return fonctionnaliteRepository.save(
                fonctionnalite
        );
    }


    // =========================================================
    // CREER UNE SOUS-FONCTIONNALITE
    // =========================================================

    private Fonctionnalite createChildFeature(
            Fonctionnalite parent,
            String nom,
            String code,
            boolean disponiblePdv,
            boolean disponibleBackoffice,
            int ordre) {

        Fonctionnalite fonctionnalite =
                new Fonctionnalite();

        fonctionnalite.setNomFonctionnalite(
                nom
        );

        fonctionnalite.setCodeFonctionnalite(
                code
        );

        fonctionnalite.setDisponiblePdv(
                disponiblePdv
        );

        fonctionnalite.setDisponibleBackoffice(
                disponibleBackoffice
        );

        fonctionnalite.setOrdre_affichage(
                ordre
        );

        fonctionnalite.setModule(
                parent.getModule()
        );

        fonctionnalite.setFonctionnaliteParent(
                parent
        );

        return fonctionnaliteRepository.save(
                fonctionnalite
        );
    }


    // =========================================================
    // ROLE <-> FONCTIONNALITE
    // =========================================================

    private void saveRoleFonctionnalite(
            Role role,
            Fonctionnalite fonctionnalite,
            InterfaceType type) {

        /*
         * Evite une double insertion accidentelle.
         */

        if (roleFonctionnaliteRepository
                .findByRoleAndFonctionnaliteAndInterfaceType(
                        role,
                        fonctionnalite,
                        type
                )
                .isPresent()) {

            return;
        }

        RoleFonctionnalite rf =
                new RoleFonctionnalite();

        rf.setRole(
                role
        );

        rf.setFonctionnalite(
                fonctionnalite
        );

        rf.setInterfaceType(
                type
        );

        rf.setAutorise(
                true
        );

        rf.setDate_attribution(
                new Date()
        );

        roleFonctionnaliteRepository.save(
                rf
        );
    }


    // =========================================================
    // CREER EMPLOYE RESPONSABLE DE TABLE
    // =========================================================

    private Employee creerEmployeeResponsableTable(
            String prenom,
            String nom,
            String email,
            String telephone,
            String matricule,
            String codePin,
            Role role,
            Restaurant restaurant,
            PointDeVente pdv) {

        Employee employee = new Employee();

        employee.setNom(nom);
        employee.setPrenom(prenom);
        employee.setEmail(email);
        employee.setTelephone(telephone);

        employee.setMot_de_passe(
                passwordEncoder.encode(matricule.toLowerCase())
        );

        employee.setDate_creation(new Date());
        employee.setStatut(StatutUtilisateur.ACTIF);
        employee.setMatricule(matricule);
        employee.setDate_embauche(new Date());

        employee.setCodePin(
                passwordEncoder.encode(codePin)
        );

        employee.setSalaire_base(1200L);
        employee.setStatutPresence(StatutPresence.ABSENT);
        employee.setRole(role);
        employee.setRestaurant(restaurant);
        employee.setPdvAffecte(pdv);

        // Phase 3C : Ahmed, Sarah et Yassine (les employés créés par
        // cette méthode, cf. commentaire "EMPLOYES RESPONSABLES DE
        // TABLES" plus haut) sont les employés de démonstration
        // éligibles à l'attribution automatique/manuelle des prises
        // en charge. Mohamed (serveur ci-dessous) et les autres rôles
        // (Manager, Cuisinier...) restent non éligibles par défaut.
        employee.setEligibleAttributionAutomatique(true);

        return employeeRepository.save(employee);
    }


    // =========================================================
    // CREER TABLE (avec affectation permanente et position)
    // =========================================================

    private TableRestaurant creerTable(
            int numeroTable,
            int capacite,
            Restaurant restaurant,
            Employee serveurAttribue,
            int positionX,
            int positionY) {

        TableRestaurant table = new TableRestaurant();

        table.setNumeroTable(numeroTable);
        table.setCapacite(capacite);
        table.setStatut(StatutTable.LIBRE);
        table.setQrActif(false);
        table.setRestaurant(restaurant);
        table.setServeurAttribue(serveurAttribue);
        table.setPositionX(positionX);
        table.setPositionY(positionY);

        return tableRepository.save(table);
    }


    // =========================================================
    // CREER PRISE EN CHARGE (temporaire, données de test)
    // =========================================================

    private PriseEnChargeTable creerPriseEnCharge(
            TableRestaurant table,
            Employee employee,
            Date dateDebut,
            Date dateFin,
            StatutPriseEnCharge statut) {

        PriseEnChargeTable prise = new PriseEnChargeTable();

        prise.setTable(table);
        prise.setEmployee(employee);
        prise.setDateDebut(dateDebut);
        prise.setDateFin(dateFin);
        prise.setStatut(statut);

        return priseEnChargeTableRepository.save(prise);
    }


    // =========================================================
    // CREER PRODUIT
    // =========================================================

    private Produit creerProduit(
            String nom,
            String description,
            double prix,
            int tempsPreparation,
            Categorie categorie,
            Restaurant restaurant,
            double coutUnitaire,
            String image) {

        Produit produit =
                new Produit();

        produit.setNom(
                nom
        );

        produit.setDescription(
                description
        );

        produit.setPrix(
                prix
        );

        produit.setDisponible(
                true
        );

        produit.setTemps_preparation(
                tempsPreparation
        );

        produit.setCategorieParent(
                categorie
        );

        produit.setRestaurant(
                restaurant
        );

        produit.setCoutUnitaire(
                coutUnitaire
        );

        produit.setImage(
                image
        );

        return produitRepository.save(
                produit
        );
    }


    // =========================================================
    // CREER INGREDIENT
    // =========================================================

    private void creerIngredient(
            String nom,
            String unite,
            double stock,
            double seuilAlerte,
            Fournisseur fournisseur) {

        Ingredient ingredient =
                new Ingredient();

        ingredient.setNom(
                nom
        );

        ingredient.setUnite_mesure(
                unite
        );

        ingredient.setQuantite_stock(
                stock
        );

        ingredient.setSeuil_alerte(
                seuilAlerte
        );

        ingredient.setFournisseur(
                fournisseur
        );

        ingredient.setRestaurant(
                fournisseur.getRestaurant()
        );

        ingredientRepository.save(
                ingredient
        );
    }


    // =========================================================
    // CREER MODIFICATEUR
    // =========================================================

    private Modificateur creerModificateur(
            String nom,
            double prixSupplementaire,
            Produit... produits) {

        Modificateur modificateur =
                new Modificateur();

        modificateur.setNom_modificateur(
                nom
        );

        modificateur.setPrix_supplementaire(
                prixSupplementaire
        );

        modificateur.setProduits(
                Arrays.asList(produits)
        );

        return modificateurRepository.save(
                modificateur
        );
    }


    // =========================================================
    // CREER LIGNE COMMANDE
    // =========================================================

    private LigneCommande creerLigneCommande(
            Commande commande,
            Produit produit,
            int quantite,
            String remarque) {

        LigneCommande ligne =
                new LigneCommande();

        ligne.setCommande(
                commande
        );

        ligne.setProduit(
                produit
        );

        ligne.setQuantite(
                quantite
        );

        ligne.setPrix_unitaire(
                produit.getPrix()
        );

        ligne.setRemarque(
                remarque
        );

        return ligneCommandeRepository.save(
                ligne
        );
    }


    // =========================================================
    // ATTACHER UN MODIFICATEUR
    // =========================================================

    private void attacherModificateur(
            LigneCommande ligne,
            Modificateur modificateur,
            int quantite) {

        LigneCommandeModificateur lcm =
                new LigneCommandeModificateur();

        lcm.setLigneCommande(
                ligne
        );

        lcm.setModificateur(
                modificateur
        );

        lcm.setQuantite(
                quantite
        );

        ligneCommandeModificateurRepository.save(
                lcm
        );
    }
    // =========================================================
    // HELPERS - ROLES / RESTAURANTS / PDV
    // =========================================================

    private Role creerRole(
            String nom,
            String description,
            boolean accesBackoffice,
            boolean accesPdv) {

        Role role = new Role();

        role.setNom_role(nom);
        role.setDescription(description);
        role.setAcces_backoffice(accesBackoffice);
        role.setAcces_pdv(accesPdv);
        role.setDate_creation(new Date());

        return roleRepository.save(role);
    }


    private void autoriser(
            Role role,
            InterfaceType type,
            Fonctionnalite... fonctionnalites) {

        for (Fonctionnalite fonctionnalite : fonctionnalites) {
            saveRoleFonctionnalite(
                    role,
                    fonctionnalite,
                    type
            );
        }
    }


    private Restaurant creerRestaurant(
            String nom,
            String adresse,
            Devise devise,
            LangueParDefaut langue,
            LocalTime ouverture,
            LocalTime fermeture) {

        Restaurant restaurant = new Restaurant();

        restaurant.setNomRestaurant(nom);
        restaurant.setAdresse(adresse);
        restaurant.setDevise(devise);
        restaurant.setLangue_par_defaut(langue);
        restaurant.setStatut(StatutRestaurant.ACTIF);
        restaurant.setHoraires_ouverture(ouverture);
        restaurant.setHoraires_fermeture(fermeture);

        return restaurantRepository.save(restaurant);
    }


    private PointDeVente creerPdv(
            String nom,
            String appareil,
            Restaurant restaurant) {

        PointDeVente pdv = new PointDeVente();

        pdv.setNomPdv(nom);
        pdv.setAppareil_pos(appareil);
        pdv.setStatutConnexion("CONNECTE");
        pdv.setRestaurant(restaurant);

        return pointDeVenteRepository.save(pdv);
    }


    private Categorie creerCategorie(
            String nom,
            String description,
            int ordre,
            Restaurant restaurant,
            Categorie parent) {

        Categorie categorie = new Categorie();

        categorie.setNom(nom);
        categorie.setOrdre_affichage(ordre);
        categorie.setDescription_categorie(description);
        categorie.setRestaurant(restaurant);
        categorie.setCategorieParent(parent);

        return categorieRepository.save(categorie);
    }


    private Employee creerEmployeeDemo(
            String prenom,
            String nom,
            String email,
            String telephone,
            String matricule,
            String codePin,
            Role role,
            Restaurant restaurant,
            PointDeVente pdv,
            boolean eligibleAttribution) {

        Employee employee = new Employee();

        employee.setNom(nom);
        employee.setPrenom(prenom);
        employee.setEmail(email);
        employee.setTelephone(telephone);
        employee.setMot_de_passe(
                passwordEncoder.encode(matricule.toLowerCase())
        );
        employee.setDate_creation(new Date());
        employee.setStatut(StatutUtilisateur.ACTIF);
        employee.setMatricule(matricule);
        employee.setDate_embauche(new Date());
        employee.setCodePin(
                passwordEncoder.encode(codePin)
        );
        employee.setSalaire_base(1200L);
        employee.setStatutPresence(StatutPresence.PRESENT);
        employee.setRole(role);
        employee.setRestaurant(restaurant);
        employee.setPdvAffecte(pdv);
        employee.setEligibleAttributionAutomatique(eligibleAttribution);

        return employeeRepository.save(employee);
    }


    private ClientAuthentifie creerClient(
            String nom,
            String prenom,
            String email,
            String telephone,
            String adresse,
            Restaurant restaurant) {

        ClientAuthentifie client = new ClientAuthentifie();

        client.setNom(nom);
        client.setPrenom(prenom);
        client.setEmail(email);
        client.setTelephone(telephone);
        client.setMot_de_passe(
                passwordEncoder.encode("client123")
        );
        client.setDate_creation(new Date());
        client.setStatut(StatutUtilisateur.ACTIF);
        client.setAdresse(adresse);
        client.setRestaurant(restaurant);
        client.setPreferences("Cuisine méditerranéenne");
        client.setAllergie("");

        return clientAuthentifieRepository.save(client);
    }


    private ModePaiement creerModePaiement(
            String libelle,
            ClientAuthentifie client) {

        ModePaiement modePaiement = new ModePaiement();

        modePaiement.setLibelle(libelle);
        modePaiement.setActif(true);
        modePaiement.setClient(client);

        return modePaiementRepository.save(modePaiement);
    }


    private void creerModesPaiement(ClientAuthentifie client) {

        creerModePaiement("Carte bancaire", client);
        creerModePaiement("Espèces", client);
    }


    private Fournisseur creerFournisseur(
            String nom,
            String telephone,
            String adresse,
            String email,
            int delaiLivraison,
            Restaurant restaurant) {

        Fournisseur fournisseur = new Fournisseur();

        fournisseur.setNom(nom);
        fournisseur.setNumTel(telephone);
        fournisseur.setAdresse(adresse);
        fournisseur.setEmail(email);
        fournisseur.setDelai_livraison_moyen(delaiLivraison);
        fournisseur.setRestaurant(restaurant);

        return fournisseurRepository.save(fournisseur);
    }


    // =========================================================
    // HELPERS - COMMANDES / VENTES
    // =========================================================

    private Commande creerCommandeDemo(
            Date dateCommande,
            StatutCommande statut,
            ModeCommande modeCommande,
            ClientAuthentifie client,
            TableRestaurant table,
            Employee employee,
            Produit[] produits,
            int[] quantites) {

        if (produits == null || quantites == null
                || produits.length != quantites.length
                || produits.length == 0) {

            throw new IllegalArgumentException(
                    "Une commande de démonstration doit contenir au moins une ligne."
            );
        }

        Commande commande = new Commande();

        commande.setDateCommande(dateCommande);
        commande.setStatut(statut);
        commande.setModeCommande(modeCommande);
        commande.setClient(client);
        commande.setTable(table);
        commande.setEmployee(employee);

        double total = 0.0;

        for (int i = 0; i < produits.length; i++) {
            total += produits[i].getPrix() * quantites[i];
        }

        commande.setMontant_total(total);

        commande = commandeRepository.save(commande);

        for (int i = 0; i < produits.length; i++) {
            creerLigneCommande(
                    commande,
                    produits[i],
                    quantites[i],
                    ""
            );
        }

        return commande;
    }


    private Vente creerVenteDemo(
            Commande commande,
            Date dateVente,
            Employee employee,
            PointDeVente pdv,
            ModePaiement modePaiement,
            String prefixRecu) {

        double montantTtc = commande.getMontant_total() == null
                ? 0.0
                : commande.getMontant_total();

        Vente vente = new Vente();

        vente.setDateVente(dateVente);
        vente.setMontantTtc(montantTtc);
        vente.setMontantHt(montantTtc / 1.19);
        vente.setReduction(null);
        vente.setMontantReduction(0.0);
        vente.setCommande(commande);
        vente.setPointDeVente(pdv);
        vente.setEmployee(employee);
        vente.setModePaiement(modePaiement);

        vente = venteRepository.save(vente);

        Recu recu = new Recu();

        recu.setNumeroRecu(
                "REC-" + prefixRecu + "-"
                        + String.format("%04d", vente.getId_vente())
        );
        recu.setDate_emission(dateVente);
        recu.setLogo_affiche(true);
        recu.setEntete_personnalise(
                commande.getClient() != null
                        ? commande.getClient().getRestaurant().getNomRestaurant()
                        : "Restaurant"
        );
        recu.setPied_de_page_personnalise("Merci pour votre visite !");
        recu.setCommentaire_client("Vente de démonstration");
        recu.setVente(vente);

        recuRepository.save(recu);

        return vente;
    }


    private Reservation creerReservationDemo(
            ClientAuthentifie client,
            Restaurant restaurant,
            TableRestaurant table,
            java.time.LocalDate date,
            java.time.LocalTime heure,
            int nombrePersonnes,
            StatutReservation statut,
            Employee confirmePar) {

        Reservation reservation = new Reservation();

        reservation.setDateReservation(date);
        reservation.setHeureReservation(heure);
        reservation.setNombre_personnes(nombrePersonnes);
        reservation.setStatut(statut);
        reservation.setDate_creation(new Date());
        reservation.setClient(client);
        reservation.setRestaurant(restaurant);
        reservation.setTable(table);
        reservation.setConfirmePar(confirmePar);
        reservation.setCommentaire_client("Réservation de démonstration");

        return reservationRepository.save(reservation);
    }


    private Reclamation creerReclamationDemo(
            ClientAuthentifie client,
            Restaurant restaurant,
            Commande commande,
            String sujet,
            String description,
            StatutReclamation statut) {

        Reclamation reclamation = new Reclamation();

        reclamation.setSujet(sujet);
        reclamation.setDescription(description);
        reclamation.setDate_creation(new Date());
        reclamation.setStatut(statut);
        reclamation.setClient(client);
        reclamation.setCommande(commande);

        return reclamationRepository.save(reclamation);
    }


    // =========================================================
    // HELPERS - DATES
    // =========================================================

    private Date dateAgoMonths(int months) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -months);

        return calendar.getTime();
    }

}