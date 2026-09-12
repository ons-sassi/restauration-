
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Modificateur } from '../../../../../core/models/modificateur.model';
import { Produit } from '../../../../../core/models/produit.model';

import { ModificateurService } from '../../../../../core/services/modificateur.service';
import { ProduitService } from '../../../../../core/services/produit.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-modificateur-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modificateur-list.component.html',
  styleUrl: './modificateur-list.component.css',
})
export class ModificateurListComponent implements OnInit {
  modificateurs: Modificateur[] = [];
  produits: Produit[] = [];

  loading = false;
  error = '';

  searchTerm = '';
  selectedProduitId: number | 'tous' = 'tous';

  constructor(
    private readonly modificateurService: ModificateurService,
    private readonly produitService: ProduitService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    // Protection de la page
    if (!this.canViewModificateurs()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadModificateurs();
    this.loadProduits();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canViewModificateurs(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_MODIFICATEURS_VOIR')
    );
  }

  canAddModificateur(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_MODIFICATEURS_AJOUTER')
    );
  }

  canEditModificateur(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_MODIFICATEURS_MODIFIER')
    );
  }

  canDeleteModificateur(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_MODIFICATEURS_SUPPRIMER')
    );
  }

  // =========================================================
  // MODIFICATEURS
  // =========================================================

  loadModificateurs(): void {
    if (!this.canViewModificateurs()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loading = true;
    this.error = '';

    this.modificateurService.getAll().subscribe({
      next: (data) => {
        this.modificateurs = Array.isArray(data) ? data : [];

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error(
          'Erreur lors du chargement des modificateurs :',
          err,
        );

        this.modificateurs = [];
        this.loading = false;

        this.error = 'Impossible de charger les modificateurs.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // PRODUITS
  // =========================================================

  loadProduits(): void {
    if (!this.canViewModificateurs()) {
      return;
    }

    this.produitService.getAll().subscribe({
      next: (data) => {
        const tous = Array.isArray(data) ? data : [];

        const restaurantId =
          this.authService.getSelectedRestaurantId();

        this.produits = restaurantId
          ? tous.filter(
              (p) =>
                p.restaurant?.id_restaurant === restaurantId,
            )
          : tous;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur produits :', err);

        this.produits = [];

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FILTRAGE
  // =========================================================

  get modificateursFiltres(): Modificateur[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.modificateurs.filter((modificateur) => {
      const nom =
        modificateur.nom_modificateur?.toLowerCase() ?? '';

      const nomsProduits = (modificateur.produits ?? [])
        .map((p) => p.nom?.toLowerCase() ?? '')
        .join(' ');

      const matchesSearch =
        !search ||
        nom.includes(search) ||
        nomsProduits.includes(search);

      const matchesProduit =
        this.selectedProduitId === 'tous' ||
        (modificateur.produits ?? []).some(
          (p) =>
            p.id_element === this.selectedProduitId,
        );

      return matchesSearch && matchesProduit;
    });
  }

  // =========================================================
  // STATISTIQUES
  // =========================================================

  get produitsDistinctsCount(): number {
    const ids = new Set(
      this.modificateurs
        .flatMap((m) => m.produits ?? [])
        .map((p) => p.id_element)
        .filter(
          (id): id is number => !!id,
        ),
    );

    return ids.size;
  }

  get prixMoyen(): number {
    if (this.modificateurs.length === 0) {
      return 0;
    }

    const total = this.modificateurs.reduce(
      (sum, m) =>
        sum +
        (Number(m.prix_supplementaire) || 0),
      0,
    );

    return total / this.modificateurs.length;
  }

  get prixSupplementaireMax(): number {
    if (this.modificateurs.length === 0) {
      return 0;
    }

    return Math.max(
      ...this.modificateurs.map(
        (m) =>
          Number(m.prix_supplementaire) || 0,
      ),
    );
  }

  // =========================================================
  // NAVIGATION
  // =========================================================

  nouveauModificateur(): void {
    if (!this.canAddModificateur()) {
      return;
    }

    this.router.navigate([
      '/back-office/menu/modificateurs/nouveau',
    ]);
  }

  modifierModificateur(
    modificateur: Modificateur,
  ): void {
    if (!this.canEditModificateur()) {
      return;
    }

    this.router.navigate([
      '/back-office/menu/modificateurs',
      modificateur.id_modificateur,
      'modifier',
    ]);
  }

  voirDetails(
    modificateur: Modificateur,
  ): void {
    if (!this.canViewModificateurs()) {
      return;
    }

    this.router.navigate([
      '/back-office/menu/modificateurs',
      modificateur.id_modificateur,
    ]);
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerModificateur(
    modificateur: Modificateur,
  ): void {
    // Double protection
    if (!this.canDeleteModificateur()) {
      return;
    }

    const confirmation = window.confirm(
      `Voulez-vous vraiment supprimer le modificateur "${modificateur.nom_modificateur}" ?`,
    );

    if (!confirmation) {
      return;
    }

    this.modificateurService
      .delete(modificateur.id_modificateur)
      .subscribe({
        next: () => {
          this.modificateurs =
            this.modificateurs.filter(
              (m) =>
                m.id_modificateur !==
                modificateur.id_modificateur,
            );

          this.cdr.detectChanges();
        },

        error: (err) => {
          console.error(
            'Erreur suppression modificateur :',
            err,
          );

          const message =
            err?.error?.message ||
            'Impossible de supprimer le modificateur.';

          window.alert(message);
        },
      });
  }

  // =========================================================
  // RESET
  // =========================================================

  resetFiltres(): void {
    this.searchTerm = '';
    this.selectedProduitId = 'tous';
  }

  // =========================================================
  // TRACK
  // =========================================================

  trackByModificateur(
    index: number,
    modificateur: Modificateur,
  ): number {
    return modificateur.id_modificateur;
  }
}

