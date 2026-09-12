// src/app/features/back-office/clients/conseils/conseil-list.component.ts
//
// BACK OFFICE — GESTION DES CLIENTS — "Conseils clients"
// ---------------------------------------------------------------
// Affiche les suggestions/conseils déposés par les clients
// (entité Suggestion côté backend) afin que l'équipe (permission
// CLIENTS_CONSEILS) puisse les consulter, les marquer comme
// "pris en compte" une fois traités, ou les supprimer.

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { Suggestion } from '../../../../core/models/suggestion.model';

import { SuggestionService } from '../../../../core/services/suggestion.service';
import { AuthService } from '../../../../core/services/auth.service';

type FiltreTraitement = 'tous' | 'traites' | 'non-traites';

@Component({
  selector: 'app-conseil-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './conseil-list.component.html',
  styleUrl: './conseil-list.component.css',
})
export class ConseilListComponent implements OnInit {
  suggestions: Suggestion[] = [];

  loading = false;
  errorMessage = '';
  successMessage = '';

  // -----------------------------------------------------------
  // FILTRES
  // -----------------------------------------------------------

  searchTerm = '';
  selectedFiltre: FiltreTraitement = 'tous';

  // -----------------------------------------------------------
  // DÉTAIL
  // -----------------------------------------------------------

  suggestionSelectionnee: Suggestion | null = null;

  // -----------------------------------------------------------
  // SUPPRESSION
  // -----------------------------------------------------------

  suggestionASupprimer: Suggestion | null = null;
  suppression = false;

  // -----------------------------------------------------------
  // MISE À JOUR "PRISE EN COMPTE" EN COURS
  // -----------------------------------------------------------

  idEnCoursDeMiseAJour: number | null = null;

  constructor(
    private readonly suggestionService: SuggestionService,
    public readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    this.loadSuggestions();
  }

