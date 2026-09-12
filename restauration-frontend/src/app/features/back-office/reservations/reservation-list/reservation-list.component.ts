import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { Reservation } from '../../../../core/models/reservation.model';
import { TableRestaurant } from '../../../../core/models/table-restaurant.model';
import { ClientAuthentifie } from '../../../../core/models/client-authentifie.model';

import { ReservationService } from '../../../../core/services/reservation.service';
import { TableRestaurantService } from '../../../../core/services/table-restaurant.service';
import { ClientService } from '../../../../core/services/client.service';
import { AuthService } from '../../../../core/services/auth.service';

import { StatutReservation } from '../../../../core/models/enums/statut-reservation.enum';

type SensTri = 'asc' | 'desc';

@Component({
  selector: 'app-reservation-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reservation-list.component.html',
  styleUrl: './reservation-list.component.css',
})
export class ReservationListComponent implements OnInit {
  reservations: Reservation[] = [];

  loading = false;
  errorMessage = '';

  // -----------------------------------------------------------
  // FILTRES / TRI
  // -----------------------------------------------------------

  searchTerm = '';
  selectedStatut: StatutReservation | 'tous' = 'tous';
  selectedDate = '';

  statutsReservation = Object.values(StatutReservation);

  sensTriDate: SensTri = 'asc';

  // -----------------------------------------------------------
  // PERMISSIONS
  // -----------------------------------------------------------

  canModifier(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('RESERVATIONS_MODIFIER');
  }

  canAnnuler(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('RESERVATIONS_ANNULER');
  }

  // La création n'est pas soumise à une permission séparée : on
  // considère qu'un utilisateur pouvant modifier les réservations
  // peut aussi en créer une nouvelle.
  canCreer(): boolean {
    return this.canModifier();
  }

  // -----------------------------------------------------------
  // ANNULATION
  // -----------------------------------------------------------

  reservationAAnnuler: Reservation | null = null;
  processingAnnulation = false;

  // -----------------------------------------------------------
  // DÉTAILS (lecture seule)
  // -----------------------------------------------------------

  reservationEnDetails: Reservation | null = null;

  // -----------------------------------------------------------
  // MODIFICATION
  // -----------------------------------------------------------

  reservationEnEdition: Reservation | null = null;
  formEdition: {
    nombre_personnes: number;
    dateReservation: string;
    heureReservation: string;
    statut: StatutReservation;
    tableId: number | null;
  } | null = null;

  tablesDisponibles: TableRestaurant[] = [];
  loadingTables = false;
  processingEdition = false;
  errorEdition = '';

  // -----------------------------------------------------------
  // CONFIRMATION (table obligatoire)
  // -----------------------------------------------------------

  reservationAConfirmer: Reservation | null = null;
  tableIdConfirmation: number | null = null;
  tablesDisponiblesConfirmation: TableRestaurant[] = [];
  loadingTablesConfirmation = false;
  processingConfirmation = false;
  errorConfirmation = '';

  // -----------------------------------------------------------
  // CRÉATION D'UNE NOUVELLE RÉSERVATION
  // -----------------------------------------------------------

  creationOuverte = false;
  processingCreation = false;
  errorCreation = '';

  clientsDisponibles: ClientAuthentifie[] = [];
  loadingClients = false;

  formCreation: {
    clientId: number | null;
    nombre_personnes: number;
    dateReservation: string;
    heureReservation: string;
    tableId: number | null;
    commentaire_client: string;
  } = {
    clientId: null,
    nombre_personnes: 1,
    dateReservation: '',
    heureReservation: '',
    tableId: null,
    commentaire_client: '',
  };

