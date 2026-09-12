import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { Commande } from '../../../../core/models/commande.model';
import { LigneCommande } from '../../../../core/models/ligne-commande.model';

import { CommandeService } from '../../../../core/services/commande.service';
import { AuthService } from '../../../../core/services/auth.service';
import { TableRestaurantService } from '../../../../core/services/table-restaurant.service';

import { StatutCommande } from '../../../../core/models/enums/statut-commande.enum';
import { ModeCommande } from '../../../../core/models/enums/mode-commande.enum';
import { StatutTable } from '../../../../core/models/enums/statut-table.enum';

@Component({
  selector: 'app-commandes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './commandes.component.html',
  styleUrl: './commandes.component.css',
})
export class CommandesComponent implements OnInit {
  commandes: Commande[] = [];

  loading = false;
  errorMessage = '';

  // -----------------------------------------------------------
  // CALENDRIER / JOUR SÉLECTIONNÉ
  // -----------------------------------------------------------

  // Format yyyy-MM-dd attendu par <input type="date">.
  selectedDate: string = this.formatDateInput(new Date());

  // -----------------------------------------------------------
  // FILTRES
  // -----------------------------------------------------------

  searchTerm = '';
  selectedStatut: StatutCommande | 'tous' = 'tous';
  selectedMode: ModeCommande | 'tous' = 'tous';

  statuts = Object.values(StatutCommande);

  // Les 3 modes utiles en gestion des clients : à emporter, livraison,
  // sur place (les 2 variantes "sur place" — QR scanné ou numéro de
  // table saisi manuellement — sont regroupées sous un seul filtre).
  modes = [
    { value: ModeCommande.A_EMPORTER, label: 'À emporter' },
    { value: ModeCommande.LIVRAISON, label: 'Livraison' },
    { value: ModeCommande.SUR_PLACE, label: 'Sur place' },
    { value: ModeCommande.SCAN_QR_TABLE, label: 'Sur place (QR table)' },
    { value: ModeCommande.SAISIE_MANUELLE_NUMERO_TABLE, label: 'Sur place (n° table)' },
    { value: ModeCommande.EN_LIGNE, label: 'En ligne' },
  ];

  // -----------------------------------------------------------
  // ANNULATION
  // -----------------------------------------------------------

  commandeAAnnuler: Commande | null = null;
  annulation = false;
  successMessage = '';

  // -----------------------------------------------------------
  // DÉTAIL (VOIR LES LIGNES DE LA COMMANDE)
  // -----------------------------------------------------------

  commandeSelectionnee: Commande | null = null;
  lignesCommandeSelectionnee: LigneCommande[] = [];
  chargementLignes = false;
  erreurLignes = '';

  // -----------------------------------------------------------
  // MARQUER PRÊTE (SERVIE) EN COURS
  // -----------------------------------------------------------

  idEnCoursDeMiseAJour: number | null = null;

  // -----------------------------------------------------------
  // PRISE DE TABLE PAR LE CLIENT / LIBÉRATION DE LA TABLE
  // -----------------------------------------------------------
  // "Confirmer la prise de table" : le client s'est bien installé à sa
  // table -> celle-ci passe à OCCUPÉE et disparaît de la liste des
  // tables disponibles côté client (voir ClientCommandeService.
  // getTablesDisponibles(), filtrée sur le statut LIBRE côté backend).
  // "Table vidée" : le client est parti -> la table repasse à LIBRE et
  // redevient immédiatement disponible pour une nouvelle commande.
  idTableEnCoursDeMiseAJour: number | null = null;

  constructor(
    private readonly commandeService: CommandeService,
    private readonly tableService: TableRestaurantService,
    public readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    this.loadCommandes();
  }

  // =============================================================
  // PERMISSIONS
  // =============================================================

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  canVoirCommandes(): boolean {
    return this.authService.isAdmin() || this.hasPermission('CLIENTS_COMMANDES');
  }

