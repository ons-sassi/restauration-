import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Taxe } from '../../../core/models/taxe.model';
import { TaxeService } from '../../../core/services/taxe.service';
import { AuthService } from '../../../core/services/auth.service';

interface TaxeForm {
  id_taxe: number | null;
  nom_taxe: string;
  taux: number | null;
  applicable_a: string;
  statut: string;
}

interface RepartitionApplication {
  applicable_a: string;
  nombre: number;
  pourcentage: number;
  tauxMoyen: number;
}

const FORMULAIRE_VIDE: TaxeForm = {
  id_taxe: null,
  nom_taxe: '',
  taux: null,
  applicable_a: '',
  statut: 'ACTIF',
};

const OPTIONS_APPLICABLE_A = [
  'Tous les produits',
  'Nourriture',
  'Boissons',
  'Boissons alcoolisées',
  'Services',
];

@Component({
  selector: 'app-taxes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './taxes.component.html',
  styleUrl: './taxes.component.css',
})
export class TaxesComponent implements OnInit {
  // =========================================================
  // DONNÉES
  // =========================================================

  taxes: Taxe[] = [];

  loading = false;
  error = '';

  // =========================================================
  // FILTRES
  // =========================================================

  searchTerm = '';
  statutFiltre = '';

  readonly optionsApplicableA = OPTIONS_APPLICABLE_A;

  // =========================================================
  // MODAL / FORMULAIRE
  // =========================================================

  modalOuvert = false;
  isEditMode = false;

  formulaire: TaxeForm = { ...FORMULAIRE_VIDE };

  saving = false;
  formError = '';

