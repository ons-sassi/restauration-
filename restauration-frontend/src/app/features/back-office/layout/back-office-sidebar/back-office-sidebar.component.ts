import { Component, Input } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../../../core/services/auth.service';
import { MediaUrlPipe } from '../../../../shared/pipes/media-url.pipe';

interface BackOfficeMenuItem {
  label: string;
  route: string;
  icon: string;
  permission: string;
}

interface BackOfficeMenuGroup {
  title: string;
  icon: string;
  children: BackOfficeMenuItem[];
  permission?: string;
}

@Component({
  selector: 'app-back-office-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MediaUrlPipe],
  templateUrl: './back-office-sidebar.component.html',
  styleUrl: './back-office-sidebar.component.css',
})
export class BackOfficeSidebarComponent {
  @Input() collapsed = false;

  /**
   * Permet d'ouvrir/fermer les modules.
   */
  private readonly openedModules = new Set<string>();

  /**
   * ==========================================================
   * MENU BACK OFFICE
   * ==========================================================
   *
   * Structure :
   *
   * Module
   *   ├── Fonctionnalité
   *   ├── Fonctionnalité
   *   └── Fonctionnalité
   *
   * Chaque fonctionnalité possède sa propre permission.
   */
  private readonly menuGroups: BackOfficeMenuGroup[] = [
    // ========================================================
    // VENTES
    // ========================================================

    {
      title: 'Rapports des ventes',
      icon: 'sales',

      children: [
        {
          label: 'Récapitulatif des ventes',
          route: '/back-office/ventes/recapitulatif',
          icon: 'report',
          permission: 'VENTES_RECAPITULATIF',
        },

        {
          label: 'Vente par article',
          route: '/back-office/ventes/par-article',
          icon: 'article',
          permission: 'VENTES_PAR_ARTICLE',
        },

        {
          label: 'Vente par catégorie',
          route: '/back-office/ventes/par-categorie',
          icon: 'category',
          permission: 'VENTES_PAR_CATEGORIE',
        },

        {
          label: 'Vente par mode de paiement',
          route: '/back-office/ventes/par-mode-paiement',
          icon: 'payment',
          permission: 'VENTES_PAR_MODE_PAIEMENT',
        },

        {
          label: 'Vente par reçu',
          route: '/back-office/ventes/par-recu',
          icon: 'receipt',
          permission: 'VENTES_PAR_RECU',
        },

        {
          label: 'Vente par employé',
          route: '/back-office/ventes/par-employe',
          icon: 'employees',
          permission: 'VENTES_PAR_EMPLOYE',
        },

        {
          label: 'Vente par modificateur',
          route: '/back-office/ventes/par-modificateur',
          icon: 'modifier',
          permission: 'VENTES_PAR_MODIFICATEUR',
        },

        {
          label: 'Réductions',
          route: '/back-office/ventes/reductions',
          icon: 'discount',
          permission: 'VENTES_REDUCTIONS',
        },

        {
          label: 'Gestion caisse',
          route: '/back-office/caisse',
          icon: 'cash',
          permission: 'GESTION_CAISSE',
        },
      ],
    },

    // ========================================================
    // MENU
    // ========================================================

    {
      title: 'Menu',
      icon: 'menu',

      children: [
        {
          label: 'Produits',
          route: '/back-office/menu/produits',
          icon: 'product',
          permission: 'MENU_PRODUITS',
        },

        {
          label: 'Catégories',
          route: '/back-office/menu/categories',
          icon: 'category',
          permission: 'MENU_CATEGORIES',
        },

        {
          label: 'Modificateurs',
          route: '/back-office/menu/modificateurs',
          icon: 'modifier',
          permission: 'MENU_MODIFICATEURS',
        },

        {
          label: 'Réductions',
          route: '/back-office/menu/reductions',
          icon: 'discount',
          permission: 'MENU_REDUCTIONS',
        },
      ],
    },

    // ========================================================
    // STOCK
    // ========================================================

    {
      title: 'Gestion de stock',
      icon: 'stock',

      children: [
        {
          label: 'Ingrédients',
          route: '/back-office/stocks/ingredients',
          icon: 'ingredient',
          permission: 'STOCK_INGREDIENTS',
        },

        {
          label: 'Commandes fournisseurs',
          route: '/back-office/stocks/commandes',
          icon: 'orders',
          permission: 'STOCK_COMMANDES',
        },

        {
          label: 'Fournisseurs',
          route: '/back-office/stocks/fournisseurs',
          icon: 'supplier',
          permission: 'STOCK_FOURNISSEURS',
        },
      ],
    },

    // ========================================================
    // TAXES
    // ========================================================

    {
      title: 'Gestion des taxes',
      icon: 'tax',

      children: [
        {
          label: 'Taxes',
          route: '/back-office/taxes',
          icon: 'tax',
          permission: 'TAXE_LISTE',
        },
      ],
    },

    // ========================================================
    // EMPLOYÉS
    // ========================================================

    {
      title: 'Gestion des employés',
      icon: 'employees',

      children: [
        {
          label: 'Liste des employés',
          route: '/back-office/employees',
          icon: 'employees',
          permission: 'EMPLOYES_LISTE',
        },

        {
          label: 'Présence',
          route: '/back-office/employees/presence',
          icon: 'presence',
          permission: 'EMPLOYES_PRESENCE',
        },

        {
          label: 'Attribution des tables',
          route: '/back-office/employees/tables',
          icon: 'tables',
          permission: 'EMPLOYES_TABLES',
        },

        {
          label: 'Mes tables',
          route: '/back-office/employees/mes-tables',
          icon: 'tables',
          permission: 'EMPLOYES_MES_TABLES',
        },

        {
          label: 'Gestion des rôles',
          route: '/back-office/roles',
          icon: 'roles',
          // Permission fictive : jamais attribuée à un employé.
          // Cette page reste réservée à l'administrateur (adminGuard),
          // donc seul l'admin la voit dans le sidebar (voir visibleGroups).
          permission: 'ADMIN_ONLY',
        },
      ],
    },

    // ========================================================
    // CLIENTS
    // ========================================================

    {
      title: 'Gestion des clients',
      icon: 'clients',

      children: [
        {
          label: 'Clients',
          route: '/back-office/clients',
          icon: 'clients',
          permission: 'CLIENTS_LISTE',
        },

        {
          label: 'Commandes',
          route: '/back-office/clients/commandes',
          icon: 'orders',
          permission: 'CLIENTS_COMMANDES',
        },

        {
          label: 'Réclamations',
          route: '/back-office/clients/reclamations',
          icon: 'complaints',
          permission: 'CLIENTS_RECLAMATIONS',
        },

        {
          label: 'Conseils clients',
          route: '/back-office/clients/conseils',
          icon: 'advice',
          permission: 'CLIENTS_CONSEILS',
        },

        {
          label: 'Réservations',
          route: '/back-office/clients/reservations',
          icon: 'reservation',
          permission: 'RESERVATIONS_LISTE',
        },
      ],
    },

    // ========================================================
    // PARAMÈTRES
    // ========================================================

    {
      title: 'Paramètres',
      icon: 'settings',

      children: [
        {
          label: 'Méthodes de paiement',
          route: '/back-office/parametres/methodes-paiement',
          icon: 'payment',
          permission: 'PARAMETRES_PAIEMENT',
        },

        {
          label: 'Détails du reçu',
          route: '/back-office/parametres/recu',
          icon: 'receipt',
          permission: 'PARAMETRES_RECU',
        },

        {
          label: 'Option de restauration',
          route: '/back-office/parametres/restauration',
          icon: 'restaurant',
          permission: 'PARAMETRES_RESTAURATION',
        },

        {
          label: 'Appareils PDV',
          route: '/back-office/parametres/appareils-pdv',
          icon: 'device',
          permission: 'PARAMETRES_APPAREILS_PDV',
        },

        {
          label: 'Données du compte',
          route: '/back-office/parametres/compte',
          icon: 'account',
          permission: 'PARAMETRES_COMPTE',
        },
      ],
    },
  ];

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  // ==========================================================
  // ADMIN
  // ==========================================================

