import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Categorie } from '../../../../../core/models/categorie.model';
import { MediaUrlPipe } from '../../../../../shared/pipes/media-url.pipe';

import { CategorieService } from '../../../../../core/services/categorie.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-categorie-form',
  standalone: true,
  imports: [CommonModule, FormsModule, MediaUrlPipe],
  templateUrl: './categorie-form.component.html',
  styleUrl: './categorie-form.component.css',
})
export class CategorieFormComponent implements OnInit {
  isEditMode = false;
  categorieId: number | null = null;

  loading = false;
  saving = false;

  errorMessage = '';
  successMessage = '';
  imageError = '';

  categoriesParentesDisponibles: Categorie[] = [];

  categorie: Categorie = {
    id_element: 0,
    nom: '',
    description_categorie: '',
    ordre_affichage: 0,
    image: '',
    categorieParent: null as unknown as Categorie,
    restaurant: {} as any,
  };

  constructor(
    private readonly categorieService: CategorieService,
    private readonly authService: AuthService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    /*
     * =======================================================
     * MODE MODIFICATION
     * =======================================================
     */

    if (id) {
      const parsedId = Number(id);

      if (Number.isNaN(parsedId)) {
        this.router.navigate(['/back-office/menu/categories']);
        return;
      }

      this.categorieId = parsedId;
      this.isEditMode = true;

      /*
       * Vérification de la permission MODIFIER.
       */
      if (!this.canEditCategory()) {
        this.router.navigate(['/back-office/menu/categories']);
        return;
      }

      this.loadCategorie(parsedId);
      return;
    }

    /*
     * =======================================================
     * MODE CREATION
     * =======================================================
     */

    /*
     * Vérification de la permission AJOUTER.
     */
    if (!this.canAddCategory()) {
      this.router.navigate(['/back-office/menu/categories']);
      return;
    }

    const restaurant = this.authService.getSelectedRestaurant();

    if (restaurant) {
      this.categorie.restaurant = restaurant;
    } else {
      this.errorMessage = 'Aucun restaurant sélectionné.';
    }

    this.loadCategoriesParentes();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  /**
   * L'utilisateur peut voir les catégories.
   */
  canViewCategories(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_CATEGORIES_VOIR');
  }

  /**
   * L'utilisateur peut créer une catégorie.
   */
  canAddCategory(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_CATEGORIES_AJOUTER');
  }

  /**
   * L'utilisateur peut modifier une catégorie.
   */
  canEditCategory(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_CATEGORIES_MODIFIER');
  }

  /**
   * L'utilisateur peut supprimer une catégorie.
   *
   * Non utilisé directement dans ce formulaire,
   * mais conservé pour garder la logique centralisée.
   */
  canDeleteCategory(): boolean {
    return (
      this.authService.isAdmin() || this.authService.hasPermission('MENU_CATEGORIES_SUPPRIMER')
    );
  }

  /**
   * Permission requise pour le formulaire courant.
   */
  canSave(): boolean {
    if (this.isEditMode) {
      return this.canEditCategory();
    }

    return this.canAddCategory();
  }

  // =========================================================
  // CATEGORIES PARENTES
  // =========================================================

  loadCategoriesParentes(): void {
    /*
     * Une personne qui ne peut pas consulter les catégories
     * ne doit pas charger la liste des catégories parentes.
     */
    if (!this.canViewCategories()) {
      this.categoriesParentesDisponibles = [];
      return;
    }

    this.categorieService.getAll().subscribe({
      next: (data) => {
        const toutes = Array.isArray(data) ? data : [];

        this.categoriesParentesDisponibles = toutes.filter(
          (c) => c.id_element !== this.categorieId,
        );

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur catégories :', err);

        this.categoriesParentesDisponibles = [];

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // CATEGORIE
  // =========================================================

  loadCategorie(id: number): void {
    /*
     * Double vérification avant le chargement.
     */
    if (!this.canEditCategory()) {
      this.router.navigate(['/back-office/menu/categories']);
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.categorieService.getById(id).subscribe({
      next: (data) => {
        this.categorie = { ...data };

        this.loading = false;

        this.loadCategoriesParentes();

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur catégorie :', err);

        this.loading = false;

        this.errorMessage = 'Impossible de charger la catégorie.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // CATEGORIE PARENTE
  // =========================================================

  onCategorieParentChange(event: Event): void {
    if (!this.canSave()) {
      return;
    }

    const select = event.target as HTMLSelectElement;

    const id = Number(select.value);

    if (!id) {
      this.categorie.categorieParent = null as unknown as Categorie;

      return;
    }

    const parent = this.categoriesParentesDisponibles.find((c) => c.id_element === id);

    if (parent) {
      this.categorie.categorieParent = parent;
    }
  }

  get selectedCategorieParentId(): number | null {
    return this.categorie.categorieParent?.id_element ?? null;
  }

  // =========================================================
  // IMAGE
  // =========================================================

  onImageSelected(event: Event): void {
    if (!this.canSave()) {
      return;
    }

    const input = event.target as HTMLInputElement;

    const file = input.files?.[0];

    if (!file) {
      return;
    }

    this.imageError = '';

    const maxSize = 2 * 1024 * 1024;

    if (file.size > maxSize) {
      this.imageError = 'L’image ne doit pas dépasser 2 Mo.';

      input.value = '';

      return;
    }

    if (!file.type.startsWith('image/')) {
      this.imageError = 'Veuillez sélectionner une image.';

      input.value = '';

      return;
    }

    const reader = new FileReader();

    reader.onload = () => {
      this.categorie.image = reader.result as string;

      this.cdr.detectChanges();
    };

    reader.onerror = () => {
      this.imageError = 'Impossible de lire l’image.';

      this.cdr.detectChanges();
    };

    reader.readAsDataURL(file);
  }

  supprimerImage(): void {
    if (!this.canSave()) {
      return;
    }

    this.categorie.image = '';
  }

  // =========================================================
  // SAVE
  // =========================================================

  save(): void {
    this.errorMessage = '';
    this.successMessage = '';

    /*
     * =======================================================
     * VERIFICATION DE PERMISSION
     * =======================================================
     *
     * Très important :
     * même si le bouton est caché dans le HTML,
     * quelqu'un peut potentiellement déclencher la méthode.
     */

    if (!this.canSave()) {
      this.errorMessage = this.isEditMode
        ? 'Vous n’avez pas la permission de modifier une catégorie.'
        : 'Vous n’avez pas la permission de créer une catégorie.';

      return;
    }

    // -------------------------------
    // VALIDATION
    // -------------------------------

    if (!this.categorie.nom?.trim()) {
      this.errorMessage = 'Le nom de la catégorie est obligatoire.';

      return;
    }

    if (!this.categorie.restaurant?.id_restaurant) {
      this.errorMessage = 'Aucun restaurant sélectionné.';

      return;
    }

    // -------------------------------
    // NORMALISATION
    // -------------------------------

    this.categorie.nom = this.categorie.nom.trim();

    this.categorie.description_categorie = this.categorie.description_categorie?.trim() ?? '';

    // -------------------------------
    // PAYLOAD
    // -------------------------------

    const payload: any = {
      nom: this.categorie.nom,

      description_categorie: this.categorie.description_categorie,

      ordre_affichage: Number(this.categorie.ordre_affichage || 0),

      image: this.categorie.image || null,

      categorieParent: this.categorie.categorieParent,

      restaurant: this.categorie.restaurant,
    };

    this.saving = true;

    // =======================================================
    // UPDATE
    // =======================================================

    if (this.isEditMode && this.categorieId !== null) {
      /*
       * Sécurité supplémentaire.
       */
      if (!this.canEditCategory()) {
        this.saving = false;

        this.errorMessage = 'Vous n’avez pas la permission de modifier une catégorie.';

        return;
      }

      this.categorieService.update(this.categorieId, payload).subscribe({
        next: () => {
          this.saving = false;

          this.successMessage = 'Catégorie modifiée avec succès.';

          this.cdr.detectChanges();

          setTimeout(() => {
            this.router.navigate(['/back-office/menu/categories']);
          }, 600);
        },

        error: (err) => {
          console.error('Erreur modification :', err);

          this.saving = false;

          /*
           * On essaie de conserver le message
           * envoyé par le backend.
           */
          this.errorMessage = err?.error?.message || 'Impossible de modifier la catégorie.';

          this.cdr.detectChanges();
        },
      });

      return;
    }

    // =======================================================
    // CREATE
    // =======================================================

    /*
     * Sécurité supplémentaire.
     */
    if (!this.canAddCategory()) {
      this.saving = false;

      this.errorMessage = 'Vous n’avez pas la permission de créer une catégorie.';

      return;
    }

    this.categorieService.create(payload).subscribe({
      next: () => {
        this.saving = false;

        this.successMessage = 'Catégorie créée avec succès.';

        this.cdr.detectChanges();

        setTimeout(() => {
          this.router.navigate(['/back-office/menu/categories']);
        }, 600);
      },

      error: (err) => {
        console.error('Erreur création :', err);

        this.saving = false;

        this.errorMessage = err?.error?.message || 'Impossible de créer la catégorie.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // ANNULER
  // =========================================================

  cancel(): void {
    this.router.navigate(['/back-office/menu/categories']);
  }

  // =========================================================
  // RESTAURANT
  // =========================================================

  get restaurantNom(): string {
    return this.categorie.restaurant?.nomRestaurant || 'Aucun restaurant';
  }
}
