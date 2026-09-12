import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { VenteService } from '../../../../core/services/vente.service';
import { AuthService } from '../../../../core/services/auth.service';

export interface VenteParModificateur {
  modificateurId: number | null;
  modificateur: string;
  produit: string;
  quantiteVendue: number;
  chiffreAffaires: number;
}

@Component({
  selector: 'app-vente-par-modificateur',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vente-par-modificateur.component.html',
  styleUrl: './vente-par-modificateur.component.css',
})
export class VenteParModificateurComponent implements OnInit {
  // ==========================================================
  // DONNÉES
  // ==========================================================

  ventes: VenteParModificateur[] = [];

  loading = false;

  errorMessage = '';

  // ==========================================================
  // FILTRES
  // ==========================================================

  dateDebut = '';

  dateFin = '';


  // ==========================================================
  // TOTALS
  // ==========================================================

  totalQuantite = 0;

  totalChiffreAffaires = 0;

  constructor(
    private readonly venteService: VenteService,
    private readonly cdr: ChangeDetectorRef,
    public readonly authService: AuthService,
  ) {}

  // ==========================================================
  // PERMISSIONS
  // ==========================================================

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  /**
   * Permission pour consulter les ventes par modificateur.
   */
  canViewVentesParModificateur(): boolean {
    return this.hasPermission('VENTES_PAR_MODIFICATEUR');
  }

  // ==========================================================
  // INIT
  // ==========================================================

  ngOnInit(): void {
    const aujourdHui = new Date();

    this.dateFin = this.formatDate(aujourdHui);

    const debut = new Date(aujourdHui);
    debut.setDate(debut.getDate() - 30);

    this.dateDebut = this.formatDate(debut);

    this.chargerVentes();
  }

  // ==========================================================
  // CHARGEMENT
  // ==========================================================

  chargerVentes(): void {
    if (!this.canViewVentesParModificateur()) {
      console.warn('Permission refusée : VENTES_PAR_MODIFICATEUR');
      this.loading = false;
      return;
    }

    this.errorMessage = '';

    this.loading = true;

    this.venteService
      .getVentesParModificateur(null, this.dateDebut, this.dateFin)
      .subscribe({
        next: (data) => {
          this.ventes = data ?? [];

          this.calculerTotaux();

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {
          console.error('Erreur lors du chargement des ventes par modificateur', error);

          this.ventes = [];

          this.calculerTotaux();

          this.errorMessage =
            error?.error?.message ?? 'Impossible de charger les ventes par modificateur.';

          this.loading = false;

          this.cdr.detectChanges();
        },
      });
  }

  // ==========================================================
  // RECHERCHER
  // ==========================================================

  rechercher(): void {
    if (!this.canViewVentesParModificateur()) {
      console.warn('Permission refusée : VENTES_PAR_MODIFICATEUR');
      return;
    }

    if (!this.dateDebut || !this.dateFin) {
      this.errorMessage = 'Veuillez sélectionner une date de début et une date de fin.';

      return;
    }

    if (this.dateDebut > this.dateFin) {
      this.errorMessage = 'La date de début doit être antérieure ou égale à la date de fin.';

      return;
    }

    this.chargerVentes();
  }

  // ==========================================================
  // RÉINITIALISER
  // ==========================================================

  reinitialiser(): void {
    if (!this.canViewVentesParModificateur()) {
      console.warn('Permission refusée : VENTES_PAR_MODIFICATEUR');
      return;
    }

    const aujourdHui = new Date();

    this.dateFin = this.formatDate(aujourdHui);

    const debut = new Date(aujourdHui);
    debut.setDate(debut.getDate() - 30);

    this.dateDebut = this.formatDate(debut);


    this.chargerVentes();
  }

  // ==========================================================
  // TOTALS
  // ==========================================================

  private calculerTotaux(): void {
    this.totalQuantite = this.ventes.reduce(
      (total, vente) => total + (vente.quantiteVendue ?? 0),
      0,
    );

    this.totalChiffreAffaires = this.ventes.reduce(
      (total, vente) => total + (vente.chiffreAffaires ?? 0),
      0,
    );
  }

  // ==========================================================
  // FORMAT DATE
  // ==========================================================

  private formatDate(date: Date): string {
    const year = date.getFullYear();

    const month = String(date.getMonth() + 1).padStart(2, '0');

    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
