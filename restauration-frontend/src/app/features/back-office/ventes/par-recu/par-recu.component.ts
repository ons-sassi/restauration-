import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VenteService } from '../../../../core/services/vente.service';
import { AuthService } from '../../../../core/services/auth.service';
import { PointDeVenteService } from '../../../../core/services/point-de-vente.service';
import { PointDeVente } from '../../../../core/models/point-de-vente.model';
import { VenteParRecu } from '../../../../core/models/vente-par-recu.model';

@Component({
  selector: 'app-par-recu',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './par-recu.component.html',
  styleUrls: ['./par-recu.component.css'],
})
export class ParRecuComponent implements OnInit {
  // ============================================================
  // DONNÉES
  // ============================================================

  ventes: VenteParRecu[] = [];

  loading = false;
  errorMessage = '';

  // ============================================================
  // FILTRES
  // ============================================================

  dateDebut: string = '';
  dateFin: string = '';

  pointDeVenteId: number | null = null;
  pointDeVentes: PointDeVente[] = [];

  // ============================================================
  // TOTAUX
  // ============================================================

  totalMontantTtc = 0;
  totalNombreRecus = 0;

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
   * Permission pour consulter les ventes par reçu.
   */
  canViewVentesParRecu(): boolean {
    return this.hasPermission('VENTES_PAR_RECU');
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

  // ============================================================
  // CHARGER VENTES
  // ============================================================

  chargerVentes(): void {
    if (!this.canViewVentesParRecu()) {
      console.warn('Permission refusée : VENTES_PAR_RECU');
      this.loading = false;
      return;
    }

    this.loading = true;

    this.errorMessage = '';

    this.venteService
      .getVentesParRecu(this.pointDeVenteId, this.dateDebut, this.dateFin)
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

  // ============================================================
  // CALCUL DES TOTAUX
  // ============================================================

  calculerTotaux(): void {
    this.totalMontantTtc = this.arrondir(
      this.ventes.reduce((total, vente) => total + (vente.montantTtc ?? 0), 0),
    );

    this.totalNombreRecus = this.ventes.length;
  }

  // ============================================================
  // RECHERCHE
  // ============================================================

  rechercher(): void {
    if (!this.canViewVentesParRecu()) {
      console.warn('Permission refusée : VENTES_PAR_RECU');
      return;
    }

    this.chargerVentes();
  }

  // ============================================================
  // REINITIALISER
  // ============================================================

  reinitialiser(): void {
    if (!this.canViewVentesParRecu()) {
      console.warn('Permission refusée : VENTES_PAR_RECU');
      return;
    }

    this.pointDeVenteId = null;

    this.initialiserDates();

    this.chargerVentes();
  }

  // ============================================================
  // ARRONDI
  // ============================================================

  private arrondir(valeur: number): number {
    return Math.round(valeur * 100) / 100;
  }
}
