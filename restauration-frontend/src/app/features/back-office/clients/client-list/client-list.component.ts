import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { ClientAuthentifie } from '../../../../core/models/client-authentifie.model';

import { ClientService } from '../../../../core/services/client.service';
import { AuthService } from '../../../../core/services/auth.service';

import { StatutUtilisateur } from '../../../../core/models/enums/statut-utilisateur.enum';

type SensTri = 'asc' | 'desc';

@Component({
  selector: 'app-client-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-list.component.html',
  styleUrl: './client-list.component.css',
})
export class ClientListComponent implements OnInit {
  clients: ClientAuthentifie[] = [];

  loading = false;
  errorMessage = '';

  // -----------------------------------------------------------
  // FILTRES / TRI
  // -----------------------------------------------------------

  searchTerm = '';
  selectedStatutCompte: StatutUtilisateur | 'tous' = 'tous';

  statutsCompte = Object.values(StatutUtilisateur);
// -----------------------------------------------------------
  // BLOCAGE / DEBLOCAGE
  // -----------------------------------------------------------

  clientAction: ClientAuthentifie | null = null;
  actionEnCours: 'bloquer' | 'debloquer' | null = null;
  processingAction = false;

  constructor(
    private readonly clientService: ClientService,
    public readonly authService: AuthService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    this.loadClients();
  }

  // =============================================================
  // PERMISSIONS
  // =============================================================

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  canViewClients(): boolean {
    return this.authService.isAdmin() || this.hasPermission('CLIENTS_LISTE');
  }

  canBloquerClient(): boolean {
    return this.authService.isAdmin() || this.hasPermission('CLIENTS_LISTE');
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadClients(): void {
    this.loading = true;
    this.errorMessage = '';

    this.clientService
      .getAll()
      .pipe(
        // Filet de sécurité : si le backend ne répond jamais
        // (serveur arrêté, CORS bloqué, etc.), on ne reste pas
        // bloqué indéfiniment sur "Chargement...".
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors du chargement des clients :', error);

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
              `Impossible de charger la liste des clients (${error?.status ?? 'erreur inconnue'}).`;
          }

          return of([] as ClientAuthentifie[]);
        }),
      )
      .subscribe((clients) => {
        this.clients = Array.isArray(clients) ? clients : [];
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  // =============================================================
  // FILTRAGE + TRI
  // =============================================================

  get clientsFiltres(): ClientAuthentifie[] {
    const search = this.searchTerm.trim().toLowerCase();

    const filtres = this.clients.filter((client) => {
      const nom = client.nom?.toLowerCase() ?? '';
      const prenom = client.prenom?.toLowerCase() ?? '';
      const email = client.email?.toLowerCase() ?? '';
      const telephone = client.telephone?.toLowerCase() ?? '';

      const matchesSearch =
        !search ||
        nom.includes(search) ||
        prenom.includes(search) ||
        email.includes(search) ||
        telephone.includes(search);

      const matchesStatutCompte =
        this.selectedStatutCompte === 'tous' || client.statut === this.selectedStatutCompte;

      return matchesSearch && matchesStatutCompte;
    });

    return filtres;
  }// =============================================================
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

  voirClient(client: ClientAuthentifie): void {
    this.router.navigate(['/back-office/clients', client.id_utilisateur]);
  }

  retour(): void {
    this.router.navigate(['/back-office']);
  }

  // =============================================================
  // BLOCAGE / DEBLOCAGE DU COMPTE
  // =============================================================

  demanderBlocage(client: ClientAuthentifie): void {
    if (!this.canBloquerClient()) {
      console.warn('Permission refusée : CLIENTS_LISTE');
      return;
    }

    this.clientAction = client;
    this.actionEnCours = this.estBloque(client) ? 'debloquer' : 'bloquer';
    this.cdr.detectChanges();
  }

  annulerAction(): void {
    this.clientAction = null;
    this.actionEnCours = null;
    this.cdr.detectChanges();
  }

  confirmerAction(): void {
    if (!this.clientAction || !this.actionEnCours) {
      return;
    }

    this.processingAction = true;

    const clientId = this.clientAction.id_utilisateur;

    const request$ =
      this.actionEnCours === 'bloquer'
        ? this.clientService.desactiver(clientId)
        : this.clientService.activer(clientId);

    request$.subscribe({
      next: (clientMisAJour) => {
        this.clients = this.clients.map((c) =>
          c.id_utilisateur === clientId ? clientMisAJour : c,
        );

        this.clientAction = null;
        this.actionEnCours = null;
        this.processingAction = false;

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du changement de statut du client :', error);

        this.errorMessage =
          error?.error?.message ?? 'Impossible de modifier le statut de ce client.';

        this.clientAction = null;
        this.actionEnCours = null;
        this.processingAction = false;

        this.cdr.detectChanges();
      },
    });
  }
}
