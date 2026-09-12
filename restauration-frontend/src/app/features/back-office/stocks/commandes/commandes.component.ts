import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { BonDeCommandeStock } from '../../../../core/models/bon-de-commande-stock.model';
import { LigneBonDeCommande } from '../../../../core/models/ligne-bon-de-commande.model';
import { Fournisseur } from '../../../../core/models/fournisseur.model';
import { Ingredient } from '../../../../core/models/ingredient.model';
import { StatutBonCommande } from '../../../../core/models/enums/statut-bon-commande.enum';

import { BonCommandeStockService } from '../../../../core/services/bon-commande-stock.service';
import { FournisseurService } from '../../../../core/services/fournisseur.service';
import { IngredientService } from '../../../../core/services/ingredient.service';
import { AuthService } from '../../../../core/services/auth.service';

import { StatusBadgeComponent } from '../../../../shared/components/status-badge/status-badge.component';

// =========================================================
// LIGNE DE FORMULAIRE (pour la création d'une commande)
// =========================================================

interface LigneForm {
  ingredientId: number | null;
  quantite_commandee: number | null;
  prix_unitaire: number | null;
}

function ligneVide(): LigneForm {
  return {
    ingredientId: null,
    quantite_commandee: null,
    prix_unitaire: null,
  };
}

@Component({
  selector: 'app-commandes-fournisseurs',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  templateUrl: './commandes.component.html',
  styleUrl: './commandes.component.css',
})
export class CommandesFournisseursComponent implements OnInit {
  // =========================================================
  // DONNÉES
  // =========================================================

  commandes: BonDeCommandeStock[] = [];
  fournisseurs: Fournisseur[] = [];
  ingredients: Ingredient[] = [];

  loading = false;
  error = '';

  readonly StatutBonCommande = StatutBonCommande;

  // =========================================================
  // FILTRES
  // =========================================================

  searchTerm = '';
  selectedFournisseur: number | null = null;
  selectedStatut = 'tous';

  // =========================================================
  // MODAL - NOUVELLE COMMANDE
  // =========================================================

  modalOuvert = false;

  fournisseurId: number | null = null;
  lignes: LigneForm[] = [ligneVide()];

  saving = false;
  formError = '';

  // =========================================================
  // MODAL - DÉTAILS D'UNE COMMANDE
  // =========================================================

  detailsOuvert = false;
  commandeSelectionnee: BonDeCommandeStock | null = null;
  lignesDetails: LigneBonDeCommande[] = [];
  loadingDetails = false;