  // =============================================================
  // PERMISSIONS
  // =============================================================

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  canGerer(): boolean {
    return this.authService.isAdmin() || this.hasPermission('CLIENTS_CONSEILS');
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadSuggestions(): void {
    this.loading = true;
    this.errorMessage = '';

    this.suggestionService
      .getAll()
      .pipe(
        // Filet de sécurité : si le backend ne répond jamais
        // (serveur arrêté, CORS bloqué, etc.), on ne reste pas
        // bloqué indéfiniment sur "Chargement...".
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors du chargement des conseils clients :', error);

          if (error?.name === 'TimeoutError') {
            this.errorMessage =
              'Le serveur ne répond pas. Vérifiez que le backend est démarré ' +
              'et que le CORS est bien configuré.';
          } else if (error?.status === 0) {
            this.errorMessage =
              'Connexion au serveur impossible. Vérifiez que le backend tourne ' +
              'et que le CORS autorise localhost:4200.';
          } else {
            this.errorMessage =
              error?.error?.message ??
              `Impossible de charger les conseils clients (${error?.status ?? 'erreur inconnue'}).`;
          }

          return of([] as Suggestion[]);
        }),
      )
      .subscribe((suggestions) => {
        this.suggestions = Array.isArray(suggestions)
          ? [...suggestions].sort(
              (a, b) =>
                new Date(b.dateCreation).getTime() - new Date(a.dateCreation).getTime(),
            )
          : [];

        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  // =============================================================
  // FILTRAGE
  // =============================================================

  get suggestionsFiltrees(): Suggestion[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.suggestions.filter((suggestion) => {
      const nomClient = this.nomClient(suggestion).toLowerCase();
      const contenu = suggestion.contenu?.toLowerCase() ?? '';
      const idSuggestion = suggestion.id_suggestion?.toString() ?? '';

      const matchesSearch =
        !search ||
        nomClient.includes(search) ||
        contenu.includes(search) ||
        idSuggestion.includes(search);

      const matchesFiltre =
        this.selectedFiltre === 'tous' ||
        (this.selectedFiltre === 'traites' && suggestion.priseEnCompte) ||
        (this.selectedFiltre === 'non-traites' && !suggestion.priseEnCompte);

      return matchesSearch && matchesFiltre;
    });
  }

  // -----------------------------------------------------------
  // COMPTEURS (pour les cartes de synthèse)
  // -----------------------------------------------------------

  get nombreTraites(): number {
    return this.suggestions.filter((s) => s.priseEnCompte).length;
  }

  get nombreNonTraites(): number {
    return this.suggestions.filter((s) => !s.priseEnCompte).length;
  }

  // =============================================================
  // AFFICHAGE
  // =============================================================

  nomClient(suggestion: Suggestion): string {
    const client = suggestion.client;

    if (!client) {
      return 'Client inconnu';
    }

    return `${client.prenom ?? ''} ${client.nom ?? ''}`.trim() || 'Client';
  }

  dateFormatee(date: string): string {
    if (!date) {
      return '—';
    }

    return new Date(date).toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  // =============================================================
  // DÉTAIL
  // =============================================================

  voirDetail(suggestion: Suggestion): void {
    this.suggestionSelectionnee = suggestion;
    this.cdr.detectChanges();
  }

  fermerDetail(): void {
    this.suggestionSelectionnee = null;
    this.cdr.detectChanges();
  }

  // =============================================================
  // PRISE EN COMPTE
  // =============================================================

  basculerPriseEnCompte(suggestion: Suggestion): void {
    if (!this.canGerer()) {
      console.warn('Permission refusée : CLIENTS_CONSEILS');
      return;
    }

    const idSuggestion = suggestion.id_suggestion;
    const nouvelleValeur = !suggestion.priseEnCompte;

    this.idEnCoursDeMiseAJour = idSuggestion;
    this.errorMessage = '';

    this.suggestionService.updatePriseEnCompte(idSuggestion, nouvelleValeur).subscribe({
      next: (suggestionMiseAJour) => {
        this.suggestions = this.suggestions.map((s) =>
          s.id_suggestion === idSuggestion ? suggestionMiseAJour : s,
        );

        if (this.suggestionSelectionnee?.id_suggestion === idSuggestion) {
          this.suggestionSelectionnee = suggestionMiseAJour;
        }

        this.idEnCoursDeMiseAJour = null;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors de la mise à jour du conseil client :', error);

        this.errorMessage =
          error?.error?.message ?? 'Impossible de mettre à jour ce conseil client.';

        this.idEnCoursDeMiseAJour = null;
        this.cdr.detectChanges();
      },
    });
  }

  // =============================================================
  // SUPPRESSION
  // =============================================================

  demanderSuppression(suggestion: Suggestion): void {
    if (!this.canGerer()) {
      console.warn('Permission refusée : CLIENTS_CONSEILS');
      return;
    }

    this.suggestionSelectionnee = null;
    this.suggestionASupprimer = suggestion;
    this.successMessage = '';
    this.cdr.detectChanges();
  }

  annulerSuppression(): void {
    this.suggestionASupprimer = null;
    this.cdr.detectChanges();
  }

  confirmerSuppression(): void {
    if (!this.suggestionASupprimer) {
      return;
    }

    this.suppression = true;

    const idSuggestion = this.suggestionASupprimer.id_suggestion;

    this.suggestionService.delete(idSuggestion).subscribe({
      next: () => {
        this.suggestions = this.suggestions.filter(
          (s) => s.id_suggestion !== idSuggestion,
        );

        this.suggestionASupprimer = null;
        this.suppression = false;
        this.successMessage = 'Conseil client supprimé avec succès.';

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors de la suppression du conseil client :', error);

        this.errorMessage =
          error?.error?.message ?? 'Impossible de supprimer ce conseil client.';

        this.suggestionASupprimer = null;
        this.suppression = false;
        this.cdr.detectChanges();
      },
    });
  }
}
