import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { RestaurantService } from '../../../core/services/restaurant.service';
import { Restaurant } from '../../../core/models/restaurant.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-restaurant-selection',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './restaurant-selection.component.html',
  styleUrl: './restaurant-selection.component.css',
})
export class RestaurantSelectionComponent implements OnInit {
  restaurants: Restaurant[] = [];
  loading = false;
  selecting = false;
  errorMessage = '';
  selectedRestaurantId: number | null = null;

  get isSuperAdmin(): boolean {
    return this.authService.isSuperAdmin();
  }

  get title(): string {
    return this.isSuperAdmin ? 'Choisir un restaurant' : 'Votre restaurant';
  }

  get subtitle(): string {
    return this.isSuperAdmin
      ? 'Sélectionnez le restaurant que vous souhaitez administrer.'
      : 'Accédez à votre espace de gestion.';
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

  constructor(
    private readonly authService: AuthService,
    private readonly restaurantService: RestaurantService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadRestaurants();
  }

  loadRestaurants(): void {
    this.loading = true;
    this.errorMessage = '';

    if (this.isSuperAdmin) {
      this.restaurantService.getAllRestaurants().subscribe({
        next: (restaurants) => this.handleRestaurants(restaurants),
        error: (error) => this.handleLoadError(error),
      });
      return;
    }

    const restaurantId = this.authService.getRestaurantId();

    if (restaurantId === null || restaurantId === undefined) {
      this.loading = false;
      this.errorMessage = 'Aucun restaurant associé à votre compte.';
      return;
    }

    this.restaurantService.getRestaurant(restaurantId).subscribe({
      next: (restaurant) => this.handleRestaurants([restaurant]),
      error: (error) => this.handleLoadError(error),
    });
  }

  private handleRestaurants(restaurants: Restaurant[]): void {
    this.restaurants = Array.isArray(restaurants) ? restaurants : [];
    this.loading = false;
    this.cdr.detectChanges();
  }

  private handleLoadError(error: any): void {
    console.error('Erreur chargement restaurants :', error);
    this.loading = false;
    this.restaurants = [];

    if (error?.status === 401) {
      this.errorMessage = 'Votre session a expiré.';
    } else if (error?.status === 403) {
      this.errorMessage = "Vous n'avez pas accès aux restaurants.";
    } else {
      this.errorMessage = 'Impossible de charger les restaurants.';
    }

    this.cdr.detectChanges();
  }

  selectRestaurant(restaurant: Restaurant): void {
    const id = restaurant.id_restaurant;

    if (id === null || id === undefined || this.selecting) {
      return;
    }

    this.errorMessage = '';
    this.selectedRestaurantId = id;

    if (!this.isSuperAdmin) {
      this.authService.setSelectedRestaurant(restaurant);
      this.router.navigate(['/back-office/ventes/recapitulatif']);
      return;
    }

    this.selecting = true;

    this.authService.selectRestaurant(id).subscribe({
      next: () => {
        this.selecting = false;
        this.authService.setSelectedRestaurant(restaurant);
        this.router.navigate(['/back-office/ventes/recapitulatif']);
      },
      error: (error) => {
        console.error('Erreur sélection restaurant :', error);
        this.selecting = false;
        this.selectedRestaurantId = null;
        this.errorMessage =
          error?.error?.message || "Impossible d'ouvrir ce restaurant.";
        this.cdr.detectChanges();
      },
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
