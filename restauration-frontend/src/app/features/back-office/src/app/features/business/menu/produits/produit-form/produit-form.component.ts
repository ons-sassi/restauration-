import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Produit } from '../../../../../core/models/produit.model';
import { Categorie } from '../../../../../core/models/categorie.model';
import { Ingredient } from '../../../../../core/models/ingredient.model';
import { MediaUrlPipe } from '../../../../../shared/pipes/media-url.pipe';

import { ProduitService } from '../../../../../core/services/produit.service';
import { CategorieService } from '../../../../../core/services/categorie.service';
import { IngredientService } from '../../../../../core/services/ingredient.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-produit-form',
  standalone: true,
  imports: [CommonModule, FormsModule, MediaUrlPipe],
  templateUrl: './produit-form.component.html',
  styleUrl: './produit-form.component.css',
})
export class ProduitFormComponent implements OnInit {
  isEditMode = false;
  produitId: number | null = null;

  loading = false;
  saving = false;

  errorMessage = '';
  successMessage = '';
  imageError = '';

  categories: Categorie[] = [];
  ingredientsDisponibles: Ingredient[] = [];

  ingredientASelectionner: number | null = null;

  produit: Produit = {
    id_element: 0,
    nom: '',
    description: '',
    prix: 0,
    disponible: true,
    temps_preparation: 0,
    ordre_affichage: 0,
    image: '',
    categorieParent: null as unknown as Categorie,
    restaurant: {} as any,
    ingredients: [],
  };

