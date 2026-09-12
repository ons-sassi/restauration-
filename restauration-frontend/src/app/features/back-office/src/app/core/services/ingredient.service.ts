import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Ingredient } from '../models/ingredient.model';

@Injectable({
  providedIn: 'root',
})
export class IngredientService {
  private readonly API_URL = `${environment.apiUrl}/ingredients`;

  constructor(private readonly http: HttpClient) {}

  // =========================================================
  // GET ALL
  // =========================================================

  getAll(): Observable<Ingredient[]> {
    return this.http.get<Ingredient[]>(this.API_URL);
  }

  // =========================================================
  // GET BY ID
  // =========================================================

  getById(id: number): Observable<Ingredient> {
    return this.http.get<Ingredient>(`${this.API_URL}/${id}`);
  }

  // =========================================================
  // SEARCH
  // =========================================================

  search(
    nomIngredient?: string,
    nomFournisseur?: string,
    nomProduit?: string,
  ): Observable<Ingredient[]> {
    let params = new HttpParams();

    if (nomIngredient && nomIngredient.trim()) {
      params = params.set('nomIngredient', nomIngredient.trim());
    }

    if (nomFournisseur && nomFournisseur.trim()) {
      params = params.set('nomFournisseur', nomFournisseur.trim());
    }

    if (nomProduit && nomProduit.trim()) {
      params = params.set('nomProduit', nomProduit.trim());
    }

    return this.http.get<Ingredient[]>(`${this.API_URL}/search`, {
      params,
    });
  }

  // =========================================================
  // CREATE
  // =========================================================

  create(ingredient: Partial<Ingredient>): Observable<Ingredient> {
    return this.http.post<Ingredient>(`${this.API_URL}/ajouter`, ingredient);
  }

  // =========================================================
  // UPDATE
  // =========================================================

  update(id: number, ingredient: Partial<Ingredient>): Observable<Ingredient> {
    return this.http.put<Ingredient>(`${this.API_URL}/modifier/${id}`, ingredient);
  }

  // =========================================================
  // DELETE
  // =========================================================

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/delete/${id}`);
  }

  // =========================================================
  // SOUS LE SEUIL D'ALERTE
  // =========================================================

  getUnderSeuil(): Observable<Ingredient[]> {
    return this.http.get<Ingredient[]>(`${this.API_URL}/under-seuil`);
  }

  // =========================================================
  // PROCHE DE LA PÉREMPTION
  // =========================================================

  getPresDePeremption(nbrJourRestant: number): Observable<Ingredient[]> {
    const params = new HttpParams().set('nbrJourRestant', nbrJourRestant);

    return this.http.get<Ingredient[]>(`${this.API_URL}/pres-de-peremption`, {
      params,
    });
  }

  // =========================================================
  // PÉRIMÉS
  // =========================================================

  getPerimes(): Observable<Ingredient[]> {
    return this.http.get<Ingredient[]>(`${this.API_URL}/perimes`);
  }
}
