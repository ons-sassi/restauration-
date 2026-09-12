import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { VenteService } from '../../../../core/services/vente.service';
import { AuthService } from '../../../../core/services/auth.service';
import { VenteParEmployee } from '../../../../core/models/vente-par-employee.model';

@Component({
  selector: 'app-vente-par-employe',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vente-par-employe.component.html',
  styleUrl: './vente-par-employe.component.css',
})
export class VenteParEmployeComponent implements OnInit {
  // ==========================================================
  // DONNÉES
  // ==========================================================

  ventes: VenteParEmployee[] = [];

  loading = false;

  errorMessage = '';

  // ==========================================================
  // FILTRES
  // ==========================================================

  dateDebut = '';

  dateFin = '';


  // ==========================================================
  // TOTAUX
  // ==========================================================

  totalNombreVentes = 0;

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
   * Permission pour consulter les ventes par employé.
   */
  canViewVentesParEmploye(): boolean {
    return this.hasPermission('VENTES_PAR_EMPLOYE');
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
    if (!this.canViewVentesParEmploye()) {
      console.warn('Permission refusée : VENTES_PAR_EMPLOYE');
      this.loading = false;
      return;
    }

    this.errorMessage = '';

    this.loading = true;

    this.venteService
      .getVentesParEmployee(null, this.dateDebut, this.dateFin)
      .subscribe({
        next: (data) => {
          this.ventes = data ?? [];

          this.calculerTotaux();

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {
          console.error('Erreur lors du chargement des ventes par employé', error);

          this.ventes = [];

          this.calculerTotaux();

          this.errorMessage =
            error?.error?.message ?? 'Impossible de charger les ventes par employé.';

          this.loading = false;

          this.cdr.detectChanges();
        },
      });
  }

  // ==========================================================
  // RECHERCHER
  // ==========================================================

  rechercher(): void {
    if (!this.canViewVentesParEmploye()) {
      console.warn('Permission refusée : VENTES_PAR_EMPLOYE');
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
    if (!this.canViewVentesParEmploye()) {
      console.warn('Permission refusée : VENTES_PAR_EMPLOYE');
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
  // TOTAUX
  // ==========================================================

  private calculerTotaux(): void {
    this.totalNombreVentes = this.ventes.reduce(
      (total, vente) => total + (vente.nombreVentes ?? 0),
      0,
    );

    this.totalChiffreAffaires = this.ventes.reduce(
      (total, vente) => total + (vente.chiffreAffaires ?? 0),
      0,
    );
  }

  // ==========================================================
  // MEILLEUR EMPLOYE (initiales pour l'avatar)
  // ==========================================================

  initiales(nomComplet: string): string {
    if (!nomComplet) {
      return '?';
    }

    return nomComplet
      .split(' ')
      .filter((partie) => partie.length > 0)
      .slice(0, 2)
      .map((partie) => partie.charAt(0).toUpperCase())
      .join('');
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