  constructor(
    private readonly produitService: ProduitService,
    private readonly categorieService: CategorieService,
    private readonly ingredientService: IngredientService,
    private readonly authService: AuthService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    this.loadCategories();
    this.loadIngredients();

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      const parsedId = Number(id);

      if (!Number.isNaN(parsedId)) {
        this.produitId = parsedId;
        this.isEditMode = true;

        this.loadProduit(parsedId);
      }
    } else {
      const restaurant = this.authService.getSelectedRestaurant();

      if (restaurant) {
        this.produit.restaurant = restaurant;
      } else {
        this.errorMessage = 'Aucun restaurant sélectionné.';
      }
    }
  }

  // =========================================================
  // CATEGORIES
  // =========================================================

  loadCategories(): void {
    this.categorieService.getAll().subscribe({
      next: (data) => {
        this.categories = Array.isArray(data) ? data : [];

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur catégories :', err);

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // INGREDIENTS
  // =========================================================

  loadIngredients(): void {
    this.ingredientService.getAll().subscribe({
      next: (data) => {
        this.ingredientsDisponibles = Array.isArray(data) ? data : [];

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur ingrédients :', err);

        this.ingredientsDisponibles = [];

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // PRODUIT
  // =========================================================

  loadProduit(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.produitService.getById(id).subscribe({
      next: (data) => {
        this.produit = {
          ...data,
          ingredients: data.ingredients ?? [],
        };

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur produit :', err);

        this.loading = false;

        this.errorMessage = 'Impossible de charger le produit.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // CATEGORIE
  // =========================================================

  onCategorieChange(event: Event): void {
    const select = event.target as HTMLSelectElement;

    const id = Number(select.value);

    if (!id) {
      this.produit.categorieParent = null as unknown as Categorie;

      return;
    }

    const categorie = this.categories.find((c) => c.id_element === id);

    if (categorie) {
      this.produit.categorieParent = categorie;
    }
  }

  get selectedCategorieId(): number | null {
    return this.produit.categorieParent?.id_element ?? null;
  }

  // =========================================================
  // INGREDIENTS
  // =========================================================

  get ingredientsAAjouter(): Ingredient[] {
    const ids = new Set(
      (this.produit.ingredients ?? []).map((ingredient) => ingredient.id_ingredient),
    );

    return this.ingredientsDisponibles.filter((ingredient) => !ids.has(ingredient.id_ingredient));
  }

  ajouterIngredient(): void {
    if (this.ingredientASelectionner === null) {
      return;
    }

    const ingredient = this.ingredientsDisponibles.find(
      (item) => item.id_ingredient === this.ingredientASelectionner,
    );

    if (!ingredient) {
      return;
    }

    const existe = this.produit.ingredients.some(
      (item) => item.id_ingredient === ingredient.id_ingredient,
    );

    if (!existe) {
      this.produit.ingredients = [...this.produit.ingredients, ingredient];
    }

    this.ingredientASelectionner = null;
  }

  retirerIngredient(ingredient: Ingredient): void {
    this.produit.ingredients = this.produit.ingredients.filter(
      (item) => item.id_ingredient !== ingredient.id_ingredient,
    );
  }

  // =========================================================
  // IMAGE
  // =========================================================

  onImageSelected(event: Event): void {
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
      this.produit.image = reader.result as string;
    };

    reader.onerror = () => {
      this.imageError = 'Impossible de lire l’image.';
    };

    reader.readAsDataURL(file);
  }

  supprimerImage(): void {
    this.produit.image = '';
  }
  // =========================================================
  // PERMISSIONS
  // =========================================================

  get canAjouter(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_PRODUITS_AJOUTER');
  }

  get canModifier(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_PRODUITS_MODIFIER');
  }

  /**
   * Permission requise pour l'action en cours (création ou modification).
   */
  get canSave(): boolean {
    return this.isEditMode ? this.canModifier : this.canAjouter;
  }

  // =========================================================
  // SAVE
  // =========================================================

  save(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.canSave) {
      this.errorMessage = this.isEditMode
        ? 'Permission refusée : MENU_PRODUITS_MODIFIER'
        : 'Permission refusée : MENU_PRODUITS_AJOUTER';

      return;
    }

    // -------------------------------
    // VALIDATION
    // -------------------------------

    if (!this.produit.nom?.trim()) {
      this.errorMessage = 'Le nom du produit est obligatoire.';

      return;
    }

    if (this.produit.prix === null || this.produit.prix === undefined || this.produit.prix < 0) {
      this.errorMessage = 'Le prix est invalide.';

      return;
    }

    if (
      this.produit.temps_preparation === null ||
      this.produit.temps_preparation === undefined ||
      this.produit.temps_preparation < 0
    ) {
      this.errorMessage = 'Le temps de préparation est invalide.';

      return;
    }

    if (!this.produit.restaurant?.id_restaurant) {
      this.errorMessage = 'Aucun restaurant sélectionné.';

      return;
    }

    // -------------------------------
    // NORMALISATION
    // -------------------------------

    this.produit.nom = this.produit.nom.trim();

    this.produit.description = this.produit.description?.trim() ?? '';

    // -------------------------------
    // PAYLOAD
    // -------------------------------

    const payload: any = {
      nom: this.produit.nom,

      description: this.produit.description,

      prix: Number(this.produit.prix),

      disponible: this.produit.disponible,

      temps_preparation: Number(this.produit.temps_preparation),

      ordre_affichage: Number(this.produit.ordre_affichage || 0),

      image: this.produit.image || null,

      categorieParent: this.produit.categorieParent,

      restaurant: this.produit.restaurant,

      ingredients: this.produit.ingredients ?? [],
    };

    this.saving = true;

    // -------------------------------
    // UPDATE
    // -------------------------------

    if (this.isEditMode && this.produitId !== null) {
      this.produitService.update(this.produitId, payload).subscribe({
        next: () => {
          this.saving = false;

          this.successMessage = 'Produit modifié avec succès.';

          this.cdr.detectChanges();

          setTimeout(() => {
            this.router.navigate(['/back-office/menu/produits']);
          }, 600);
        },

        error: (err) => {
          console.error('Erreur modification :', err);

          this.saving = false;

          this.errorMessage = 'Impossible de modifier le produit.';

          this.cdr.detectChanges();
        },
      });

      return;
    }

    // -------------------------------
    // CREATE
    // -------------------------------

    this.produitService.create(payload).subscribe({
      next: () => {
        this.saving = false;

        this.successMessage = 'Produit créé avec succès.';

        this.cdr.detectChanges();

        setTimeout(() => {
          this.router.navigate(['/back-office/menu/produits']);
        }, 600);
      },

      error: (err) => {
        console.error('Erreur création :', err);

        this.saving = false;

        this.errorMessage = 'Impossible de créer le produit.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // ANNULER
  // =========================================================

  cancel(): void {
    this.router.navigate(['/back-office/menu/produits']);
  }

  // =========================================================
  // RESTAURANT
  // =========================================================

  get restaurantNom(): string {
    return this.produit.restaurant?.nomRestaurant || 'Aucun restaurant';
  }
}
