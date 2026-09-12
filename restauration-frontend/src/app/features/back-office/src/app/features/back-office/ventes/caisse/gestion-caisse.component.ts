import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SessionCaisseService } from '../../../../core/services/session-caisse.service';
import { AuthService } from '../../../../core/services/auth.service';
import { SessionCaisse } from '../../../../core/models/session-caisse.model';

type FiltreStatut = 'TOUTES' | 'OUVERTES' | 'FERMEES';

@Component({
  selector: 'app-gestion-caisse',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-caisse.component.html',
  styleUrl: './gestion-caisse.component.css',
})
export class GestionCaisseComponent implements OnInit {
  // ==========================================================
  // DONNÉES
  // ==========================================================

  sessions: SessionCaisse[] = [];

  sessionsFiltrees: SessionCaisse[] = [];

  loading = false;

  errorMessage = '';

  // ==========================================================
  // FILTRES
  // ==========================================================

  dateDebut = '';

  dateFin = '';

  statut: FiltreStatut = 'TOUTES';

  // ==========================================================
  // FERMETURE D'UNE SESSION
  // ==========================================================

  sessionEnFermeture: SessionCaisse | null = null;

  montantFermetureSaisi: number | null = null;

  fermetureEnCours = false;

  // Résumé financier de la session en cours de fermeture
  // (total vendu / montant théorique attendu).
  resumeEnCours = false;

  totalVentesSession: number | null = null;

  montantTheoriqueSession: number | null = null;

  resumeErreur = '';

  // ==========================================================
  // TOTALS
  // ==========================================================

  totalOuvertures = 0;

  totalFermetures = 0;

  totalEcart = 0;

  nombreOuvertes = 0;

  constructor(
    private readonly sessionCaisseService: SessionCaisseService,
    private readonly cdr: ChangeDetectorRef,
    public readonly authService: AuthService,
  ) {}

  // ==========================================================
  // PERMISSIONS
  // ==========================================================

