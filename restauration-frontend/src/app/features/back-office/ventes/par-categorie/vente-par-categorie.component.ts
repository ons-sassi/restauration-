import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { VenteService } from '../../../../core/services/vente.service';
import { AuthService } from '../../../../core/services/auth.service';

export interface VenteParCategorie {
  categorieId: number | null;
  categorie: string;
  quantiteVendue: number;
  chiffreAffaires: number;
  cout: number;
  margeBrute: number;
}

@Component({
  selector: 'app-vente-par-categorie',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vente-par-categorie.component.html',
  styleUrl: './vente-par-categorie.component.css',
})
export class VenteParCategorieComponent implements OnInit {
  // ==========================================================
  // DONNÉES
  // ==========================================================

  ventes: VenteParCategorie[] = [];

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

  totalCout = 0;

  totalMargeBrute = 0;

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
   * Permission pour consulter les ventes par catégorie.
   */
  canViewVentesParCategorie(): boolean {
    return this.hasPermission('VENTES_PAR_CATEGORIE');
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
    if (!this.canViewVentesParCategorie()) {
      console.warn('Permission refusée : VENTES_PAR_CATEGORIE');
      this.loading = false;
      return;
    }

    this.errorMessage = '';

    this.loading = true;

    this.venteService
      .getVentesParCategorie(null, this.dateDebut, this.dateFin)
      .subscribe({
        next: (data) => {
          this.ventes = data ?? [];

          this.calculerTotaux();

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {
          console.error('Erreur lors du chargement des ventes par catégorie', error);

          this.ventes = [];

          this.calculerTotaux();

          this.errorMessage =
            error?.error?.message ?? 'Impossible de charger les ventes par catégorie.';

          this.loading = false;

          this.cdr.detectChanges();
        },
      });
  }

  // ==========================================================
  // RECHERCHER
  // ==========================================================

  rechercher(): void {
    if (!this.canViewVentesParCategorie()) {
      console.warn('Permission refusée : VENTES_PAR_CATEGORIE');
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
    if (!this.canViewVentesParCategorie()) {
      console.warn('Permission refusée : VENTES_PAR_CATEGORIE');
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

    this.totalCout = this.ventes.reduce((total, vente) => total + (vente.cout ?? 0), 0);

    this.totalMargeBrute = this.ventes.reduce((total, vente) => total + (vente.margeBrute ?? 0), 0);
  }

  // ==========================================================
  // POURCENTAGE MARGE
  // ==========================================================

  get tauxMarge(): number {
    if (this.totalChiffreAffaires === 0) {
      return 0;
    }

    return (this.totalMargeBrute / this.totalChiffreAffaires) * 100;
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
