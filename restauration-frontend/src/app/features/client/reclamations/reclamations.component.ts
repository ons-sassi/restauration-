import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ClientReclamationService } from '../../../core/services/client-reclamation.service';
import { ClientReclamation } from '../../../core/models/client-reclamation.model';
import { StatutReclamation } from '../../../core/models/enums/statut-reclamation.enum';
import { ClientBottomNavComponent } from '../shared/client-bottom-nav.component';

/**
 * Sidebar client — Réclamations (voir ClientReclamationController /
 * ClientReclamationServiceImpl côté backend : le ReclamationController
 * existant est réservé au back-office et ne peut pas être appelé par
 * un client authentifié).
 *
 * Une seule page : liste des réclamations du client + formulaire
 * d'ouverture d'une nouvelle réclamation (pas besoin d'un écran de
 * détail séparé pour ce premier lot).
 */
@Component({
  selector: 'app-client-reclamations',
  standalone: true,
  imports: [CommonModule, FormsModule, ClientBottomNavComponent],
  templateUrl: './reclamations.component.html',
  styleUrl: './reclamations.component.css',
})
export class ClientReclamationsComponent implements OnInit {
  readonly StatutReclamation = StatutReclamation;

  reclamations: ClientReclamation[] = [];
  loading = false;
  errorMessage = '';

  // Formulaire de création
  showForm = false;
  sujet = '';
  description = '';
  submitting = false;
  submitError = '';
  submitSuccess = false;

  constructor(
    private readonly clientReclamationService: ClientReclamationService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadReclamations();
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadReclamations(): void {
    this.loading = true;
    this.errorMessage = '';

    this.clientReclamationService.getMesReclamations().subscribe({
      next: (reclamations) => {
        // Plus récentes en premier.
        this.reclamations = [...reclamations].sort(
          (a, b) => new Date(b.date_creation).getTime() - new Date(a.date_creation).getTime(),
        );
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossible de charger vos réclamations pour le moment.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FORMULAIRE
  // =========================================================

  toggleForm(): void {
    this.showForm = !this.showForm;
    this.submitError = '';
    this.submitSuccess = false;
  }

  get formulaireValide(): boolean {
    return this.sujet.trim().length > 0 && this.description.trim().length > 0;
  }

  soumettre(): void {
    if (!this.formulaireValide || this.submitting) {
      return;
    }

    this.submitting = true;
    this.submitError = '';
    this.submitSuccess = false;

    this.clientReclamationService
      .creer({
        sujet: this.sujet.trim(),
        description: this.description.trim(),
        commandeId: null,
      })
      .subscribe({
        next: (reclamation) => {
          this.reclamations = [reclamation, ...this.reclamations];
          this.sujet = '';
          this.description = '';
          this.submitting = false;
          this.submitSuccess = true;
          this.showForm = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.submitError =
            error?.error?.message ||
            "Impossible d'envoyer la réclamation pour le moment. Réessayez.";
          this.submitting = false;
          this.cdr.detectChanges();
        },
      });
  }

  // =========================================================
  // AFFICHAGE
  // =========================================================

  statutLabel(statut: StatutReclamation): string {
    switch (statut) {
      case StatutReclamation.OUVERTE:
        return 'Ouverte';
      case StatutReclamation.EN_ATTENTE:
        return 'En attente';
      case StatutReclamation.RESOLUE:
        return 'Résolue';
      default:
        return statut;
    }
  }

  statutClass(statut: StatutReclamation): string {
    switch (statut) {
      case StatutReclamation.RESOLUE:
        return 'statut-resolue';
      case StatutReclamation.OUVERTE:
        return 'statut-ouverte';
      default:
        return 'statut-attente';
    }
  }

  retourMenu(): void {
    this.router.navigate(['/client']);
  }
}
