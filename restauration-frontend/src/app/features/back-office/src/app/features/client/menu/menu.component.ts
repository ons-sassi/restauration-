import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { ClientMenuService } from '../../../core/services/client-menu.service';
import { CartService } from '../../../core/services/cart.service';
import { ClientFavoriService } from '../../../core/services/client-favori.service';
import { ClientFavori } from '../../../core/models/client-favori.model';
import { Categorie } from '../../../core/models/categorie.model';
import { Produit } from '../../../core/models/produit.model';
import { Restaurant } from '../../../core/models/restaurant.model';
import { ClientBottomNavComponent } from '../shared/client-bottom-nav.component';
import { environment } from '../../../../environments/environment';

/**
 * Écran Menu de l'espace client (étape 3 du plan) :
 * - bande de catégories (+ sous-catégories) qui défile horizontalement
 * - "Meilleures ventes" et "Suggestions pour vous" au-dessus
 * - grille de produits de la catégorie sélectionnée ("Tout" par défaut)
 *
 * Le clic sur un produit ouvre sa fiche (features/client/produit-detail
 * — étape 4 du plan : choix des modificateurs, quantité, ajout au
 * panier).
 *
 * IMPORTANT : ce composant appelle ClientMenuService
 * ("/api/client/menu/**"), jamais ProduitService/CategorieService
 * ("/api/produits/**") qui sont réservés au Back Office (voir le
 * commentaire de ClientMenuController côté backend).
 */
@Component({
  selector: 'app-client-menu',
  standalone: true,
  imports: [CommonModule, ClientBottomNavComponent],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css',
})
export class ClientMenuComponent implements OnInit {
  readonly restaurant: Restaurant | null;

  categories: Categorie[] = [];
  sousCategories: Categorie[] = [];
  meilleuresVentes: Produit[] = [];
  suggestions: Produit[] = [];
  produits: Produit[] = [];

  selectedCategorieId: number | null = null;
  selectedSousCategorieId: number | null = null;

  loadingCategories = false;
  loadingProduits = false;
  loadingMeilleuresVentes = false;
  loadingSuggestions = false;

  errorMessage = '';

  // Favoris (voir ClientFavoriController côté backend) : chargés une
  // seule fois en Set d'ids plutôt qu'un appel estFavori() par carte
  // affichée, pour éviter un aller-retour réseau par produit.
  favoriIds = new Set<number>();
  favoriEnCoursId: number | null = null;

  readonly cartItemCount$: Observable<number>;

