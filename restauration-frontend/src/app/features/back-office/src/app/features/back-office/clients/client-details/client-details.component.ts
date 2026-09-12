import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { ClientAuthentifie } from '../../../../core/models/client-authentifie.model';
import { ClientService } from '../../../../core/services/client.service';import { AuthService } from '../../../../core/services/auth.service';

import { StatutUtilisateur } from '../../../../core/models/enums/statut-utilisateur.enum';

@Component({
  selector: 'app-client-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './client-details.component.html',
  styleUrl: './client-details.component.css',
})
export class ClientDetailsComponent implements OnInit {
  client: ClientAuthentifie | null = null;

  loading = false;
  error = '';  // -----------------------------------------------------------
  // BLOCAGE / DEBLOCAGE
  // -----------------------------------------------------------

  actionEnCours: 'bloquer' | 'debloquer' | null = null;
  processingAction = false;
  errorAction = '';

  constructor(
    private readonly clientService: ClientService,public readonly authService: AuthService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // PERMISSIONS
  // =============================================================

  canBloquerClient(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('CLIENTS_LISTE');
  }

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);

    if (!idParam || Number.isNaN(id) || id <= 0) {
      this.error = 'Identifiant du client invalide.';
      return;
    }

    this.loadClient(id);
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadClient(id: number): void {
    this.loading = true;
    this.error = '';
    this.client = null;

    this.clientService
      .getById(id)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (data) => {
          this.client = data;
        },

        error: (err) => {
          console.error('Erreur lors du chargement du client :', err);

          this.client = null;
          this.error = err?.error?.message ?? 'Impossible de charger ce client.';
        },
      });
  }

  // =============================================================
  // AFFICHAGE
  // =============================================================

  nomComplet(client: ClientAuthentifie): string {
    return `${client.prenom ?? ''} ${client.nom ?? ''}`.trim() || '—';
  }

  estBloque(client: ClientAuthentifie): boolean {
    return (
      client.statut === StatutUtilisateur.INACTIF || client.statut === StatutUtilisateur.SUSPENDU
    );
  }
  // =============================================================
  // NAVIGATION
  // =============================================================

  retour(): void {
    this.router.navigate(['/back-office/clients']);
  }

  // =============================================================
  // BLOCAGE / DEBLOCAGE DU COMPTE
  // =============================================================

  demanderBlocage(): void {
    if (!this.client || !this.canBloquerClient()) {
      return;
    }

    this.actionEnCours = this.estBloque(this.client) ? 'debloquer' : 'bloquer';
    this.errorAction = '';
    this.cdr.detectChanges();
  }

  annulerAction(): void {
    this.actionEnCours = null;
    this.errorAction = '';
    this.cdr.detectChanges();
  }

  confirmerAction(): void {
    if (!this.client || !this.actionEnCours) {
      return;
    }

    this.processingAction = true;
    this.errorAction = '';

    const clientId = this.client.id_utilisateur;

    const request$ =
      this.actionEnCours === 'bloquer'
        ? this.clientService.desactiver(clientId)
        : this.clientService.activer(clientId);

    request$.subscribe({
      next: (clientMisAJour) => {
        this.client = clientMisAJour;

        this.actionEnCours = null;
        this.processingAction = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du changement de statut du client :', err);

        this.errorAction = err?.error?.message ?? 'Impossible de modifier le statut de ce client.';
        this.processingAction = false;

        this.cdr.detectChanges();
      },
    });
  }
}
