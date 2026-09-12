import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { CartService } from '../../../core/services/cart.service';

export type ClientNavTab = 'menu' | 'favoris' | 'historique' | 'panier' | 'compte';

/**
 * Barre de navigation basse commune à l'espace client (accueil menu,
 * favoris, historique, panier, mon compte). Un seul composant
 * réutilisé partout pour garder une navigation cohérente, plutôt que
 * de dupliquer des boutons "header" différents sur chaque page.
 *
 * Le panier affiche le nombre d'articles en direct via CartService,
 * déjà utilisé par ClientMenuComponent (cartItemCount$).
 */
@Component({
  selector: 'app-client-bottom-nav',
  standalone: true,
  imports: [CommonModule],
  template: `
    <nav class="client-bottom-nav">
      <button type="button" class="client-nav-item" [class.active]="active === 'menu'" (click)="go('/client')">
        <span class="nav-icon">🍽️</span>
        <span>Menu</span>
      </button>

      <button type="button" class="client-nav-item" [class.active]="active === 'favoris'" (click)="go('/client/favoris')">
        <span class="nav-icon">♥</span>
        <span>Favoris</span>
      </button>

      <button type="button" class="client-nav-item" [class.active]="active === 'historique'" (click)="go('/client/historique')">
        <span class="nav-icon">🧾</span>
        <span>Commandes</span>
      </button>

      <button type="button" class="client-nav-item" [class.active]="active === 'panier'" (click)="go('/client/panier')">
        <span class="nav-icon">🛒</span>
        @if ((cartItemCount$ | async); as count) {
          @if (count > 0) {
            <span class="client-nav-badge">{{ count }}</span>
          }
        }
        <span>Panier</span>
      </button>

      <button type="button" class="client-nav-item" [class.active]="active === 'compte'" (click)="go('/client/mon-compte')">
        <span class="nav-icon">👤</span>
        <span>Compte</span>
      </button>
    </nav>
  `,
})
export class ClientBottomNavComponent {
  @Input() active: ClientNavTab | null = null;

  readonly cartItemCount$: Observable<number>;

  constructor(
    private readonly cartService: CartService,
    private readonly router: Router,
  ) {
    this.cartItemCount$ = this.cartService.itemCount$;
  }

  go(path: string): void {
    this.router.navigate([path]);
  }
}