  canAnnulerCommande(): boolean {
    return this.authService.isAdmin() || this.hasPermission('CLIENTS_COMMANDE_ANNULER');
  }

  // Même permission que l'annulation : on considère que l'équipe qui peut
  // annuler une commande peut aussi la faire avancer (la marquer prête).
  canChangerStatutCommande(): boolean {
    return this.authService.isAdmin() || this.hasPermission('CLIENTS_COMMANDE_ANNULER');
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadCommandes(): void {
    if (!this.selectedDate) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const { debut, fin } = this.plageJournee(this.selectedDate);

    this.commandeService
      .getByDate(debut, fin)
      .pipe(
        // Filet de sécurité : si le backend ne répond jamais
        // (serveur arrêté, CORS bloqué, etc.), on ne reste pas
        // bloqué indéfiniment sur "Chargement...".
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors du chargement des commandes :', error);

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
              `Impossible de charger les commandes (${error?.status ?? 'erreur inconnue'}).`;
          }

          return of([] as Commande[]);
        }),
      )
      .subscribe((commandes) => {
        this.commandes = Array.isArray(commandes) ? commandes : [];
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  // Construit les bornes [00:00:00.000 ; 23:59:59.999] du jour
  // sélectionné, à partir d'une chaîne yyyy-MM-dd (heure locale).
  private plageJournee(dateStr: string): { debut: Date; fin: Date } {
    const [annee, mois, jour] = dateStr.split('-').map((v) => Number(v));

    const debut = new Date(annee, mois - 1, jour, 0, 0, 0, 0);
    const fin = new Date(annee, mois - 1, jour, 23, 59, 59, 999);

    return { debut, fin };
  }

  private formatDateInput(date: Date): string {
    const annee = date.getFullYear();
    const mois = `${date.getMonth() + 1}`.padStart(2, '0');
    const jour = `${date.getDate()}`.padStart(2, '0');

    return `${annee}-${mois}-${jour}`;
  }

  // =============================================================
  // NAVIGATION DANS LE CALENDRIER
  // =============================================================

  jourPrecedent(): void {
    this.changerJour(-1);
  }

  jourSuivant(): void {
    this.changerJour(1);
  }

  aujourdHui(): void {
    this.selectedDate = this.formatDateInput(new Date());
    this.loadCommandes();
  }

  private changerJour(offset: number): void {
    const [annee, mois, jour] = this.selectedDate.split('-').map((v) => Number(v));
    const date = new Date(annee, mois - 1, jour);

    date.setDate(date.getDate() + offset);

    this.selectedDate = this.formatDateInput(date);
    this.loadCommandes();
  }

  onDateChange(): void {
    this.loadCommandes();
  }

  // =============================================================
  // FILTRAGE
  // =============================================================

  get commandesFiltrees(): Commande[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.commandes.filter((commande) => {
      const nomClient = this.nomClient(commande).toLowerCase();
      const numeroTable = commande.table?.numeroTable?.toString() ?? '';
      const idCommande = commande.id_commande?.toString() ?? '';

      const matchesSearch =
        !search ||
        nomClient.includes(search) ||
        numeroTable.includes(search) ||
        idCommande.includes(search);

      const matchesStatut =
        this.selectedStatut === 'tous' || commande.statut === this.selectedStatut;

      const matchesMode =
        this.selectedMode === 'tous' || commande.modeCommande === this.selectedMode;

      return matchesSearch && matchesStatut && matchesMode;
    });
  }

  // =============================================================
  // AFFICHAGE
  // =============================================================

  nomClient(commande: Commande): string {
    if (commande.client) {
      return `${commande.client.prenom ?? ''} ${commande.client.nom ?? ''}`.trim() || 'Client';
    }

    if (commande.clientNonAuthentifie) {
      return `Client anonyme (session #${commande.clientNonAuthentifie.id_session})`;
    }

    return 'Client non renseigné';
  }

  tableLabel(commande: Commande): string {
    return commande.table ? `Table ${commande.table.numeroTable}` : '—';
  }

  modeLabel(commande: Commande): string {
    const trouve = this.modes.find((m) => m.value === commande.modeCommande);
    return trouve ? trouve.label : commande.modeCommande || '—';
  }

  // -----------------------------------------------------------
  // COORDONNÉES DU CLIENT (client authentifié en priorité, sinon
  // session anonyme — voir ClientAuthentifie / ClientNonAuthentifie)
  // -----------------------------------------------------------

  telephoneClient(commande: Commande): string {
    return commande.client?.telephone || '—';
  }

  emailClient(commande: Commande): string {
    return commande.client?.email || '—';
  }

  allergieClient(commande: Commande): string {
    return commande.client?.allergie || commande.clientNonAuthentifie?.allergie || '—';
  }

  // Adresse de livraison : uniquement pertinente si modeCommande =
  // LIVRAISON (voir Commande.adresseLivraison côté backend).
  estLivraison(commande: Commande): boolean {
    return commande.modeCommande === ModeCommande.LIVRAISON;
  }

  adresseLivraisonAffichee(commande: Commande): string {
    return commande.adresseLivraison || '—';
  }

  heureCommande(commande: Commande): string {
    if (!commande.dateCommande) {
      return '—';
    }

    return new Date(commande.dateCommande).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  // =============================================================
  // ANNULATION
  // =============================================================

  canBeCancelled(commande: Commande): boolean {
    return (
      commande.statut === StatutCommande.EN_ATTENTE || commande.statut === StatutCommande.SERVIE
    );
  }

  // Une commande ne peut être marquée "prête" que si elle est encore
  // en attente (pas déjà servie, payée ou annulée).
  canBeMarkedReady(commande: Commande): boolean {
    return commande.statut === StatutCommande.EN_ATTENTE;
  }

  // =============================================================
  // DÉTAIL DE LA COMMANDE
  // =============================================================

  voirDetail(commande: Commande): void {
    this.commandeSelectionnee = commande;
    this.lignesCommandeSelectionnee = [];
    this.erreurLignes = '';
    this.chargementLignes = true;
    this.cdr.detectChanges();

    this.commandeService
      .getLignes(commande.id_commande)
      .pipe(
        timeout(15000),
        catchError((error) => {
          console.error('Erreur lors du chargement des lignes de la commande :', error);
          this.erreurLignes =
            error?.error?.message ?? 'Impossible de charger le détail de cette commande.';
          return of([] as LigneCommande[]);
        }),
      )
      .subscribe((lignes) => {
        this.lignesCommandeSelectionnee = Array.isArray(lignes) ? lignes : [];
        this.chargementLignes = false;
        this.cdr.detectChanges();
      });
  }

  fermerDetail(): void {
    this.commandeSelectionnee = null;
    this.lignesCommandeSelectionnee = [];
    this.erreurLignes = '';
    this.cdr.detectChanges();
  }

  // =============================================================
  // MARQUER PRÊTE (PASSAGE AU STATUT "SERVIE")
  // =============================================================

  marquerPrete(commande: Commande): void {
    if (!this.canChangerStatutCommande() || !this.canBeMarkedReady(commande)) {
      return;
    }

    const commandeId = commande.id_commande;

    this.idEnCoursDeMiseAJour = commandeId;
    this.errorMessage = '';

    this.commandeService.changerStatut(commandeId, StatutCommande.SERVIE).subscribe({
      next: (commandeMiseAJour) => {
        this.commandes = this.commandes.map((c) =>
          c.id_commande === commandeId ? commandeMiseAJour : c,
        );

        this.successMessage = `Commande #${commandeId} marquée comme prête.`;
        this.idEnCoursDeMiseAJour = null;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du passage au statut "prête" :', error);

        this.errorMessage =
          error?.error?.message ?? 'Impossible de marquer cette commande comme prête.';

        this.idEnCoursDeMiseAJour = null;
        this.cdr.detectChanges();
      },
    });
  }

  // =============================================================
  // LIBÉRATION DE LA TABLE
  // =============================================================
  // La prise de table est désormais confirmée automatiquement par le
  // backend dès que le client choisit sa table à la commande (voir
  // ClientCommandeServiceImpl.creerCommandeDepuisPanier, qui passe la
  // table à OCCUPÉE) : plus besoin d'action manuelle ici pour ça. La
  // seule action qui reste côté Back Office est de libérer la table
  // une fois le client parti, pour qu'elle redevienne disponible.

  // Même permission que les autres actions de suivi de commande sur
  // cette page (marquer prête / annuler).
  canGererTable(): boolean {
    return this.canChangerStatutCommande();
  }

  tableEstOccupee(commande: Commande): boolean {
    return commande.table?.statut === StatutTable.OCCUPEE;
  }

  // Marque la table comme vidée par le client : elle repasse à LIBRE et
  // redevient donc immédiatement disponible pour une nouvelle commande
  // côté client.
  libererTable(commande: Commande): void {
    if (!this.canGererTable() || !commande.table || !this.tableEstOccupee(commande)) {
      return;
    }

    this.changerStatutTable(commande, StatutTable.LIBRE, 'libérée');
  }

  private changerStatutTable(commande: Commande, statut: StatutTable, libelle: string): void {
    const table = commande.table;

    if (!table) {
      return;
    }

    const tableId = table.id_table;

    this.idTableEnCoursDeMiseAJour = tableId;
    this.errorMessage = '';

    this.tableService.updateStatut(tableId, statut).subscribe({
      next: (tableMiseAJour) => {
        // Toutes les commandes du jour liées à cette même table doivent
        // refléter le nouveau statut, pas seulement celle sur laquelle
        // on a cliqué (plusieurs commandes peuvent partager une table).
        this.commandes = this.commandes.map((c) =>
          c.table && c.table.id_table === tableId ? { ...c, table: tableMiseAJour } : c,
        );

        this.successMessage = `Table ${tableMiseAJour.numeroTable} marquée comme ${libelle}.`;
        this.idTableEnCoursDeMiseAJour = null;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du changement de statut de la table :', error);

        this.errorMessage =
          error?.error?.message ?? 'Impossible de mettre à jour le statut de cette table.';

        this.idTableEnCoursDeMiseAJour = null;
        this.cdr.detectChanges();
      },
    });
  }

  demanderAnnulation(commande: Commande): void {
    if (!this.canAnnulerCommande() || !this.canBeCancelled(commande)) {
      return;
    }

    this.commandeAAnnuler = commande;
    this.successMessage = '';
    this.cdr.detectChanges();
  }

  annulerAnnulation(): void {
    this.commandeAAnnuler = null;
    this.cdr.detectChanges();
  }

  confirmerAnnulation(): void {
    if (!this.commandeAAnnuler) {
      return;
    }

    this.annulation = true;

    const commandeId = this.commandeAAnnuler.id_commande;
    const avaitUneTable = !!this.commandeAAnnuler.table;

    this.commandeService.annuler(commandeId).subscribe({
      next: (commandeMiseAJour) => {
        this.commandes = this.commandes.map((c) =>
          c.id_commande === commandeId ? commandeMiseAJour : c,
        );

        this.commandeAAnnuler = null;
        this.annulation = false;

        this.successMessage = avaitUneTable
          ? 'Commande annulée. Si une réservation était liée à sa table, elle a été annulée et la table libérée.'
          : 'Commande annulée avec succès.';

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error("Erreur lors de l'annulation de la commande :", error);

        this.errorMessage = error?.error?.message ?? 'Impossible d’annuler cette commande.';

        this.commandeAAnnuler = null;
        this.annulation = false;

        this.cdr.detectChanges();
      },
    });
  }
}
