import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { ClientCommandeService } from '../../../core/services/client-commande.service';
import { Commande } from '../../../core/models/commande.model';
import { ClientLigneCommandeConfirmation } from '../../../core/models/client-commande-request.model';
import { StatutCommande } from '../../../core/models/enums/statut-commande.enum';
import { ModeCommande } from '../../../core/models/enums/mode-commande.enum';
import { ClientBottomNavComponent } from '../shared/client-bottom-nav.component';

/**
 * Sidebar client — Historique de commandes.
 *
 * Backend déjà safe et fonctionnel (voir ClientCommandeController) :
 * GET /api/client/commandes et GET /api/client/commandes/{id} dérivent
 * le client connecté via clientAuthentifieService.getClientConnecte(),
 * pas d'IDOR. Seul ajout backend nécessaire : GET
 * /api/client/commandes/{id}/lignes, car CommandeDTO n'a pas de champ
 * "lignes" (voir ClientCommandeService.getMesLignesCommande — le
 * endpoint back-office équivalent, /api/commandes/{id}/lignes, suppose
 * un Employee connecté et n'est pas utilisable ici).
 *
 * Une seule page : liste des commandes du client, la plus récente en
 * premier. Le détail des lignes (produits, quantités, modificateurs)
 * est chargé à la demande quand on déplie une commande, et mis en
 * cache pour ne pas refaire l'appel si on la replie/déplie à nouveau.
 */
@Component({
  selector: 'app-client-historique',
  standalone: true,
  imports: [CommonModule, ClientBottomNavComponent],
  templateUrl: './historique.component.html',
  styleUrl: './historique.component.css',
})
export class ClientCommandeHistoriqueComponent implements OnInit {
  readonly StatutCommande = StatutCommande;

  commandes: Commande[] = [];
  loading = false;
  errorMessage = '';

  expandedId: number | null = null;
  lignesParCommande = new Map<number, ClientLigneCommandeConfirmation[]>();
  loadingLignesId: number | null = null;
  lignesErrorId: number | null = null;

  constructor(
    private readonly clientCommandeService: ClientCommandeService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadCommandes();
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadCommandes(): void {
    this.loading = true;
    this.errorMessage = '';

    this.clientCommandeService.getAll().subscribe({
      next: (commandes) => {
        // Plus récentes en premier.
        this.commandes = [...commandes].sort(
          (a, b) => new Date(b.dateCommande).getTime() - new Date(a.dateCommande).getTime(),
        );
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossible de charger votre historique pour le moment.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // DÉTAIL (dépliage)
  // =========================================================

  toggleDetail(commande: Commande): void {
    if (this.expandedId === commande.id_commande) {
      this.expandedId = null;
      return;
    }

    this.expandedId = commande.id_commande;

    if (!this.lignesParCommande.has(commande.id_commande)) {
      this.loadLignes(commande.id_commande);
    }
  }

  loadLignes(commandeId: number): void {
    this.loadingLignesId = commandeId;
    this.lignesErrorId = null;

    this.clientCommandeService.getLignes(commandeId).subscribe({
      next: (lignes) => {
        this.lignesParCommande.set(commandeId, Array.isArray(lignes) ? lignes : []);
        this.loadingLignesId = null;
        this.cdr.detectChanges();
      },
      error: () => {
        this.lignesErrorId = commandeId;
        this.loadingLignesId = null;
        this.cdr.detectChanges();
      },
    });
  }

  lignesDe(commande: Commande): ClientLigneCommandeConfirmation[] {
    return this.lignesParCommande.get(commande.id_commande) ?? [];
  }

  // =========================================================
  // AFFICHAGE
  // =========================================================

  statutLabel(statut: StatutCommande): string {
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

  statutClass(statut: StatutCommande): string {
    switch (statut) {
      case StatutCommande.PAYEE:
        return 'statut-payee';
      case StatutCommande.SERVIE:
        return 'statut-servie';
      case StatutCommande.ANNULEE:
        return 'statut-annulee';
      default:
        return 'statut-attente';
    }
  }

  modeLabel(mode: ModeCommande): string {
    switch (mode) {
      case ModeCommande.A_EMPORTER:
        return 'À emporter';
      case ModeCommande.LIVRAISON:
        return 'Livraison';
      case ModeCommande.SAISIE_MANUELLE_NUMERO_TABLE:
        return 'Sur place';
      case ModeCommande.SUR_PLACE:
        return 'Sur place';
      case ModeCommande.SCAN_QR_TABLE:
        return 'Sur place (QR)';
      case ModeCommande.EN_LIGNE:
        return 'En ligne';
      default:
        return mode;
    }
  }

  formatPrix(prix: number | null | undefined): string {
    return prix !== null && prix !== undefined ? `${prix.toFixed(2)} DT` : '';
  }

  retourMenu(): void {
    this.router.navigate(['/client']);
  }
}
