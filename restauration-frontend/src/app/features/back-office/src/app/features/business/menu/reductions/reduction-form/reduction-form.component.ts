import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Reduction } from '../../../../../core/models/reduction.model';
import {
  TypeReduction,
  ApplicationReduction,
} from '../../../../../core/models/enums/type-reduction.enum';
import { Produit } from '../../../../../core/models/produit.model';

import { ReductionService } from '../../../../../core/services/reduction.service';
import { ProduitService } from '../../../../../core/services/produit.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-reduction-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reduction-form.component.html',
  styleUrl: './reduction-form.component.css',
})
export class ReductionFormComponent implements OnInit {
  isEditMode = false;
  reductionId: number | null = null;

  loading = false;
  saving = false;

  errorMessage = '';
  successMessage = '';

  readonly TypeReduction = TypeReduction;
  readonly ApplicationReduction = ApplicationReduction;

  reduction: Reduction = {
    id_reduction: 0,
    nom_reduction: '',
    type: TypeReduction.POURCENTAGE,
    valeur: 0,
    date_debut: '',
    date_fin: '',
    conditions_application: '',
    application_produits: ApplicationReduction.TOUS_PRODUITS,
    produits_ids: [],
    montant_minimum: null,
    automatique: true,
    active: true,
    nombre_applications_autorise: null,
  };

  // Bascule "n'importe quel montant" / "à partir d'un certain montant".
  // Séparé de reduction.montant_minimum pour pouvoir garder la valeur
  // saisie même si l'utilisateur décoche puis recoche la case.
  aUnMontantMinimum = false;

  // Bascule "illimité" / "nombre précis d'applications autorisées".
  // Séparé de reduction.nombre_applications_autorise pour garder la
  // valeur saisie même si l'utilisateur décoche puis recoche la case.
  aUneLimiteApplications = false;

  // Liste des produits du menu, pour la sélection "produits spécifiques".
  produitsDisponibles: Produit[] = [];
  produitsLoading = false;
  produitsError = '';

  constructor(
    private readonly reductionService: ReductionService,
    private readonly produitService: ProduitService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly authService: AuthService,
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
        this.router.navigate(['/back-office/menu/reductions']);
        return;
      }

      this.reductionId = parsedId;
      this.isEditMode = true;

      /*
       * Vérification de la permission MODIFIER.
       */
      if (!this.canEditReduction()) {
        this.router.navigate(['/back-office/menu/reductions']);
        return;
      }

      this.loadProduits();
      this.loadReduction(parsedId);
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
    if (!this.canAddReduction()) {
      this.router.navigate(['/back-office/menu/reductions']);
      return;
    }

    /*
     * Le backend exige un restaurant explicite à la création
     * (voir ReductionController.saveReduction), même pattern que
     * ProduitFormComponent.
     */
    const restaurant = this.authService.getSelectedRestaurant();

    if (restaurant) {
      this.reduction.restaurant = restaurant;
    } else {
      this.errorMessage = 'Aucun restaurant sélectionné.';
    }

    this.loadProduits();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canAddReduction(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_REDUCTIONS_AJOUTER');
  }

  canEditReduction(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_REDUCTIONS_MODIFIER');
  }

  canSave(): boolean {
    if (this.isEditMode) {
      return this.canEditReduction();
    }

    return this.canAddReduction();
  }

  // =========================================================
  // PRODUITS (pour la sélection "produits spécifiques")
  // =========================================================

  loadProduits(): void {
    this.produitsLoading = true;
    this.produitsError = '';

    this.produitService.getAll().subscribe({
      next: (data) => {
        this.produitsDisponibles = Array.isArray(data) ? data : [];

        this.produitsLoading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des produits :', err);

        this.produitsDisponibles = [];
        this.produitsLoading = false;

        this.produitsError = 'Impossible de charger la liste des produits.';

        this.cdr.detectChanges();
      },
    });
  }

  isProduitSelectionne(id: number): boolean {
    return this.reduction.produits_ids?.includes(id) ?? false;
  }

