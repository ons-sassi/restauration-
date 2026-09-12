import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { CartService, CartItem } from '../../../core/services/cart.service';
import { ClientCommandeService } from '../../../core/services/client-commande.service';
import { AuthService } from '../../../core/services/auth.service';
import { MonCompteService } from '../../../core/services/mon-compte.service';
import { ModeCommande } from '../../../core/models/enums/mode-commande.enum';
import { TableRestaurant } from '../../../core/models/table-restaurant.model';
import { ModeleRecu } from '../../../core/models/modele-recu.model';
import { Restaurant } from '../../../core/models/restaurant.model';
import { MonCompte } from '../../../core/models/mon-compte.model';
import { ReceiptTicketComponent, ReceiptTicketLine } from '../../../shared/components/receipt-ticket/receipt-ticket.component';
import {
  ClientCommandeConfirmation,
  ClientCommandeRequest,
} from '../../../core/models/client-commande-request.model';

/**
 * Finalisation du panier (étape 5 du plan) : choix du mode de commande,
 * puis soumission au backend (voir ClientCommandeService.creerDepuisPanier
 * / ClientCommandeServiceImpl côté back — le prix de chaque ligne y est
 * intégralement recalculé, jamais celui affiché ici côté Angular).
 *
 * Seuls 3 modes ont un sens pour le client authentifié : à emporter,
 * livraison (adresse requise) et sur place avec un numéro de table
 * saisi manuellement (résolu en une vraie table côté backend).
 */
@Component({
  selector: 'app-client-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, ReceiptTicketComponent],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css',
})
export class ClientCheckoutComponent implements OnInit {
  readonly ModeCommande = ModeCommande;

  items: CartItem[] = [];
  total = 0;

  modeCommande: ModeCommande = ModeCommande.A_EMPORTER;

  // Livraison : le client peut utiliser l'adresse enregistrée dans son
  // compte ou saisir une adresse différente uniquement pour cette commande.
  utiliserAdresseCompte = true;
  adresseCompte = '';
  adresseAlternative = '';

  numeroTable: number | null = null;

  // -----------------------------------------------------------
  // TABLES DISPONIBLES (mode "sur place")
  // -----------------------------------------------------------
  // Corrige l'ancienne saisie libre du numéro de table : le client ne
  // peut désormais choisir que parmi les tables réellement disponibles
  // (statut LIBRE) de son restaurant, chargées depuis le backend.
  tablesDisponibles: TableRestaurant[] = [];
  chargementTables = false;
  erreurTables = '';

  loading = false;
  errorMessage = '';

  confirmation: ClientCommandeConfirmation | null = null;

  // -----------------------------------------------------------
  // REÇU (design défini côté back-office > Paramètres > Détails du
  // reçu — voir DetailsRecuComponent). Une fois la commande
  // confirmée, on charge le ModeleRecu du restaurant pour afficher
  // au client un ticket fidèle à ce qui a été configuré (en-tête,
  // pied de page, logo, infos client, allergies, modificateurs...).
  // -----------------------------------------------------------
  restaurantRecu: Restaurant | null = null;
  modeleRecu: ModeleRecu | null = null;
  clientRecu: MonCompte | null = null;

  constructor(
    private readonly cartService: CartService,
    private readonly clientCommandeService: ClientCommandeService,
    private readonly authService: AuthService,
    private readonly monCompteService: MonCompteService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.items = this.cartService.getItems();

    if (this.items.length === 0) {
      this.router.navigate(['/client']);
      return;
    }

    this.cartService.total$.subscribe((total) => {
      this.total = total;
      this.cdr.detectChanges();
    });

    this.chargerTablesDisponibles();
    this.chargerCompteClient();
  }

  // =========================================================
  // TABLES DISPONIBLES (mode "sur place")
  // =========================================================

