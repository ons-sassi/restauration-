import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Categorie } from '../models/categorie.model';

@Injectable({
  providedIn: 'root',
})
export class CategorieService {
  private readonly API_URL = `${environment.apiUrl}/produits/categories`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // GET TOUTES LES CATÉGORIES
  // =========================================================

  getAll(): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(this.API_URL);
  }

  // =========================================================
  // GET UNE CATÉGORIE PAR ID
  // =========================================================

  getById(id: number): Observable<Categorie> {
    return this.http.get<Categorie>(`${this.API_URL}/${id}`);
  }

  // =========================================================
  // RECHERCHER UNE CATÉGORIE PAR NOM
  // =========================================================

  getByNom(nom: string): Observable<Categorie> {
    return this.http.get<Categorie>(`${this.API_URL}/nom/${encodeURIComponent(nom)}`);
  }

  // =========================================================
  // CRÉER UNE CATÉGORIE
  // =========================================================

  create(categorie: Categorie): Observable<Categorie> {
    return this.http.post<Categorie>(this.API_URL, categorie);
  }

  // =========================================================
  // MODIFIER UNE CATÉGORIE
  // =========================================================

  update(id: number, categorie: Categorie): Observable<Categorie> {
    return this.http.put<Categorie>(`${this.API_URL}/${id}`, categorie);
  }

  // =========================================================
  // SUPPRIMER UNE CATÉGORIE
  // =========================================================

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  // =========================================================
  // SOUS-CATÉGORIES D'UNE CATÉGORIE PARENTE
  // =========================================================

  getByCategorieParent(categorie: Categorie): Observable<Categorie[]> {
    return this.http.post<Categorie[]>(`${this.API_URL}/by-parent`, categorie);
  }
}
