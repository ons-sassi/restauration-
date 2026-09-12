import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VenteService } from '../../../../core/services/vente.service';
import { AuthService } from '../../../../core/services/auth.service';
import { PointDeVenteService } from '../../../../core/services/point-de-vente.service';
import { PointDeVente } from '../../../../core/models/point-de-vente.model';
import { VenteParModePaiement } from '../../../../core/models/vente-par-mode-paiement.model';

@Component({
  selector: 'app-par-mode-paiement',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './par-mode-paiement.component.html',
  styleUrls: ['./par-mode-paiement.component.css'],
})
export class ParModePaiementComponent implements OnInit {
  // ============================================================
  // DONNÉES
  // ============================================================

  ventes: VenteParModePaiement[] = [];

  loading = false;
  errorMessage = '';

  // ============================================================
  // FILTRES
  // ============================================================

  dateDebut: string = '';
  dateFin: string = '';

  pointDeVentes: PointDeVente[] = [];

  pointDeVenteId: number | null = null;

  // ============================================================
  // TOTAUX
  // ============================================================

  totalChiffreAffaires = 0;
  totalNombreVentes = 0;

  constructor(
    private venteService: VenteService,
    private readonly cdr: ChangeDetectorRef,
    public readonly authService: AuthService,
    private readonly pointDeVenteService: PointDeVenteService,
  ) {}

  // ============================================================
  // PERMISSIONS
  // ============================================================

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  /**
   * Permission pour consulter les ventes par mode de paiement.
   */
  canViewVentesParModePaiement(): boolean {
    return this.hasPermission('VENTES_PAR_MODE_PAIEMENT');
  }

  // ============================================================
  // INIT
  // ============================================================

  ngOnInit(): void {
    this.initialiserDates();
    this.chargerPointsDeVente();

    this.chargerVentes();
  }

  // ============================================================
  // DATES PAR DEFAUT
  // ============================================================

  private initialiserDates(): void {
    const aujourdHui = new Date();

    const premierJour = new Date(aujourdHui.getFullYear(), aujourdHui.getMonth(), 1);

    this.dateDebut = this.formaterDate(premierJour);

    this.dateFin = this.formaterDate(aujourdHui);
  }

  // ============================================================
  // FORMAT DATE
  // ============================================================

  private formaterDate(date: Date): string {
    const annee = date.getFullYear();

    const mois = String(date.getMonth() + 1).padStart(2, '0');

    const jour = String(date.getDate()).padStart(2, '0');

    return `${annee}-${mois}-${jour}`;
  }

  // ============================================================
  // POINTS DE VENTE
  // ============================================================

  chargerPointsDeVente(): void {
    const restaurantId = this.authService.getSelectedRestaurantId();

    const request = restaurantId != null
      ? this.pointDeVenteService.getByRestaurant(restaurantId)
      : this.pointDeVenteService.getAll();

    request.subscribe({
      next: (pdvs) => {
        this.pointDeVentes = Array.isArray(pdvs) ? pdvs : [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur lors du chargement des points de vente', error);
        this.pointDeVentes = [];
        this.cdr.detectChanges();
      },
    });
  }

  // ============================================================
  // CHARGER VENTES
  // ============================================================

  chargerVentes(): void {
    if (!this.canViewVentesParModePaiement()) {
      console.warn('Permission refusée : VENTES_PAR_MODE_PAIEMENT');
      this.loading = false;
      return;
    }

    this.loading = true;

    this.errorMessage = '';

    this.venteService
      .getVentesParModePaiement(this.pointDeVenteId, this.dateDebut, this.dateFin)
      .subscribe({
        next: (ventes) => {
          this.ventes = ventes;

          this.calculerTotaux();

          this.loading = false;

          this.cdr.detectChanges();
        },
        error: (error) => {
          this.errorMessage = this.getErrorMessage(error);

          this.loading = false;

          this.cdr.detectChanges();
        },
      });
  }

  // ============================================================
  // MESSAGE D'ERREUR
  // ============================================================

  private getErrorMessage(error: any): string {
    if (error?.status === 401 || error?.status === 403) {
      return 'Votre session a expiré. Veuillez vous reconnecter.';
    }

    return 'Une erreur est survenue lors du chargement des ventes.';
  }

  private formatDate(date: Date): string {
    const annee = date.getFullYear();

    const mois = String(date.getMonth() + 1).padStart(2, '0');

    const jour = String(date.getDate()).padStart(2, '0');

    return `${annee}-${mois}-${jour}`;
  }

  // ============================================================
  // CALCUL DES TOTAUX
  // ============================================================

  calculerTotaux(): void {
    this.totalChiffreAffaires = this.arrondir(
      this.ventes.reduce((total, vente) => total + (vente.chiffreAffaires ?? 0), 0),
    );

    this.totalNombreVentes = this.ventes.reduce(
      (total, vente) => total + (vente.nombreVentes ?? 0),
      0,
    );
  }

  // ============================================================
  // RECHERCHE
  // ============================================================

  rechercher(): void {
    if (!this.canViewVentesParModePaiement()) {
      console.warn('Permission refusée : VENTES_PAR_MODE_PAIEMENT');
      return;
    }

    this.chargerVentes();
  }

  // ============================================================
  // REINITIALISER
  // ============================================================

  reinitialiser(): void {
    if (!this.canViewVentesParModePaiement()) {
      console.warn('Permission refusée : VENTES_PAR_MODE_PAIEMENT');
      return;
    }

    this.pointDeVenteId = null;

    this.initialiserDates();
    this.chargerPointsDeVente();

    this.chargerVentes();
  }

  // ============================================================
  // ARRONDI
  // ============================================================

  private arrondir(valeur: number): number {
    return Math.round(valeur * 100) / 100;
  }
}