  constructor(
    private readonly reservationService: ReservationService,
    private readonly tableService: TableRestaurantService,
    private readonly clientService: ClientService,
    public readonly authService: AuthService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    this.loadReservations();
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadReservations(): void {
    this.loading = true;
    this.errorMessage = '';

    this.reservationService
      .getAll()
      .pipe(
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors du chargement des réservations :', error);

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
              `Impossible de charger la liste des réservations (${error?.status ?? 'erreur inconnue'}).`;
          }

          return of([] as Reservation[]);
        }),
      )
      .subscribe((reservations) => {
        this.reservations = Array.isArray(reservations) ? reservations : [];
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  // =============================================================
  // FILTRAGE + TRI
  // =============================================================

  get reservationsFiltrees(): Reservation[] {
    const search = this.searchTerm.trim().toLowerCase();

    const filtrees = this.reservations.filter((reservation) => {
      const nom = reservation.client?.nom?.toLowerCase() ?? '';
      const prenom = reservation.client?.prenom?.toLowerCase() ?? '';
      const email = reservation.client?.email?.toLowerCase() ?? '';

      const matchesSearch =
        !search || nom.includes(search) || prenom.includes(search) || email.includes(search);

      const matchesStatut =
        this.selectedStatut === 'tous' || reservation.statut === this.selectedStatut;

      const matchesDate = !this.selectedDate || reservation.dateReservation === this.selectedDate;

      return matchesSearch && matchesStatut && matchesDate;
    });

    const sens = this.sensTriDate === 'asc' ? 1 : -1;

    return [...filtrees].sort((a, b) => {
      const dateA = `${a.dateReservation ?? ''} ${a.heureReservation ?? ''}`;
      const dateB = `${b.dateReservation ?? ''} ${b.heureReservation ?? ''}`;

      return dateA.localeCompare(dateB) * sens;
    });
  }

  basculerSensTriDate(): void {
    this.sensTriDate = this.sensTriDate === 'asc' ? 'desc' : 'asc';
  }

  // =============================================================
  // AFFICHAGE
  // =============================================================

  nomCompletClient(reservation: Reservation): string {
    const client = reservation.client;

    return `${client?.prenom ?? ''} ${client?.nom ?? ''}`.trim() || '—';
  }

  libelleTable(reservation: Reservation): string {
    return reservation.table ? `Table ${reservation.table.numeroTable}` : 'Non assignée';
  }

  classeBadgeStatut(reservation: Reservation): string {
    const map: Record<StatutReservation, string> = {
      [StatutReservation.EN_ATTENTE]: 'statut-en-attente',
      [StatutReservation.CONFIRMEE]: 'statut-confirmee',
      [StatutReservation.ANNULEE]: 'statut-annulee',
      [StatutReservation.HONOREE]: 'statut-honoree',
      [StatutReservation.NO_SHOW]: 'statut-no-show',
    };

    return map[reservation.statut] ?? '';
  }

  peutConfirmer(reservation: Reservation): boolean {
    return reservation.statut === StatutReservation.EN_ATTENTE;
  }

  peutAnnuler(reservation: Reservation): boolean {
    return (
      reservation.statut !== StatutReservation.ANNULEE &&
      reservation.statut !== StatutReservation.HONOREE &&
      reservation.statut !== StatutReservation.NO_SHOW
    );
  }

  // Ligne surlignée en rouge pour une réservation annulée (souvent
  // annulée par le client lui-même) — repérage visuel rapide côté
  // employé.
  classeLigne(reservation: Reservation): string {
    return reservation.statut === StatutReservation.ANNULEE ? 'row-annulee' : '';
  }

  // =============================================================
  // NAVIGATION
  // =============================================================

  retour(): void {
    this.router.navigate(['/back-office/clients']);
  }

  // =============================================================
  // DÉTAILS (lecture seule)
  // =============================================================

  voirDetails(reservation: Reservation): void {
    this.reservationEnDetails = reservation;
    this.cdr.detectChanges();
  }

  fermerDetails(): void {
    this.reservationEnDetails = null;
    this.cdr.detectChanges();
  }

  // =============================================================
  // CONFIRMATION D'UNE RESERVATION (table obligatoire)
  // =============================================================

  ouvrirConfirmation(reservation: Reservation): void {
    this.reservationAConfirmer = reservation;
    this.tableIdConfirmation = reservation.table?.id_table ?? null;
    this.errorConfirmation = '';
    this.tablesDisponiblesConfirmation = [];

    const restaurantId = reservation.restaurant?.id_restaurant;

    if (restaurantId) {
      this.loadingTablesConfirmation = true;

      this.tableService
        .getByRestaurant(restaurantId)
        .pipe(
          catchError((error) => {
            console.error('Erreur lors du chargement des tables :', error);
            return of([] as TableRestaurant[]);
          }),
        )
        .subscribe((tables) => {
          this.tablesDisponiblesConfirmation = tables;
          this.loadingTablesConfirmation = false;
          this.cdr.detectChanges();
        });
    }

    this.cdr.detectChanges();
  }

  fermerConfirmation(): void {
    this.reservationAConfirmer = null;
    this.tableIdConfirmation = null;
    this.tablesDisponiblesConfirmation = [];
    this.errorConfirmation = '';
    this.cdr.detectChanges();
  }

  validerConfirmation(): void {
    if (!this.reservationAConfirmer) {
      return;
    }

    if (!this.tableIdConfirmation) {
      this.errorConfirmation = 'Veuillez assigner une table pour confirmer cette réservation.';
      return;
    }

    this.processingConfirmation = true;
    this.errorConfirmation = '';

    const id = this.reservationAConfirmer.id_reservation;

    this.reservationService.confirmer(id, this.tableIdConfirmation).subscribe({
      next: (reservationMiseAJour) => {
        this.reservations = this.reservations.map((r) =>
          r.id_reservation === reservationMiseAJour.id_reservation ? reservationMiseAJour : r,
        );

        this.processingConfirmation = false;
        this.fermerConfirmation();
      },

      error: (error) => {
        console.error('Erreur lors de la confirmation de la réservation :', error);

        this.errorConfirmation =
          error?.error?.message ?? 'Impossible de confirmer cette réservation.';

        this.processingConfirmation = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =============================================================
  // ANNULATION D'UNE RESERVATION
  // =============================================================

  demanderAnnulation(reservation: Reservation): void {
    if (!this.canAnnuler()) {
      console.warn('Permission refusée : RESERVATIONS_ANNULER');
      return;
    }

    this.reservationAAnnuler = reservation;
    this.cdr.detectChanges();
  }

  fermerAnnulation(): void {
    this.reservationAAnnuler = null;
    this.cdr.detectChanges();
  }

  confirmerAnnulation(): void {
    if (!this.reservationAAnnuler) {
      return;
    }

    this.processingAnnulation = true;

    const id = this.reservationAAnnuler.id_reservation;

    this.reservationService.annuler(id).subscribe({
      next: (reservationMiseAJour) => {
        this.reservations = this.reservations.map((r) =>
          r.id_reservation === reservationMiseAJour.id_reservation ? reservationMiseAJour : r,
        );

        this.reservationAAnnuler = null;
        this.processingAnnulation = false;

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors de l’annulation de la réservation :', error);

        this.errorMessage = error?.error?.message ?? 'Impossible d’annuler cette réservation.';

        this.reservationAAnnuler = null;
        this.processingAnnulation = false;

        this.cdr.detectChanges();
      },
    });
  }

  // =============================================================
  // MODIFICATION D'UNE RESERVATION
  // =============================================================

  ouvrirEdition(reservation: Reservation): void {
    if (!this.canModifier()) {
      console.warn('Permission refusée : RESERVATIONS_MODIFIER');
      return;
    }

    this.reservationEnEdition = reservation;
    this.errorEdition = '';

    this.formEdition = {
      nombre_personnes: reservation.nombre_personnes,
      dateReservation: reservation.dateReservation,
      heureReservation: reservation.heureReservation,
      statut: reservation.statut,
      tableId: reservation.table?.id_table ?? null,
    };

    this.tablesDisponibles = [];

    const restaurantId = reservation.restaurant?.id_restaurant;

    if (restaurantId) {
      this.loadingTables = true;

      this.tableService
        .getByRestaurant(restaurantId)
        .pipe(
          catchError((error) => {
            console.error('Erreur lors du chargement des tables :', error);
            return of([] as TableRestaurant[]);
          }),
        )
        .subscribe((tables) => {
          this.tablesDisponibles = tables;
          this.loadingTables = false;
          this.cdr.detectChanges();
        });
    }

    this.cdr.detectChanges();
  }

  fermerEdition(): void {
    this.reservationEnEdition = null;
    this.formEdition = null;
    this.tablesDisponibles = [];
    this.errorEdition = '';
    this.cdr.detectChanges();
  }

  enregistrerEdition(): void {
    if (!this.reservationEnEdition || !this.formEdition) {
      return;
    }

    if (!this.formEdition.nombre_personnes || this.formEdition.nombre_personnes < 1) {
      this.errorEdition = 'Le nombre de personnes doit être supérieur à 0.';
      return;
    }

    if (!this.formEdition.dateReservation || !this.formEdition.heureReservation) {
      this.errorEdition = 'La date et l’heure de la réservation sont obligatoires.';
      return;
    }

    this.processingEdition = true;
    this.errorEdition = '';

    const id = this.reservationEnEdition.id_reservation;

    const tableChoisie = this.formEdition.tableId
      ? (this.tablesDisponibles.find((t) => t.id_table === this.formEdition!.tableId) ?? null)
      : null;

    const payload: Partial<Reservation> = {
      nombre_personnes: this.formEdition.nombre_personnes,
      dateReservation: this.formEdition.dateReservation,
      heureReservation: this.formEdition.heureReservation,
      statut: this.formEdition.statut,
    };

    if (tableChoisie) {
      payload.table = tableChoisie;
    }

    this.reservationService.update(id, payload as Reservation).subscribe({
      next: (reservationMiseAJour) => {
        this.reservations = this.reservations.map((r) =>
          r.id_reservation === reservationMiseAJour.id_reservation ? reservationMiseAJour : r,
        );

        this.processingEdition = false;
        this.fermerEdition();
      },

      error: (error) => {
        console.error('Erreur lors de la modification de la réservation :', error);

        this.errorEdition = error?.error?.message ?? 'Impossible d’enregistrer les modifications.';

        this.processingEdition = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =============================================================
  // CRÉATION D'UNE NOUVELLE RÉSERVATION
  // =============================================================

  ouvrirCreation(): void {
    if (!this.canCreer()) {
      console.warn('Permission refusée : RESERVATIONS_MODIFIER');
      return;
    }

    this.creationOuverte = true;
    this.errorCreation = '';

    this.formCreation = {
      clientId: null,
      nombre_personnes: 1,
      dateReservation: this.selectedDate || this.formatDateAujourdHui(),
      heureReservation: '',
      tableId: null,
      commentaire_client: '',
    };

    // Chargement des clients (pour le sélecteur).
    this.clientsDisponibles = [];
    this.loadingClients = true;

    this.clientService
      .getAll()
      .pipe(
        catchError((error) => {
          console.error('Erreur lors du chargement des clients :', error);
          return of([] as ClientAuthentifie[]);
        }),
      )
      .subscribe((clients) => {
        this.clientsDisponibles = Array.isArray(clients) ? clients : [];
        this.loadingClients = false;
        this.cdr.detectChanges();
      });

    // Chargement des tables du restaurant courant (pour l'assignation
    // optionnelle d'une table à la réservation).
    this.tablesDisponibles = [];
    const restaurantId = this.authService.getRestaurantId();

    if (restaurantId) {
      this.loadingTables = true;

      this.tableService
        .getByRestaurant(restaurantId)
        .pipe(
          catchError((error) => {
            console.error('Erreur lors du chargement des tables :', error);
            return of([] as TableRestaurant[]);
          }),
        )
        .subscribe((tables) => {
          this.tablesDisponibles = tables;
          this.loadingTables = false;
          this.cdr.detectChanges();
        });
    }

    this.cdr.detectChanges();
  }

  fermerCreation(): void {
    this.creationOuverte = false;
    this.errorCreation = '';
    this.tablesDisponibles = [];
    this.clientsDisponibles = [];
    this.cdr.detectChanges();
  }

  private formatDateAujourdHui(): string {
    const date = new Date();
    const annee = date.getFullYear();
    const mois = `${date.getMonth() + 1}`.padStart(2, '0');
    const jour = `${date.getDate()}`.padStart(2, '0');

    return `${annee}-${mois}-${jour}`;
  }

  enregistrerCreation(): void {
    if (!this.formCreation.clientId) {
      this.errorCreation = 'Veuillez sélectionner un client.';
      return;
    }

    if (!this.formCreation.nombre_personnes || this.formCreation.nombre_personnes < 1) {
      this.errorCreation = 'Le nombre de personnes doit être supérieur à 0.';
      return;
    }

    if (!this.formCreation.dateReservation || !this.formCreation.heureReservation) {
      this.errorCreation = 'La date et l’heure de la réservation sont obligatoires.';
      return;
    }

    const clientChoisi = this.clientsDisponibles.find(
      (c) => c.id_utilisateur === this.formCreation.clientId,
    );

    if (!clientChoisi) {
      this.errorCreation = 'Client introuvable, veuillez réessayer.';
      return;
    }

    const restaurantId = this.authService.getRestaurantId();
    const selectedRestaurant = this.authService.getSelectedRestaurant();

    const tableChoisie = this.formCreation.tableId
      ? (this.tablesDisponibles.find((t) => t.id_table === this.formCreation.tableId) ?? null)
      : null;

    const payload: Partial<Reservation> = {
      client: clientChoisi,
      nombre_personnes: this.formCreation.nombre_personnes,
      dateReservation: this.formCreation.dateReservation,
      heureReservation: this.formCreation.heureReservation,
      statut: StatutReservation.EN_ATTENTE,
      commentaire_client: this.formCreation.commentaire_client,
      restaurant: selectedRestaurant ?? ({ id_restaurant: restaurantId ?? undefined } as any),
    };

    if (tableChoisie) {
      payload.table = tableChoisie;
    }

    this.processingCreation = true;
    this.errorCreation = '';

    this.reservationService.create(payload as Reservation).subscribe({
      next: (nouvelleReservation) => {
        this.reservations = [nouvelleReservation, ...this.reservations];

        this.processingCreation = false;
        this.fermerCreation();
      },

      error: (error) => {
        console.error('Erreur lors de la création de la réservation :', error);

        this.errorCreation = error?.error?.message ?? 'Impossible de créer cette réservation.';

        this.processingCreation = false;
        this.cdr.detectChanges();
      },
    });
  }
}
