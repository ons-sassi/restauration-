// src/app/app.routes.ts

import { Routes } from '@angular/router';

// ============================================================
// AUTH
// ============================================================

import { UnifiedLoginComponent } from './features/auth/login/unified-login/unified-login.component';
import { ClientMenuComponent } from './features/client/menu/menu.component';
import { ClientProduitDetailComponent } from './features/client/produit-detail/produit-detail.component';
import { ClientRestaurantSelectionComponent } from './features/client/restaurant-selection/restaurant-selection.component';
import { ClientAuthComponent } from './features/client/auth/client-auth.component';
import { ClientPanierComponent } from './features/client/panier/panier.component';
import { ClientCheckoutComponent } from './features/client/checkout/checkout.component';
import { ClientReclamationsComponent } from './features/client/reclamations/reclamations.component';
import { ClientSuggestionsComponent } from './features/client/suggestions/suggestions.component';
import { ClientCommandeHistoriqueComponent } from './features/client/historique/historique.component';
import { ClientFavorisComponent } from './features/client/favoris/favoris.component';
import { ClientReservationsComponent } from './features/client/reservations/reservations.component';
import { ClientMonCompteComponent } from './features/client/mon-compte/mon-compte.component';
import { RestaurantSelectionComponent } from './features/auth/restaurant-selection/restaurant-selection.component';

// ============================================================
// GUARDS
// ============================================================

import { backOfficeGuard } from './core/guards/back-office.guard';
import { pdvGuard } from './core/guards/pdv.guard';
import { PermissionGuard } from './core/guards/permission.guard';
import { clientGuard } from './core/guards/client.guard';
import { adminGuard } from './core/guards/admin.guard';

// ============================================================
// PRODUITS
// ============================================================

import { ProduitListComponent } from './features/business/menu/produits/produit-list/produit-list.component';
import { ProduitFormComponent } from './features/business/menu/produits/produit-form/produit-form.component';
import { ProduitDetailComponent } from './features/business/menu/produits/produit-details/produit-details.component';

// ============================================================
// CATEGORIES
// ============================================================

import { CategorieListComponent } from './features/business/menu/categories/categorie-list/categorie-list.component';
import { CategorieFormComponent } from './features/business/menu/categories/categorie-form/categorie-form.component';
import { CategorieDetailComponent } from './features/business/menu/categories/categorie-details/categorie-details.component';

// ============================================================
// MODIFICATEURS
// ============================================================

import { ModificateurListComponent } from './features/business/menu/modificateurs/modificateur-list/modificateur-list.component';
import { ModificateurDetailComponent } from './features/business/menu/modificateurs/modificateur-details/modificateur-details.component';
import { ModificateurFormComponent } from './features/business/menu/modificateurs/modificateur-form/modificateur-form.component';

// ============================================================
// BACK OFFICE
// ============================================================

import { BackOfficeLayoutComponent } from './features/back-office/layout/back-office-layout/back-office-layout.component';

import { RecapitulatifVentesComponent } from './features/back-office/ventes/recapitulatif/recapitulatif-ventes.component';
import { VenteParArticleComponent } from './features/back-office/ventes/par-article/vente-par-article.component';
import { VenteParCategorieComponent } from './features/back-office/ventes/par-categorie/vente-par-categorie.component';
import { ParModePaiementComponent } from './features/back-office/ventes/par-mode-paiement/par-mode-paiement.component';
import { ParRecuComponent } from './features/back-office/ventes/par-recu/par-recu.component';
import { VenteParEmployeComponent } from './features/back-office/ventes/par-employe/vente-par-employe.component';
import { VenteParModificateurComponent } from './features/back-office/ventes/par-modificateur/vente-par-modificateur.component';
import { VentesReductionsComponent } from './features/back-office/ventes/reductions/ventes-reductions.component';
import { GestionCaisseComponent } from './features/back-office/ventes/caisse/gestion-caisse.component';

// ============================================================
// MENU / RÉDUCTIONS
// ============================================================

import { ReductionListComponent } from './features/business/menu/reductions/reduction-list/reduction-list.component';
import { ReductionFormComponent } from './features/business/menu/reductions/reduction-form/reduction-form.component';

// ============================================================
// MENU / POINTS CADEAU
// ============================================================
// ============================================================
// GESTION DE STOCK
// ============================================================

