// src/app/features/back-office/employees/mes-tables/mes-tables-employe.component.ts
//
// BACK OFFICE — "Mes tables"
// ---------------------------------------------------------------
// Permet à un employé responsable de tables (permission
// EMPLOYES_TABLES, cf. rôle "Serveur") de retrouver, depuis le
// Back Office, les tables dont il est responsable — la même liste
// que sur la page "Mes tables" du PDV, accessible sans changer
// d'interface, avec en plus :
//
//   - un plan visuel en glisser-déposer (identique dans l'esprit à
//     "Attribution des tables"), pour organiser ses propres tables
//     comme elles sont disposées en salle ;
//   - un simple clic sur une table pour voir ses détails et changer
//     son état (Libre / Occupée / Réservée) directement, sans
//     repasser par le PDV.
//
// Les actions de prise en charge ("Prendre en charge" / "Client
// parti") restent réservées au PDV, qui est l'outil du service en
// salle : elles ne sont qu'affichées ici en lecture seule.
//
// Sécurité : la liste vient de TableRestaurantService.getMyTables(),
// qui appelle GET /api/tables/my-tables. Cet endpoint backend déduit
// l'employé du token JWT (email) : il n'y a donc, côté frontend,
// AUCUN employeeId envoyé au serveur — qu'on soit connecté au PDV ou
// au Back Office, on ne peut voir/modifier que ses propres tables.

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
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { TableRestaurant } from '../../../../core/models/table-restaurant.model';
import { StatutTable } from '../../../../core/models/enums/statut-table.enum';
import { Commande } from '../../../../core/models/commande.model';
import { StatutCommande } from '../../../../core/models/enums/statut-commande.enum';
import { LigneCommande } from '../../../../core/models/ligne-commande.model';
import { Reclamation } from '../../../../core/models/reclamation.model';
import { StatutReclamation } from '../../../../core/models/enums/statut-reclamation.enum';
import { PriseEnChargeTable } from '../../../../core/models/prise-en-charge-table.model';

import { TableRestaurantService } from '../../../../core/services/table-restaurant.service';
import { CommandeService } from '../../../../core/services/commande.service';
import { ReclamationService } from '../../../../core/services/reclamation.service';
import { PriseEnChargeTableService } from '../../../../core/services/prise-en-charge-table.service';
import { AuthService } from '../../../../core/services/auth.service';

type FiltreMesTables = 'TOUTES' | StatutTable;

// Une "vue" combine la table avec sa commande active, sa
// réclamation active et sa prise en charge active — identique à la
// version PDV, pour affichage uniquement (aucune action de service
// ici, seulement la consultation + le changement de statut).
interface MesTableVM {
  table: TableRestaurant;
  commandeActive: Commande | null;
  lignes: LigneCommande[];
  reclamationActive: Reclamation | null;
  priseEnChargeActive: PriseEnChargeTable | null;
}

// Dimensions utilisées pour le placement automatique et les limites
// de déplacement dans le plan visuel (mêmes valeurs que la page
// "Attribution des tables", pour une disposition cohérente).
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
  selector: 'app-mes-tables-employe',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mes-tables-employe.component.html',
  styleUrl: './mes-tables-employe.component.css',
})
export class MesTablesEmployeComponent implements OnInit, OnDestroy {
  @ViewChild('planCanvas') planCanvasRef?: ElementRef<HTMLDivElement>;

  vmTables: MesTableVM[] = [];

  loading = false;
  errorMessage = '';

  // -----------------------------------------------------------
  // FILTRES
  // -----------------------------------------------------------

  selectedFiltre: FiltreMesTables = 'TOUTES';

  filtres: { label: string; value: FiltreMesTables }[] = [
    { label: 'Toutes', value: 'TOUTES' },
    { label: 'Libres', value: StatutTable.LIBRE },
    { label: 'Occupées', value: StatutTable.OCCUPEE },
    { label: 'Réservées', value: StatutTable.RESERVEE },
  ];

  // -----------------------------------------------------------
  // DÉTAILS (modal, lecture seule)
  // -----------------------------------------------------------

  selectedVm: MesTableVM | null = null;

  // -----------------------------------------------------------
  // CHANGEMENT DE STATUT (dans la modale de détails, simple clic)
  // -----------------------------------------------------------

  nouveauStatut: StatutTable | null = null;
  statutSaving = false;
  statutError = '';