  constructor(
    private readonly taxeService: TaxeService,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    if (!this.canViewTaxes()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadTaxes();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canViewTaxes(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('TAXE_LISTE');
  }

  canAddTaxe(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('TAXE_AJOUTER');
  }

  canEditTaxe(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('TAXE_MODIFIER');
  }

  canDeleteTaxe(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('TAXE_SUPPRIMER');
  }

  canViewStatistiques(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('TAXE_STATISTIQUES');
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadTaxes(): void {
    this.loading = true;
    this.error = '';

    this.taxeService.getAll().subscribe({
      next: (data) => {
        this.taxes = Array.isArray(data) ? data : [];

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des taxes :', err);

        this.taxes = [];
        this.loading = false;

        this.error = 'Impossible de charger les taxes.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FILTRAGE (tableau)
  // =========================================================

  get taxesFiltrees(): Taxe[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.taxes.filter((taxe) => {
      const nom = taxe.nom_taxe?.toLowerCase() ?? '';
      const applicable = taxe.applicable_a?.toLowerCase() ?? '';

      const matchSearch = !search || nom.includes(search) || applicable.includes(search);

      const matchStatut =
        !this.statutFiltre || (taxe.statut ?? '').toUpperCase() === this.statutFiltre;

      return matchSearch && matchStatut;
    });
  }

  // =========================================================
  // INDICATEURS GLOBAUX
  // =========================================================

  get totalTaxes(): number {
    return this.taxes.length;
  }

  get totalTaxesActives(): number {
    return this.taxes.filter((t) => (t.statut ?? '').toUpperCase() === 'ACTIF').length;
  }

  get totalTaxesInactives(): number {
    return this.totalTaxes - this.totalTaxesActives;
  }

  get tauxMoyen(): number {
    if (this.taxes.length === 0) {
      return 0;
    }

    const total = this.taxes.reduce((acc, t) => acc + (t.taux ?? 0), 0);

    return Math.round((total / this.taxes.length) * 100) / 100;
  }

  get tauxMax(): Taxe | null {
    if (this.taxes.length === 0) {
      return null;
    }

    return this.taxes.reduce((max, t) => ((t.taux ?? 0) > (max.taux ?? 0) ? t : max));
  }

  get tauxMin(): Taxe | null {
    if (this.taxes.length === 0) {
      return null;
    }

    return this.taxes.reduce((min, t) => ((t.taux ?? 0) < (min.taux ?? 0) ? t : min));
  }

  // =========================================================
  // RÉPARTITION PAR APPLICATION
  // =========================================================

  get repartitionParApplication(): RepartitionApplication[] {
    const total = this.taxes.length;

    if (total === 0) {
      return [];
    }

    const groupes = new Map<string, Taxe[]>();

    for (const taxe of this.taxes) {
      const cle = taxe.applicable_a?.trim() || 'Non spécifié';

      if (!groupes.has(cle)) {
        groupes.set(cle, []);
      }

      groupes.get(cle)!.push(taxe);
    }

    return Array.from(groupes.entries())
      .map(([applicable_a, liste]) => {
        const tauxTotal = liste.reduce((acc, t) => acc + (t.taux ?? 0), 0);

        return {
          applicable_a,
          nombre: liste.length,
          pourcentage: Math.round((liste.length / total) * 1000) / 10,
          tauxMoyen: Math.round((tauxTotal / liste.length) * 100) / 100,
        };
      })
      .sort((a, b) => b.nombre - a.nombre);
  }

  trackByApplication(index: number, item: RepartitionApplication): string {
    return item.applicable_a;
  }

  // =========================================================
  // MODAL - OUVERTURE
  // =========================================================

  nouvelleTaxe(): void {
    if (!this.canAddTaxe()) {
      console.warn('Permission refusée : TAXE_AJOUTER');
      return;
    }

    this.isEditMode = false;
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.formError = '';
    this.modalOuvert = true;
    this.cdr.detectChanges();
  }

  modifierTaxe(taxe: Taxe): void {
    if (!this.canEditTaxe()) {
      console.warn('Permission refusée : TAXE_MODIFIER');
      return;
    }

    try {
      this.isEditMode = true;
      this.formError = '';

      this.formulaire = {
        id_taxe: taxe.id_taxe,
        nom_taxe: taxe.nom_taxe,
        taux: taxe.taux,
        applicable_a: taxe.applicable_a,
        statut: (taxe.statut ?? 'ACTIF').toUpperCase(),
      };

      this.modalOuvert = true;
      this.cdr.detectChanges();
    } catch (e) {
      console.error("Erreur lors de l'ouverture du formulaire taxe :", e);
    }
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
    if (this.isEditMode ? !this.canEditTaxe() : !this.canAddTaxe()) {
      this.formError = this.isEditMode
        ? 'Permission refusée : TAXE_MODIFIER'
        : 'Permission refusée : TAXE_AJOUTER';
      return;
    }

    this.formError = '';

    const nom = this.formulaire.nom_taxe.trim();
    const applicable = this.formulaire.applicable_a.trim();
    const taux = this.formulaire.taux;

    if (!nom) {
      this.formError = 'Veuillez saisir le nom de la taxe.';
      return;
    }

    if (taux === null || taux < 0 || taux > 100) {
      this.formError = 'Veuillez saisir un taux valide (entre 0 et 100).';
      return;
    }

    if (!applicable) {
      this.formError = "Veuillez sélectionner à quoi s'applique la taxe.";
      return;
    }

    const payload: Partial<Taxe> = {
      nom_taxe: nom,
      taux,
      applicable_a: applicable,
      statut: this.formulaire.statut,
    };

    this.saving = true;

    const request$ =
      this.isEditMode && this.formulaire.id_taxe !== null
        ? this.taxeService.update(this.formulaire.id_taxe, payload as Taxe)
        : this.taxeService.create(payload as Taxe);

    request$.subscribe({
      next: () => {
        this.saving = false;

        this.modalOuvert = false;
        this.formulaire = { ...FORMULAIRE_VIDE };

        this.loadTaxes();
      },

      error: (err) => {
        console.error("Erreur lors de l'enregistrement de la taxe :", err);

        this.saving = false;

        this.formError =
          err?.error?.message || "Impossible d'enregistrer la taxe. Veuillez réessayer.";

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerTaxe(taxe: Taxe): void {
    if (!this.canDeleteTaxe()) {
      console.warn('Permission refusée : TAXE_SUPPRIMER');
      return;
    }

    const confirmation = window.confirm(
      `Voulez-vous vraiment supprimer la taxe "${taxe.nom_taxe}" ?`,
    );

    if (!confirmation) {
      return;
    }

    this.taxeService.delete(taxe.id_taxe).subscribe({
      next: () => {
        this.taxes = this.taxes.filter((t) => t.id_taxe !== taxe.id_taxe);

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors de la suppression de la taxe :', err);

        window.alert(
          'Impossible de supprimer cette taxe. Elle est peut-être encore utilisée par des produits.',
        );
      },
    });
  }

  // =========================================================
  // BASCULE STATUT RAPIDE
  // =========================================================

  basculerStatut(taxe: Taxe): void {
    if (!this.canEditTaxe()) {
      console.warn('Permission refusée : TAXE_MODIFIER');
      return;
    }

    const nouveauStatut = (taxe.statut ?? '').toUpperCase() === 'ACTIF' ? 'INACTIF' : 'ACTIF';

    this.taxeService.update(taxe.id_taxe, { ...taxe, statut: nouveauStatut }).subscribe({
      next: (updated) => {
        taxe.statut = updated.statut;
        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du changement de statut de la taxe :', err);

        window.alert('Impossible de modifier le statut de cette taxe.');
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

  trackByTaxe(index: number, taxe: Taxe): number {
    return taxe.id_taxe;
  }
}