  /**
   * L'ADMIN voit automatiquement tous les modules
   * et toutes leurs fonctionnalités.
   */
  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  // ==========================================================
  // LOGO DU RESTAURANT
  // ==========================================================

  get restaurantLogo(): string | null {
    return this.authService.getSelectedRestaurant()?.logo ?? null;
  }

  get restaurantInitiale(): string {
    return this.authService.getSelectedRestaurant()?.nomRestaurant?.charAt(0) ?? 'R';
  }

  // ==========================================================
  // GROUPES VISIBLES
  // ==========================================================

  get visibleGroups(): BackOfficeMenuGroup[] {
    /**
     * ADMIN
     *
     * Aucun filtrage.
     */
    if (this.isAdmin) {
      return this.menuGroups;
    }

    /**
     * EMPLOYÉ
     *
     * On conserve uniquement les fonctionnalités
     * pour lesquelles l'utilisateur possède la permission.
     */
    return this.menuGroups
      .map((group) => ({
        ...group,

        children: group.children.filter((item) => this.authService.hasPermission(item.permission)),
      }))
      .filter((group) => group.children.length > 0);
  }

  // ==========================================================
  // MODULE OUVERT ?
  // ==========================================================

  isModuleOpen(group: BackOfficeMenuGroup): boolean {
    /**
     * Si le sidebar est réduit,
     * les sous-menus ne sont pas affichés.
     */
    if (this.collapsed) {
      return false;
    }

    return this.openedModules.has(group.title);
  }

  // ==========================================================
  // OUVRIR / FERMER
  // ==========================================================

  toggleModule(group: BackOfficeMenuGroup): void {
    if (this.openedModules.has(group.title)) {
      this.openedModules.delete(group.title);
    } else {
      this.openedModules.add(group.title);
    }
  }

  // ==========================================================
  // ROUTE ACTIVE
  // ==========================================================

  isRouteActive(group: BackOfficeMenuGroup): boolean {
    return group.children.some((item) => this.router.url.startsWith(item.route));
  }

  // ==========================================================
  // SIDEBAR
  // ==========================================================

  toggleSidebar(): void {
    this.collapsed = !this.collapsed;
  }
}
