// src/app/features/pdv/pages/tables/mes-tables/mes-tables.component.ts
//
// PHASE 3A — "Mes tables" (consultation, inchangée)
// PHASE 3B — cycle de PRISE EN CHARGE d'une table (nouveau)
// ---------------------------------------------------------------
// Interface employé permettant de consulter les tables dont il est
// responsable (serveurAttribue, affectation PERMANENTE — 3A) et de
// gérer la prise en charge TEMPORAIRE du client actuellement installé
// (3B) : démarrage ("Prendre en charge"), fin ("Client parti").
//
// Sécurité : la liste vient de TableRestaurantService.getMyTables(),
// qui appelle GET /api/tables/my-tables. Cet endpoint backend déduit
// l'employé du token JWT (email) : il n'y a donc, côté frontend,
// AUCUN employeeId envoyé au serveur. Il en va de même pour démarrer/
// terminer une prise en charge (PriseEnChargeTableService) : aucun
// employeeId n'est envoyé, le backend déduit l'employé du token JWT.

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { TableRestaurant } from '../../../../../core/models/table-restaurant.model';
import { StatutTable } from '../../../../../core/models/enums/statut-table.enum';
import { Commande } from '../../../../../core/models/commande.model';
import { StatutCommande } from '../../../../../core/models/enums/statut-commande.enum';
import { LigneCommande } from '../../../../../core/models/ligne-commande.model';
import { Reclamation } from '../../../../../core/models/reclamation.model';
import { StatutReclamation } from '../../../../../core/models/enums/statut-reclamation.enum';
import { PriseEnChargeTable } from '../../../../../core/models/prise-en-charge-table.model';

import { TableRestaurantService } from '../../../../../core/services/table-restaurant.service';
import { CommandeService } from '../../../../../core/services/commande.service';
import { ReclamationService } from '../../../../../core/services/reclamation.service';
import { PriseEnChargeTableService } from '../../../../../core/services/prise-en-charge-table.service';

type FiltreMesTables = 'TOUTES' | StatutTable;

// Une "vue" combine la table avec, si elles existent déjà dans le
// projet, sa commande active, sa réclamation active (3A) et sa prise
// en charge active (3B). Rien de tout cela n'est inventé : on
// réutilise les services déjà présents / créés phase par phase.
interface MesTableVM {
  table: TableRestaurant;
  commandeActive: Commande | null;
  lignes: LigneCommande[];
  reclamationActive: Reclamation | null;
  priseEnChargeActive: PriseEnChargeTable | null;
}

@Component({
  selector: 'app-mes-tables',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mes-tables.component.html',
  styleUrl: './mes-tables.component.css',
})
export class MesTablesComponent implements OnInit {
  vmTables: MesTableVM[] = [];

  loading = false;
  errorMessage = '';

  // -----------------------------------------------------------
  // FILTRES (uniquement sur les tables déjà chargées, donc
  // uniquement celles de l'employé connecté)
  // -----------------------------------------------------------

  selectedFiltre: FiltreMesTables = 'TOUTES';

  filtres: { label: string; value: FiltreMesTables }[] = [
    { label: 'Toutes', value: 'TOUTES' },
    { label: 'Libres', value: StatutTable.LIBRE },
    { label: 'Occupées', value: StatutTable.OCCUPEE },
    { label: 'Réservées', value: StatutTable.RESERVEE },
  ];

  // -----------------------------------------------------------
  // DÉTAILS (modal)
  // -----------------------------------------------------------

  selectedVm: MesTableVM | null = null;

  // -----------------------------------------------------------
  // PRISE EN CHARGE (phase 3B)
  // -----------------------------------------------------------

  // Table pour laquelle une action (prendre en charge / client parti)
  // est en cours d'exécution : désactive le bouton correspondant pour
  // éviter un double-clic (ex. double démarrage refusé côté backend).
  actionEnCoursTableId: number | null = null;

  // Table pour laquelle la confirmation "Client parti" est affichée.
  tableAConfirmer: MesTableVM | null = null;

  actionErrorMessage = '';

  constructor(
    private readonly tableService: TableRestaurantService,
    private readonly commandeService: CommandeService,
    private readonly reclamationService: ReclamationService,
    private readonly priseEnChargeService: PriseEnChargeTableService,
  ) {}

