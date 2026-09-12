
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { Categorie } from '../../../../../core/models/categorie.model';
import { Produit } from '../../../../../core/models/produit.model';

import { CategorieService } from '../../../../../core/services/categorie.service';
import { ProduitService } from '../../../../../core/services/produit.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-categorie-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './categorie-details.component.html',
  styleUrl: './categorie-details.component.css',
})
export class CategorieDetailComponent implements OnInit {
  categorie: Categorie | null = null;

  sousCategories: Categorie[] = [];
  produits: Produit[] = [];

  loading = false;
  loadingContenu = false;

  error = '';

  constructor(
    private readonly categorieService: CategorieService,
    private readonly produitService: ProduitService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    // =========================================================
    // PROTECTION : VOIR LES CATÉGORIES
    // =========================================================

    if (!this.canViewCategories()) {
      this.router.navigate(['/back-office']);
      return;
    }

    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);

    if (!idParam || Number.isNaN(id) || id <= 0) {
      this.error = 'Identifiant de la catégorie invalide.';
      this.loading = false;
      return;
    }

    this.loadCategorie(id);
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canViewCategories(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_CATEGORIES_VOIR')
    );
  }

  canEditCategory(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_CATEGORIES_MODIFIER')
    );
  }

  canDeleteCategory(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_CATEGORIES_SUPPRIMER')
    );
  }

  // =========================================================
  // CATEGORIE
  // =========================================================

  loadCategorie(id: number): void {
    if (!this.canViewCategories()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loading = true;
    this.error = '';
    this.categorie = null;

    this.categorieService
      .getById(id)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (data: Categorie) => {
          if (!data) {
            this.error =
              'La catégorie n’existe pas ou la réponse du serveur est vide.';
            return;
          }

          this.categorie = data;

          this.loadContenu(data);
        },

        error: (err) => {
          console.error('Erreur catégorie :', err);

          this.categorie = null;
          this.error = 'Impossible de charger la catégorie.';
        },
      });
  }

  // =========================================================
  // CONTENU
  // =========================================================

  loadContenu(categorie: Categorie): void {
    if (!this.canViewCategories()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadingContenu = true;

    this.categorieService.getByCategorieParent(categorie).subscribe({
      next: (data) => {
        this.sousCategories = Array.isArray(data) ? data : [];

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur sous-catégories :', err);

        this.sousCategories = [];

        this.cdr.detectChanges();
      },
    });

    this.produitService.getByCategorieParent(categorie).subscribe({
      next: (data) => {
        this.produits = Array.isArray(data) ? data : [];

        this.loadingContenu = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur produits de la catégorie :', err);

        this.produits = [];
        this.loadingContenu = false;

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // NAVIGATION
  // =========================================================

  modifier(): void {
    if (!this.categorie) {
      return;
    }

    // Double vérification de sécurité
    if (!this.canEditCategory()) {
      return;
    }

    this.router.navigate([
      '/back-office/menu/categories',
      this.categorie.id_element,
      'modifier',
    ]);
  }

  retour(): void {
    this.router.navigate(['/back-office/menu/categories']);
  }

  voirProduit(produit: Produit): void {
    if (!this.canViewCategories()) {
      return;
    }

    this.router.navigate([
      '/back-office/menu/produits',
      produit.id_element,
    ]);
  }

  voirSousCategorie(categorie: Categorie): void {
    if (!this.canViewCategories()) {
      return;
    }

    this.router.navigate([
      '/back-office/menu/categories',
      categorie.id_element,
    ]);
  }

  // =========================================================
  // AFFICHAGE
  // =========================================================

  get parentNom(): string {
    return this.categorie?.categorieParent?.nom || 'Catégorie racine';
  }

  get restaurantNom(): string {
    return (
      this.categorie?.restaurant?.nomRestaurant ||
      'Aucun restaurant'
    );
  }

  get elementsCount(): number {
    return this.sousCategories.length + this.produits.length;
  }
}

