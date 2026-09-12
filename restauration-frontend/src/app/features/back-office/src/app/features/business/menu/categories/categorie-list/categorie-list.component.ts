import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Categorie } from '../../../../../core/models/categorie.model';
import { MediaUrlPipe } from '../../../../../shared/pipes/media-url.pipe';
import { CategorieService } from '../../../../../core/services/categorie.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-categorie-list',
  standalone: true,
  imports: [CommonModule, FormsModule, MediaUrlPipe],
  templateUrl: './categorie-list.component.html',
  styleUrl: './categorie-list.component.css',
})
export class CategorieListComponent implements OnInit {
  categories: Categorie[] = [];

  loading = false;
  error = '';

  searchTerm = '';
  selectedType: 'tous' | 'racine' | 'sous-categorie' = 'tous';

  constructor(
    private readonly categorieService: CategorieService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly authService: AuthService,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    /*
     * Protection supplémentaire côté composant.
     *
     * La route est déjà protégée par PermissionGuard,
     * mais on vérifie également ici pour éviter qu'un accès
     * direct au composant permette de charger la page.
     */
    if (!this.canViewCategories()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadCategories();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canViewCategories(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_CATEGORIES_VOIR');
  }

  canAddCategory(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_CATEGORIES_AJOUTER');
  }

  canEditCategory(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_CATEGORIES_MODIFIER');
  }

  canDeleteCategory(): boolean {
    return (
      this.authService.isAdmin() || this.authService.hasPermission('MENU_CATEGORIES_SUPPRIMER')
    );
  }

  // =========================================================
  // CATEGORIES
  // =========================================================

  loadCategories(): void {
    if (!this.canViewCategories()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loading = true;
    this.error = '';

    this.categorieService.getAll().subscribe({
      next: (data) => {
        console.log('Réponse API catégories :', data);

        this.categories = Array.isArray(data) ? data : [];

        this.loading = false;

        console.log('Catégories chargées :', this.categories.length);

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des catégories :', err);

        this.categories = [];
        this.loading = false;

        this.error = 'Impossible de charger les catégories.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FILTRAGE
  // =========================================================

  get categoriesFiltrees(): Categorie[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.categories.filter((categorie) => {
      const nom = categorie.nom?.toLowerCase() ?? '';

      const description = categorie.description_categorie?.toLowerCase() ?? '';

      const matchesSearch = !search || nom.includes(search) || description.includes(search);

      let matchesType = true;

      if (this.selectedType === 'racine') {
        matchesType = !categorie.categorieParent;
      }

      if (this.selectedType === 'sous-categorie') {
        matchesType = !!categorie.categorieParent;
      }

      return matchesSearch && matchesType;
    });
  }

  // =========================================================
  // STATISTIQUES
  // =========================================================

  get categoriesRacines(): number {
    return this.categories.filter((categorie) => !categorie.categorieParent).length;
  }

  get sousCategories(): number {
    return this.categories.filter((categorie) => !!categorie.categorieParent).length;
  }

  get categoriesAvecDescription(): number {
    return this.categories.filter((categorie) => !!categorie.description_categorie?.trim()).length;
  }

  // =========================================================
  // CATEGORIE PARENTE
  // =========================================================

  getParentNom(categorie: Categorie): string {
    return categorie.categorieParent?.nom || 'Catégorie racine';
  }

  // =========================================================
  // NAVIGATION
  // =========================================================

  nouvelleCategorie(): void {
    if (!this.canAddCategory()) {
      console.warn('Permission refusée : MENU_CATEGORIES_AJOUTER');
      return;
    }

    this.router.navigate(['/back-office/menu/categories/nouvelle']);
  }

  modifierCategorie(categorie: Categorie): void {
    if (!this.canEditCategory()) {
      console.warn('Permission refusée : MENU_CATEGORIES_MODIFIER');
      return;
    }

    this.router.navigate(['/back-office/menu/categories', categorie.id_element, 'modifier']);
  }

  voirDetails(categorie: Categorie): void {
    if (!this.canViewCategories()) {
      console.warn('Permission refusée : MENU_CATEGORIES_VOIR');
      return;
    }

    this.router.navigate(['/back-office/menu/categories', categorie.id_element]);
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerCategorie(categorie: Categorie): void {
    if (!this.canDeleteCategory()) {
      console.warn('Permission refusée : MENU_CATEGORIES_SUPPRIMER');
      return;
    }

    const confirmation = window.confirm(
      `Voulez-vous vraiment supprimer la catégorie "${categorie.nom}" ?`,
    );

    if (!confirmation) {
      return;
    }

    this.categorieService.delete(categorie.id_element).subscribe({
      next: () => {
        this.categories = this.categories.filter((c) => c.id_element !== categorie.id_element);

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur suppression catégorie :', err);

        const message = err?.error?.message || 'Impossible de supprimer la catégorie.';

        window.alert(message);
      },
    });
  }

  // =========================================================
  // RESET
  // =========================================================

  resetFiltres(): void {
    this.searchTerm = '';
    this.selectedType = 'tous';
  }

  // =========================================================
  // IMAGE
  // =========================================================

  getImage(categorie: Categorie): string {
    if (!categorie.image) {
      return 'assets/images/default-category.png';
    }

    return categorie.image;
  }

  // =========================================================
  // TRACK
  // =========================================================

  trackByCategorie(index: number, categorie: Categorie): number {
    return categorie.id_element;
  }
}