  ngOnInit(): void {
    this.loadData();
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
          return;
        }

        this.chargerDetails(tables);
      },
      error: () => {
        this.vmTables = [];
        this.errorMessage = 'Impossible de récupérer vos tables. Veuillez réessayer.';
        this.loading = false;
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
              map(
                (priseEnChargeActive): MesTableVM => ({
                  table,
                  commandeActive: null,
                  lignes: [],
                  reclamationActive: null,
                  priseEnChargeActive,
                }),
              ),
            );
          }

          const lignes$ = this.commandeService
            .getLignes(commandeActive.id_commande)
            .pipe(catchError(() => of([] as LigneCommande[])));

          const reclamations$ = this.reclamationService
            .getByCommande(commandeActive)
            .pipe(catchError(() => of([] as Reclamation[])));

          return forkJoin([lignes$, reclamations$, priseEnCharge$]).pipe(
            map(
              ([lignes, reclamations, priseEnChargeActive]): MesTableVM => ({
                table,
                commandeActive,
                lignes,
                reclamationActive: this.trouverReclamationActive(reclamations),
                priseEnChargeActive,
              }),
            ),
          );
        }),
      ),
    );

    forkJoin(requetes).subscribe({
      next: (resultats) => {
        this.vmTables = resultats;
        this.loading = false;
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
        this.loading = false;
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
  // PRISE EN CHARGE (phase 3B)
  // =============================================================

  // "Prendre en charge" : uniquement proposé pour une table sans
  // prise en charge active. Le backend revérifie de toute façon que
  // l'employé connecté est bien le responsable permanent de la table
  // et qu'aucune prise en charge active n'existe déjà.
  prendreEnCharge(vm: MesTableVM): void {
    if (this.actionEnCoursTableId !== null) {
      return;
    }

    this.actionErrorMessage = '';
    this.actionEnCoursTableId = vm.table.id_table;

    this.priseEnChargeService.commencer(vm.table.id_table).subscribe({
      next: (priseEnCharge) => {
        vm.priseEnChargeActive = priseEnCharge;
        vm.table.statut = StatutTable.OCCUPEE;
        this.actionEnCoursTableId = null;
      },
      error: (err) => {
        this.actionErrorMessage =
          err?.error?.message ??
          'Impossible de démarrer la prise en charge de cette table.';
        this.actionEnCoursTableId = null;
      },
    });
  }

  // Ouvre la confirmation "Le client a-t-il quitté la table ?" avant
  // de terminer réellement la prise en charge.
  demanderClientParti(vm: MesTableVM): void {
    this.actionErrorMessage = '';
    this.tableAConfirmer = vm;
  }

  annulerClientParti(): void {
    this.tableAConfirmer = null;
  }

  confirmerClientParti(): void {
    const vm = this.tableAConfirmer;

    if (!vm || this.actionEnCoursTableId !== null) {
      return;
    }

    this.actionEnCoursTableId = vm.table.id_table;

    this.priseEnChargeService.terminer(vm.table.id_table).subscribe({
      next: () => {
        // La prise en charge est terminée, mais l'affectation
        // permanente (serveurAttribue) reste inchangée : la table
        // continue d'apparaître dans "Mes tables".
        vm.priseEnChargeActive = null;
        vm.table.statut = StatutTable.LIBRE;
        this.actionEnCoursTableId = null;
        this.tableAConfirmer = null;
      },
      error: (err) => {
        this.actionErrorMessage =
          err?.error?.message ??
          'Impossible de terminer la prise en charge de cette table.';
        this.actionEnCoursTableId = null;
        this.tableAConfirmer = null;
      },
    });
  }

  // =============================================================
  // FILTRES / STATISTIQUES
  // =============================================================

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

  selectionnerFiltre(filtre: FiltreMesTables): void {
    this.selectedFiltre = filtre;
  }

  // =============================================================
  // DÉTAILS (modal)
  // =============================================================

  ouvrirDetails(vm: MesTableVM): void {
    this.selectedVm = vm;
  }

  fermerDetails(): void {
    this.selectedVm = null;
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
}