import { IngredientsComponent } from './features/back-office/stocks/ingredients/ingredients.component';
import { FournisseursComponent } from './features/back-office/stocks/fournisseurs/fournisseurs.component';
import { CommandesFournisseursComponent } from './features/back-office/stocks/commandes/commandes.component';

// ============================================================
// TAXES
// ============================================================

import { TaxesComponent } from './features/back-office/taxes/taxes.component';

// ============================================================
// PARAMÈTRES
// ============================================================

import { ModesPaiementComponent } from './features/back-office/parametres/modes-paiement/modes-paiement.component';
import { OptionsRestaurationComponent } from './features/back-office/parametres/options-restauration/options-restauration.component';
import { AppareilsPdvComponent } from './features/back-office/parametres/appareils-pdv/appareils-pdv.component';

// ============================================================
// ROLES
// ============================================================

import { RoleFormComponent } from './features/back-office/roles/role-form/role-form.component';
import { RoleListComponent } from './features/back-office/roles/role-list/role-list.component';

// ============================================================
// EMPLOYÉS
// ============================================================

import { EmployeeListComponent } from './features/back-office/employees/employee-list/employee-list.component';
import { EmployeeFormComponent } from './features/back-office/employees/employee-form/employee-form.component';
import { TableAttributionComponent } from './features/back-office/employees/table-attribution/table-attribution.component';
import { MesTablesEmployeComponent } from './features/back-office/employees/mes-tables/mes-tables-employe.component';
import { PresenceComponent } from './features/back-office/employees/presence/presence.component';

// ============================================================
// CLIENTS
// ============================================================

import { ClientListComponent } from './features/back-office/clients/client-list/client-list.component';
import { ClientDetailsComponent } from './features/back-office/clients/client-details/client-details.component';
import { CommandesComponent } from './features/back-office/clients/commandes/commandes.component';
import { ReclamationListComponent } from './features/back-office/clients/reclamations/reclamation-list.component';
import { ConseilListComponent } from './features/back-office/clients/conseils/conseil-list.component';

// ============================================================
// RÉSERVATIONS
// ============================================================

import { ReservationListComponent } from './features/back-office/reservations/reservation-list/reservation-list.component';

// ============================================================
// PDV
// ============================================================

import { PdvLayoutComponent } from './features/pdv/layout/pdv-layout/pdv-layout.component';
import { MesTablesComponent } from './features/pdv/pages/tables/mes-tables/mes-tables.component';

// ============================================================
// MON COMPTE
// ============================================================

import { MonCompteComponent } from './features/back-office/parametres/mon-compte/mon-compte.component';

// ============================================================
// DÉTAILS DU REÇU
// ============================================================

import { DetailsRecuComponent } from './features/back-office/parametres/modeles-recus/details-recu.component';

// ============================================================
// ROUTES
// ============================================================