  constructor(
    private readonly authService: AuthService,
    private readonly clientMenuService: ClientMenuService,
    private readonly cartService: CartService,
    private readonly clientFavoriService: ClientFavoriService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {
    this.restaurant = this.authService.getSelectedRestaurant();
    this.cartItemCount$ = this.cartService.itemCount$;
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadMeilleuresVentes();
    this.loadSuggestions();
    this.loadProduits();
    this.loadFavoris();
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadCategories(): void {
    this.loadingCategories = true;

    this.clientMenuService.getCategoriesRacines().subscribe({
      next: (categories) => {
        this.categories = Array.isArray(categories) ? categories : [];
        this.loadingCategories = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement catégories :', error);
        this.categories = [];
        this.loadingCategories = false;
        this.cdr.detectChanges();
      },
    });
  }

  loadMeilleuresVentes(): void {
    this.loadingMeilleuresVentes = true;

    this.clientMenuService.getMeilleuresVentes(8).subscribe({
      next: (produits) => {
        this.meilleuresVentes = Array.isArray(produits) ? produits : [];
        this.loadingMeilleuresVentes = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement meilleures ventes :', error);
        this.meilleuresVentes = [];
        this.loadingMeilleuresVentes = false;
        this.cdr.detectChanges();
      },
    });
  }

  loadSuggestions(): void {
    this.loadingSuggestions = true;

    this.clientMenuService.getSuggestions(8).subscribe({
      next: (produits) => {
        this.suggestions = Array.isArray(produits) ? produits : [];
        this.loadingSuggestions = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement suggestions :', error);
        this.suggestions = [];
        this.loadingSuggestions = false;
        this.cdr.detectChanges();
      },
    });
  }

  loadProduits(): void {
    this.loadingProduits = true;
    this.errorMessage = '';

    const categorieId = this.selectedSousCategorieId ?? this.selectedCategorieId ?? undefined;

    this.clientMenuService.getProduits(categorieId).subscribe({
      next: (produits) => {
        this.produits = Array.isArray(produits) ? produits : [];
        this.loadingProduits = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement produits :', error);
        this.produits = [];
        this.loadingProduits = false;
        this.errorMessage = 'Impossible de charger le menu pour le moment.';
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // SELECTION CATEGORIE / SOUS-CATEGORIE
  // =========================================================

  selectCategorie(categorie: Categorie | null): void {
    this.selectedCategorieId = categorie?.id_element ?? null;
    this.selectedSousCategorieId = null;
    this.sousCategories = [];

    this.loadProduits();

    if (categorie) {
      this.clientMenuService.getSousCategories(categorie.id_element).subscribe({
        next: (sousCategories) => {
          this.sousCategories = Array.isArray(sousCategories) ? sousCategories : [];
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Erreur chargement sous-catégories :', error);
          this.sousCategories = [];
          this.cdr.detectChanges();
        },
      });
    }
  }

  selectSousCategorie(sousCategorie: Categorie | null): void {
    this.selectedSousCategorieId = sousCategorie?.id_element ?? null;
    this.loadProduits();
  }

  isCategorieActive(categorie: Categorie | null): boolean {
    return this.selectedCategorieId === (categorie?.id_element ?? null);
  }

  isSousCategorieActive(sousCategorie: Categorie | null): boolean {
    return this.selectedSousCategorieId === (sousCategorie?.id_element ?? null);
  }

  // =========================================================
  // FAVORIS
  // =========================================================

  loadFavoris(): void {
    this.clientFavoriService.getMesFavoris().subscribe({
      next: (favoris) => {
        this.favoriIds = new Set(favoris.map((f) => f.produitId));
        this.cdr.detectChanges();
      },
      error: () => {
        // Échec silencieux : le menu reste utilisable sans les cœurs.
      },
    });
  }

  isFavori(produit: Produit): boolean {
    return this.favoriIds.has(produit.id_element);
  }

  toggleFavori(produit: Produit, event: Event): void {
    // Le cœur est imbriqué dans une carte cliquable (ouvre la fiche
    // produit) : éviter que le clic déclenche aussi la navigation.
    event.stopPropagation();

    if (this.favoriEnCoursId !== null) {
      return;
    }

    const dejaFavori = this.isFavori(produit);
    this.favoriEnCoursId = produit.id_element;

    const action: Observable<ClientFavori | void> = dejaFavori
      ? this.clientFavoriService.supprimer(produit.id_element)
      : this.clientFavoriService.ajouter(produit.id_element);

    action.subscribe({
      next: () => {
        const ids = new Set(this.favoriIds);
        if (dejaFavori) {
          ids.delete(produit.id_element);
        } else {
          ids.add(produit.id_element);
        }
        this.favoriIds = ids;
        this.favoriEnCoursId = null;
        this.cdr.detectChanges();
      },
      error: () => {
        this.favoriEnCoursId = null;
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // AFFICHAGE PRODUIT
  // =========================================================

  getImage(element: { image: string | null }): string {
    const image = element.image?.trim();

    if (!image) {
      return 'assets/images/default-product.png';
    }

    if (image.startsWith('data:') || image.startsWith('http://') || image.startsWith('https://')) {
      return image;
    }

    return `${environment.filesBaseUrl}${image}`;
  }

  formatPrix(prix: number | null | undefined): string {
    return prix !== null && prix !== undefined ? `${prix.toFixed(2)} DT` : '';
  }

  openProduit(produit: Produit): void {
    this.router.navigate(['/client/produits', produit.id_element]);
  }

  openPanier(): void {
    this.router.navigate(['/client/panier']);
  }

  openReclamations(): void {
    this.router.navigate(['/client/reclamations']);
  }

  openReservations(): void {
    this.router.navigate(['/client/reservations']);
  }

  openSuggestions(): void {
    this.router.navigate(['/client/suggestions']);
  }

  openHistorique(): void {
    this.router.navigate(['/client/historique']);
  }

  openFavoris(): void {
    this.router.navigate(['/client/favoris']);
  }

  openMonCompte(): void {
    this.router.navigate(['/client/mon-compte']);
  }

  // =========================================================
  // DIVERS
  // =========================================================

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/client/restaurant-selection']);
  }
}
