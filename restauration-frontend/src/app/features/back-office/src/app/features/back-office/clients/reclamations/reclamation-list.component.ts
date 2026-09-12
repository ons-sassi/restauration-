// src/app/features/back-office/clients/reclamations/reclamation-list.component.ts
//
// BACK OFFICE — GESTION DES CLIENTS — "Réclamations"
// ---------------------------------------------------------------
// Permet à l'équipe (permission CLIENTS_RECLAMATIONS) de consulter
// toutes les réclamations déposées par les clients, de filtrer par
// statut ou de rechercher par client / sujet, puis de répondre à
// une réclamation (ce qui la fait automatiquement passer au statut
// RESOLUE côté backend) ou de la supprimer.

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { Reclamation } from '../../../../core/models/reclamation.model';
import { StatutReclamation } from '../../../../core/models/enums/statut-reclamation.enum';

import { ReclamationService } from '../../../../core/services/reclamation.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-reclamation-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reclamation-list.component.html',
  styleUrl: './reclamation-list.component.css',
})
export class ReclamationListComponent implements OnInit {
  reclamations: Reclamation[] = [];

  loading = false;
  errorMessage = '';
  successMessage = '';

  // -----------------------------------------------------------
  // FILTRES
  // -----------------------------------------------------------

  searchTerm = '';
  selectedStatut: StatutReclamation | 'tous' = 'tous';

  statuts = Object.values(StatutReclamation);

  // -----------------------------------------------------------
  // DÉTAIL
  // -----------------------------------------------------------

  reclamationSelectionnee: Reclamation | null = null;

  // -----------------------------------------------------------
  // RÉPONSE
  // -----------------------------------------------------------

  reclamationARepondre: Reclamation | null = null;
  texteReponse = '';
  envoiReponse = false;
  errorReponse = '';

  // -----------------------------------------------------------
  // SUPPRESSION
  // -----------------------------------------------------------

  reclamationASupprimer: Reclamation | null = null;
  suppression = false;

  constructor(
    private readonly reclamationService: ReclamationService,
    public readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    this.loadReclamations();
  }

  // =============================================================
  // PERMISSIONS
  // =============================================================

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  canRepondre(): boolean {
    return this.authService.isAdmin() || this.hasPermission('CLIENTS_RECLAMATIONS');
  }