  // -----------------------------------------------------------
  // ACTION RAPIDE : TABLE VIDÉE
  // -----------------------------------------------------------
  // Raccourci directement sur la carte (sans passer par la modale) :
  // "Table vidée" fait repasser la table à LIBRE (à nouveau disponible
  // côté client). La prise de table, elle, est désormais automatique
  // dès que le client passe commande sur cette table (voir
  // ClientCommandeServiceImpl côté backend), donc plus besoin de bouton
  // "Confirmer la prise de table" ici. Réutilise le même endpoint que
  // le changement de statut de la modale (TableRestaurantService.
  // updateStatut).
  idTableActionRapideEnCours: number | null = null;
  actionRapideErrorMessage = '';

  statutsDisponibles: { label: string; value: StatutTable }[] = [
    { label: 'Libre', value: StatutTable.LIBRE },
    { label: 'Occupée', value: StatutTable.OCCUPEE },
    { label: 'Réservée', value: StatutTable.RESERVEE },
  ];

  // -----------------------------------------------------------
  // PLAN VISUEL — GLISSER / DÉPOSER
  // -----------------------------------------------------------

  private livePositions = new Map<number, PositionXY>();
  private defaultPositions = new Map<number, PositionXY>();

  draggingTableId: number | null = null;

  private dragState: DragState | null = null;
  private suppressNextClick = false;

  private readonly onPointerMove = (event: PointerEvent): void => this.handlePointerMove(event);
  private readonly onPointerUp = (event: PointerEvent): void => this.handlePointerUp(event);

