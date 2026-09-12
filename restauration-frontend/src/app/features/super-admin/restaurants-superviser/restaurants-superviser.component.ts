// src/app/features/super-admin/restaurants-superviser/restaurants-superviser.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { RestaurantService } from '../../../core/services/restaurant.service';

import { Restaurant } from '../../../core/models/restaurant.model';

@Component({
  selector: 'app-restaurants-superviser',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './restaurants-superviser.component.html',
  styleUrl: './restaurants-superviser.component.css',
})
export class RestaurantsSuperviserComponent implements OnInit {
  // =========================================================
  // VARIABLES
  // =========================================================

  restaurants: Restaurant[] = [];

  loading = false;

  errorMessage = '';

  searchKeyword = '';

  // Id du restaurant en cours de bascule (désactive juste son
  // bouton "Entrer" pendant l'appel, pas toute la liste).
  enteringRestaurantId: number | null = null;

  enterErrorMessage = '';

  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly authService: AuthService,
    private readonly restaurantService: RestaurantService,
    private readonly router: Router,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    this.loadRestaurants();
  }

  // =========================================================
  // LOAD RESTAURANTS
  // =========================================================

  loadRestaurants(): void {
    this.loading = true;

    this.errorMessage = '';

    this.restaurantService.getAllRestaurants().subscribe({
      next: (restaurants) => {
        this.restaurants = Array.isArray(restaurants) ? restaurants : [];

        this.loading = false;
      },

      error: (error) => {
        console.error('Erreur chargement restaurants (super-admin) :', error);

        this.loading = false;

        this.restaurants = [];

        if (error?.status === 401) {
          this.errorMessage = 'Votre session a expiré.';
        } else if (error?.status === 403) {
          this.errorMessage = "Vous n'avez pas accès à la supervision plateforme.";
        } else {
          this.errorMessage = 'Impossible de charger les restaurants.';
        }
      },
    });
  }

  // =========================================================
  // RECHERCHE
  // =========================================================

  onSearch(): void {
    const keyword = this.searchKeyword.trim();

    if (!keyword) {
      this.loadRestaurants();

      return;
    }

    this.loading = true;

    this.errorMessage = '';

    this.restaurantService.searchRestaurants(keyword).subscribe({
      next: (restaurants) => {
        this.restaurants = Array.isArray(restaurants) ? restaurants : [];

        this.loading = false;
      },

      error: (error) => {
        console.error('Erreur recherche restaurants (super-admin) :', error);

        this.loading = false;

        this.restaurants = [];

        this.errorMessage = 'Impossible de rechercher les restaurants.';
      },
    });
  }

  // =========================================================
  // OUVRIR UN RESTAURANT
  // =========================================================

  enterRestaurant(restaurant: Restaurant): void {
    if (restaurant.id_restaurant === null || restaurant.id_restaurant === undefined) {
      return;
    }

    this.enterErrorMessage = '';

    this.enteringRestaurantId = restaurant.id_restaurant;

    this.authService.selectRestaurant(restaurant.id_restaurant).subscribe({
      next: () => {
        this.enteringRestaurantId = null;

        this.router.navigate(['/back-office/ventes/recapitulatif']);
      },

      error: (error) => {
        console.error('Erreur ouverture restaurant :', error);

        this.enteringRestaurantId = null;

        if (error?.status === 409) {
          this.enterErrorMessage =
            error?.error?.message ??
            "Impossible d'ouvrir ce restaurant.";
        } else if (error?.status === 404) {
          this.enterErrorMessage = 'Ce restaurant est introuvable.';
        } else {
          this.enterErrorMessage = "Impossible d'entrer dans ce restaurant.";
        }
      },
    });
  }

  // =========================================================
  // LOGOUT
  // =========================================================

  logout(): void {
    this.authService.logout();
  }
}
