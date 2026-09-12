import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';

import { TableRestaurant } from '../../../../core/models/table-restaurant.model';
import { Employee } from '../../../../core/models/employee.model';
import { StatutTable } from '../../../../core/models/enums/statut-table.enum';

import { TableRestaurantService } from '../../../../core/services/table-restaurant.service';
import { AuthService } from '../../../../core/services/auth.service';

type FiltreTable = 'TOUTES' | StatutTable | 'NON_ATTRIBUEE';

// Dimensions utilisées pour le placement automatique et les limites
// de déplacement dans le plan visuel du restaurant (phase 2B).
const CARD_WIDTH = 190;
const CARD_HEIGHT = 148;
const PLAN_PADDING = 24;
const PLAN_COLUMNS = 4;

// Distance (en px) à partir de laquelle un pointerdown suivi d'un
// mouvement est considéré comme un glissement plutôt qu'un simple
// clic. Une valeur trop basse (ex. 4px) fait basculer en "glissement"
// le moindre tremblement de souris/trackpad pendant un clic, ce qui
// annule l'ouverture de la modale et donne l'impression qu'il faut
// cliquer deux fois.
const DRAG_THRESHOLD = 10;

interface PositionXY {
  x: number;
  y: number;
}

interface DragState {
  tableId: number;
  startClientX: number;
  startClientY: number;
  originX: number;
  originY: number;
  moved: boolean;
}

@Component({
  selector: 'app-table-attribution',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './table-attribution.component.html',
  styleUrl: './table-attribution.component.css',
})
export class TableAttributionComponent implements OnInit, OnDestroy {
  @ViewChild('planCanvas') planCanvasRef?: ElementRef<HTMLDivElement>;

  tables: TableRestaurant[] = [];
  employes: Employee[] = [];

  loading = false;
  errorMessage = '';
  successMessage = '';

  // -----------------------------------------------------------
  // FILTRES
  // -----------------------------------------------------------

  selectedFiltre: FiltreTable = 'TOUTES';

  filtres: { label: string; value: FiltreTable }[] = [
    { label: 'Toutes', value: 'TOUTES' },
    { label: 'Libres', value: StatutTable.LIBRE },
    { label: 'Occupées', value: StatutTable.OCCUPEE },
    { label: 'Réservées', value: StatutTable.RESERVEE },
    { label: 'Non attribuées', value: 'NON_ATTRIBUEE' },
  ];

  // -----------------------------------------------------------
  // MODAL / SÉLECTION D'UNE TABLE
  // -----------------------------------------------------------

  selectedTable: TableRestaurant | null = null;
  selectedEmployeeId: number | null = null;

  saving = false;
  modalError = '';

  // Suppression d'une table (depuis les détails)
  confirmingSuppression = false;
  deleting = false;

  // -----------------------------------------------------------
  // AJOUT D'UNE TABLE
  // -----------------------------------------------------------

  showAddModal = false;
  addSaving = false;
  addError = '';

  newTableNumero: number | null = null;
  newTableCapacite: number | null = null;
  newTableEmployeeId: number | null = null;

  // -----------------------------------------------------------
  // PLAN VISUEL DU RESTAURANT (phase 2B)
  // -----------------------------------------------------------

  // Positions "en direct" pendant un déplacement, avant sauvegarde
  // côté backend (clé = id_table).
  private livePositions = new Map<number, PositionXY>();

  // Positions par défaut calculées pour les tables qui n'ont jamais
  // été positionnées (positionX/positionY = null), afin qu'elles
  // n'apparaissent pas toutes superposées au même endroit.
  private defaultPositions = new Map<number, PositionXY>();

  draggingTableId: number | null = null;

  private dragState: DragState | null = null;
  private suppressNextClick = false;

  private readonly onPointerMove = (event: PointerEvent): void => this.handlePointerMove(event);
  private readonly onPointerUp = (event: PointerEvent): void => this.handlePointerUp(event);

