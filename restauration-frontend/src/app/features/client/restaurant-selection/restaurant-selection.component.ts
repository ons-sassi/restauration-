import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { PublicRestaurantService } from '../../../core/services/public-restaurant.service';
import { Restaurant } from '../../../core/models/restaurant.model';
import { environment } from '../../../../environments/environment';

/**
 * Écran public (sans authentification) : le client choisit son
 * restaurant AVANT de se connecter/s'inscrire.
 *
 * Pourquoi cet ordre : ClientAuthentifie.restaurant est une colonne
 * obligatoire côté backend (un compte client appartient toujours à
 * un seul restaurant). Choisir le restaurant en premier permet de
 * pré-remplir ClientRegisterDTO.restaurantId à l'inscription, et de
 * détecter une éventuelle "réconciliation" à la connexion (voir
 * features/client/auth) si le compte appartient en réalité à un
 * autre restaurant que celui affiché ici.
 */
@Component({
  selector: 'app-client-restaurant-selection',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './restaurant-selection.component.html',
  styleUrl: './restaurant-selection.component.css',
})
export class ClientRestaurantSelectionComponent implements OnInit {
  restaurants: Restaurant[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly publicRestaurantService: PublicRestaurantService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadRestaurants();
  }

  // Le backend renvoie une URL relative pour le logo (ex.
  // "/uploads/logos-restaurant/xxx.jpg") qu'il faut préfixer avec
  // l'origine du backend pour que l'image se charge correctement.
  logoUrl(restaurant: Restaurant): string | null {
    const logo = restaurant.logo?.trim();

    if (!logo) {
      return null;
    }

    if (logo.startsWith('http://') || logo.startsWith('https://')) {
      return logo;
    }

    return `${environment.filesBaseUrl}${logo}`;
  }

  loadRestaurants(): void {
    this.loading = true;
    this.errorMessage = '';

    this.publicRestaurantService.getAllRestaurants().subscribe({
      next: (restaurants) => {
        this.restaurants = Array.isArray(restaurants) ? restaurants : [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement restaurants :', error);
        this.loading = false;
        this.restaurants = [];
        this.errorMessage = 'Impossible de charger les restaurants pour le moment.';
        this.cdr.detectChanges();
      },
    });
  }

  selectRestaurant(restaurant: Restaurant): void {
    if (restaurant.id_restaurant === null || restaurant.id_restaurant === undefined) {
      return;
    }

    // On mémorise le restaurant choisi (utilisé par l'écran
    // login/inscription suivant) sans se connecter : setSelectedRestaurant
    // ne fait que persister l'objet en local, il ne requiert aucune
    // authentification.
    this.authService.setSelectedRestaurant(restaurant);

    this.router.navigate(['/client/auth']);
  }
}
