
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Modificateur } from '../../../../../core/models/modificateur.model';
import { Produit } from '../../../../../core/models/produit.model';

import { ModificateurService } from '../../../../../core/services/modificateur.service';
import { ProduitService } from '../../../../../core/services/produit.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-modificateur-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modificateur-form.component.html',
  styleUrl: './modificateur-form.component.css',
})
export class ModificateurFormComponent implements OnInit {
  isEditMode = false;
  modificateurId: number | null = null;

  loading = false;
  saving = false;

  errorMessage = '';
  successMessage = '';

  produitsDisponibles: Produit[] = [];

  modificateur: Modificateur = {
    id_modificateur: 0,
    nom_modificateur: '',
    prix_supplementaire: 0,
    produits: [],
  };

  constructor(
    private readonly modificateurService: ModificateurService,
    private readonly produitService: ProduitService,
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

    // ---------------------------------------------------------
    // MODE MODIFICATION
    // ---------------------------------------------------------

    if (id) {
      const parsedId = Number(id);

      if (Number.isNaN(parsedId) || parsedId <= 0) {
        this.errorMessage = 'Identifiant du modificateur invalide.';
        return;
      }

      // Il faut avoir le droit de modifier
      if (!this.canEditModificateur()) {
        this.router.navigate(['/back-office']);
        return;
      }

      this.modificateurId = parsedId;
      this.isEditMode = true;

      this.loadModificateur(parsedId);
      return;
    }

    // ---------------------------------------------------------
    // MODE CREATION
    // ---------------------------------------------------------

    if (!this.canAddModificateur()) {
      this.router.navigate(['/back-office']);
      return;
    }

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

  canSave(): boolean {
    return this.isEditMode
      ? this.canEditModificateur()
      : this.canAddModificateur();
  }

  // =========================================================
  // PRODUITS
  // =========================================================

  loadProduits(): void {
    if (!this.canSave()) {
      return;
    }

    this.produitService.getAll().subscribe({
      next: (data) => {
        const tous = Array.isArray(data) ? data : [];

        const restaurantId =
          this.authService.getSelectedRestaurantId();

        this.produitsDisponibles = restaurantId
          ? tous.filter(
              (p) =>
                p.restaurant?.id_restaurant === restaurantId,
            )
          : tous;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur produits :', err);

        this.produitsDisponibles = [];

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // MODIFICATEUR
  // =========================================================

  loadModificateur(id: number): void {
    if (!this.canEditModificateur()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.modificateurService.getById(id).subscribe({
      next: (data) => {
        if (!data) {
          this.errorMessage =
            'Le modificateur demandé est introuvable.';
          this.loading = false;
          return;
        }

        this.modificateur = { ...data };

        this.loading = false;

        this.loadProduits();

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur modificateur :', err);

        this.loading = false;

        this.errorMessage =
          'Impossible de charger le modificateur.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // PRODUITS SELECTIONNES
  // =========================================================

  isProduitSelected(produit: Produit): boolean {
    return (this.modificateur.produits ?? []).some(
      (p) => p.id_element === produit.id_element,
    );
  }

  toggleProduit(
    produit: Produit,
    event: Event,
  ): void {
    // Double protection
    if (!this.canSave()) {
      return;
    }

    const checked =
      (event.target as HTMLInputElement).checked;

    const produitsActuels =
      this.modificateur.produits ?? [];

    if (checked) {
      if (
        !produitsActuels.some(
          (p) =>
            p.id_element === produit.id_element,
        )
      ) {
        this.modificateur.produits = [
          ...produitsActuels,
          produit,
        ];
      }

      return;
    }

    this.modificateur.produits =
      produitsActuels.filter(
        (p) =>
          p.id_element !== produit.id_element,
      );
  }

  get produitsSelectionnesCount(): number {
    return (
      this.modificateur.produits ?? []
    ).length;
  }

  // =========================================================
  // SAVE
  // =========================================================

  save(): void {
    // ---------------------------------------------------------
    // PROTECTION
    // ---------------------------------------------------------

    if (!this.canSave()) {
      this.errorMessage =
        'Vous n’avez pas l’autorisation d’effectuer cette opération.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    // ---------------------------------------------------------
    // VALIDATION NOM
    // ---------------------------------------------------------

    if (
      !this.modificateur.nom_modificateur?.trim()
    ) {
      this.errorMessage =
        'Le nom du modificateur est obligatoire.';
      return;
    }

    // ---------------------------------------------------------
    // VALIDATION PRIX
    // ---------------------------------------------------------

    if (
      this.modificateur.prix_supplementaire === null ||
      this.modificateur.prix_supplementaire === undefined ||
      Number(
        this.modificateur.prix_supplementaire,
      ) < 0
    ) {
      this.errorMessage =
        'Le prix supplémentaire doit être un nombre positif ou nul.';
      return;
    }

    // ---------------------------------------------------------
    // VALIDATION PRODUITS
    // ---------------------------------------------------------

    if (
      !this.modificateur.produits ||
      this.modificateur.produits.length === 0
    ) {
      this.errorMessage =
        'Veuillez sélectionner au moins un produit associé.';
      return;
    }

    // ---------------------------------------------------------
    // NORMALISATION
    // ---------------------------------------------------------

    this.modificateur.nom_modificateur =
      this.modificateur.nom_modificateur.trim();

    // ---------------------------------------------------------
    // PAYLOAD
    // ---------------------------------------------------------

    const payload: any = {
      nom_modificateur:
        this.modificateur.nom_modificateur,

      prix_supplementaire:
        Number(
          this.modificateur.prix_supplementaire,
        ),

      produits: this.modificateur.produits,
    };

    this.saving = true;

    // =========================================================
    // UPDATE
    // =========================================================

    if (
      this.isEditMode &&
      this.modificateurId !== null
    ) {
      // Vérification supplémentaire
      if (!this.canEditModificateur()) {
        this.saving = false;
        this.errorMessage =
          'Vous n’avez pas l’autorisation de modifier ce modificateur.';
        return;
      }

      this.modificateurService
        .update(
          this.modificateurId,
          payload,
        )
        .subscribe({
          next: () => {
            this.saving = false;

            this.successMessage =
              'Modificateur modifié avec succès.';

            this.cdr.detectChanges();

            setTimeout(() => {
              this.router.navigate([
                '/back-office/menu/modificateurs',
              ]);
            }, 600);
          },

          error: (err) => {
            console.error(
              'Erreur modification :',
              err,
            );

            this.saving = false;

            this.errorMessage =
              err?.error?.message ||
              'Impossible de modifier le modificateur.';

            this.cdr.detectChanges();
          },
        });

      return;
    }

    // =========================================================
    // CREATE
    // =========================================================

    if (!this.canAddModificateur()) {
      this.saving = false;
      this.errorMessage =
        'Vous n’avez pas l’autorisation de créer un modificateur.';
      return;
    }

    this.modificateurService
      .create(payload)
      .subscribe({
        next: () => {
          this.saving = false;

          this.successMessage =
            'Modificateur créé avec succès.';

          this.cdr.detectChanges();

          setTimeout(() => {
            this.router.navigate([
              '/back-office/menu/modificateurs',
            ]);
          }, 600);
        },

        error: (err) => {
          console.error(
            'Erreur création :',
            err,
          );

          this.saving = false;

          this.errorMessage =
            err?.error?.message ||
            'Impossible de créer le modificateur.';

          this.cdr.detectChanges();
        },
      });
  }

  // =========================================================
  // ANNULER
  // =========================================================

  cancel(): void {
    this.router.navigate([
      '/back-office/menu/modificateurs',
    ]);
  }
}