  private successTimeoutId: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private readonly tableService: TableRestaurantService,
    public readonly authService: AuthService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    this.loadData();
  }

  ngOnDestroy(): void {
    this.detachDragListeners();

    if (this.successTimeoutId) {
      clearTimeout(this.successTimeoutId);
    }
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      tables: this.tableService.getAll(),
      employes: this.tableService.getEmployesEligibles(),
    })
      .pipe(
        // Filet de sécurité : si le backend ne répond jamais
        // (serveur arrêté, CORS bloqué, etc.), on ne reste pas
        // bloqué indéfiniment sur "Chargement...".
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors du chargement des tables :', error);

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
              `Impossible de charger les tables (${error?.status ?? 'erreur inconnue'}).`;
          }

          return of({ tables: [] as TableRestaurant[], employes: [] as Employee[] });
        }),
      )
      .subscribe(({ tables, employes }) => {
        this.tables = Array.isArray(tables) ? tables : [];
        this.employes = Array.isArray(employes) ? employes : [];
        this.recalculerPositionsParDefaut();
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  // =============================================================
  // FILTRAGE
  // =============================================================

  selectionnerFiltre(filtre: FiltreTable): void {
    this.selectedFiltre = filtre;
    // Forcer explicitement le rafraîchissement de la liste filtrée
    // sans attendre un éventuel prochain cycle de détection de
    // changement (ex. sur mobile / événements hors zone Angular).
    this.cdr.detectChanges();
  }

  get tablesFiltrees(): TableRestaurant[] {
    switch (this.selectedFiltre) {
      case 'TOUTES':
        return this.tables;

      case 'NON_ATTRIBUEE':
        return this.tables.filter((table) => !table.serveurAttribue);

      default:
        return this.tables.filter((table) => table.statut === this.selectedFiltre);
    }
  }

  // =============================================================
  // STATISTIQUES
  // =============================================================

  get totalTables(): number {
    return this.tables.length;
  }

  get totalAttribuees(): number {
    return this.tables.filter((table) => !!table.serveurAttribue).length;
  }

  get totalNonAttribuees(): number {
    return this.totalTables - this.totalAttribuees;
  }

  get statsParEmploye(): { nom: string; count: number }[] {
    const compteurs = new Map<number, { nom: string; count: number }>();

    for (const table of this.tables) {
      const employe = table.serveurAttribue;

      if (!employe) {
        continue;
      }

      const existant = compteurs.get(employe.id_utilisateur);

      if (existant) {
        existant.count += 1;
      } else {
        compteurs.set(employe.id_utilisateur, {
          nom: this.nomComplet(employe),
          count: 1,
        });
      }
    }

    return Array.from(compteurs.values()).sort((a, b) => b.count - a.count);
  }

  // =============================================================
  // AFFICHAGE
  // =============================================================

  nomComplet(employe: Employee): string {
    return `${employe.prenom ?? ''} ${employe.nom ?? ''}`.trim() || '—';
  }

  statutLabel(statut: StatutTable): string {
    switch (statut) {
      case StatutTable.LIBRE:
        return 'LIBRE';
      case StatutTable.OCCUPEE:
        return 'OCCUPÉE';
      case StatutTable.RESERVEE:
        return 'RÉSERVÉE';
      default:
        return statut;
    }
  }

  statutClass(statut: StatutTable): string {
    switch (statut) {
      case StatutTable.LIBRE:
        return 'libre';
      case StatutTable.OCCUPEE:
        return 'occupee';
      case StatutTable.RESERVEE:
        return 'reservee';
      default:
        return '';
    }
  }

  // =============================================================
  // MODAL / DÉTAILS D'UNE TABLE
  // =============================================================

  ouvrirDetails(table: TableRestaurant): void {
    this.selectedTable = table;
    this.selectedEmployeeId = table.serveurAttribue?.id_utilisateur ?? null;
    this.modalError = '';
    this.confirmingSuppression = false;
    // Forcer le rendu immédiat de la modale : ne pas dépendre d'un
    // futur cycle de détection de changement qui peut ne pas être
    // planifié tout de suite après un clic sur la carte de table.
    this.cdr.detectChanges();
  }

  fermerModal(): void {
    if (this.saving || this.deleting) {
      return;
    }

    this.selectedTable = null;
    this.selectedEmployeeId = null;
    this.modalError = '';
    this.confirmingSuppression = false;
    this.cdr.detectChanges();
  }

  // =============================================================
  // AFFECTATION
  // =============================================================

  enregistrerAffectation(): void {
    if (!this.selectedTable || !this.selectedEmployeeId) {
      return;
    }

    const tableId = this.selectedTable.id_table;

    this.saving = true;
    this.modalError = '';

    this.tableService
      .assignerServeur(tableId, this.selectedEmployeeId)
      .pipe(
        timeout(15000),
        catchError((error) => {
          console.error("Erreur lors de l'attribution de la table :", error);
          return of(null);
        }),
      )
      .subscribe((updatedTable) => {
        this.saving = false;

        if (!updatedTable) {
          this.modalError = "Impossible de modifier l'affectation de la table. Veuillez réessayer.";
          this.cdr.detectChanges();
          return;
        }

        this.remplacerTable(updatedTable);
        this.afficherSucces(
          `${updatedTable.serveurAttribue?.prenom ?? 'L\u2019employé'} est maintenant responsable de la table ${updatedTable.numeroTable}.`,
        );
        this.fermerModal();
        this.cdr.detectChanges();
      });
  }

  retirerAffectation(): void {
    if (!this.selectedTable) {
      return;
    }

    const tableId = this.selectedTable.id_table;
    const numeroTable = this.selectedTable.numeroTable;

    this.saving = true;
    this.modalError = '';

    this.tableService
      .retirerServeur(tableId)
      .pipe(
        timeout(15000),
        catchError((error) => {
          console.error("Erreur lors du retrait de l'affectation :", error);
          return of(null);
        }),
      )
      .subscribe((updatedTable) => {
        this.saving = false;

        if (!updatedTable) {
          this.modalError = "Impossible de modifier l'affectation de la table. Veuillez réessayer.";
          this.cdr.detectChanges();
          return;
        }

        this.remplacerTable(updatedTable);
        this.afficherSucces(`La table ${numeroTable} n'a plus d'employé responsable.`);
        this.fermerModal();
        this.cdr.detectChanges();
      });
  }

  private remplacerTable(updatedTable: TableRestaurant): void {
    this.tables = this.tables.map((table) =>
      table.id_table === updatedTable.id_table ? updatedTable : table,
    );
  }

  // =============================================================
  // SUPPRESSION D'UNE TABLE
  // =============================================================

  demanderConfirmationSuppression(): void {
    this.confirmingSuppression = true;
    this.cdr.detectChanges();
  }

  annulerSuppression(): void {
    this.confirmingSuppression = false;
    this.cdr.detectChanges();
  }

  confirmerSuppression(): void {
    if (!this.selectedTable) {
      return;
    }

    const tableId = this.selectedTable.id_table;
    const numeroTable = this.selectedTable.numeroTable;

    this.deleting = true;
    this.modalError = '';

    this.tableService
      .delete(tableId)
      .pipe(
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors de la suppression de la table :', error);
          return of('ERROR' as const);
        }),
      )
      .subscribe((result) => {
        this.deleting = false;

        if (result === 'ERROR') {
          this.modalError =
            'Impossible de supprimer cette table. Elle est peut-être encore ' +
            'liée à des commandes ou réservations existantes.';
          this.cdr.detectChanges();
          return;
        }

        this.tables = this.tables.filter((table) => table.id_table !== tableId);
        this.livePositions.delete(tableId);
        this.defaultPositions.delete(tableId);
        this.recalculerPositionsParDefaut();
        this.afficherSucces(`La table ${numeroTable} a été supprimée.`);
        this.fermerModal();
        this.cdr.detectChanges();
      });
  }

  private afficherSucces(message: string): void {
    this.successMessage = message;

    if (this.successTimeoutId) {
      clearTimeout(this.successTimeoutId);
    }

    this.successTimeoutId = setTimeout(() => {
      this.successMessage = '';
      this.cdr.detectChanges();
    }, 4000);
  }

  // =============================================================
  // AJOUT D'UNE TABLE
  // =============================================================

  ouvrirAjoutTable(): void {
    this.newTableNumero = this.prochainNumeroTableSuggere();
    this.newTableCapacite = null;
    this.newTableEmployeeId = null;
    this.addError = '';
    this.showAddModal = true;
    this.cdr.detectChanges();
  }

  fermerAjoutTable(): void {
    if (this.addSaving) {
      return;
    }

    this.showAddModal = false;
    this.addError = '';
    this.cdr.detectChanges();
  }

  private prochainNumeroTableSuggere(): number | null {
    if (this.tables.length === 0) {
      return 1;
    }

    const maxNumero = Math.max(...this.tables.map((table) => table.numeroTable ?? 0));
    return maxNumero + 1;
  }

  confirmerAjoutTable(): void {
    if (!this.newTableNumero || !this.newTableCapacite) {
      this.addError = 'Le numéro de table et la capacité sont obligatoires.';
      return;
    }

    const restaurant = this.authService.getSelectedRestaurant();

    if (!restaurant?.id_restaurant) {
      this.addError = 'Aucun restaurant sélectionné.';
      return;
    }

    const employeChoisi = this.newTableEmployeeId
      ? this.employes.find((employe) => employe.id_utilisateur === this.newTableEmployeeId)
      : undefined;

    const position = this.calculerPositionParDefaut(this.tables.length);

    const payload: Partial<TableRestaurant> = {
      numeroTable: this.newTableNumero,
      capacite: this.newTableCapacite,
      statut: StatutTable.LIBRE,
      restaurant,
      serveurAttribue: employeChoisi,
      positionX: position.x,
      positionY: position.y,
    };

    this.addSaving = true;
    this.addError = '';

    this.tableService
      .create(payload)
      .pipe(
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors de la création de la table :', error);
          return of(null);
        }),
      )
      .subscribe((createdTable) => {
        this.addSaving = false;

        if (!createdTable) {
          this.addError = 'Impossible de créer la table. Veuillez réessayer.';
          this.cdr.detectChanges();
          return;
        }

        this.tables = [...this.tables, createdTable];
        this.showAddModal = false;
        this.afficherSucces(`La table ${createdTable.numeroTable} a été ajoutée.`);
        this.cdr.detectChanges();
      });
  }

  // =============================================================
  // PLAN VISUEL DU RESTAURANT — POSITIONNEMENT
  // =============================================================

  private recalculerPositionsParDefaut(): void {
    this.defaultPositions.clear();

    let index = 0;

    for (const table of this.tables) {
      if (table.positionX == null || table.positionY == null) {
        this.defaultPositions.set(table.id_table, this.calculerPositionParDefaut(index));
      }

      index += 1;
    }
  }

  private calculerPositionParDefaut(index: number): PositionXY {
    const col = index % PLAN_COLUMNS;
    const row = Math.floor(index / PLAN_COLUMNS);

    return {
      x: PLAN_PADDING + col * (CARD_WIDTH + PLAN_PADDING),
      y: PLAN_PADDING + row * (CARD_HEIGHT + PLAN_PADDING),
    };
  }

  getPosition(table: TableRestaurant): PositionXY {
    const live = this.livePositions.get(table.id_table);

    if (live) {
      return live;
    }

    if (table.positionX != null && table.positionY != null) {
      return { x: table.positionX, y: table.positionY };
    }

    return this.defaultPositions.get(table.id_table) ?? { x: PLAN_PADDING, y: PLAN_PADDING };
  }

  get planHeight(): number {
    const rows = Math.ceil(Math.max(this.tablesFiltrees.length, 1) / PLAN_COLUMNS);
    return rows * (CARD_HEIGHT + PLAN_PADDING) + PLAN_PADDING;
  }

  // =============================================================
  // PLAN VISUEL DU RESTAURANT — DÉPLACEMENT (DRAG & DROP)
  // =============================================================

  onCardPointerDown(event: PointerEvent, table: TableRestaurant): void {
    if (event.button !== 0) {
      return;
    }

    // IMPORTANT : ne PAS appeler event.preventDefault() ici.
    // Sur Chrome/Edge, appeler preventDefault() sur un pointerdown
    // supprime l'événement `click` compatible qui suit — ce qui
    // empêchait (click)="onCardClick(table)" de se déclencher, et
    // donc la modale de détails de s'ouvrir. La sélection de texte
    // pendant le drag est déjà bloquée par CSS (user-select: none,
    // touch-action: none sur .table-card), donc preventDefault()
    // n'était de toute façon pas nécessaire ici.

    const origin = this.getPosition(table);

    this.dragState = {
      tableId: table.id_table,
      startClientX: event.clientX,
      startClientY: event.clientY,
      originX: origin.x,
      originY: origin.y,
      moved: false,
    };

    this.draggingTableId = table.id_table;

    document.addEventListener('pointermove', this.onPointerMove);
    document.addEventListener('pointerup', this.onPointerUp);
    document.addEventListener('pointercancel', this.onPointerUp);
  }

  private handlePointerMove(event: PointerEvent): void {
    if (!this.dragState) {
      return;
    }

    const dx = event.clientX - this.dragState.startClientX;
    const dy = event.clientY - this.dragState.startClientY;

    if (!this.dragState.moved && (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD)) {
      this.dragState.moved = true;
    }

    if (!this.dragState.moved) {
      return;
    }

    const bounds = this.planCanvasRef?.nativeElement.getBoundingClientRect();
    const maxX = bounds ? Math.max(bounds.width - CARD_WIDTH, 0) : Number.MAX_SAFE_INTEGER;
    const maxY = bounds ? Math.max(bounds.height - CARD_HEIGHT, 0) : Number.MAX_SAFE_INTEGER;

    const newX = this.clamp(this.dragState.originX + dx, 0, maxX);
    const newY = this.clamp(this.dragState.originY + dy, 0, maxY);

    this.livePositions.set(this.dragState.tableId, { x: newX, y: newY });
    this.cdr.detectChanges();
  }

  private handlePointerUp(_event?: PointerEvent): void {
    this.detachDragListeners();

    if (!this.dragState) {
      this.draggingTableId = null;
      return;
    }

    const { tableId, moved } = this.dragState;
    this.draggingTableId = null;
    this.dragState = null;

    if (!moved) {
      return;
    }

    // Un vrai déplacement a eu lieu : on empêche le prochain (click)
    // d'ouvrir la modale de détails.
    this.suppressNextClick = true;

    const position = this.livePositions.get(tableId);

    if (position) {
      this.sauvegarderPosition(tableId, position.x, position.y);
    }
  }

  private detachDragListeners(): void {
    document.removeEventListener('pointermove', this.onPointerMove);
    document.removeEventListener('pointerup', this.onPointerUp);
    document.removeEventListener('pointercancel', this.onPointerUp);
  }

  private sauvegarderPosition(tableId: number, x: number, y: number): void {
    this.tableService
      .updatePosition(tableId, x, y)
      .pipe(
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors de la sauvegarde de la position :', error);
          return of(null);
        }),
      )
      .subscribe((updatedTable) => {
        if (!updatedTable) {
          // La sauvegarde a échoué : on annule le déplacement visuel
          // pour ne pas laisser croire que la position a été retenue.
          this.livePositions.delete(tableId);
          this.errorMessage = "La position de la table n'a pas pu être sauvegardée. Réessayez.";
          this.cdr.detectChanges();
          return;
        }

        this.remplacerTable(updatedTable);
        this.livePositions.delete(tableId);
        this.cdr.detectChanges();
      });
  }

  onCardClick(table: TableRestaurant): void {
    if (this.suppressNextClick) {
      this.suppressNextClick = false;
      return;
    }

    this.ouvrirDetails(table);
  }

  private clamp(value: number, min: number, max: number): number {
    return Math.min(Math.max(value, min), max);
  }

  // =============================================================
  // NAVIGATION
  // =============================================================

  retour(): void {
    this.router.navigate(['/back-office/employees']);
  }
}