  constructor(
    private readonly bonCommandeService: BonCommandeStockService,
    private readonly fournisseurService: FournisseurService,
    private readonly ingredientService: IngredientService,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    if (!this.canViewCommandes()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadCommandes();
    this.loadFournisseurs();
    this.loadIngredients();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canViewCommandes(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('STOCK_COMMANDES');
  }

  // Créer une nouvelle commande a son propre code dans le cahier
  // des fonctionnalités.
  canAddCommande(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('STOCK_COMMANDES_AJOUTER');
  }

  // Pas de code dédié pour changer le statut / supprimer : on
  // s'appuie sur le code général de gestion des commandes.
  canManageCommandes(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('STOCK_COMMANDES');
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadCommandes(): void {
    this.loading = true;
    this.error = '';

    this.bonCommandeService.getAll().subscribe({
      next: (data) => {
        this.commandes = Array.isArray(data) ? data : [];

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des commandes fournisseurs :', err);

        this.commandes = [];
        this.loading = false;

        this.error = 'Impossible de charger les commandes fournisseurs.';

        this.cdr.detectChanges();
      },
    });
  }

  loadFournisseurs(): void {
    this.fournisseurService.getAll().subscribe({
      next: (data) => {
        this.fournisseurs = Array.isArray(data) ? data : [];

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des fournisseurs :', err);

        this.fournisseurs = [];

        this.cdr.detectChanges();
      },
    });
  }

  loadIngredients(): void {
    this.ingredientService.getAll().subscribe({
      next: (data) => {
        this.ingredients = Array.isArray(data) ? data : [];

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des ingrédients :', err);

        this.ingredients = [];

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FILTRAGE
  // =========================================================

  get commandesFiltrees(): BonDeCommandeStock[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.commandes.filter((commande) => {
      const nomFournisseur = commande.fournisseur?.nom?.toLowerCase() ?? '';

      const matchesSearch = !search || nomFournisseur.includes(search);

      const matchesFournisseur =
        this.selectedFournisseur === null ||
        commande.fournisseur?.id_fournisseur === this.selectedFournisseur;

      const matchesStatut =
        this.selectedStatut === 'tous' || commande.statut === this.selectedStatut;

      return matchesSearch && matchesFournisseur && matchesStatut;
    });
  }

  // =========================================================
  // STATISTIQUES
  // =========================================================

  get totalCommandes(): number {
    return this.commandes.length;
  }

  get totalEnAttente(): number {
    // Toute commande qui n'est ni livrée ni annulée est considérée "en attente"
    // (couvre aussi un statut EN_ATTENTE explicite ou manquant).
    return this.commandes.filter(
      (c) => c.statut !== StatutBonCommande.LIVRE && c.statut !== StatutBonCommande.ANNULE,
    ).length;
  }

  get totalLivrees(): number {
    return this.commandes.filter((c) => c.statut === StatutBonCommande.LIVRE).length;
  }

  get totalAnnulees(): number {
    return this.commandes.filter((c) => c.statut === StatutBonCommande.ANNULE).length;
  }

  get montantTotalGlobal(): number {
    return this.commandes.reduce((acc, c) => acc + (c.montant_total ?? 0), 0);
  }

  // =========================================================
  // AFFICHAGE STATUT
  // =========================================================

  getStatutLabel(commande: BonDeCommandeStock): string {
    switch (commande.statut) {
      case StatutBonCommande.EN_ATTENTE:
        return 'En attente';
      case StatutBonCommande.LIVRE:
        return 'Livré';
      case StatutBonCommande.ANNULE:
        return 'Annulé';
      default:
        return commande.statut;
    }
  }

  getStatutType(commande: BonDeCommandeStock): 'success' | 'warning' | 'danger' | 'info' {
    switch (commande.statut) {
      case StatutBonCommande.LIVRE:
        return 'success';
      case StatutBonCommande.ANNULE:
        return 'danger';
      case StatutBonCommande.EN_ATTENTE:
        return 'warning';
      default:
        return 'info';
    }
  }

  // =========================================================
  // MODAL - NOUVELLE COMMANDE
  // =========================================================

  nouvelleCommande(): void {
    if (!this.canAddCommande()) {
      console.warn('Permission refusée : STOCK_COMMANDES_AJOUTER');
      return;
    }

    this.fournisseurId = null;
    this.lignes = [ligneVide()];
    this.formError = '';
    this.modalOuvert = true;
    this.cdr.detectChanges();
  }

  fermerModal(): void {
    // Fermer la fenêtre doit toujours fonctionner, même si un enregistrement
    // est en cours (sinon le clic sur la croix ne fait rien de visible).
    this.modalOuvert = false;
    this.saving = false;
    this.fournisseurId = null;
    this.lignes = [ligneVide()];
    this.formError = '';
    this.cdr.detectChanges();
  }

  ajouterLigne(): void {
    this.lignes.push(ligneVide());
  }

  supprimerLigne(index: number): void {
    if (this.lignes.length === 1) {
      return;
    }

    this.lignes.splice(index, 1);
  }

  getIngredientNom(ingredientId: number | null): string {
    const ingredient = this.ingredients.find((i) => i.id_ingredient === ingredientId);
    return ingredient?.nom ?? '';
  }

  // =========================================================
  // FOURNISSEUR <-> INGRÉDIENTS (filtrage croisé)
  // =========================================================

  /**
   * Ingrédients à proposer dans les lignes de commande :
   * uniquement ceux fournis par le fournisseur sélectionné.
   * Si aucun fournisseur n'est encore choisi, on propose tous les ingrédients.
   */
  get ingredientsDisponibles(): Ingredient[] {
    if (this.fournisseurId === null) {
      return this.ingredients;
    }

    return this.ingredients.filter(
      (ingredient) => ingredient.fournisseur?.id_fournisseur === this.fournisseurId,
    );
  }

  /**
   * Appelé quand on change le fournisseur de la commande : on retire des
   * lignes les ingrédients qui ne sont pas fournis par ce fournisseur.
   */
  onFournisseurChange(): void {
    if (this.fournisseurId === null) {
      return;
    }

    for (const ligne of this.lignes) {
      if (ligne.ingredientId === null) {
        continue;
      }

      const ingredient = this.ingredients.find((i) => i.id_ingredient === ligne.ingredientId);

      if (ingredient?.fournisseur?.id_fournisseur !== this.fournisseurId) {
        ligne.ingredientId = null;
      }
    }
  }

  /**
   * Appelé quand on choisit un ingrédient sur une ligne : si aucun
   * fournisseur n'est encore sélectionné pour la commande, on le déduit
   * automatiquement à partir du fournisseur de cet ingrédient.
   */
  onIngredientChange(ligne: LigneForm): void {
    if (ligne.ingredientId === null) {
      return;
    }

    const ingredient = this.ingredients.find((i) => i.id_ingredient === ligne.ingredientId);

    if (!ingredient?.fournisseur?.id_fournisseur) {
      return;
    }

    if (this.fournisseurId === null) {
      this.fournisseurId = ingredient.fournisseur.id_fournisseur;
    }
  }

  /** Nom du fournisseur associé à un ingrédient, pour affichage/info. */
  getFournisseurDeIngredient(ingredientId: number | null): string {
    const ingredient = this.ingredients.find((i) => i.id_ingredient === ingredientId);
    return ingredient?.fournisseur?.nom ?? '';
  }

  get totalFormulaire(): number {
    return this.lignes.reduce((acc, ligne) => {
      const quantite = ligne.quantite_commandee ?? 0;
      const prix = ligne.prix_unitaire ?? 0;

      return acc + quantite * prix;
    }, 0);
  }

  // =========================================================
  // ENREGISTREMENT (PASSER COMMANDE)
  // =========================================================

  passerCommande(): void {
    if (!this.canAddCommande()) {
      this.formError = 'Permission refusée : STOCK_COMMANDES_AJOUTER';
      return;
    }

    this.formError = '';

    if (this.fournisseurId === null) {
      this.formError = 'Veuillez sélectionner un fournisseur.';
      return;
    }

    if (this.lignes.length === 0) {
      this.formError = 'Veuillez ajouter au moins une ligne de commande.';
      return;
    }

    for (const ligne of this.lignes) {
      if (ligne.ingredientId === null) {
        this.formError = 'Veuillez sélectionner un ingrédient pour chaque ligne.';
        return;
      }

      if (ligne.quantite_commandee === null || ligne.quantite_commandee <= 0) {
        this.formError = 'Veuillez saisir une quantité valide pour chaque ligne.';
        return;
      }

      if (ligne.prix_unitaire === null || ligne.prix_unitaire < 0) {
        this.formError = 'Veuillez saisir un prix unitaire valide pour chaque ligne.';
        return;
      }
    }

    const payload = {
      fournisseurId: this.fournisseurId,
      lignes: this.lignes.map((ligne) => ({
        ingredient: { id_ingredient: ligne.ingredientId },
        quantite_commandee: ligne.quantite_commandee,
        prix_unitaire: ligne.prix_unitaire,
      })),
    };

    this.saving = true;

    this.bonCommandeService.passerCommande(payload).subscribe({
      next: () => {
        this.saving = false;

        this.modalOuvert = false;
        this.fournisseurId = null;
        this.lignes = [ligneVide()];

        this.loadCommandes();
      },

      error: (err) => {
        console.error('Erreur lors de la création de la commande fournisseur :', err);

        this.saving = false;

        this.formError =
          err?.error?.message || 'Impossible de créer la commande. Veuillez réessayer.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // CHANGEMENT DE STATUT
  // =========================================================

  marquerLivree(commande: BonDeCommandeStock): void {
    if (!this.canManageCommandes()) {
      console.warn('Permission refusée : STOCK_COMMANDES');
      return;
    }

    this.changerStatut(commande, StatutBonCommande.LIVRE);
  }

  annulerCommande(commande: BonDeCommandeStock): void {
    if (!this.canManageCommandes()) {
      console.warn('Permission refusée : STOCK_COMMANDES');
      return;
    }

    const confirmation = window.confirm('Voulez-vous vraiment annuler cette commande ?');

    if (!confirmation) {
      return;
    }

    this.changerStatut(commande, StatutBonCommande.ANNULE);
  }

  private changerStatut(commande: BonDeCommandeStock, statut: StatutBonCommande): void {
    this.bonCommandeService.updateStatut(commande.id_bon_commande, statut).subscribe({
      next: (updated) => {
        const index = this.commandes.findIndex(
          (c) => c.id_bon_commande === commande.id_bon_commande,
        );

        if (index !== -1) {
          this.commandes[index] = updated;
        }

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du changement de statut :', err);

        window.alert('Impossible de modifier le statut de cette commande.');
      },
    });
  }

  // =========================================================
  // DÉTAILS D'UNE COMMANDE
  // =========================================================

  voirDetails(commande: BonDeCommandeStock): void {
    this.commandeSelectionnee = commande;
    this.lignesDetails = [];
    this.detailsOuvert = true;
    this.loadingDetails = true;

    this.cdr.detectChanges();

    this.bonCommandeService.getLignes(commande.id_bon_commande).subscribe({
      next: (lignes) => {
        this.lignesDetails = Array.isArray(lignes) ? lignes : [];
        this.loadingDetails = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des lignes de la commande :', err);

        this.lignesDetails = [];
        this.loadingDetails = false;

        this.cdr.detectChanges();
      },
    });
  }

  fermerDetails(): void {
    this.detailsOuvert = false;
    this.commandeSelectionnee = null;
    this.lignesDetails = [];
    this.cdr.detectChanges();
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerCommande(commande: BonDeCommandeStock): void {
    if (!this.canManageCommandes()) {
      console.warn('Permission refusée : STOCK_COMMANDES');
      return;
    }

    const confirmation = window.confirm(
      `Voulez-vous vraiment supprimer la commande #${commande.id_bon_commande} ?`,
    );

    if (!confirmation) {
      return;
    }

    this.bonCommandeService.delete(commande.id_bon_commande).subscribe({
      next: () => {
        this.commandes = this.commandes.filter(
          (c) => c.id_bon_commande !== commande.id_bon_commande,
        );

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors de la suppression de la commande :', err);

        window.alert('Impossible de supprimer cette commande.');
      },
    });
  }

  // =========================================================
  // RESET / TRACK
  // =========================================================

  resetFiltres(): void {
    this.searchTerm = '';
    this.selectedFournisseur = null;
    this.selectedStatut = 'tous';
  }

  trackByCommande(index: number, commande: BonDeCommandeStock): number {
    return commande.id_bon_commande;
  }

  trackByLigne(index: number): number {
    return index;
  }

  trackByLigneDetail(index: number, ligne: LigneBonDeCommande): number {
    return ligne.id_ligne;
  }
}
