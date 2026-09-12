import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { Employee } from '../../../../core/models/employee.model';

import { EmployeeService } from '../../../../core/services/employee.service';
import { AuthService } from '../../../../core/services/auth.service';

import { StatutPresence } from '../../../../core/models/enums/statut-presence.enum';
import { StatutUtilisateur } from '../../../../core/models/enums/statut-utilisateur.enum';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.css',
})
export class EmployeeListComponent implements OnInit {
  employees: Employee[] = [];

  loading = false;
  errorMessage = '';

  // -----------------------------------------------------------
  // FILTRES
  // -----------------------------------------------------------

  searchTerm = '';
  selectedStatutPresence: StatutPresence | 'tous' = 'tous';
  selectedStatutCompte: StatutUtilisateur | 'tous' = 'tous';

  statutsPresence = Object.values(StatutPresence);
  statutsCompte = Object.values(StatutUtilisateur);

  // -----------------------------------------------------------
  // SUPPRESSION
  // -----------------------------------------------------------

  employeeToDelete: Employee | null = null;
  deleting = false;

  constructor(
    private readonly employeeService: EmployeeService,
    public readonly authService: AuthService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    this.loadEmployees();
  }

  // =============================================================
  // PERMISSIONS
  // =============================================================

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  canViewEmployees(): boolean {
    return this.authService.isAdmin() || this.hasPermission('EMPLOYES_LISTE');
  }

  canAddEmployee(): boolean {
    return this.authService.isAdmin() || this.hasPermission('EMPLOYES_AJOUTER');
  }

  canEditEmployee(): boolean {
    return this.authService.isAdmin() || this.hasPermission('EMPLOYES_MODIFIER');
  }

  canDeleteEmployee(): boolean {
    return this.authService.isAdmin() || this.hasPermission('EMPLOYES_SUPPRIMER');
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadEmployees(): void {
    this.loading = true;
    this.errorMessage = '';

    this.employeeService
      .getAll()
      .pipe(
        // Filet de sécurité : si le backend ne répond jamais
        // (serveur arrêté, CORS bloqué, etc.), on ne reste pas
        // bloqué indéfiniment sur "Chargement...".
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors du chargement des employés :', error);

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
              `Impossible de charger la liste des employés (${error?.status ?? 'erreur inconnue'}).`;
          }

          return of([] as Employee[]);
        }),
      )
      .subscribe((employees) => {
        this.employees = Array.isArray(employees) ? employees : [];
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  // =============================================================
  // FILTRAGE
  // =============================================================

  get employeesFiltres(): Employee[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.employees.filter((employee) => {
      const nom = employee.nom?.toLowerCase() ?? '';
      const prenom = employee.prenom?.toLowerCase() ?? '';
      const email = employee.email?.toLowerCase() ?? '';
      const matricule = employee.matricule?.toLowerCase() ?? '';
      const telephone = employee.telephone?.toLowerCase() ?? '';

      const matchesSearch =
        !search ||
        nom.includes(search) ||
        prenom.includes(search) ||
        email.includes(search) ||
        matricule.includes(search) ||
        telephone.includes(search);

      const matchesStatutPresence =
        this.selectedStatutPresence === 'tous' ||
        employee.statutPresence === this.selectedStatutPresence;

      const matchesStatutCompte =
        this.selectedStatutCompte === 'tous' || employee.statut === this.selectedStatutCompte;

      return matchesSearch && matchesStatutPresence && matchesStatutCompte;
    });
  }

  // =============================================================
  // AFFICHAGE
  // =============================================================

  nomComplet(employee: Employee): string {
    return `${employee.prenom ?? ''} ${employee.nom ?? ''}`.trim() || '—';
  }

  roleNom(employee: Employee): string {
    return employee.role?.nom_role || 'Aucun rôle';
  }

  pdvNom(employee: Employee): string {
    return employee.pdvAffecte?.nomPdv || 'Non affecté';
  }

  // =============================================================
  // NAVIGATION
  // =============================================================

  nouvelEmployee(): void {
    if (!this.canAddEmployee()) {
      console.warn('Permission refusée : EMPLOYES_AJOUTER');
      return;
    }

    this.router.navigate(['/back-office/employees/nouveau']);
  }

  modifierEmployee(employee: Employee): void {
    if (!this.canEditEmployee()) {
      console.warn('Permission refusée : EMPLOYES_MODIFIER');
      return;
    }

    this.router.navigate(['/back-office/employees', employee.id_utilisateur, 'modifier']);
  }

  // =============================================================
  // SUPPRESSION
  // =============================================================

  demanderSuppression(employee: Employee): void {
    if (!this.canDeleteEmployee()) {
      console.warn('Permission refusée : EMPLOYES_SUPPRIMER');
      return;
    }

    this.employeeToDelete = employee;
    this.cdr.detectChanges();
  }

  annulerSuppression(): void {
    this.employeeToDelete = null;
    this.cdr.detectChanges();
  }

  confirmerSuppression(): void {
    if (!this.employeeToDelete) {
      return;
    }

    this.deleting = true;

    this.employeeService.delete(this.employeeToDelete.id_utilisateur).subscribe({
      next: () => {
        this.employees = this.employees.filter(
          (e) => e.id_utilisateur !== this.employeeToDelete!.id_utilisateur,
        );

        this.employeeToDelete = null;
        this.deleting = false;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors de la suppression :', error);

        this.errorMessage = error?.error?.message ?? 'Impossible de supprimer cet employé.';
        this.employeeToDelete = null;
        this.deleting = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =============================================================
  // RETOUR
  // =============================================================

  retour(): void {
    this.router.navigate(['/back-office']);
  }
}