  hasPermission(permission: string): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission(permission);
  }

  /**
   * Permission pour consulter la gestion de caisse.
   */
  canViewCaisse(): boolean {
    return this.hasPermission('GESTION_CAISSE');
  }

  /**
   * Permission pour fermer une session de caisse.
   * Même code que la consultation : la fermeture fait partie
   * du même droit "GESTION_CAISSE" dans le cahier des permissions.
   */
  canFermerCaisse(): boolean {
    return this.hasPermission('GESTION_CAISSE');
  }

  // ==========================================================
  // INIT
  // ==========================================================

  ngOnInit(): void {
    const aujourdHui = new Date();

    const ilYaSeptJours = new Date();

    ilYaSeptJours.setDate(aujourdHui.getDate() - 7);

    this.dateDebut = this.formatDate(ilYaSeptJours);

    this.dateFin = this.formatDate(aujourdHui);

    this.chargerSessions();
  }

  // ==========================================================
  // CHARGEMENT
  // ==========================================================

  chargerSessions(): void {
    if (!this.canViewCaisse()) {
      console.warn('Permission refusée : GESTION_CAISSE');
      this.loading = false;
      return;
    }

    this.errorMessage = '';

    this.loading = true;

    const debut = new Date(`${this.dateDebut}T00:00:00`);

    const fin = new Date(`${this.dateFin}T23:59:59`);

    this.sessionCaisseService.getByDate(debut, fin).subscribe({
      next: (data) => {
        this.sessions = data ?? [];

        this.appliquerFiltreStatut();

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du chargement des sessions de caisse', error);

        this.sessions = [];

        this.appliquerFiltreStatut();

        this.errorMessage =
          error?.error?.message ?? 'Impossible de charger les sessions de caisse.';

        this.loading = false;

        this.cdr.detectChanges();
      },
    });
  }

  // ==========================================================
  // RECHERCHER
  // ==========================================================

  rechercher(): void {
    if (!this.canViewCaisse()) {
      console.warn('Permission refusée : GESTION_CAISSE');
      return;
    }

    if (!this.dateDebut || !this.dateFin) {
      this.errorMessage = 'Veuillez sélectionner une date de début et une date de fin.';

      return;
    }

    if (this.dateDebut > this.dateFin) {
      this.errorMessage = 'La date de début doit être antérieure ou égale à la date de fin.';

      return;
    }

    this.chargerSessions();
  }

  // ==========================================================
  // RÉINITIALISER
  // ==========================================================

  reinitialiser(): void {
    if (!this.canViewCaisse()) {
      console.warn('Permission refusée : GESTION_CAISSE');
      return;
    }

    const aujourdHui = new Date();

    const ilYaSeptJours = new Date();

    ilYaSeptJours.setDate(aujourdHui.getDate() - 7);

    this.dateDebut = this.formatDate(ilYaSeptJours);

    this.dateFin = this.formatDate(aujourdHui);

    this.statut = 'TOUTES';

    this.chargerSessions();
  }

  // ==========================================================
  // FILTRE STATUT (client-side)
  // ==========================================================

  changerStatut(statut: FiltreStatut): void {
    this.statut = statut;

    this.appliquerFiltreStatut();
  }

  private appliquerFiltreStatut(): void {
    if (this.statut === 'OUVERTES') {
      this.sessionsFiltrees = this.sessions.filter((s) => !s.dateFermeture);
    } else if (this.statut === 'FERMEES') {
      this.sessionsFiltrees = this.sessions.filter((s) => !!s.dateFermeture);
    } else {
      this.sessionsFiltrees = this.sessions;
    }

    this.calculerTotaux();
  }

  // ==========================================================
  // TOTALS
  // ==========================================================

  private calculerTotaux(): void {
    this.totalOuvertures = this.sessionsFiltrees.reduce(
      (total, s) => total + (s.montantOuverture ?? 0),
      0,
    );

    this.totalFermetures = this.sessionsFiltrees.reduce(
      (total, s) => total + (s.montantFermeture ?? 0),
      0,
    );

    this.totalEcart = this.sessionsFiltrees.reduce((total, s) => total + (s.ecartCaisse ?? 0), 0);

    this.nombreOuvertes = this.sessionsFiltrees.filter((s) => !s.dateFermeture).length;
  }

  // ==========================================================
  // FERMETURE D'UNE SESSION
  // ==========================================================

  ouvrirFormulaireFermeture(session: SessionCaisse): void {
    if (!this.canFermerCaisse()) {
      console.warn('Permission refusée : GESTION_CAISSE');
      return;
    }

    this.sessionEnFermeture = session;

    this.montantFermetureSaisi = session.montantOuverture ?? 0;

    this.chargerResumeSession(session.id_session_caisse);
  }

  annulerFermeture(): void {
    this.sessionEnFermeture = null;

    this.montantFermetureSaisi = null;

    this.totalVentesSession = null;

    this.montantTheoriqueSession = null;

    this.resumeErreur = '';
  }

  // ==========================================================
  // RÉSUMÉ DE LA SESSION (total vendu / montant théorique)
  // ==========================================================

  private chargerResumeSession(id: number): void {
    this.resumeEnCours = true;

    this.resumeErreur = '';

    this.sessionCaisseService.getResume(id).subscribe({
      next: (resume) => {
        this.totalVentesSession = resume.totalVentes ?? 0;

        this.montantTheoriqueSession = resume.montantTheorique ?? 0;

        this.resumeEnCours = false;

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du calcul du résumé de la session', error);

        this.totalVentesSession = null;

        this.montantTheoriqueSession = null;

        this.resumeErreur = error?.error?.message ?? 'Impossible de calculer le total des ventes.';

        this.resumeEnCours = false;

        this.cdr.detectChanges();
      },
    });
  }

  /**
   * Écart prévisionnel entre le montant saisi par l'utilisateur
   * et le montant théorique attendu en caisse (ouverture +
   * ventes de la session). Positif = surplus, négatif = manque.
   */
  get ecartPrevisionnel(): number | null {
    if (this.montantFermetureSaisi == null || this.montantTheoriqueSession == null) {
      return null;
    }

    return this.montantFermetureSaisi - this.montantTheoriqueSession;
  }

  confirmerFermeture(): void {
    if (!this.canFermerCaisse()) {
      console.warn('Permission refusée : GESTION_CAISSE');
      return;
    }

    if (!this.sessionEnFermeture || this.montantFermetureSaisi == null) {
      return;
    }

    this.fermetureEnCours = true;

    this.sessionCaisseService
      .fermer(this.sessionEnFermeture.id_session_caisse, this.montantFermetureSaisi)
      .subscribe({
        next: () => {
          this.fermetureEnCours = false;

          this.annulerFermeture();

          this.cdr.detectChanges();

          this.chargerSessions();
        },

        error: (error) => {
          console.error('Erreur lors de la fermeture de la caisse', error);

          this.fermetureEnCours = false;

          this.errorMessage =
            error?.error?.message ?? 'Impossible de fermer cette session de caisse.';

          this.cdr.detectChanges();
        },
      });
  }

  // ==========================================================
  // AFFICHAGE
  // ==========================================================

  nomEmploye(session: SessionCaisse): string {
    if (!session.employee) {
      return '—';
    }

    return `${session.employee.prenom ?? ''} ${session.employee.nom ?? ''}`.trim();
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();

    const month = String(date.getMonth() + 1).padStart(2, '0');

    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
