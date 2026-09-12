import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';

import { ClientMenuService } from '../../../core/services/client-menu.service';
import { CartItem, CartService } from '../../../core/services/cart.service';
import { ClientFavoriService } from '../../../core/services/client-favori.service';
import { ClientFavori } from '../../../core/models/client-favori.model';
import { Produit } from '../../../core/models/produit.model';
import { Modificateur } from '../../../core/models/modificateur.model';
import { MediaUrlPipe } from '../../../shared/pipes/media-url.pipe';

/**
 * Fiche produit de l'espace client (étape 4 du plan) : choix des
 * modificateurs, quantité, ajout au panier.
 *
 * Modificateur (voir backend) est un modèle simple : pas de groupes,
 * pas de min/max, pas de flag "obligatoire" — juste une liste de
 * choix indépendants, chacun ajoutant son prix_supplementaire. Le
 * client coche librement ceux qu'il veut.
 */
@Component({
  selector: 'app-client-produit-detail',
  standalone: true,
  imports: [CommonModule, MediaUrlPipe],
  templateUrl: './produit-detail.component.html',
  styleUrl: './produit-detail.component.css',
})
export class ClientProduitDetailComponent implements OnInit {
  produit: Produit | null = null;
  modificateurs: Modificateur[] = [];
  selectedModificateurIds = new Set<number>();

  quantite = 1;

  loading = false;
  errorMessage = '';
  ajoutConfirme = false;

  // Panier latéral
  panierOuvert = true;

  // Favoris (voir ClientFavoriController côté backend) : état chargé
  // séparément du produit lui-même, pour ne pas faire dépendre
  // l'affichage de la fiche produit d'un éventuel échec du service
  // favoris.
  estFavori = false;
  favoriEnCours = false;

  // Observables du panier : ils sont initialisés dans le constructeur
  // après l'injection de CartService afin d'éviter TS2729.
  readonly panierItems$: Observable<CartItem[]>;
  readonly panierTotal$: Observable<number>;
  readonly panierItemCount$: Observable<number>;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly clientMenuService: ClientMenuService,
    private readonly cartService: CartService,
    private readonly clientFavoriService: ClientFavoriService,
    private readonly cdr: ChangeDetectorRef,
  ) {
    this.panierItems$ = this.cartService.items$;
    this.panierTotal$ = this.cartService.total$;
    this.panierItemCount$ = this.cartService.itemCount$;
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) {
      this.errorMessage = 'Produit introuvable.';
      return;
    }

    this.loading = true;

    this.clientMenuService.getProduit(id).subscribe({
      next: (produit) => {
        this.produit = produit;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement produit :', error);
        this.errorMessage = 'Ce produit est introuvable ou indisponible.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });

    this.clientMenuService.getModificateurs(id).subscribe({
      next: (modificateurs) => {
        this.modificateurs = Array.isArray(modificateurs) ? modificateurs : [];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement modificateurs :', error);
        this.modificateurs = [];
        this.cdr.detectChanges();
      },
    });

    this.clientFavoriService.estFavori(id).subscribe({
      next: (estFavori) => {
        this.estFavori = estFavori;
        this.cdr.detectChanges();
      },
      error: () => {
        // Échec silencieux : la fiche produit reste utilisable, on
        // affiche juste le cœur comme "non favori" par défaut.
      },
    });
  }

  // =========================================================
  // FAVORIS
  // =========================================================

  toggleFavori(): void {
    if (!this.produit || this.favoriEnCours) {
      return;
    }

    const produitId = this.produit.id_element;
    this.favoriEnCours = true;

    const action: Observable<ClientFavori | void> = this.estFavori
      ? this.clientFavoriService.supprimer(produitId)
      : this.clientFavoriService.ajouter(produitId);

    action.subscribe({
      next: () => {
        this.estFavori = !this.estFavori;
        this.favoriEnCours = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.favoriEnCours = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // MODIFICATEURS
  // =========================================================

  toggleModificateur(modificateur: Modificateur): void {
    if (this.selectedModificateurIds.has(modificateur.id_modificateur)) {
      this.selectedModificateurIds.delete(modificateur.id_modificateur);
    } else {
      this.selectedModificateurIds.add(modificateur.id_modificateur);
    }
  }

  isModificateurSelected(modificateur: Modificateur): boolean {
    return this.selectedModificateurIds.has(modificateur.id_modificateur);
  }

  private getModificateursSelectionnes(): Modificateur[] {
    return this.modificateurs.filter((mod) =>
      this.selectedModificateurIds.has(mod.id_modificateur),
    );
  }

  // =========================================================
  // QUANTITE
  // =========================================================

  incrementerQuantite(): void {
    this.quantite += 1;
  }

  decrementerQuantite(): void {
    if (this.quantite > 1) {
      this.quantite -= 1;
    }
  }

  // =========================================================
  // PRIX
  // =========================================================

  getPrixUnitaire(): number {
    if (!this.produit) {
      return 0;
    }

    return this.cartService.calculerPrixUnitaire(this.produit, this.getModificateursSelectionnes());
  }

  getPrixTotal(): number {
    return this.getPrixUnitaire() * this.quantite;
  }

  // =========================================================
  // PANIER
  // =========================================================

  ajouterAuPanier(): void {
    if (!this.produit) {
      return;
    }

    this.cartService.addItem(this.produit, this.getModificateursSelectionnes(), this.quantite);

    this.ajoutConfirme = true;

    // Réinitialiser la sélection pour un éventuel nouvel ajout du
    // même produit avec d'autres options, sans quitter la page.
    this.quantite = 1;
    this.selectedModificateurIds.clear();
  }

  supprimerDuPanier(item: CartItem): void {
    this.cartService.removeItem(item.ligneId);
  }

  incrementerPanier(item: CartItem): void {
    this.cartService.updateQuantite(item.ligneId, item.quantite + 1);
  }

  decrementerPanier(item: CartItem): void {
    this.cartService.updateQuantite(item.ligneId, item.quantite - 1);
  }

  getPanierSousTotal(item: CartItem): number {
    return item.prixUnitaire * item.quantite;
  }

  getPanierModificateurs(item: CartItem): string {
    return item.modificateurs?.map((m) => m.nom_modificateur).join(', ') || '';
  }

  formatPrixPanier(prix: number | null | undefined): string {
    return prix !== null && prix !== undefined ? `${prix.toFixed(2)} DT` : '0.00 DT';
  }

  togglePanier(): void {
    this.panierOuvert = !this.panierOuvert;
  }

  allerAuPanier(): void {
    this.router.navigate(['/client/panier']);
  }

  passerCommandeDepuisPanier(): void {
    if (this.cartService.getItems().length > 0) {
      this.router.navigate(['/client/checkout']);
    }
  }

  retourMenu(): void {
    this.router.navigate(['/client']);
  }
}
