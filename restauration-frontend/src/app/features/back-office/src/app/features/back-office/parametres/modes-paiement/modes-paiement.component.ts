import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ModePaiement } from '../../../../core/models/mode-paiement.model';
import { ModePaiementService } from '../../../../core/services/mode-paiement.service';
import { AuthService } from '../../../../core/services/auth.service';

interface ModePaiementForm {
  id_mode_paiement: number | null;
  libelle: string;
  actif: boolean;
}

const FORMULAIRE_VIDE: ModePaiementForm = {
  id_mode_paiement: null,
  libelle: '',
  actif: true,
};

@Component({
  selector: 'app-modes-paiement',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modes-paiement.component.html',
  styleUrl: './modes-paiement.component.css',
})
export class ModesPaiementComponent implements OnInit {
  // =========================================================
  // DONNÉES
  // =========================================================

  modesPaiement: ModePaiement[] = [];

  loading = false;
  error = '';

  // =========================================================
  // FILTRES
  // =========================================================

  searchTerm = '';
  statutFiltre = '';

  // =========================================================
  // MODAL / FORMULAIRE
  // =========================================================

  modalOuvert = false;
  isEditMode = false;

  formulaire: ModePaiementForm = { ...FORMULAIRE_VIDE };

  saving = false;
  formError = '';

  constructor(
    private readonly modePaiementService: ModePaiementService,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    if (!this.canVoir()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadModesPaiement();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canVoir(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('PARAMETRES_PAIEMENT');
  }

  canGerer(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('PARAMETRES_PAIEMENT');
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadModesPaiement(): void {
    this.loading = true;
    this.error = '';

    this.modePaiementService.getAll().subscribe({
      next: (data) => {
        this.modesPaiement = Array.isArray(data) ? data : [];

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des méthodes de paiement :', err);

        this.modesPaiement = [];
        this.loading = false;

        this.error = 'Impossible de charger les méthodes de paiement.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FILTRAGE
  // =========================================================

  get modesPaiementFiltres(): ModePaiement[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.modesPaiement.filter((mode) => {
      const libelle = mode.libelle?.toLowerCase() ?? '';

      const matchSearch = !search || libelle.includes(search);

      const matchStatut =
        !this.statutFiltre ||
        (this.statutFiltre === 'ACTIF' ? mode.actif : !mode.actif);

      return matchSearch && matchStatut;
    });
  }

  // =========================================================
  // INDICATEURS GLOBAUX
  // =========================================================

  get totalModes(): number {
    return this.modesPaiement.length;
  }

  get totalModesActifs(): number {
    return this.modesPaiement.filter((m) => m.actif).length;
  }

  get totalModesInactifs(): number {
    return this.totalModes - this.totalModesActifs;
  }

  // =========================================================
  // MODAL - OUVERTURE
  // =========================================================

  nouveauMode(): void {
    if (!this.canGerer()) {
      console.warn('Permission refusée : PARAMETRES_PAIEMENT');
      return;
    }

    this.isEditMode = false;
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.formError = '';
    this.modalOuvert = true;
    this.cdr.detectChanges();
  }

  modifierMode(mode: ModePaiement): void {
    if (!this.canGerer()) {
      console.warn('Permission refusée : PARAMETRES_PAIEMENT');
      return;
    }

    this.isEditMode = true;
    this.formError = '';

    this.formulaire = {
      id_mode_paiement: mode.id_mode_paiement,
      libelle: mode.libelle,
      actif: mode.actif,
    };

    this.modalOuvert = true;
    this.cdr.detectChanges();
  }

  fermerModal(): void {
    if (this.saving) {
      return;
    }

    this.modalOuvert = false;
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.formError = '';
  }

  // =========================================================
  // ENREGISTREMENT
  // =========================================================

  enregistrer(): void {
    if (!this.canGerer()) {
      this.formError = 'Permission refusée : PARAMETRES_PAIEMENT';
      return;
    }

    this.formError = '';

    const libelle = this.formulaire.libelle.trim();

    if (!libelle) {
      this.formError = 'Veuillez saisir le nom de la méthode de paiement.';
      return;
    }

    const dejaExiste = this.modesPaiement.some(
      (m) =>
        m.libelle.trim().toLowerCase() === libelle.toLowerCase() &&
        m.id_mode_paiement !== this.formulaire.id_mode_paiement,
    );

    if (dejaExiste) {
      this.formError = 'Une méthode de paiement portant ce nom existe déjà.';
      return;
    }

    const payload: Partial<ModePaiement> = {
      libelle,
      actif: this.formulaire.actif,
    };

    this.saving = true;

    const request$ =
      this.isEditMode && this.formulaire.id_mode_paiement !== null
        ? this.modePaiementService.update(
            this.formulaire.id_mode_paiement,
            payload as ModePaiement,
          )
        : this.modePaiementService.create(payload as ModePaiement);

    request$.subscribe({
      next: () => {
        this.saving = false;

        this.modalOuvert = false;
        this.formulaire = { ...FORMULAIRE_VIDE };

        this.loadModesPaiement();
      },

      error: (err) => {
        console.error("Erreur lors de l'enregistrement de la méthode de paiement :", err);

        this.saving = false;

        this.formError =
          err?.error?.message ||
          "Impossible d'enregistrer la méthode de paiement. Veuillez réessayer.";

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerMode(mode: ModePaiement): void {
    if (!this.canGerer()) {
      console.warn('Permission refusée : PARAMETRES_PAIEMENT');
      return;
    }

    const confirmation = window.confirm(
      `Voulez-vous vraiment retirer la méthode de paiement "${mode.libelle}" ?`,
    );

    if (!confirmation) {
      return;
    }

    this.modePaiementService.delete(mode.id_mode_paiement).subscribe({
      next: () => {
        this.modesPaiement = this.modesPaiement.filter(
          (m) => m.id_mode_paiement !== mode.id_mode_paiement,
        );

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors de la suppression de la méthode de paiement :', err);

        window.alert(
          'Impossible de retirer cette méthode de paiement. Elle est peut-être encore utilisée par des ventes.',
        );
      },
    });
  }

  // =========================================================
  // BASCULE STATUT RAPIDE
  // =========================================================

  basculerStatut(mode: ModePaiement): void {
    if (!this.canGerer()) {
      console.warn('Permission refusée : PARAMETRES_PAIEMENT');
      return;
    }

    const request$ = mode.actif
      ? this.modePaiementService.desactiver(mode.id_mode_paiement)
      : this.modePaiementService.activer(mode.id_mode_paiement);

    request$.subscribe({
      next: (updated) => {
        mode.actif = updated.actif;
        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du changement de statut de la méthode de paiement :', err);

        window.alert('Impossible de modifier le statut de cette méthode de paiement.');
      },
    });
  }

  // =========================================================
  // RESET / TRACK
  // =========================================================

  resetFiltres(): void {
    this.searchTerm = '';
    this.statutFiltre = '';
  }

  trackByMode(index: number, mode: ModePaiement): number {
    return mode.id_mode_paiement;
  }
}
