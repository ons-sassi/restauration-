import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Ingredient } from '../../../../core/models/ingredient.model';
import { Fournisseur } from '../../../../core/models/fournisseur.model';

import { IngredientService } from '../../../../core/services/ingredient.service';
import { FournisseurService } from '../../../../core/services/fournisseur.service';
import { AuthService } from '../../../../core/services/auth.service';

import { StatusBadgeComponent } from '../../../../shared/components/status-badge/status-badge.component';

interface IngredientForm {
  id_ingredient: number | null;
  nom: string;
  unite_mesure: string;
  quantite_stock: number | null;
  seuil_alerte: number | null;
  date_peremption: string;
  fournisseurId: number | null;
}

const FORMULAIRE_VIDE: IngredientForm = {
  id_ingredient: null,
  nom: '',
  unite_mesure: '',
  quantite_stock: null,
  seuil_alerte: null,
  date_peremption: '',
  fournisseurId: null,
};

const SEUIL_JOURS_PEREMPTION = 7;

@Component({
  selector: 'app-ingredients',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  templateUrl: './ingredients.component.html',
  styleUrl: './ingredients.component.css',
})
export class IngredientsComponent implements OnInit {
  // =========================================================
  // DONNÉES
  // =========================================================

  ingredients: Ingredient[] = [];
  fournisseurs: Fournisseur[] = [];

  loading = false;
  error = '';

  // =========================================================
  // FILTRES
  // =========================================================

  searchTerm = '';
  selectedFournisseur: number | null = null;
  selectedStatut = 'tous';

  // =========================================================
  // MODAL / FORMULAIRE
  // =========================================================

  modalOuvert = false;
  isEditMode = false;

  formulaire: IngredientForm = { ...FORMULAIRE_VIDE };

  saving = false;
  formError = '';

