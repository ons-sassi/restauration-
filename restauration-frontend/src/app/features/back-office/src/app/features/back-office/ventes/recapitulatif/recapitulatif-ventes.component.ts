import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../../../core/services/auth.service';
import { PointDeVenteService } from '../../../../core/services/point-de-vente.service';
import { VenteService } from '../../../../core/services/vente.service';
import { PointDeVente } from '../../../../core/models/point-de-vente.model';
import { Vente } from '../../../../core/models/vente.model';
import {
  PeriodeRapport,
  VenteGraph,
  VenteRecapitulatif,
} from '../../../../core/models/vente-recapitulatif.model';

@Component({
  selector: 'app-recapitulatif-ventes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './recapitulatif-ventes.component.html',
  styleUrl: './recapitulatif-ventes.component.css',
})
export class RecapitulatifVentesComponent implements OnInit {
  readonly periodes: { value: PeriodeRapport; label: string }[] = [
    { value: PeriodeRapport.JOUR, label: 'Jour' },
    { value: PeriodeRapport.SEMAINE, label: 'Semaine' },
    { value: PeriodeRapport.MOIS, label: 'Mois' },
    { value: PeriodeRapport.TRIMESTRE, label: 'Trimestre' },
    { value: PeriodeRapport.ANNEE, label: 'Année' },
  ];

  pointDeVentes: PointDeVente[] = [];
  ventes: Vente[] = [];
  recapitulatif: VenteRecapitulatif = this.emptyRecapitulatif();

  pointDeVenteId: number | null = null;
  dateDebut = '';
  dateFin = '';
  periode: PeriodeRapport = PeriodeRapport.JOUR;
  recherche = '';

  loading = true;
  errorMessage = '';

  constructor(
    private readonly venteService: VenteService,
    private readonly pointDeVenteService: PointDeVenteService,
    public readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.initialiserDates();
    this.chargerDonneesInitiales();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  /**
   * Vérifie si l'utilisateur possède une permission.
   *
   * L'Admin possède automatiquement toutes les permissions
   * grâce à AuthService.
   */
  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  /**
   * Permission pour consulter le récapitulatif des ventes.
   */
  canViewRecapitulatif(): boolean {
    return this.hasPermission('VENTES_RECAPITULATIF');
  }

  chargerDonneesInitiales(): void {
    if (!this.canViewRecapitulatif()) {
      console.warn('Permission refusée : VENTES_RECAPITULATIF');
      this.loading = false;
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const restaurantId = this.authService.getSelectedRestaurantId();

    const pdvRequest = restaurantId
      ? this.pointDeVenteService.getByRestaurant(restaurantId)
      : this.pointDeVenteService.getAll();

    forkJoin({
      pdvs: pdvRequest,
      recap: this.venteService.getRecapitulatif(
        this.pointDeVenteId,
        this.dateDebut,
        this.dateFin,
        this.periode,
      ),
      ventes: this.venteService.getAll(),
    }).subscribe({
      next: ({ pdvs, recap, ventes }) => {
        this.pointDeVentes = Array.isArray(pdvs) ? pdvs : [];
        this.recapitulatif = recap ?? this.emptyRecapitulatif();
        this.ventes = this.filtrerVentes(ventes ?? []);
        this.loading = false;

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement récapitulatif ventes', error);
        this.loading = false;
        this.errorMessage = this.messageErreur(error);

        this.cdr.detectChanges();
      },
    });
  }

  appliquerFiltres(): void {
    if (!this.canViewRecapitulatif()) {
      console.warn('Permission refusée : VENTES_RECAPITULATIF');
      return;
    }

    if (!this.dateDebut || !this.dateFin) {
      this.errorMessage = 'Veuillez renseigner les deux dates.';
      return;
    }

    if (this.dateDebut > this.dateFin) {
      this.errorMessage = 'La date de début doit être antérieure à la date de fin.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      recap: this.venteService.getRecapitulatif(
        this.pointDeVenteId,
        this.dateDebut,
        this.dateFin,
        this.periode,
      ),
      ventes: this.venteService.getAll(),
    }).subscribe({
      next: ({ recap, ventes }) => {
        this.recapitulatif = recap ?? this.emptyRecapitulatif();
        this.ventes = this.filtrerVentes(ventes ?? []);
        this.loading = false;

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur application filtres ventes', error);
        this.loading = false;
        this.errorMessage = this.messageErreur(error);

        this.cdr.detectChanges();
      },
    });
  }

  reinitialiserFiltres(): void {
    if (!this.canViewRecapitulatif()) {
      console.warn('Permission refusée : VENTES_RECAPITULATIF');
      return;
    }

    this.initialiserDates();
    this.pointDeVenteId = null;
    this.periode = PeriodeRapport.JOUR;
    this.recherche = '';
    this.appliquerFiltres();
  }