  canSupprimer(): boolean {
    return this.authService.isAdmin() || this.hasPermission('CLIENTS_RECLAMATIONS');
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadReclamations(): void {
    this.loading = true;
    this.errorMessage = '';

    this.reclamationService
      .getAll()
      .pipe(
        // Filet de sécurité : si le backend ne répond jamais
        // (serveur arrêté, CORS bloqué, etc.), on ne reste pas
        // bloqué indéfiniment sur "Chargement...".
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors du chargement des réclamations :', error);

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
              `Impossible de charger les réclamations (${error?.status ?? 'erreur inconnue'}).`;
          }

          return of([] as Reclamation[]);
        }),
      )
      .subscribe((reclamations) => {
        this.reclamations = Array.isArray(reclamations)
          ? [...reclamations].sort(
              (a, b) =>
                new Date(b.date_creation).getTime() - new Date(a.date_creation).getTime(),
            )
          : [];

        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  // =============================================================
  // FILTRAGE
  // =============================================================

  get reclamationsFiltrees(): Reclamation[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.reclamations.filter((reclamation) => {
      const nomClient = this.nomClient(reclamation).toLowerCase();
      const sujet = reclamation.sujet?.toLowerCase() ?? '';
      const idReclamation = reclamation.id_reclamation?.toString() ?? '';

      const matchesSearch =
        !search ||
        nomClient.includes(search) ||
        sujet.includes(search) ||
        idReclamation.includes(search);

      const matchesStatut =
        this.selectedStatut === 'tous' || reclamation.statut === this.selectedStatut;

      return matchesSearch && matchesStatut;
    });
  }

  // -----------------------------------------------------------
  // COMPTEURS (pour les cartes de synthèse)
  // -----------------------------------------------------------

  get nombreOuvertes(): number {
    return this.reclamations.filter((r) => r.statut === StatutReclamation.OUVERTE).length;
  }

  get nombreEnAttente(): number {
    return this.reclamations.filter((r) => r.statut === StatutReclamation.EN_ATTENTE).length;
  }

  get nombreResolues(): number {
    return this.reclamations.filter((r) => r.statut === StatutReclamation.RESOLUE).length;
  }

  // =============================================================
  // AFFICHAGE
  // =============================================================

  nomClient(reclamation: Reclamation): string {
    const client = reclamation.client;

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

  classeBadgeStatut(reclamation: Reclamation): string {
    const map: Record<StatutReclamation, string> = {
      [StatutReclamation.OUVERTE]: 'statut-ouverte',
      [StatutReclamation.EN_ATTENTE]: 'statut-en-attente',
      [StatutReclamation.RESOLUE]: 'statut-resolue',
    };

    return map[reclamation.statut] ?? '';
  }

  estResolue(reclamation: Reclamation): boolean {
    return reclamation.statut === StatutReclamation.RESOLUE;
  }

  // =============================================================
  // DÉTAIL
  // =============================================================

  voirDetail(reclamation: Reclamation): void {
    this.reclamationSelectionnee = reclamation;
    this.cdr.detectChanges();
  }

  fermerDetail(): void {
    this.reclamationSelectionnee = null;
    this.cdr.detectChanges();
  }

  // =============================================================
  // RÉPONSE
  // =============================================================

  ouvrirReponse(reclamation: Reclamation): void {
    if (!this.canRepondre()) {
      console.warn('Permission refusée : CLIENTS_RECLAMATIONS');
      return;
    }

    this.reclamationSelectionnee = null;
    this.reclamationARepondre = reclamation;
    this.texteReponse = reclamation.reponse_employee || '';
    this.errorReponse = '';
    this.successMessage = '';
    this.cdr.detectChanges();
  }

  annulerReponse(): void {
    this.reclamationARepondre = null;
    this.texteReponse = '';
    this.errorReponse = '';
    this.cdr.detectChanges();
  }

  confirmerReponse(): void {
    if (!this.reclamationARepondre) {
      return;
    }

    if (!this.texteReponse.trim()) {
      this.errorReponse = 'La réponse ne peut pas être vide.';
      this.cdr.detectChanges();
      return;
    }

    this.envoiReponse = true;
    this.errorReponse = '';

    const idReclamation = this.reclamationARepondre.id_reclamation;

    this.reclamationService.repondre(idReclamation, this.texteReponse.trim()).subscribe({
      next: (reclamationMiseAJour) => {
        this.reclamations = this.reclamations.map((r) =>
          r.id_reclamation === idReclamation ? reclamationMiseAJour : r,
        );

        this.reclamationARepondre = null;
        this.texteReponse = '';
        this.envoiReponse = false;
        this.successMessage = 'Réponse envoyée. La réclamation est marquée comme résolue.';

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error("Erreur lors de l'envoi de la réponse :", error);

        this.errorReponse =
          error?.error?.message ?? "Impossible d'envoyer la réponse pour le moment.";

        this.envoiReponse = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =============================================================
  // SUPPRESSION
  // =============================================================

  demanderSuppression(reclamation: Reclamation): void {
    if (!this.canSupprimer()) {
      console.warn('Permission refusée : CLIENTS_RECLAMATIONS');
      return;
    }

    this.reclamationSelectionnee = null;
    this.reclamationASupprimer = reclamation;
    this.successMessage = '';
    this.cdr.detectChanges();
  }

  annulerSuppression(): void {
    this.reclamationASupprimer = null;
    this.cdr.detectChanges();
  }

  confirmerSuppression(): void {
    if (!this.reclamationASupprimer) {
      return;
    }

    this.suppression = true;

    const idReclamation = this.reclamationASupprimer.id_reclamation;

    this.reclamationService.delete(idReclamation).subscribe({
      next: () => {
        this.reclamations = this.reclamations.filter(
          (r) => r.id_reclamation !== idReclamation,
        );

        this.reclamationASupprimer = null;
        this.suppression = false;
        this.successMessage = 'Réclamation supprimée avec succès.';

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors de la suppression de la réclamation :', error);

        this.errorMessage =
          error?.error?.message ?? 'Impossible de supprimer cette réclamation.';

        this.reclamationASupprimer = null;
        this.suppression = false;
        this.cdr.detectChanges();
      },
    });
  }
}
