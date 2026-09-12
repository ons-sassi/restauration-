import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Produit } from '../models/produit.model';
import { Categorie } from '../models/categorie.model';

@Injectable({
  providedIn: 'root',
})
export class ProduitService {
  private readonly API_URL = `${environment.apiUrl}/produits`;

  constructor(private readonly http: HttpClient) {}

  // =========================================================
  // GET ALL
  // =========================================================

  getAll(): Observable<Produit[]> {
    return this.http.get<Produit[]>(this.API_URL);
  }

  // =========================================================
  // GET BY ID
  // =========================================================

  getById(id: number): Observable<Produit> {
    return this.http.get<Produit>(`${this.API_URL}/${id}`);
  }

  // =========================================================
  // GET BY NOM
  // =========================================================

  getByNom(nom: string): Observable<Produit> {
    return this.http.get<Produit>(`${this.API_URL}/nom/${encodeURIComponent(nom)}`);
  }

  // =========================================================
  // GET DISPONIBILITE
  // =========================================================

  getByDisponible(disponible: boolean): Observable<Produit[]> {
    return this.http.get<Produit[]>(`${this.API_URL}/disponible/${disponible}`);
  }

  // =========================================================
  // SEARCH
  // =========================================================

  search(
    nom?: string,
    categorieId?: number,
    restaurantId?: number,
    disponible?: boolean,
  ): Observable<Produit[]> {
    let params = new HttpParams();

    if (nom && nom.trim()) {
      params = params.set('nom', nom.trim());
    }

    if (categorieId !== undefined && categorieId !== null) {
      params = params.set('categorieId', categorieId);
    }

    if (restaurantId !== undefined && restaurantId !== null) {
      params = params.set('restaurantId', restaurantId);
    }

    if (disponible !== undefined && disponible !== null) {
      params = params.set('disponible', disponible);
    }

    return this.http.get<Produit[]>(`${this.API_URL}/search`, {
      params,
    });
  }

  // =========================================================
  // CREATE
  // =========================================================

  create(produit: Partial<Produit>): Observable<Produit> {
    return this.http.post<Produit>(this.API_URL, produit);
  }

  // =========================================================
  // UPDATE
  // =========================================================

  update(id: number, produit: Partial<Produit>): Observable<Produit> {
    return this.http.put<Produit>(`${this.API_URL}/${id}`, produit);
  }

  // =========================================================
  // DELETE
  // =========================================================

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  // =========================================================
  // PAR INGREDIENT
  // =========================================================

  getByIngredient(ingredient: any): Observable<Produit[]> {
    return this.http.post<Produit[]>(`${this.API_URL}/by-ingredient`, ingredient);
  }

  // =========================================================
  // PAR CATEGORIE
  // =========================================================

  getByCategorieParent(categorie: Categorie): Observable<Produit[]> {
    return this.http.post<Produit[]>(`${this.API_URL}/by-categorie-parent`, categorie);
  }

  // =========================================================
  // PAR RESTAURANT
  // =========================================================

  getByRestaurant(restaurant: any): Observable<Produit[]> {
    return this.http.post<Produit[]>(`${this.API_URL}/by-restaurant`, restaurant);
  }
}
