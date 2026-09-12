import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { CartService, CartItem } from '../../../core/services/cart.service';
import { MediaUrlPipe } from '../../../shared/pipes/media-url.pipe';

/**
 * Panier de l'espace client (étape 5 du plan). Affiche les lignes déjà
 * stockées par CartService (voir produit-detail — étape 4), permet
 * d'ajuster les quantités / de supprimer une ligne, et mène à l'écran
 * de finalisation (features/client/checkout).
 */
@Component({
  selector: 'app-client-panier',
  standalone: true,
  imports: [CommonModule, MediaUrlPipe],
  templateUrl: './panier.component.html',
  styleUrl: './panier.component.css',
})
export class ClientPanierComponent implements OnInit {
  items: CartItem[] = [];

  readonly total$: Observable<number>;

  constructor(
    private readonly cartService: CartService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {
    this.total$ = this.cartService.total$;
  }

  ngOnInit(): void {
    this.cartService.items$.subscribe((items) => {
      this.items = items;
      this.cdr.detectChanges();
    });
  }

  incrementer(item: CartItem): void {
    this.cartService.updateQuantite(item.ligneId, item.quantite + 1);
  }

  decrementer(item: CartItem): void {
    this.cartService.updateQuantite(item.ligneId, item.quantite - 1);
  }

  supprimer(item: CartItem): void {
    this.cartService.removeItem(item.ligneId);
  }

  getSousTotal(item: CartItem): number {
    return item.prixUnitaire * item.quantite;
  }

  getModificateursLabel(item: CartItem): string {
    return item.modificateurs.map((m) => m.nom_modificateur).join(', ');
  }

  formatPrix(prix: number | null | undefined): string {
    return prix !== null && prix !== undefined ? `${prix.toFixed(2)} DT` : '';
  }

  retourMenu(): void {
    this.router.navigate(['/client']);
  }

  passerCommande(): void {
    if (this.items.length === 0) {
      return;
    }

    this.router.navigate(['/client/checkout']);
  }
}
