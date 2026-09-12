import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Fournisseur } from '../../../../core/models/fournisseur.model';
import { FournisseurService } from '../../../../core/services/fournisseur.service';
import { AuthService } from '../../../../core/services/auth.service';

interface FournisseurForm {
  id_fournisseur: number | null;
  nom: string;
  numTel: string;
  adresse: string;
  email: string;
  delai_livraison_moyen: number | null;
}

const FORMULAIRE_VIDE: FournisseurForm = {
  id_fournisseur: null,
  nom: '',
  numTel: '',
  adresse: '',
  email: '',
  delai_livraison_moyen: null,
};

@Component({
  selector: 'app-fournisseurs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './fournisseurs.component.html',
  styleUrl: './fournisseurs.component.css',
})
export class FournisseursComponent implements OnInit {
  // =========================================================
  // DONNÉES
  // =========================================================

  fournisseurs: Fournisseur[] = [];

  loading = false;
  error = '';

  // =========================================================
  // FILTRES
  // =========================================================

  searchTerm = '';

  // =========================================================
  // MODAL / FORMULAIRE
  // =========================================================

  modalOuvert = false;
  isEditMode = false;

  formulaire: FournisseurForm = { ...FORMULAIRE_VIDE };

  saving = false;
  formError = '';

  constructor(
    private readonly fournisseurService: FournisseurService,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    if (!this.canViewFournisseurs()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadFournisseurs();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================
  // Un seul code pour ce module (STOCK_FOURNISSEURS) : il gouverne
  // l'accès à la page ainsi que les actions ajouter / modifier / supprimer.

  canViewFournisseurs(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('STOCK_FOURNISSEURS');
  }

  canManageFournisseurs(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('STOCK_FOURNISSEURS');
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadFournisseurs(): void {
    this.loading = true;
    this.error = '';

    this.fournisseurService.getAll().subscribe({
      next: (data) => {
        this.fournisseurs = Array.isArray(data) ? data : [];

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des fournisseurs :', err);

        this.fournisseurs = [];
        this.loading = false;

        this.error = 'Impossible de charger les fournisseurs.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FILTRAGE
  // =========================================================

  get fournisseursFiltres(): Fournisseur[] {
    const search = this.searchTerm.trim().toLowerCase();

    if (!search) {
      return this.fournisseurs;
    }

    return this.fournisseurs.filter((fournisseur) => {
      const nom = fournisseur.nom?.toLowerCase() ?? '';
      const email = fournisseur.email?.toLowerCase() ?? '';
      const adresse = fournisseur.adresse?.toLowerCase() ?? '';
      const numTel = fournisseur.numTel?.toLowerCase() ?? '';

      return (
        nom.includes(search) ||
        email.includes(search) ||
        adresse.includes(search) ||
        numTel.includes(search)
      );
    });
  }

  // =========================================================
  // STATISTIQUES
  // =========================================================

  get totalFournisseurs(): number {
    return this.fournisseurs.length;
  }

  get delaiMoyenGlobal(): number {
    if (this.fournisseurs.length === 0) {
      return 0;
    }

    const total = this.fournisseurs.reduce((acc, f) => acc + (f.delai_livraison_moyen ?? 0), 0);

    return Math.round(total / this.fournisseurs.length);
  }

  // =========================================================
  // MODAL - OUVERTURE
  // =========================================================

  nouveauFournisseur(): void {
    if (!this.canManageFournisseurs()) {
      console.warn('Permission refusée : STOCK_FOURNISSEURS');
      return;
    }

    this.isEditMode = false;
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.formError = '';
    this.modalOuvert = true;
    this.cdr.detectChanges();
  }

  modifierFournisseur(fournisseur: Fournisseur): void {
    if (!this.canManageFournisseurs()) {
      console.warn('Permission refusée : STOCK_FOURNISSEURS');
      return;
    }

    try {
      this.isEditMode = true;
      this.formError = '';

      this.formulaire = {
        id_fournisseur: fournisseur.id_fournisseur,
        nom: fournisseur.nom,
        numTel: fournisseur.numTel,
        adresse: fournisseur.adresse,
        email: fournisseur.email,
        delai_livraison_moyen: fournisseur.delai_livraison_moyen,
      };

      this.modalOuvert = true;
      this.cdr.detectChanges();
    } catch (e) {
      console.error("Erreur lors de l'ouverture du formulaire fournisseur :", e);
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
    if (!this.canManageFournisseurs()) {
      this.formError = 'Permission refusée : STOCK_FOURNISSEURS';
      return;
    }

    this.formError = '';

    const nom = this.formulaire.nom.trim();
    const email = this.formulaire.email.trim();
    const numTel = this.formulaire.numTel.trim();
    const adresse = this.formulaire.adresse.trim();
    const delai = this.formulaire.delai_livraison_moyen;

    if (!nom) {
      this.formError = 'Veuillez saisir le nom du fournisseur.';
      return;
    }

    if (!numTel) {
      this.formError = 'Veuillez saisir un numéro de téléphone.';
      return;
    }

    if (!adresse) {
      this.formError = "Veuillez saisir l'adresse du fournisseur.";
      return;
    }

    if (delai === null || delai < 0) {
      this.formError = 'Veuillez saisir un délai de livraison moyen valide.';
      return;
    }

    const payload: Partial<Fournisseur> = {
      nom,
      numTel,
      adresse,
      email,
      delai_livraison_moyen: delai,
    };

    this.saving = true;

    const request$ =
      this.isEditMode && this.formulaire.id_fournisseur !== null
        ? this.fournisseurService.update(this.formulaire.id_fournisseur, payload)
        : this.fournisseurService.create(payload);

    request$.subscribe({
      next: () => {
        this.saving = false;

        this.modalOuvert = false;
        this.formulaire = { ...FORMULAIRE_VIDE };

        this.loadFournisseurs();
      },

      error: (err) => {
        console.error("Erreur lors de l'enregistrement du fournisseur :", err);

        this.saving = false;

        this.formError =
          err?.error?.message || "Impossible d'enregistrer le fournisseur. Veuillez réessayer.";

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerFournisseur(fournisseur: Fournisseur): void {
    if (!this.canManageFournisseurs()) {
      console.warn('Permission refusée : STOCK_FOURNISSEURS');
      return;
    }

    const confirmation = window.confirm(
      `Voulez-vous vraiment supprimer le fournisseur "${fournisseur.nom}" ?`,
    );

    if (!confirmation) {
      return;
    }

    this.fournisseurService.delete(fournisseur.id_fournisseur).subscribe({
      next: () => {
        this.fournisseurs = this.fournisseurs.filter(
          (f) => f.id_fournisseur !== fournisseur.id_fournisseur,
        );

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors de la suppression du fournisseur :', err);

        window.alert(
          'Impossible de supprimer ce fournisseur. Il est peut-être encore lié à des ingrédients ou des commandes.',
        );
      },
    });
  }

  // =========================================================
  // RESET / TRACK
  // =========================================================

  resetFiltres(): void {
    this.searchTerm = '';
  }

  trackByFournisseur(index: number, fournisseur: Fournisseur): number {
    return fournisseur.id_fournisseur;
  }
}
