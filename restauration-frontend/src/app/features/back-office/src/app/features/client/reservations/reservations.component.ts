import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ClientReservationService } from '../../../core/services/client-reservation.service';
import { ClientReservation } from '../../../core/models/client-reservation.model';
import { StatutReservation } from '../../../core/models/enums/statut-reservation.enum';
import { ClientBottomNavComponent } from '../shared/client-bottom-nav.component';

/**
 * Sidebar client — Réservations (voir ClientReservationController /
 * ClientReservationServiceImpl côté backend : ReservationController
 * existant est réservé au back-office et ne peut pas être appelé par
 * un client authentifié — même bug pattern que réclamations).
 *
 * Une seule page : liste des réservations du client + formulaire de
 * demande d'une nouvelle réservation (date + heure + nombre de
 * personnes + commentaire optionnel). Le restaurant est toujours
 * celui du compte connecté (jamais choisi ici, voir
 * ClientReservationServiceImpl), et aucune table n'est sélectionnée
 * par le client : c'est le restaurant qui l'assigne ensuite
 * (numeroTable reste null jusque-là).
 */
@Component({
  selector: 'app-client-reservations',
  standalone: true,
  imports: [CommonModule, FormsModule, ClientBottomNavComponent],
  templateUrl: './reservations.component.html',
  styleUrl: './reservations.component.css',
})
export class ClientReservationsComponent implements OnInit {
  readonly StatutReservation = StatutReservation;

  reservations: ClientReservation[] = [];
  loading = false;
  errorMessage = '';

  // Date minimale sélectionnable dans le champ date (aujourd'hui) —
  // même validation que côté backend (ClientReservationServiceImpl :
  // pas de date de réservation dans le passé).
  readonly dateMin = this.today();

  // Formulaire de création
  showForm = false;
  nombrePersonnes: number | null = 2;
  dateReservation = '';
  heureReservation = '';
  commentaire = '';
  submitting = false;
  submitError = '';
  submitSuccess = false;

  annulationEnCoursId: number | null = null;

  constructor(
    private readonly clientReservationService: ClientReservationService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadReservations(): void {
    this.loading = true;
    this.errorMessage = '';

    this.clientReservationService.getMesReservations().subscribe({
      next: (reservations) => {
        // Réservations les plus proches en premier (date puis heure
        // croissantes).
        this.reservations = [...reservations].sort((a, b) => {
          const parDate = a.dateReservation.localeCompare(b.dateReservation);
          return parDate !== 0 ? parDate : a.heureReservation.localeCompare(b.heureReservation);
        });
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossible de charger vos réservations pour le moment.';
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
    return (
      !!this.nombrePersonnes &&
      this.nombrePersonnes > 0 &&
      !!this.dateReservation &&
      !!this.heureReservation
    );
  }

  soumettre(): void {
    if (!this.formulaireValide || this.submitting) {
      return;
    }

    this.submitting = true;
    this.submitError = '';
    this.submitSuccess = false;

    this.clientReservationService
      .creer({
        nombrePersonnes: this.nombrePersonnes as number,
        dateReservation: this.dateReservation,
        heureReservation: this.heureReservation,
        commentaireClient: this.commentaire.trim() || null,
      })
      .subscribe({
        next: (reservation) => {
          this.reservations = [...this.reservations, reservation].sort((a, b) => {
            const parDate = a.dateReservation.localeCompare(b.dateReservation);
            return parDate !== 0 ? parDate : a.heureReservation.localeCompare(b.heureReservation);
          });
          this.nombrePersonnes = 2;
          this.dateReservation = '';
          this.heureReservation = '';
          this.commentaire = '';
          this.submitting = false;
          this.submitSuccess = true;
          this.showForm = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.submitError =
            error?.error?.message || "Impossible d'envoyer la demande de réservation. Réessayez.";
          this.submitting = false;
          this.cdr.detectChanges();
        },
      });
  }

  // =========================================================
  // ANNULATION
  // =========================================================

  peutAnnuler(reservation: ClientReservation): boolean {
    return (
      reservation.statut === StatutReservation.EN_ATTENTE ||
      reservation.statut === StatutReservation.CONFIRMEE
    );
  }

  annuler(reservation: ClientReservation): void {
    if (this.annulationEnCoursId !== null) {
      return;
    }

    this.annulationEnCoursId = reservation.id_reservation;

    this.clientReservationService.annuler(reservation.id_reservation).subscribe({
      next: (updated) => {
        this.reservations = this.reservations.map((r) =>
          r.id_reservation === updated.id_reservation ? updated : r,
        );
        this.annulationEnCoursId = null;
        this.cdr.detectChanges();
      },
      error: () => {
        this.annulationEnCoursId = null;
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // AFFICHAGE
  // =========================================================

  statutLabel(statut: StatutReservation): string {
    switch (statut) {
      case StatutReservation.EN_ATTENTE:
        return 'En attente';
      case StatutReservation.CONFIRMEE:
        return 'Confirmée';
      case StatutReservation.ANNULEE:
        return 'Annulée';
      case StatutReservation.HONOREE:
        return 'Honorée';
      case StatutReservation.NO_SHOW:
        return 'Non présenté';
      default:
        return statut;
    }
  }

  statutClass(statut: StatutReservation): string {
    switch (statut) {
      case StatutReservation.CONFIRMEE:
      case StatutReservation.HONOREE:
        return 'statut-confirmee';
      case StatutReservation.ANNULEE:
      case StatutReservation.NO_SHOW:
        return 'statut-annulee';
      default:
        return 'statut-attente';
    }
  }

  private today(): string {
    const d = new Date();
    const mois = `${d.getMonth() + 1}`.padStart(2, '0');
    const jour = `${d.getDate()}`.padStart(2, '0');
    return `${d.getFullYear()}-${mois}-${jour}`;
  }

  retourMenu(): void {
    this.router.navigate(['/client']);
  }
}
