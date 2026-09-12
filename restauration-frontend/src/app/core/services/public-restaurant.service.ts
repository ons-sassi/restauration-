import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { Restaurant } from '../models/restaurant.model';

/**
 * Restaurants publics — utilisé par l'écran client "Choisir un
 * restaurant" (features/client/restaurant-selection), AVANT toute
 * authentification. Ne pas confondre avec RestaurantService
 * (/api/restaurants), réservé au SUPERADMIN.
 */
@Injectable({
  providedIn: 'root',
})
export class PublicRestaurantService {
  private readonly API_URL = `${environment.apiUrl}/public/restaurants`;

  constructor(private readonly http: HttpClient) {}

  getAllRestaurants(): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(this.API_URL);
  }

  getRestaurant(id: number): Observable<Restaurant> {
    return this.http.get<Restaurant>(`${this.API_URL}/${id}`);
  }
}
