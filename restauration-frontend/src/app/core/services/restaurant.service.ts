import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { Restaurant } from '../models/restaurant.model';

@Injectable({
  providedIn: 'root',
})
export class RestaurantService {
  private readonly API_URL = `${environment.apiUrl}/restaurants`;

  constructor(private readonly http: HttpClient) {}

  // ============================================================
  // GET ALL
  // ============================================================

  getAllRestaurants(): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(this.API_URL);
  }

  // ============================================================
  // GET BY ID
  // ============================================================

  getRestaurant(id: number): Observable<Restaurant> {
    return this.http.get<Restaurant>(`${this.API_URL}/${id}`);
  }

  // ============================================================
  // SEARCH
  // ============================================================

  searchRestaurants(keyword: string): Observable<Restaurant[]> {
    let params = new HttpParams();

    if (keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }

    return this.http.get<Restaurant[]>(`${this.API_URL}/search`, { params });
  }

  // ============================================================
  // CREATE
  // ============================================================

  createRestaurant(restaurant: Restaurant): Observable<Restaurant> {
    return this.http.post<Restaurant>(this.API_URL, restaurant);
  }

  // ============================================================
  // UPDATE
  // ============================================================

  updateRestaurant(id: number, restaurant: Restaurant): Observable<Restaurant> {
    return this.http.put<Restaurant>(`${this.API_URL}/${id}`, restaurant);
  }

  // ============================================================
  // UPLOAD LOGO
  // ============================================================

  // Même principe que MonCompteService.uploadPhoto : le fichier est
  // envoyé immédiatement (multipart/form-data), le backend
  // l'enregistre sur le disque et renvoie le restaurant à jour, avec
  // "logo" contenant une URL relative (ex.
  // "/uploads/logos-restaurant/xxx.jpg") à préfixer avec
  // environment.filesBaseUrl côté composant.
  uploadLogo(id: number, fichier: File): Observable<Restaurant> {
    const formData = new FormData();
    formData.append('file', fichier);

    return this.http.post<Restaurant>(`${this.API_URL}/${id}/logo`, formData);
  }

  // ============================================================
  // DELETE
  // ============================================================

  deleteRestaurant(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