  constructor(
    private readonly ingredientService: IngredientService,
    private readonly fournisseurService: FournisseurService,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router,
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
    if (!this.canViewIngredients()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadIngredients();
    this.loadFournisseurs();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================
  // Le cahier des fonctionnalités ne définit qu'un seul code pour
  // ce module (STOCK_INGREDIENTS) : il gouverne l'accès à la page
  // ainsi que les actions ajouter / modifier / supprimer.

  canViewIngredients(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('STOCK_INGREDIENTS');
  }

  canManageIngredients(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('STOCK_INGREDIENTS');
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadIngredients(): void {
    this.loading = true;
    this.error = '';

    this.ingredientService.getAll().subscribe({
      next: (data) => {
        this.ingredients = Array.isArray(data) ? data : [];

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des ingrédients :', err);

        this.ingredients = [];
        this.loading = false;

        this.error = 'Impossible de charger les ingrédients.';

        this.cdr.detectChanges();
      },
    });
  }

  loadFournisseurs(): void {
    this.fournisseurService.getAll().subscribe({
      next: (data) => {
        this.fournisseurs = Array.isArray(data) ? data : [];

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des fournisseurs :', err);

        this.fournisseurs = [];

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // STATUT D'UN INGRÉDIENT
  // =========================================================

  getStatut(ingredient: Ingredient): 'perime' | 'stock-faible' | 'proche-peremption' | 'normal' {
    const joursAvantPeremption = this.getJoursAvantPeremption(ingredient);

    if (joursAvantPeremption !== null && joursAvantPeremption < 0) {
      return 'perime';
    }

    if ((ingredient.quantite_stock ?? 0) <= (ingredient.seuil_alerte ?? 0)) {
      return 'stock-faible';
    }

    if (joursAvantPeremption !== null && joursAvantPeremption <= SEUIL_JOURS_PEREMPTION) {
      return 'proche-peremption';
    }

    return 'normal';
  }

  private getJoursAvantPeremption(ingredient: Ingredient): number | null {
    if (!ingredient.date_peremption) {
      return null;
    }

    const aujourdhui = new Date();
    aujourdhui.setHours(0, 0, 0, 0);

    const dateExpiration = new Date(ingredient.date_peremption);
    dateExpiration.setHours(0, 0, 0, 0);

    const diffMs = dateExpiration.getTime() - aujourdhui.getTime();

    return Math.round(diffMs / (1000 * 60 * 60 * 24));
  }

  getStatutLabel(ingredient: Ingredient): string {
    switch (this.getStatut(ingredient)) {
      case 'perime':
        return 'Périmé';
      case 'stock-faible':
        return 'Stock faible';
      case 'proche-peremption':
        return 'Proche péremption';
      default:
        return 'Normal';
    }
  }

  getStatutType(ingredient: Ingredient): 'success' | 'warning' | 'danger' | 'info' {
    switch (this.getStatut(ingredient)) {
      case 'perime':
        return 'danger';
      case 'stock-faible':
        return 'warning';
      case 'proche-peremption':
        return 'info';
      default:
        return 'success';
    }
  }

  // =========================================================
  // FILTRAGE
  // =========================================================

  get ingredientsFiltres(): Ingredient[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.ingredients.filter((ingredient) => {
      const nom = ingredient.nom?.toLowerCase() ?? '';

      const matchesSearch = !search || nom.includes(search);

      const matchesFournisseur =
        this.selectedFournisseur === null ||
        ingredient.fournisseur?.id_fournisseur === this.selectedFournisseur;

      const matchesStatut =
        this.selectedStatut === 'tous' || this.getStatut(ingredient) === this.selectedStatut;

      return matchesSearch && matchesFournisseur && matchesStatut;
    });
  }

  // =========================================================
  // STATISTIQUES
  // =========================================================

  get totalIngredients(): number {
    return this.ingredients.length;
  }

  get totalStockFaible(): number {
    return this.ingredients.filter((i) => this.getStatut(i) === 'stock-faible').length;
  }

  get totalProchePeremption(): number {
    return this.ingredients.filter((i) => this.getStatut(i) === 'proche-peremption').length;
  }

  get totalPerimes(): number {
    return this.ingredients.filter((i) => this.getStatut(i) === 'perime').length;
  }

  get totalFournisseurs(): number {
    const ids = new Set(
      this.ingredients
        .map((i) => i.fournisseur?.id_fournisseur)
        .filter((id): id is number => id !== undefined && id !== null),
    );

    return ids.size;
  }

  // =========================================================
  // AFFICHAGE
  // =========================================================

  getFournisseurNom(ingredient: Ingredient): string {
    return ingredient.fournisseur?.nom || 'Sans fournisseur';
  }

  // =========================================================
  // MODAL - OUVERTURE
  // =========================================================

  nouvelIngredient(): void {
    if (!this.canManageIngredients()) {
      console.warn('Permission refusée : STOCK_INGREDIENTS');
      return;
    }

    this.isEditMode = false;
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.formError = '';
    this.modalOuvert = true;
  }
  modifierIngredient(ingredient: Ingredient): void {
    if (!this.canManageIngredients()) {
      console.warn('Permission refusée : STOCK_INGREDIENTS');
      return;
    }

    try {
      this.isEditMode = true;
      this.formError = '';

      this.formulaire = {
        id_ingredient: ingredient.id_ingredient,
        nom: ingredient.nom,
        unite_mesure: ingredient.unite_mesure,
        quantite_stock: ingredient.quantite_stock,
        seuil_alerte: ingredient.seuil_alerte,
        date_peremption: this.formatDateForInput(ingredient.date_peremption),
        fournisseurId: ingredient.fournisseur?.id_fournisseur ?? null,
      };

      this.modalOuvert = true;
      this.cdr.detectChanges();
    } catch (e) {
      console.error('ERREUR modifierIngredient:', e);
    }
  }

  fermerModal(): void {
    // Fermer la fenêtre doit toujours fonctionner, même si un enregistrement
    // est en cours (sinon le clic sur la croix ne fait rien de visible).
    this.modalOuvert = false;
    this.saving = false;
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.formError = '';
    this.cdr.detectChanges();
  }

  private formatDateForInput(date: string | Date | null | undefined): string {
    if (!date) {
      return '';
    }

    const d = new Date(date);

    if (Number.isNaN(d.getTime())) {
      return '';
    }

    return d.toISOString().slice(0, 10);
  }

  // =========================================================
  // ENREGISTREMENT
  // =========================================================

  enregistrer(): void {
    if (!this.canManageIngredients()) {
      this.formError = 'Permission refusée : STOCK_INGREDIENTS';
      return;
    }

    this.formError = '';

    const nom = this.formulaire.nom.trim();
    const uniteMesure = this.formulaire.unite_mesure.trim();
    const quantiteStock = this.formulaire.quantite_stock;
    const seuilAlerte = this.formulaire.seuil_alerte;

    if (!nom) {
      this.formError = "Veuillez saisir le nom de l'ingrédient.";
      return;
    }

    if (!uniteMesure) {
      this.formError = "Veuillez saisir l'unité de mesure.";
      return;
    }

    if (quantiteStock === null || quantiteStock < 0) {
      this.formError = 'Veuillez saisir une quantité en stock valide.';
      return;
    }

    if (seuilAlerte === null || seuilAlerte < 0) {
      this.formError = "Veuillez saisir un seuil d'alerte valide.";
      return;
    }

    const fournisseurSelectionne =
      this.fournisseurs.find((f) => f.id_fournisseur === this.formulaire.fournisseurId) ??
      undefined;

    const payload: Partial<Ingredient> = {
      nom,
      unite_mesure: uniteMesure,
      quantite_stock: quantiteStock,
      seuil_alerte: seuilAlerte,
      date_peremption: this.formulaire.date_peremption || undefined,
      fournisseur: fournisseurSelectionne,
    };

    this.saving = true;

    const request$ =
      this.isEditMode && this.formulaire.id_ingredient !== null
        ? this.ingredientService.update(this.formulaire.id_ingredient, payload)
        : this.ingredientService.create(payload);

    request$.subscribe({
      next: () => {
        this.saving = false;

        this.modalOuvert = false;
        this.formulaire = { ...FORMULAIRE_VIDE };

        this.loadIngredients();
      },

      error: (err) => {
        console.error("Erreur lors de l'enregistrement de l'ingrédient :", err);

        this.saving = false;

        this.formError =
          err?.error?.message || "Impossible d'enregistrer l'ingrédient. Veuillez réessayer.";

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerIngredient(ingredient: Ingredient): void {
    if (!this.canManageIngredients()) {
      console.warn('Permission refusée : STOCK_INGREDIENTS');
      return;
    }

    const confirmation = window.confirm(`Voulez-vous vraiment supprimer "${ingredient.nom}" ?`);

    if (!confirmation) {
      return;
    }

    this.ingredientService.delete(ingredient.id_ingredient).subscribe({
      next: () => {
        this.ingredients = this.ingredients.filter(
          (i) => i.id_ingredient !== ingredient.id_ingredient,
        );

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error("Erreur lors de la suppression de l'ingrédient :", err);

        window.alert("Impossible de supprimer l'ingrédient.");
      },
    });
  }

  // =========================================================
  // RESET / TRACK
  // =========================================================

  resetFiltres(): void {
    this.searchTerm = '';
    this.selectedFournisseur = null;
    this.selectedStatut = 'tous';
  }

  trackByIngredient(index: number, ingredient: Ingredient): number {
    return ingredient.id_ingredient;
  }
}