  get ventesFiltrees(): Vente[] {
    const terme = this.recherche.trim().toLowerCase();

    if (!terme) {
      return this.ventes;
    }

    return this.ventes.filter((vente) => {
      const valeurs = [
        vente.id_vente,
        vente.recu?.numeroRecu,
        vente.employee?.nom,
        vente.employee?.prenom,
        vente.modePaiement?.libelle,
        vente.pointDeVente?.nomPdv,
        vente.commande?.id_commande,
      ];

      return valeurs.some((valeur) =>
        String(valeur ?? '')
          .toLowerCase()
          .includes(terme),
      );
    });
  }

  get graph(): VenteGraph[] {
    return this.recapitulatif.evolution ?? [];
  }

  get graphMax(): number {
    const max = Math.max(...this.graph.map((item) => item.montant), 0);
    return max > 0 ? max : 1;
  }

  get graphPoints(): string {
    if (this.graph.length === 0) {
      return '';
    }

    const width = 720;
    const height = 250;
    const paddingX = 20;
    const paddingY = 20;
    const usableWidth = width - paddingX * 2;
    const usableHeight = height - paddingY * 2;
    const divisor = Math.max(this.graph.length - 1, 1);

    return this.graph
      .map((item, index) => {
        const x = paddingX + (index / divisor) * usableWidth;
        const y = height - paddingY - (item.montant / this.graphMax) * usableHeight;

        return `${x.toFixed(1)},${y.toFixed(1)}`;
      })
      .join(' ');
  }

  graphX(index: number): number {
    const width = 720;
    const paddingX = 20;
    const usableWidth = width - paddingX * 2;
    const divisor = Math.max(this.graph.length - 1, 1);

    return paddingX + (index / divisor) * usableWidth;
  }

  graphY(montant: number): number {
    const height = 250;
    const paddingY = 20;
    const usableHeight = height - paddingY * 2;

    return height - paddingY - (montant / this.graphMax) * usableHeight;
  }

  formaterMontant(value: number | null | undefined): string {
    return new Intl.NumberFormat('fr-FR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value ?? 0);
  }

  formaterDate(value: string | null | undefined): string {
    if (!value) {
      return '—';
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return '—';
    }

    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
  }

  getNomEmploye(vente: Vente): string {
    const employee = vente.employee;
    if (!employee) return '—';

    const nom = [employee.prenom, employee.nom].filter(Boolean).join(' ').trim();
    return nom || employee.email || employee.matricule || '—';
  }

  getNumeroRecu(vente: Vente): string {
    return vente.recu?.numeroRecu || `#${vente.id_vente}`;
  }

  getModePaiement(vente: Vente): string {
    return vente.modePaiement?.libelle || '—';
  }

  getPdv(vente: Vente): string {
    return vente.pointDeVente?.nomPdv || '—';
  }

  trackByVente(_index: number, vente: Vente): number {
    return vente.id_vente;
  }

  private filtrerVentes(ventes: Vente[]): Vente[] {
    const debut = this.dateDebut ? this.startOfDay(this.dateDebut).getTime() : null;
    const fin = this.dateFin ? this.endOfDay(this.dateFin).getTime() : null;

    return ventes.filter((vente) => {
      const date = new Date(vente.date_vente).getTime();
      if (Number.isNaN(date)) return false;

      const dansDates = (debut === null || date >= debut) && (fin === null || date <= fin);

      const bonPdv =
        this.pointDeVenteId === null || vente.pointDeVente?.id_pdv === this.pointDeVenteId;

      return dansDates && bonPdv;
    });
  }

  private initialiserDates(): void {
    const aujourdHui = new Date();
    this.dateFin = this.formatDate(aujourdHui);

    const debut = new Date(aujourdHui);
    debut.setDate(debut.getDate() - 30);
    this.dateDebut = this.formatDate(debut);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private startOfDay(value: string): Date {
    const date = new Date(`${value}T00:00:00`);
    return date;
  }

  private endOfDay(value: string): Date {
    const date = new Date(`${value}T23:59:59.999`);
    return date;
  }

  private emptyRecapitulatif(): VenteRecapitulatif {
    return {
      chiffreAffaires: 0,
      venteBrute: 0,
      reductions: 0,
      venteNette: 0,
      margeBrute: 0,
      evolution: [],
    };
  }

  private messageErreur(error: any): string {
    if (error?.status === 401) return 'Votre session a expiré.';
    if (error?.status === 403) return 'Vous n’avez pas accès à ce rapport.';
    if (error?.status === 400) {
      return error?.error?.message || 'Les paramètres du rapport sont invalides.';
    }
    return 'Impossible de charger le récapitulatif des ventes.';
  }
}