  chargerTablesDisponibles(): void {
    this.chargementTables = true;
    this.erreurTables = '';

    this.clientCommandeService.getTablesDisponibles().subscribe({
      next: (tables) => {
        this.tablesDisponibles = tables;
        this.chargementTables = false;

        // Si la table précédemment sélectionnée n'est plus disponible
        // (prise entre-temps), on invalide la sélection plutôt que de
        // laisser un numéro obsolète partir avec la commande.
        if (
          this.numeroTable !== null &&
          !tables.some((t) => t.numeroTable === this.numeroTable)
        ) {
          this.numeroTable = null;
        }

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur lors du chargement des tables disponibles :', error);
        this.erreurTables = 'Impossible de charger les tables disponibles.';
        this.chargementTables = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // VALIDATION
  // =========================================================

  get adresseLivraison(): string {
    return this.utiliserAdresseCompte ? this.adresseCompte.trim() : this.adresseAlternative.trim();
  }

  get formulaireValide(): boolean {
    if (this.modeCommande === ModeCommande.LIVRAISON) {
      return this.adresseLivraison.length > 0;
    }

    if (this.modeCommande === ModeCommande.SAISIE_MANUELLE_NUMERO_TABLE) {
      return this.numeroTable !== null && this.numeroTable > 0;
    }

    return true;
  }

  // =========================================================
  // AFFICHAGE
  // =========================================================

  formatPrix(prix: number | null | undefined): string {
    return prix !== null && prix !== undefined ? `${prix.toFixed(2)} DT` : '';
  }

  getModificateursLabel(item: CartItem): string {
    return item.modificateurs.map((m) => m.nom_modificateur).join(', ');
  }

  // =========================================================
  // SOUMISSION
  // =========================================================

  confirmerCommande(): void {
    if (!this.formulaireValide || this.loading) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const request: ClientCommandeRequest = {
      modeCommande: this.modeCommande,
      adresseLivraison:
        this.modeCommande === ModeCommande.LIVRAISON ? this.adresseLivraison : null,
      numeroTable:
        this.modeCommande === ModeCommande.SAISIE_MANUELLE_NUMERO_TABLE
          ? this.numeroTable
          : null,
      lignes: this.items.map((item) => ({
        produitId: item.produit.id_element,
        quantite: item.quantite,
        modificateurIds: item.modificateurs.map((m) => m.id_modificateur),
      })),
    };

    this.clientCommandeService.creerDepuisPanier(request).subscribe({
      next: (confirmation) => {
        this.confirmation = confirmation;
        this.cartService.clear();
        this.loading = false;
        this.chargerDonneesRecu();
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur lors de la création de la commande :', error);

        this.errorMessage =
          error?.error?.message ||
          "Impossible de finaliser la commande pour le moment. Réessayez.";

        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  retourPanier(): void {
    this.router.navigate(['/client/panier']);
  }

  retourMenu(): void {
    this.router.navigate(['/client']);
  }

  // =========================================================
  // REÇU (design back-office)
  // =========================================================

  /**
   * Charge le modèle de reçu du restaurant (ModeleRecu, configuré
   * dans back-office > Paramètres > Détails du reçu) ainsi que les
   * infos du client connecté, pour afficher le ticket de
   * confirmation avec le même rendu que l'aperçu back-office.
   *
   * Best-effort : si le modèle n'existe pas encore (ou si l'appel
   * échoue, ex. droits backend), on retombe silencieusement sur les
   * réglages par défaut (voir les getters recuAfficher* ci-dessous)
   * plutôt que de bloquer l'écran de confirmation.
   */
  private chargerCompteClient(): void {
    this.monCompteService.get().subscribe({
      next: (compte) => {
        this.clientRecu = compte;
        this.adresseCompte = compte.adresse?.trim() ?? '';
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur lors du chargement du compte client :', error);
        this.clientRecu = null;
        this.adresseCompte = '';
        this.cdr.detectChanges();
      },
    });
  }

  choisirAdresseCompte(): void {
    this.utiliserAdresseCompte = true;
    this.cdr.detectChanges();
  }

  choisirAutreAdresse(): void {
    this.utiliserAdresseCompte = false;
    this.cdr.detectChanges();
  }

  private chargerDonneesRecu(): void {
    this.restaurantRecu = this.authService.getSelectedRestaurant();

    // Le backend déduit le restaurant depuis le client authentifié.
    // On ne demande plus au client de fournir un restaurantId.
    this.clientCommandeService.getModeleRecu().subscribe({
      next: (modele) => {
        this.modeleRecu = modele;
        this.restaurantRecu = modele.restaurant ?? this.restaurantRecu;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur lors du chargement du modèle de reçu :', error);
        this.modeleRecu = null;
        this.cdr.detectChanges();
      },
    });

    // Les informations du compte ont déjà été chargées au démarrage.
    // On conserve notamment l'e-mail et le téléphone utilisés par le reçu.
  }

  get lignesRecu(): ReceiptTicketLine[] {
    if (!this.confirmation) {
      return [];
    }

    return this.confirmation.lignes.map((ligne) => ({
      nomProduit: ligne.nomProduit,
      quantite: ligne.quantite,
      prixUnitaire: ligne.prixUnitaire,
      modificateurs: ligne.modificateurs,
      categorie: ligne.categorie ?? null,
      sousCategorie: ligne.sousCategorie ?? null,
    }));
  }

  get recuNomClient(): string {
    if (!this.clientRecu) {
      return '';
    }

    return [this.clientRecu.prenom, this.clientRecu.nom].filter(Boolean).join(' ').trim();
  }

  get recuTelephone(): string {
    return this.clientRecu?.telephone ?? '';
  }

  get recuEmail(): string {
    return this.clientRecu?.email ?? '';
  }

  get recuAdresseLivraison(): string {
    return this.confirmation?.modeCommande === ModeCommande.LIVRAISON
      ? this.confirmation.adresseLivraison ?? ''
      : '';
  }

  get recuAllergies(): string {
    return this.clientRecu?.allergie ?? '';
  }

}
