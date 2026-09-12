import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Produit } from '../../../../../core/models/produit.model';
import { Categorie } from '../../../../../core/models/categorie.model';
import { MediaUrlPipe } from '../../../../../shared/pipes/media-url.pipe';

import { ProduitService } from '../../../../../core/services/produit.service';
import { CategorieService } from '../../../../../core/services/categorie.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-produit-list',
  standalone: true,
  imports: [CommonModule, FormsModule, MediaUrlPipe],
  templateUrl: './produit-list.component.html',
  styleUrl: './produit-list.component.css',
})
export class ProduitListComponent implements OnInit {
  produits: Produit[] = [];
  categories: Categorie[] = [];

  loading = false;
  error = '';

  searchTerm = '';
  selectedCategorie: number | null = null;
  selectedDisponibilite = 'tous';

  constructor(
    private readonly produitService: ProduitService,
    private readonly categorieService: CategorieService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    public readonly authService: AuthService,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    this.loadProduits();
    this.loadCategories();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  /**
   * Vérifie si l'utilisateur possède une permission.
   *
   * L'Admin possède automatiquement toutes les permissions
   * grâce à AuthService.
   */
  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  /**
   * Permission pour voir les produits.
   */
  canViewProducts(): boolean {
    return this.hasPermission('MENU_PRODUITS_VOIR');
  }

  /**
   * Permission pour ajouter un produit.
   */
  canAddProduct(): boolean {
    return this.hasPermission('MENU_PRODUITS_AJOUTER');
  }

  /**
   * Permission pour modifier un produit.
   */
  canEditProduct(): boolean {
    return this.hasPermission('MENU_PRODUITS_MODIFIER');
  }

  /**
   * Permission pour supprimer un produit.
   */
  canDeleteProduct(): boolean {
    return this.hasPermission('MENU_PRODUITS_SUPPRIMER');
  }

  // =========================================================
  // PRODUITS
  // =========================================================

  loadProduits(): void {
    this.loading = true;
    this.error = '';

    this.produitService.getAll().subscribe({
      next: (data) => {
        console.log('Réponse API produits :', data);

        this.produits = Array.isArray(data) ? data : [];

        this.loading = false;

        console.log('Produits chargés :', this.produits.length);

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des produits :', err);

        this.produits = [];
        this.loading = false;

        this.error = 'Impossible de charger les produits.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // CATEGORIES
  // =========================================================

  loadCategories(): void {
    this.categorieService.getAll().subscribe({
      next: (data) => {
        console.log('Réponse API catégories :', data);

        this.categories = Array.isArray(data) ? data : [];

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur catégories :', err);

        this.categories = [];

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FILTRAGE
  // =========================================================

  get produitsFiltres(): Produit[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.produits.filter((produit) => {
      const nom = produit.nom?.toLowerCase() ?? '';

      const description = produit.description?.toLowerCase() ?? '';

      const matchesSearch = !search || nom.includes(search) || description.includes(search);

      const matchesCategorie =
        this.selectedCategorie === null ||
        produit.categorieParent?.id_element === this.selectedCategorie;

      let matchesDisponibilite = true;

      if (this.selectedDisponibilite === 'disponible') {
        matchesDisponibilite = produit.disponible === true;
      }

      if (this.selectedDisponibilite === 'indisponible') {
        matchesDisponibilite = produit.disponible === false;
      }

      return matchesSearch && matchesCategorie && matchesDisponibilite;
    });
  }

  // =========================================================
  // STATISTIQUES
  // =========================================================

  get produitsDisponibles(): number {
    return this.produits.filter((produit) => produit.disponible).length;
  }

  get produitsIndisponibles(): number {
    return this.produits.filter((produit) => !produit.disponible).length;
  }

  get categorieCount(): number {
    return this.categories.length;
  }

  // =========================================================
  // CATEGORIE
  // =========================================================

  getCategorieNom(produit: Produit): string {
    return produit.categorieParent?.nom || 'Sans catégorie';
  }

  // =========================================================
  // NAVIGATION
  // =========================================================

  nouveauProduit(): void {
    if (!this.canAddProduct()) {
      console.warn('Permission refusée : MENU_PRODUITS_AJOUTER');
      return;
    }

    this.router.navigate(['/back-office/menu/produits/nouveau']);
  }

  voirDetails(produit: Produit): void {
    if (!this.canViewProducts()) {
      console.warn('Permission refusée : MENU_PRODUITS_VOIR');
      return;
    }

    this.router.navigate(['/back-office/menu/produits', produit.id_element]);
  }

  modifierProduit(produit: Produit): void {
    if (!this.canEditProduct()) {
      console.warn('Permission refusée : MENU_PRODUITS_MODIFIER');
      return;
    }

    this.router.navigate(['/back-office/menu/produits', produit.id_element, 'modifier']);
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerProduit(produit: Produit): void {
    if (!this.canDeleteProduct()) {
      console.warn('Permission refusée : MENU_PRODUITS_SUPPRIMER');
      return;
    }

    const confirmation = window.confirm(`Voulez-vous vraiment supprimer "${produit.nom}" ?`);

    if (!confirmation) {
      return;
    }

    this.produitService.delete(produit.id_element).subscribe({
      next: () => {
        this.produits = this.produits.filter((p) => p.id_element !== produit.id_element);

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur suppression produit :', err);

        window.alert('Impossible de supprimer le produit.');
      },
    });
  }

  // =========================================================
  // RESET
  // =========================================================

  resetFiltres(): void {
    this.searchTerm = '';
    this.selectedCategorie = null;
    this.selectedDisponibilite = 'tous';
  }

  // =========================================================
  // IMAGE
  // =========================================================

  getImage(produit: Produit): string {
    if (!produit.image) {
      return 'assets/images/default-product.png';
    }

    return produit.image;
  }

  // =========================================================
  // TRACK
  // =========================================================

  trackByProduit(index: number, produit: Produit): number {
    return produit.id_element;
  }
}