  constructor(
    private readonly tableService: TableRestaurantService,
    private readonly commandeService: CommandeService,
    private readonly reclamationService: ReclamationService,
    private readonly priseEnChargeService: PriseEnChargeTableService,
    public readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // Le SUPERADMIN n'est pas un Employee : côté backend, cette page lui
  // affiche donc TOUTES les tables du restaurant sélectionné plutôt que
  // "ses" tables (voir TableServiceImpl.getTablesDuServeurConnecte). Le
  // libellé de la page s'adapte en conséquence (voir le template).
  get estSuperAdmin(): boolean {
    return this.authService.isSuperAdmin();
  }

  ngOnInit(): void {
    this.loadData();
  }

  ngOnDestroy(): void {
    this.detachDragListeners();
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.selectedVm = null;

    this.tableService.getMyTables().subscribe({
      next: (tables) => {
        if (!tables || tables.length === 0) {
          this.vmTables = [];
          this.loading = false;
          this.cdr.detectChanges();
          return;
        }

        this.chargerDetails(tables);
      },
      error: () => {
        this.vmTables = [];
        this.errorMessage = 'Impossible de récupérer vos tables. Veuillez réessayer.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // Pour chaque table, on va chercher (si elles existent) sa commande
  // active, sa réclamation active et sa prise en charge active. On ne
  // développe aucune nouvelle logique métier de lecture : uniquement
  // des appels aux services déjà présents dans le projet.
  private chargerDetails(tables: TableRestaurant[]): void {
    const requetes = tables.map((table) =>
      this.commandeService.getByTable(table.id_table).pipe(
        catchError(() => of([] as Commande[])),
        switchMap((commandes) => {
          const commandeActive = this.trouverCommandeActive(commandes);

          const priseEnCharge$ = this.priseEnChargeService
            .getActiveByTable(table.id_table)
            .pipe(catchError(() => of(null)));

          if (!commandeActive) {
            return priseEnCharge$.pipe(
              map((priseEnChargeActive): MesTableVM => ({
                table,
                commandeActive: null,
                lignes: [],
                reclamationActive: null,
                priseEnChargeActive,
              })),
            );
          }

          const lignes$ = this.commandeService
            .getLignes(commandeActive.id_commande)
            .pipe(catchError(() => of([] as LigneCommande[])));

          const reclamations$ = this.reclamationService
            .getByCommande(commandeActive)
            .pipe(catchError(() => of([] as Reclamation[])));

          return forkJoin([lignes$, reclamations$, priseEnCharge$]).pipe(
            map(([lignes, reclamations, priseEnChargeActive]): MesTableVM => ({
              table,
              commandeActive,
              lignes,
              reclamationActive: this.trouverReclamationActive(reclamations),
              priseEnChargeActive,
            })),
          );
        }),
      ),
    );

    forkJoin(requetes).subscribe({
      next: (resultats) => {
        this.vmTables = resultats;
        this.recalculerPositionsParDefaut();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        // Les tables ont pu être récupérées mais pas leurs détails :
        // on affiche quand même les tables plutôt que de tout bloquer.
        this.vmTables = tables.map((table) => ({
          table,
          commandeActive: null,
          lignes: [],
          reclamationActive: null,
          priseEnChargeActive: null,
        }));
        this.recalculerPositionsParDefaut();
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  private trouverCommandeActive(commandes: Commande[]): Commande | null {
    if (!commandes || commandes.length === 0) {
      return null;
    }

    const actives = commandes.filter(
      (commande) =>
        commande.statut === StatutCommande.EN_ATTENTE || commande.statut === StatutCommande.SERVIE,
    );

    if (actives.length === 0) {
      return null;
    }

    return actives.sort(
      (a, b) => new Date(b.dateCommande).getTime() - new Date(a.dateCommande).getTime(),
    )[0];
  }

  private trouverReclamationActive(reclamations: Reclamation[]): Reclamation | null {
    if (!reclamations || reclamations.length === 0) {
      return null;
    }

    const nonResolues = reclamations.filter(
      (reclamation) => reclamation.statut !== StatutReclamation.RESOLUE,
    );

    return nonResolues.length > 0 ? nonResolues[0] : null;
  }

  // =============================================================
  // FILTRES / STATISTIQUES
  // =============================================================

  selectionnerFiltre(filtre: FiltreMesTables): void {
    this.selectedFiltre = filtre;
    // Forcer explicitement le rafraîchissement de la liste filtrée
    // sans attendre un éventuel prochain cycle de détection de
    // changement (ex. sur mobile / événements hors zone Angular).
    this.cdr.detectChanges();
  }

  get tablesFiltrees(): MesTableVM[] {
    if (this.selectedFiltre === 'TOUTES') {
      return this.vmTables;
    }

    return this.vmTables.filter((vm) => vm.table.statut === this.selectedFiltre);
  }

  // L'employé connecté est nécessairement le "serveurAttribue" de
  // chacune de ses tables (c'est ce que garantit l'endpoint backend
  // /api/tables/my-tables) : pas besoin d'un appel supplémentaire
  // pour afficher son nom dans l'en-tête.
  get nomEmployeConnecte(): string {
    return this.nomComplet(this.vmTables[0]?.table.serveurAttribue);
  }

  get totalTables(): number {
    return this.vmTables.length;
  }

  get totalLibres(): number {
    return this.vmTables.filter((vm) => vm.table.statut === StatutTable.LIBRE).length;
  }

  get totalOccupees(): number {
    return this.vmTables.filter((vm) => vm.table.statut === StatutTable.OCCUPEE).length;
  }

  get totalReservees(): number {
    return this.vmTables.filter((vm) => vm.table.statut === StatutTable.RESERVEE).length;
  }

  // =============================================================
  // DÉTAILS + CHANGEMENT DE STATUT (une seule modale, simple clic)
  // =============================================================

  ouvrirDetails(vm: MesTableVM): void {
    this.selectedVm = vm;
    this.nouveauStatut = vm.table.statut;
    this.statutError = '';
    // Forcer le rendu immédiat de la modale : ne pas dépendre d'un
    // futur cycle de détection de changement qui peut ne pas être
    // planifié tout de suite après un clic sur la carte de table.
    this.cdr.detectChanges();
  }

  fermerDetails(): void {
    if (this.statutSaving) {
      return;
    }

    this.selectedVm = null;
    this.nouveauStatut = null;
    this.statutError = '';
    this.cdr.detectChanges();
  }

  confirmerChangerStatut(): void {
    if (!this.selectedVm || !this.nouveauStatut) {
      return;
    }

    const vm = this.selectedVm;
    const tableId = vm.table.id_table;

    this.statutSaving = true;
    this.statutError = '';

    this.tableService
      .updateStatut(tableId, this.nouveauStatut)
      .pipe(
        catchError((error) => {
          console.error('Erreur lors du changement de statut :', error);
          return of(null);
        }),
      )
      .subscribe((updatedTable) => {
        this.statutSaving = false;

        if (!updatedTable) {
          this.statutError = 'Impossible de modifier le statut de cette table. Veuillez réessayer.';
          this.cdr.detectChanges();
          return;
        }

        vm.table = updatedTable;
        this.fermerDetails();
        this.cdr.detectChanges();
      });
  }

  // =============================================================
  // ACTION RAPIDE : TABLE VIDÉE
  // =============================================================
  // La prise de table est désormais confirmée automatiquement par le
  // backend dès que le client passe commande sur cette table (voir
  // ClientCommandeServiceImpl.creerCommandeDepuisPanier, qui passe la
  // table à OCCUPÉE) : il n'y a donc plus de bouton "Confirmer la prise
  // de table" ici. Seule reste l'action de libération, une fois le
  // client parti, pour que la table redevienne disponible.

  // event.stopPropagation() est indispensable ici : sans ça, le clic
  // sur le bouton remonte jusqu'au (click) de la carte et ouvre en plus
  // la modale de détails.
  marquerTableVide(vm: MesTableVM, event: Event): void {
    event.stopPropagation();

    if (this.idTableActionRapideEnCours !== null || vm.table.statut !== StatutTable.OCCUPEE) {
      return;
    }

    this.changerStatutRapide(vm, StatutTable.LIBRE);
  }

  private changerStatutRapide(vm: MesTableVM, statut: StatutTable): void {
    const tableId = vm.table.id_table;

    this.actionRapideErrorMessage = '';
    this.idTableActionRapideEnCours = tableId;

    this.tableService
      .updateStatut(tableId, statut)
      .pipe(
        catchError((error) => {
          console.error('Erreur lors du changement de statut de la table :', error);
          return of(null);
        }),
      )
      .subscribe((updatedTable) => {
        this.idTableActionRapideEnCours = null;

        if (!updatedTable) {
          this.actionRapideErrorMessage =
            "Impossible de mettre à jour le statut de cette table. Veuillez réessayer.";
          this.cdr.detectChanges();
          return;
        }

        vm.table = updatedTable;
        this.cdr.detectChanges();
      });
  }

  // =============================================================
  // AFFICHAGE
  // =============================================================

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

  statutCommandeLabel(statut: StatutCommande): string {
    switch (statut) {
      case StatutCommande.EN_ATTENTE:
        return 'En attente';
      case StatutCommande.SERVIE:
        return 'Servie';
      case StatutCommande.ANNULEE:
        return 'Annulée';
      case StatutCommande.PAYEE:
        return 'Payée';
      default:
        return statut;
    }
  }

  nomComplet(employe: { nom?: string; prenom?: string } | null | undefined): string {
    if (!employe) {
      return '—';
    }

    return `${employe.prenom ?? ''} ${employe.nom ?? ''}`.trim() || '—';
  }

  // Formatte une date ISO en heure locale "HH:mm" (ex. "20:15") pour
  // l'affichage "Depuis 20:15" / "Début : 20:15".
  heureFormat(date: string | null | undefined): string {
    if (!date) {
      return '—';
    }

    const d = new Date(date);

    if (isNaN(d.getTime())) {
      return '—';
    }

    return d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  // =============================================================
  // PLAN VISUEL — POSITIONNEMENT
  // =============================================================

  private recalculerPositionsParDefaut(): void {
    this.defaultPositions.clear();

    let index = 0;

    for (const vm of this.vmTables) {
      if (vm.table.positionX == null || vm.table.positionY == null) {
        this.defaultPositions.set(vm.table.id_table, this.calculerPositionParDefaut(index));
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
  // PLAN VISUEL — GLISSER / DÉPOSER
  // =============================================================

  onCardPointerDown(event: PointerEvent, vm: MesTableVM): void {
    if (event.button !== 0) {
      return;
    }

    // Pas de event.preventDefault() ici : sur Chrome/Edge cela
    // supprimerait le `click`/`dblclick` qui suit (voir le correctif
    // apporté à la page "Attribution des tables"). La sélection de
    // texte pendant le drag est déjà bloquée par CSS (user-select:
    // none, touch-action: none sur .table-card).

    const origin = this.getPosition(vm.table);

    this.dragState = {
      tableId: vm.table.id_table,
      startClientX: event.clientX,
      startClientY: event.clientY,
      originX: origin.x,
      originY: origin.y,
      moved: false,
    };

    this.draggingTableId = vm.table.id_table;

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

    // Un vrai déplacement a eu lieu : on empêche le prochain click
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

        const vm = this.vmTables.find((item) => item.table.id_table === tableId);

        if (vm) {
          vm.table = updatedTable;
        }

        this.livePositions.delete(tableId);
        this.cdr.detectChanges();
      });
  }

  onCardClick(vm: MesTableVM): void {
    if (this.suppressNextClick) {
      this.suppressNextClick = false;
      return;
    }

    this.ouvrirDetails(vm);
  }

  private clamp(value: number, min: number, max: number): number {
    return Math.min(Math.max(value, min), max);
  }
}
