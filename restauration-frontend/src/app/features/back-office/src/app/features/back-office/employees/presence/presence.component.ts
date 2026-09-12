// src/app/features/back-office/employees/presence/presence.component.ts
//
// BACK OFFICE — "Présence"
// ---------------------------------------------------------------
// Permet à un responsable (permission EMPLOYES_PRESENCE) de marquer,
// pour un jour donné, le statut de chaque employé : Présent, Absent
// ou En congé. Affiche également, pour chaque employé, le nombre de
// fois où il a été marqué absent depuis le début du mois et depuis
// le début de l'année en cours (calculé côté serveur).
//
// Sécurité : la page est protégée côté route par PermissionGuard
// (permission 'EMPLOYES_PRESENCE') et côté backend par
// @PreAuthorize("hasAuthority('EMPLOYES_PRESENCE')") sur les
// endpoints /api/rh/presences/*.

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, timeout } from 'rxjs/operators';
import { of } from 'rxjs';

import { FeuillePresence } from '../../../../core/models/feuille-presence.model';
import { StatutPresence } from '../../../../core/models/enums/statut-presence.enum';

import { PresenceService } from '../../../../core/services/presence.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-presence',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './presence.component.html',
  styleUrl: './presence.component.css',
})
export class PresenceComponent implements OnInit {
  readonly StatutPresence = StatutPresence;

  feuille: FeuillePresence[] = [];

  loading = false;
  errorMessage = '';

  // -----------------------------------------------------------
  // FILTRES
  // -----------------------------------------------------------

  searchTerm = '';

  // Date consultée/marquée, au format 'yyyy-MM-dd' (celui attendu
  // nativement par <input type="date">).
  selectedDate: string = this.formatDate(new Date());

  // -----------------------------------------------------------
  // MARQUAGE EN COURS
  // -----------------------------------------------------------

  // id_utilisateur de l'employé dont le statut est en cours
  // d'enregistrement (pour désactiver ses boutons le temps de la
  // requête, sans bloquer le reste du tableau).
  savingEmployeeId: number | null = null;

  constructor(
    private readonly presenceService: PresenceService,
    public readonly authService: AuthService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadFeuille();
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadFeuille(): void {
    this.loading = true;
    this.errorMessage = '';

    this.presenceService
      .getFeuille(this.selectedDate)
      .pipe(
        // Filet de sécurité : si le backend ne répond jamais, on ne
        // reste pas bloqué indéfiniment sur "Chargement...".
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors du chargement de la feuille de présence :', error);

          if (error?.name === 'TimeoutError') {
            this.errorMessage =
              'Le serveur ne répond pas. Vérifiez que le backend est démarré ' +
              'et que le CORS est bien configuré.';
          } else if (error?.status === 0) {
            this.errorMessage =
              'Connexion au serveur impossible. Vérifiez que le backend tourne ' +
              'et que le CORS autorise localhost:4200.';
          } else if (error?.status === 403) {
            this.errorMessage = "Vous n'avez pas la permission de voir la feuille de présence.";
          } else {
            this.errorMessage =
              error?.error?.message ??
              `Impossible de charger la feuille de présence (${error?.status ?? 'erreur inconnue'}).`;
          }

          return of([] as FeuillePresence[]);
        }),
      )
      .subscribe((feuille) => {
        this.feuille = Array.isArray(feuille) ? feuille : [];
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  onDateChange(): void {
    if (!this.selectedDate) {
      this.selectedDate = this.formatDate(new Date());
    }

    this.loadFeuille();
  }

  allerAujourdhui(): void {
    this.selectedDate = this.formatDate(new Date());
    this.loadFeuille();
  }

  // =============================================================
  // FILTRAGE
  // =============================================================

  get feuilleFiltree(): FeuillePresence[] {
    const search = this.searchTerm.trim().toLowerCase();

    if (!search) {
      return this.feuille;
    }

    return this.feuille.filter((ligne) => {
      const nom = ligne.employee?.nom?.toLowerCase() ?? '';
      const prenom = ligne.employee?.prenom?.toLowerCase() ?? '';
      const matricule = ligne.employee?.matricule?.toLowerCase() ?? '';

      return nom.includes(search) || prenom.includes(search) || matricule.includes(search);
    });
  }

  // =============================================================
  // MARQUAGE DU STATUT
  // =============================================================

  marquer(ligne: FeuillePresence, statut: StatutPresence): void {
    const employeeId = ligne.employee.id_utilisateur;

    if (this.savingEmployeeId !== null) {
      return;
    }

    this.savingEmployeeId = employeeId;

    this.presenceService.marquer(employeeId, this.selectedDate, statut).subscribe({
      next: (presence) => {
        ligne.presenceDuJour = presence;

        // Le statut ABSENT vient éventuellement de changer : on
        // recharge la feuille entière pour que les compteurs
        // mois/année restent exacts (ex. on vient de retirer/ajouter
        // une absence du mois affiché).
        this.savingEmployeeId = null;
        this.loadFeuille();
      },

      error: (error) => {
        console.error('Erreur lors du marquage de la présence :', error);

        this.errorMessage =
          error?.error?.message ?? "Impossible d'enregistrer le statut de cet employé.";
        this.savingEmployeeId = null;
        this.cdr.detectChanges();
      },
    });
  }

  estStatutActif(ligne: FeuillePresence, statut: StatutPresence): boolean {
    return ligne.presenceDuJour?.statut === statut;
  }

  // =============================================================
  // AFFICHAGE
  // =============================================================

  nomComplet(ligne: FeuillePresence): string {
    const employee = ligne.employee;
    return `${employee?.prenom ?? ''} ${employee?.nom ?? ''}`.trim() || '—';
  }

  roleNom(ligne: FeuillePresence): string {
    return ligne.employee?.role?.nom_role || 'Aucun rôle';
  }

  get dateAffichee(): string {
    if (!this.selectedDate) {
      return '';
    }

    const [annee, mois, jour] = this.selectedDate.split('-').map(Number);
    const date = new Date(annee, mois - 1, jour);

    return date.toLocaleDateString('fr-FR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  }

  private formatDate(date: Date): string {
    const annee = date.getFullYear();
    const mois = `${date.getMonth() + 1}`.padStart(2, '0');
    const jour = `${date.getDate()}`.padStart(2, '0');

    return `${annee}-${mois}-${jour}`;
  }

  // =============================================================
  // NAVIGATION
  // =============================================================

  retour(): void {
    this.router.navigate(['/back-office/employees']);
  }
}