  toggleProduit(id: number, checked: boolean): void {
    const ids = this.reduction.produits_ids ?? [];

    if (checked) {
      if (!ids.includes(id)) {
        this.reduction.produits_ids = [...ids, id];
      }
    } else {
      this.reduction.produits_ids = ids.filter((existingId) => existingId !== id);
    }
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadReduction(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.reductionService.getById(id).subscribe({
      next: (data) => {
        this.reduction = {
          ...data,
          date_debut: this.toDateInput(data.date_debut),
          date_fin: this.toDateInput(data.date_fin),
          application_produits: data.application_produits ?? ApplicationReduction.TOUS_PRODUITS,
          produits_ids: (data.produits ?? []).map((p) => p.id),
        };

        this.aUnMontantMinimum =
          data.montant_minimum !== null &&
          data.montant_minimum !== undefined &&
          data.montant_minimum > 0;

        this.aUneLimiteApplications =
          data.nombre_applications_autorise !== null &&
          data.nombre_applications_autorise !== undefined &&
          data.nombre_applications_autorise > 0;

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement de la réduction :', err);

        this.loading = false;

        this.errorMessage = 'Impossible de charger la réduction.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // VALIDATION
  // =========================================================

  private validate(): boolean {
    if (!this.isEditMode && !this.reduction.restaurant?.id_restaurant) {
      this.errorMessage = 'Aucun restaurant sélectionné.';

      return false;
    }

    if (!this.reduction.nom_reduction?.trim()) {
      this.errorMessage = 'Le nom de la réduction est obligatoire.';

      return false;
    }

    if (!this.reduction.valeur || this.reduction.valeur <= 0) {
      this.errorMessage = 'La valeur de la réduction doit être supérieure à 0.';

      return false;
    }

    if (this.reduction.type === TypeReduction.POURCENTAGE && this.reduction.valeur > 100) {
      this.errorMessage = 'Un pourcentage ne peut pas dépasser 100%.';

      return false;
    }

    if (!this.reduction.date_debut || !this.reduction.date_fin) {
      this.errorMessage = 'Veuillez renseigner la date de début et la date de fin.';

      return false;
    }

    if (this.reduction.date_debut > this.reduction.date_fin) {
      this.errorMessage = 'La date de début doit être antérieure ou égale à la date de fin.';

      return false;
    }

    if (
      this.reduction.application_produits === ApplicationReduction.PRODUITS_SPECIFIQUES &&
      (!this.reduction.produits_ids || this.reduction.produits_ids.length === 0)
    ) {
      this.errorMessage = 'Sélectionnez au moins un produit, ou choisissez "Tous les produits".';

      return false;
    }

    if (
      this.aUnMontantMinimum &&
      (!this.reduction.montant_minimum || this.reduction.montant_minimum <= 0)
    ) {
      this.errorMessage = 'Renseignez un montant minimum supérieur à 0.';

      return false;
    }

    if (
      this.aUneLimiteApplications &&
      (!this.reduction.nombre_applications_autorise ||
        this.reduction.nombre_applications_autorise <= 0)
    ) {
      this.errorMessage =
        'Renseignez un nombre d’applications autorisées supérieur à 0, ou choisissez "Illimité".';

      return false;
    }

    return true;
  }

  // =========================================================
  // ENREGISTREMENT
  // =========================================================

  save(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.canSave()) {
      this.errorMessage = this.isEditMode
        ? 'Permission refusée : MENU_REDUCTIONS_MODIFIER'
        : 'Permission refusée : MENU_REDUCTIONS_AJOUTER';

      return;
    }

    if (!this.validate()) {
      return;
    }

    this.saving = true;

    const payload: Reduction = {
      ...this.reduction,
      // "Tous les produits" : la sélection de produits est ignorée par
      // le backend, on l'envoie vide par cohérence.
      produits_ids:
        this.reduction.application_produits === ApplicationReduction.PRODUITS_SPECIFIQUES
          ? this.reduction.produits_ids
          : [],
      // Pas de case cochée => n'importe quel montant.
      montant_minimum: this.aUnMontantMinimum ? this.reduction.montant_minimum : null,
      // Pas de case cochée => illimité.
      nombre_applications_autorise: this.aUneLimiteApplications
        ? this.reduction.nombre_applications_autorise
        : null,
    };

    const request$ = this.isEditMode
      ? this.reductionService.update(this.reductionId as number, payload)
      : this.reductionService.create(payload);

    request$.subscribe({
      next: () => {
        this.saving = false;

        this.successMessage = this.isEditMode
          ? 'Réduction modifiée avec succès.'
          : 'Réduction créée avec succès.';

        this.cdr.detectChanges();

        setTimeout(() => {
          this.router.navigate(['/back-office/menu/reductions']);
        }, 600);
      },

      error: (err) => {
        console.error('Erreur lors de l’enregistrement de la réduction :', err);

        this.saving = false;

        this.errorMessage = err?.error?.message || 'Impossible d’enregistrer la réduction.';

        this.cdr.detectChanges();
      },
    });
  }

  cancel(): void {
    this.router.navigate(['/back-office/menu/reductions']);
  }

  // =========================================================
  // OUTILS
  // =========================================================

  get suffixeValeur(): string {
    return this.reduction.type === TypeReduction.POURCENTAGE ? '%' : 'DT';
  }

  private toDateInput(value: string | Date | null | undefined): string {
    if (!value) {
      return '';
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return '';
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
