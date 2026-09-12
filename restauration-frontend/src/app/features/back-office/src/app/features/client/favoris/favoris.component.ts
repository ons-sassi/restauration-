import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { ClientFavoriService } from '../../../core/services/client-favori.service';
import { ClientFavori } from '../../../core/models/client-favori.model';
import { ClientBottomNavComponent } from '../shared/client-bottom-nav.component';

/**
 * Sidebar client — Favoris (voir ClientFavoriController côté backend :
 * n'existait pas du tout avant, ni entité, ni back, ni front).
 *
 * Une seule page : grille des produits favoris du client, avec un
 * bouton pour retirer directement chacun d'eux. L'ajout se fait depuis
 * le menu / la fiche produit (voir bouton ♥ dans menu.component et
 * produit-detail.component), pas depuis cette page.
 */
@Component({
  selector: 'app-client-favoris',
  standalone: true,
  imports: [CommonModule, ClientBottomNavComponent],
  templateUrl: './favoris.component.html',
  styleUrl: './favoris.component.css',
})
export class ClientFavorisComponent implements OnInit {
  favoris: ClientFavori[] = [];
  loading = false;
  errorMessage = '';
  removingProduitId: number | null = null;

  constructor(
    private readonly clientFavoriService: ClientFavoriService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadFavoris();
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadFavoris(): void {
    this.loading = true;
    this.errorMessage = '';

    this.clientFavoriService.getMesFavoris().subscribe({
      next: (favoris) => {
        this.favoris = Array.isArray(favoris) ? favoris : [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossible de charger vos favoris pour le moment.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // RETRAIT
  // =========================================================

  retirer(favori: ClientFavori, event: Event): void {
    // Le bouton retirer est imbriqué dans la carte cliquable : éviter
    // que le clic ouvre aussi la fiche produit.
    event.stopPropagation();

    if (this.removingProduitId !== null) {
      return;
    }

    this.removingProduitId = favori.produitId;

    this.clientFavoriService.supprimer(favori.produitId).subscribe({
      next: () => {
        this.favoris = this.favoris.filter((f) => f.produitId !== favori.produitId);
        this.removingProduitId = null;
        this.cdr.detectChanges();
      },
      error: () => {
        this.removingProduitId = null;
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // NAVIGATION / AFFICHAGE
  // =========================================================

  openProduit(favori: ClientFavori): void {
    this.router.navigate(['/client/produits', favori.produitId]);
  }

  getImage(favori: ClientFavori): string {
    return favori.imageProduit || 'assets/images/default-product.png';
  }

  formatPrix(prix: number | null | undefined): string {
    return prix !== null && prix !== undefined ? `${prix.toFixed(2)} DT` : '';
  }

  retourMenu(): void {
    this.router.navigate(['/client']);
  }
}
