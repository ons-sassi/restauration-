import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { RoleService } from '../../../../core/services/role.service';
import { AuthService } from '../../../../core/services/auth.service';
import { Role } from '../../../../core/models/role.model';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.css'],
})
export class RoleListComponent implements OnInit {
  roles: Role[] = [];

  loading = false;
  errorMessage = '';

  // Rôle en attente de confirmation de suppression.
  roleToDelete: Role | null = null;
  deleting = false;

  constructor(
    private roleService: RoleService,
    private authService: AuthService,
    private router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    // Sécurité en profondeur : cette page est réservée à l'administrateur.
    // (protection principale : adminGuard sur la route)
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadRoles();
  }

  loadRoles(): void {
    this.loading = true;
    this.errorMessage = '';

    this.roleService
      .getAll()
      .pipe(
        // Filet de sécurité : si le backend ne répond jamais
        // (serveur arrêté, CORS bloqué, etc.), on ne reste pas
        // bloqué indéfiniment sur "Chargement...".
        timeout(15000),
        catchError((error) => {
          console.error(error);

          if (error?.name === 'TimeoutError') {
            this.errorMessage =
              'Le serveur ne répond pas. Vérifiez que le backend est démarré ' +
              '(http://localhost:8085) et que le CORS est bien configuré.';
          } else if (error?.status === 0) {
            this.errorMessage =
              'Connexion au serveur impossible. Vérifiez que le backend tourne ' +
              'et que le CORS autorise localhost:4200.';
          } else {
            this.errorMessage =
              error?.error?.message ??
              `Impossible de charger la liste des rôles (${error?.status ?? 'erreur inconnue'}).`;
          }

          return of([] as Role[]);
        }),
      )
      .subscribe((roles) => {
        this.roles = roles;
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  nouveauRole(): void {
    this.router.navigate(['/back-office/roles/nouveau']);
  }

  modifierRole(role: Role): void {
    this.router.navigate(['/back-office/roles', role.id_role, 'modifier']);
  }

  demanderSuppression(role: Role): void {
    this.roleToDelete = role;
    this.cdr.detectChanges();
  }

  annulerSuppression(): void {
    this.roleToDelete = null;
    this.cdr.detectChanges();
  }

  confirmerSuppression(): void {
    if (!this.roleToDelete) {
      return;
    }

    this.deleting = true;

    this.roleService.delete(this.roleToDelete.id_role).subscribe({
      next: () => {
        this.roles = this.roles.filter((r) => r.id_role !== this.roleToDelete!.id_role);
        this.roleToDelete = null;
        this.deleting = false;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error(error);
        this.errorMessage = 'Impossible de supprimer ce rôle.';
        this.roleToDelete = null;
        this.deleting = false;
        this.cdr.detectChanges();
      },
    });
  }

  retour(): void {
    this.router.navigate(['/back-office']);
  }
}
