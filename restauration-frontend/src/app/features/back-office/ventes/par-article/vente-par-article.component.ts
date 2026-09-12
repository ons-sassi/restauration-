import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { VenteService } from '../../../../core/services/vente.service';
import { AuthService } from '../../../../core/services/auth.service';
import { PointDeVenteService } from '../../../../core/services/point-de-vente.service';
import { PointDeVente } from '../../../../core/models/point-de-vente.model';
import { VenteParArticle } from '../../../../core/models/vente-par-article.model';

@Component({
  selector: 'app-vente-par-article',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vente-par-article.component.html',
  styleUrl: './vente-par-article.component.css',
})
export class VenteParArticleComponent implements OnInit {
  ventes: VenteParArticle[] = [];

  loading = false;
  error = '';

  dateDebut = '';
  dateFin = '';

  pointDeVenteId: number | null = null;
  pointDeVentes: PointDeVente[] = [];

  constructor(
    private readonly venteService: VenteService,
    private readonly cdr: ChangeDetectorRef,
    public readonly authService: AuthService,
    private readonly pointDeVenteService: PointDeVenteService,
  ) {}

  ngOnInit(): void {
    this.initialiserDates();
    this.chargerPointsDeVente();
    this.chargerVentes();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  /**
   * Permission pour consulter les ventes par article.
   */
  canViewVentesParArticle(): boolean {
    return this.hasPermission('VENTES_PAR_ARTICLE');
  }

  /**
   * Charge les PDV du restaurant sélectionné et ne conserve que
   * les points de vente actuellement connectés.
   */
  chargerPointsDeVente(): void {
    const restaurantId = this.authService.getSelectedRestaurantId();

    const request = restaurantId
      ? this.pointDeVenteService.getByRestaurant(restaurantId)
      : this.pointDeVenteService.getAll();

    request.subscribe({
      next: (pdvs) => {
        this.pointDeVentes = (pdvs ?? []).filter(
          (pdv) => (pdv.statutConnexion ?? '').toUpperCase() === 'CONNECTE',
        );

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur lors du chargement des PDV connectés :', error);
        this.pointDeVentes = [];
        this.cdr.detectChanges();
      },
    });
  }

  /**
   * Charge les ventes regroupées par article.
   */
  chargerVentes(): void {
    if (!this.canViewVentesParArticle()) {
      console.warn('Permission refusée : VENTES_PAR_ARTICLE');
      this.loading = false;
      return;
    }

    this.loading = true;
    this.error = '';

    this.venteService
      .getVentesParArticle(this.pointDeVenteId, this.dateDebut, this.dateFin)
      .subscribe({
        next: (data: VenteParArticle[]) => {
          this.ventes = data;
          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (err) => {
          console.error('Erreur lors du chargement des ventes par article :', err);

          this.error = 'Impossible de charger les ventes par article.';

          this.loading = false;

          this.cdr.detectChanges();
        },
      });
  }

  /**
   * Applique les filtres sélectionnés.
   */
  appliquerFiltres(): void {
    if (!this.canViewVentesParArticle()) {
      console.warn('Permission refusée : VENTES_PAR_ARTICLE');
      return;
    }

    if (!this.dateDebut || !this.dateFin) {
      this.error = 'Veuillez sélectionner une date de début et une date de fin.';
      return;
    }

    if (this.dateDebut > this.dateFin) {
      this.error = 'La date de début doit être antérieure ou égale à la date de fin.';
      return;
    }

    this.chargerVentes();
  }

  /**
   * Quantité totale vendue.
   */
  get totalQuantite(): number {
    return this.ventes.reduce((total, vente) => total + vente.quantiteVendue, 0);
  }

  /**
   * Chiffre d'affaires total.
   */
  get totalChiffreAffaires(): number {
    return this.ventes.reduce((total, vente) => total + vente.chiffreAffaires, 0);
  }

  /**
   * Coût total.
   */
  get totalCout(): number {
    return this.ventes.reduce((total, vente) => total + vente.cout, 0);
  }

  /**
   * Marge brute totale.
   */
  get totalMarge(): number {
    return this.ventes.reduce((total, vente) => total + vente.margeBrute, 0);
  }

  /**
   * Initialise la période sur les deux derniers jours.
   */
  private initialiserDates(): void {
    const aujourdHui = new Date();

    this.dateFin = this.formatDate(aujourdHui);

    const debut = new Date(aujourdHui);

    debut.setDate(debut.getDate() - 30);

    this.dateDebut = this.formatDate(debut);
  }

  /**
   * Convertit une date JS en yyyy-MM-dd.
   */
  private formatDate(date: Date): string {
    const year = date.getFullYear();

    const month = String(date.getMonth() + 1).padStart(2, '0');

    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
