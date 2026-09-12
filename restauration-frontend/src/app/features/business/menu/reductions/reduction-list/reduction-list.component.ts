import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Reduction } from '../../../../../core/models/reduction.model';
import {
  TypeReduction,
  ApplicationReduction,
} from '../../../../../core/models/enums/type-reduction.enum';

import { ReductionService } from '../../../../../core/services/reduction.service';
import { AuthService } from '../../../../../core/services/auth.service';

type StatutReduction = 'active' | 'a-venir' | 'expiree' | 'desactivee';

@Component({
  selector: 'app-reduction-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reduction-list.component.html',
  styleUrl: './reduction-list.component.css',
})
export class ReductionListComponent implements OnInit {
  reductions: Reduction[] = [];

  loading = false;
  error = '';

  searchTerm = '';
  selectedType: 'tous' | TypeReduction = 'tous';
  selectedStatut: 'tous' | StatutReduction = 'tous';

  readonly TypeReduction = TypeReduction;
  readonly ApplicationReduction = ApplicationReduction;

  // Ids des réductions en cours de bascule activer/désactiver
  // (pour désactiver le bouton pendant l'appel réseau).
  togglingIds = new Set<number>();

  constructor(
    private readonly reductionService: ReductionService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly authService: AuthService,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    /*
     * Protection supplémentaire côté composant.
     *
     * La route est déjà protégée par PermissionGuard,
     * mais on vérifie également ici pour éviter qu'un accès
     * direct au composant permette de charger la page.
     */
    if (!this.canViewReductions()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadReductions();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canViewReductions(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_REDUCTIONS_VOIR');
  }

  canAddReduction(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_REDUCTIONS_AJOUTER');
  }

  canEditReduction(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_REDUCTIONS_MODIFIER');
  }

  canDeleteReduction(): boolean {
    return (
      this.authService.isAdmin() || this.authService.hasPermission('MENU_REDUCTIONS_SUPPRIMER')
    );
  }

  // =========================================================
  // REDUCTIONS
  // =========================================================

  loadReductions(): void {
    if (!this.canViewReductions()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loading = true;
    this.error = '';

    this.reductionService.getAll().subscribe({
      next: (data) => {
        this.reductions = Array.isArray(data) ? data : [];

        this.loading = false;

        // Force Angular à revérifier la vue : sans ça, la liste ne
        // s'affiche qu'après un événement DOM (recherche, réinitialiser...).
        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des réductions :', err);

        this.reductions = [];
        this.loading = false;

        this.error = 'Impossible de charger les réductions.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FILTRAGE
  // =========================================================

  get reductionsFiltrees(): Reduction[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.reductions.filter((reduction) => {
      const nom = reduction.nom_reduction?.toLowerCase() ?? '';

      const conditions = reduction.conditions_application?.toLowerCase() ?? '';

      const matchesSearch = !search || nom.includes(search) || conditions.includes(search);

      const matchesType = this.selectedType === 'tous' || reduction.type === this.selectedType;

      const matchesStatut =
        this.selectedStatut === 'tous' || this.statutReduction(reduction) === this.selectedStatut;

      return matchesSearch && matchesType && matchesStatut;
    });
  }

  // =========================================================
  // STATISTIQUES
  // =========================================================

  get totalReductions(): number {
    return this.reductions.length;
  }

  get reductionsActives(): number {
    return this.reductions.filter((r) => this.statutReduction(r) === 'active').length;
  }

  get reductionsPourcentage(): number {
    return this.reductions.filter((r) => r.type === TypeReduction.POURCENTAGE).length;
  }

  get reductionsMontant(): number {
    return this.reductions.filter((r) => r.type === TypeReduction.MONTANT_FIXE).length;
  }

  // =========================================================
  // STATUT / AFFICHAGE
  // =========================================================

  statutReduction(reduction: Reduction): StatutReduction {
    // La désactivation manuelle prime sur les dates : une réduction
    // désactivée reste "désactivée" même si elle est dans sa période
    // de validité.
    if (!this.estActive(reduction)) {
      return 'desactivee';
    }

    const aujourdHui = new Date();

    const debut = reduction.date_debut ? new Date(reduction.date_debut) : null;
    const fin = reduction.date_fin ? new Date(reduction.date_fin) : null;

    if (debut && aujourdHui < debut) {
      return 'a-venir';
    }

    if (fin && aujourdHui > fin) {
      return 'expiree';
    }

    return 'active';
  }

  // Une réduction sans champ "active" renseigné (anciennes données) est
  // considérée comme active, par cohérence avec le backend.
  estActive(reduction: Reduction): boolean {
    return reduction.active !== false;
  }

  libelleStatut(statut: StatutReduction): string {
    switch (statut) {
      case 'active':
        return 'Active';
      case 'a-venir':
        return 'À venir';
      case 'expiree':
        return 'Expirée';
      case 'desactivee':
        return 'Désactivée';
    }
  }

  // Affichage du quota d'applications : "Illimité" ou "x / y utilisées".
  libelleApplications(reduction: Reduction): string {
    if (!reduction.nombre_applications_autorise) {
      return 'Illimité';
    }

    const effectuees = reduction.nombre_applications_effectuees ?? 0;

    return `${effectuees} / ${reduction.nombre_applications_autorise}`;
  }

  libelleType(type: TypeReduction | string): string {
    return type === TypeReduction.POURCENTAGE ? 'Pourcentage' : 'Montant fixe';
  }

  libelleValeur(reduction: Reduction): string {
    return reduction.type === TypeReduction.POURCENTAGE
      ? `${reduction.valeur}%`
      : `${reduction.valeur} DT`;
  }

  // =========================================================
  // NAVIGATION
  // =========================================================

  nouvelleReduction(): void {
    if (!this.canAddReduction()) {
      console.warn('Permission refusée : MENU_REDUCTIONS_AJOUTER');
      return;
    }

    this.router.navigate(['/back-office/menu/reductions/nouvelle']);
  }

  modifierReduction(reduction: Reduction): void {
    if (!this.canEditReduction()) {
      console.warn('Permission refusée : MENU_REDUCTIONS_MODIFIER');
      return;
    }

    this.router.navigate(['/back-office/menu/reductions', reduction.id_reduction, 'modifier']);
  }

  // =========================================================
  // ACTIVER / DESACTIVER
  // =========================================================

  toggleActive(reduction: Reduction): void {
    if (!this.canEditReduction()) {
      console.warn('Permission refusée : MENU_REDUCTIONS_MODIFIER');
      return;
    }

    if (this.togglingIds.has(reduction.id_reduction)) {
      return;
    }

    const activerMaintenant = !this.estActive(reduction);

    this.togglingIds.add(reduction.id_reduction);

    const request$ = activerMaintenant
      ? this.reductionService.activer(reduction.id_reduction)
      : this.reductionService.desactiver(reduction.id_reduction);

    request$.subscribe({
      next: (updated) => {
        const index = this.reductions.findIndex((r) => r.id_reduction === reduction.id_reduction);

        if (index !== -1) {
          this.reductions[index] = { ...this.reductions[index], ...updated };
        }

        this.togglingIds.delete(reduction.id_reduction);

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du changement de statut de la réduction :', err);

        this.togglingIds.delete(reduction.id_reduction);

        const message = err?.error?.message || 'Impossible de modifier le statut de la réduction.';

        window.alert(message);

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerReduction(reduction: Reduction): void {
    if (!this.canDeleteReduction()) {
      console.warn('Permission refusée : MENU_REDUCTIONS_SUPPRIMER');
      return;
    }

    const confirmation = window.confirm(
      `Voulez-vous vraiment supprimer la réduction "${reduction.nom_reduction}" ?`,
    );

    if (!confirmation) {
      return;
    }

    this.reductionService.delete(reduction.id_reduction).subscribe({
      next: () => {
        this.reductions = this.reductions.filter((r) => r.id_reduction !== reduction.id_reduction);

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur suppression réduction :', err);

        const message = err?.error?.message || 'Impossible de supprimer la réduction.';

        window.alert(message);
      },
    });
  }

  // =========================================================
  // RESET
  // =========================================================

  resetFiltres(): void {
    this.searchTerm = '';
    this.selectedType = 'tous';
    this.selectedStatut = 'tous';
  }

  // =========================================================
  // TRACK
  // =========================================================

  trackByReduction(index: number, reduction: Reduction): number {
    return reduction.id_reduction;
  }
}