export const routes: Routes = [
  // ==========================================================
  // AUTH
  // ==========================================================

  {
    // Plus d'écran de choix intermédiaire : accès direct à la
    // connexion Back Office (staff). Les clients arrivent, eux,
    // par un lien dédié à leur restaurant (voir
    // /client/restaurant-selection plus bas).
    path: '',
    redirectTo: 'auth/back-office-login',
    pathMatch: 'full',
  },

  {
    // Ancienne URL de l'écran de choix, conservée en redirection
    // pour ne pas casser les liens/favoris existants.
    path: 'auth/login',
    redirectTo: 'auth/back-office-login',
    pathMatch: 'full',
  },

  {
    path: 'client',
    component: ClientMenuComponent,
    canActivate: [clientGuard],
  },

  {
    path: 'client/produits/:id',
    component: ClientProduitDetailComponent,
    canActivate: [clientGuard],
  },

  {
    // Étape 5 : panier (voir CartService, alimenté depuis la fiche
    // produit à l'étape 4).
    path: 'client/panier',
    component: ClientPanierComponent,
    canActivate: [clientGuard],
  },

  {
    // Étape 5 : finalisation (mode de commande + confirmation).
    path: 'client/checkout',
    component: ClientCheckoutComponent,
    canActivate: [clientGuard],
  },

  {
    // Sidebar client : réclamations (voir ClientReclamationController
    // côté backend).
    path: 'client/reclamations',
    component: ClientReclamationsComponent,
    canActivate: [clientGuard],
  },

  {
    // Sidebar client : suggestions/conseils (voir
    // ClientSuggestionController côté backend).
    path: 'client/suggestions',
    component: ClientSuggestionsComponent,
    canActivate: [clientGuard],
  },

  {
    // Sidebar client : historique de commandes (voir
    // ClientCommandeController.getMesCommandes/getMaCommande +
    // getMesLignesCommande côté backend).
    path: 'client/historique',
    component: ClientCommandeHistoriqueComponent,
    canActivate: [clientGuard],
  },

  {
    // Sidebar client : favoris (voir ClientFavoriController côté
    // backend — n'existait pas du tout avant ce lot).
    path: 'client/favoris',
    component: ClientFavorisComponent,
    canActivate: [clientGuard],
  },

  {
    // Sidebar client : réservations (voir ClientReservationController
    // côté backend — n'existait pas du tout avant ce lot, même
    // logique que réclamations/favoris).
    path: 'client/reservations',
    component: ClientReservationsComponent,
    canActivate: [clientGuard],
  },

  {
    // Sidebar client : mon compte. Réutilise MonCompteController
    // (/api/mon-compte, déjà générique pour tout Utilisateur
    // authentifié) — aucun contrôleur dédié nécessaire, voir
    // ClientMonCompteComponent.
    path: 'client/mon-compte',
    component: ClientMonCompteComponent,
    canActivate: [clientGuard],
  },

  {
    // Public : le client choisit son restaurant AVANT de se
    // connecter/s'inscrire (voir ClientRestaurantSelectionComponent).
    path: 'client/restaurant-selection',
    component: ClientRestaurantSelectionComponent,
  },

  {
    // Public : login + inscription pour le restaurant choisi
    // juste avant (redirige lui-même vers restaurant-selection
    // si aucun restaurant n'est sélectionné).
    path: 'client/auth',
    component: ClientAuthComponent,
  },

  {
    // Connexion Super-Admin + Employé (Back Office) UNIQUEMENT.
    // Les comptes Client ne peuvent pas se connecter ici : voir
    // UnifiedLoginComponent.onSubmit(), qui n'appelle que les
    // endpoints /login/back-office et /login/super-admin.
    // La connexion client se fait exclusivement sur /client/auth.
    path: 'auth/back-office-login',
    component: UnifiedLoginComponent,
  },

  {
    // Le login PDV n'est plus exposé dans l'interface : toute
    // tentative d'accès direct est renvoyée vers la connexion
    // Back Office.
    path: 'auth/pdv-login',
    redirectTo: 'auth/back-office-login',
    pathMatch: 'full',
  },

  {
    path: 'auth/restaurant-selection',
    component: RestaurantSelectionComponent,
    canActivate: [backOfficeGuard],
  },

  {
    // Ancienne URL dédiée SUPERADMIN conservée
    // pour ne pas casser les anciens liens.
    path: 'auth/super-admin-login',
    redirectTo: 'auth/back-office-login',
    pathMatch: 'full',
  },

  // ==========================================================
  // SUPER ADMIN
  // ==========================================================

  {
    path: 'super-admin/restaurants',
    redirectTo: 'auth/restaurant-selection',
    pathMatch: 'full',
  },

  // ==========================================================
  // BACK OFFICE
  // ==========================================================

  {
    path: 'back-office',

    component: BackOfficeLayoutComponent,

    canActivate: [backOfficeGuard],

    children: [
      // ========================================================
      // GESTION DES ROLES
      // ========================================================

      {
        path: 'roles',
        component: RoleListComponent,
        canActivate: [adminGuard],
      },

      {
        path: 'roles/nouveau',
        component: RoleFormComponent,
        canActivate: [adminGuard],
      },

      {
        path: 'roles/:id/modifier',
        component: RoleFormComponent,
        canActivate: [adminGuard],
      },

      // ========================================================
      // MON COMPTE
      // ========================================================

      {
        path: 'mon-compte',
        component: MonCompteComponent,
      },

      // ========================================================
      // GESTION DES EMPLOYÉS
      // ========================================================

      {
        path: 'employees',
        component: EmployeeListComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'EMPLOYES_LISTE',
        },
      },

      {
        path: 'employees/nouveau',
        component: EmployeeFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'EMPLOYES_AJOUTER',
        },
      },

      {
        path: 'employees/:id/modifier',
        component: EmployeeFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'EMPLOYES_MODIFIER',
        },
      },

      // ========================================================
      // PRÉSENCE
      // ========================================================

      {
        path: 'employees/presence',
        component: PresenceComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'EMPLOYES_PRESENCE',
        },
      },

      // ========================================================
      // ATTRIBUTION DES TABLES
      // ========================================================

      {
        path: 'employees/tables',
        component: TableAttributionComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'EMPLOYES_TABLES',
        },
      },

      // ========================================================
      // MES TABLES — EMPLOYÉ
      // ========================================================

      {
        path: 'employees/mes-tables',
        component: MesTablesEmployeComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'EMPLOYES_MES_TABLES',
        },
      },

      // ========================================================
      // GESTION DES CLIENTS
      // ========================================================

      {
        path: 'clients',
        component: ClientListComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'CLIENTS_LISTE',
        },
      },

      {
        path: 'clients/commandes',
        component: CommandesComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'CLIENTS_COMMANDES',
        },
      },

      {
        path: 'clients/reservations',
        component: ReservationListComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'RESERVATIONS_LISTE',
        },
      },

      {
        path: 'clients/reclamations',
        component: ReclamationListComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'CLIENTS_RECLAMATIONS',
        },
      },

      {
        path: 'clients/conseils',
        component: ConseilListComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'CLIENTS_CONSEILS',
        },
      },

      {
        path: 'clients/:id',
        component: ClientDetailsComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'CLIENTS_LISTE',
        },
      },

      // ========================================================
      // BACK OFFICE DEFAULT
      // ========================================================
      //
      // IMPORTANT :
      // Il faut une vraie redirection ici.
      //
      // Avant :
      //
      // {
      //   path: '',
      //   canActivate: [backOfficeGuard],
      //   pathMatch: 'full',
      // }
      //
      // Cette route ne possédait aucun component ni redirectTo,
      // ce qui pouvait laisser le contenu du router-outlet vide.
      //
      // Maintenant :
      // /back-office -> /back-office/mon-compte
      //

      {
        path: '',
        redirectTo: 'mon-compte',
        pathMatch: 'full',
      },

      // ========================================================
      // VENTES
      // ========================================================

      {
        path: 'ventes/recapitulatif',
        component: RecapitulatifVentesComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'VENTES_RECAPITULATIF',
        },
      },

      {
        path: 'ventes/par-article',
        component: VenteParArticleComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'VENTES_PAR_ARTICLE',
        },
      },

      {
        path: 'ventes/par-categorie',
        component: VenteParCategorieComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'VENTES_PAR_CATEGORIE',
        },
      },

      {
        path: 'ventes/par-mode-paiement',
        component: ParModePaiementComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'VENTES_PAR_MODE_PAIEMENT',
        },
      },

      {
        path: 'ventes/par-recu',
        component: ParRecuComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'VENTES_PAR_RECU',
        },
      },

      {
        path: 'ventes/par-employe',
        component: VenteParEmployeComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'VENTES_PAR_EMPLOYE',
        },
      },

      {
        path: 'ventes/par-modificateur',
        component: VenteParModificateurComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'VENTES_PAR_MODIFICATEUR',
        },
      },

      {
        path: 'ventes/reductions',
        component: VentesReductionsComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'VENTES_REDUCTIONS',
        },
      },

      // ========================================================
      // CAISSE
      // ========================================================

      {
        path: 'caisse',
        component: GestionCaisseComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'GESTION_CAISSE',
        },
      },

      // ========================================================
      // MENU / PRODUITS
      // ========================================================

      {
        path: 'menu/produits',
        component: ProduitListComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_PRODUITS_VOIR',
        },
      },

      {
        path: 'menu/produits/nouveau',
        component: ProduitFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_PRODUITS_AJOUTER',
        },
      },

      {
        path: 'menu/produits/:id/modifier',
        component: ProduitFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_PRODUITS_MODIFIER',
        },
      },

      {
        path: 'menu/produits/:id',
        component: ProduitDetailComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_PRODUITS_VOIR',
        },
      },

      // ========================================================
      // MENU / CATEGORIES
      // ========================================================

      {
        path: 'menu/categories',
        component: CategorieListComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_CATEGORIES_VOIR',
        },
      },

      {
        path: 'menu/categories/nouvelle',
        component: CategorieFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_CATEGORIES_AJOUTER',
        },
      },

      {
        path: 'menu/categories/:id/modifier',
        component: CategorieFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_CATEGORIES_MODIFIER',
        },
      },

      {
        path: 'menu/categories/:id',
        component: CategorieDetailComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_CATEGORIES_VOIR',
        },
      },

      // ========================================================
      // MENU / MODIFICATEURS
      // ========================================================

      {
        path: 'menu/modificateurs',
        component: ModificateurListComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_MODIFICATEURS_VOIR',
        },
      },

      {
        path: 'menu/modificateurs/nouveau',
        component: ModificateurFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_MODIFICATEURS_AJOUTER',
        },
      },

      {
        path: 'menu/modificateurs/:id/modifier',
        component: ModificateurFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_MODIFICATEURS_MODIFIER',
        },
      },

      {
        path: 'menu/modificateurs/:id',
        component: ModificateurDetailComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_MODIFICATEURS_VOIR',
        },
      },

      // ========================================================
      // MENU / RÉDUCTIONS
      // ========================================================

      {
        path: 'menu/reductions',
        component: ReductionListComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_REDUCTIONS_VOIR',
        },
      },

      {
        path: 'menu/reductions/nouvelle',
        component: ReductionFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_REDUCTIONS_AJOUTER',
        },
      },

      {
        path: 'menu/reductions/:id/modifier',
        component: ReductionFormComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'MENU_REDUCTIONS_MODIFIER',
        },
      },

      // ========================================================
      // GESTION DE STOCK
      // ========================================================

      {
        path: 'stocks',
        redirectTo: 'stocks/ingredients',
        pathMatch: 'full',
      },

      {
        path: 'stocks/ingredients',
        component: IngredientsComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'STOCK_INGREDIENTS',
        },
      },

      {
        path: 'stocks/fournisseurs',
        component: FournisseursComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'STOCK_FOURNISSEURS',
        },
      },

      {
        path: 'stocks/commandes',
        component: CommandesFournisseursComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'STOCK_COMMANDES',
        },
      },

      // ========================================================
      // TAXES
      // ========================================================

      {
        path: 'taxes',
        component: TaxesComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'TAXE_LISTE',
        },
      },

      // ========================================================
      // PARAMÈTRES / MÉTHODES DE PAIEMENT
      // ========================================================

      {
        path: 'parametres/methodes-paiement',
        component: ModesPaiementComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'PARAMETRES_PAIEMENT',
        },
      },

      // ========================================================
      // PARAMÈTRES / DÉTAILS DU REÇU
      // ========================================================

      {
        path: 'parametres/recu',
        component: DetailsRecuComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'PARAMETRES_RECU',
        },
      },

      // ========================================================
      // PARAMÈTRES / OPTION DE RESTAURATION
      // ========================================================

      {
        path: 'parametres/restauration',
        component: OptionsRestaurationComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'PARAMETRES_RESTAURATION',
        },
      },

      // ========================================================
      // PARAMÈTRES / APPAREILS PDV
      // ========================================================

      {
        path: 'parametres/appareils-pdv',
        component: AppareilsPdvComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'PARAMETRES_APPAREILS_PDV',
        },
      },

      // ========================================================
      // PARAMÈTRES / DONNÉES DU COMPTE
      // ========================================================

      {
        path: 'parametres/compte',
        component: MonCompteComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'PARAMETRES_COMPTE',
        },
      },
    ],
  },

  // ==========================================================
  // PDV
  // ==========================================================

  {
    path: 'pdv',

    component: PdvLayoutComponent,

    canActivate: [pdvGuard],

    children: [
      {
        path: 'tables',
        component: MesTablesComponent,

        canActivate: [PermissionGuard],

        data: {
          permission: 'EMPLOYES_MES_TABLES',
        },
      },
    ],
  },

  // ==========================================================
  // FALLBACK
  // ==========================================================

  {
    path: '**',
    redirectTo: 'auth/back-office-login',
  },
];
