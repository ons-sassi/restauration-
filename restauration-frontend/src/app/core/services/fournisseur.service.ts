import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Fournisseur } from '../models/fournisseur.model';

@Injectable({
  providedIn: 'root',
})
export class FournisseurService {
  private readonly API_URL = `${environment.apiUrl}/fournisseurs`;

  constructor(private readonly http: HttpClient) {}

  // =========================================================
  // GET ALL
  // =========================================================

  getAll(): Observable<Fournisseur[]> {
    return this.http.get<Fournisseur[]>(this.API_URL);
  }

  // =========================================================
  // GET BY ID
  // =========================================================

  getById(id: number): Observable<Fournisseur> {
    return this.http.get<Fournisseur>(`${this.API_URL}/${id}`);
  }

  // =========================================================
  // SEARCH
  // =========================================================

  search(nom?: string, adresse?: string, email?: string, num?: string): Observable<Fournisseur[]> {
    let params = new HttpParams();

    if (nom && nom.trim()) {
      params = params.set('nom', nom.trim());
    }

    if (adresse && adresse.trim()) {
      params = params.set('adresse', adresse.trim());
    }

    if (email && email.trim()) {
      params = params.set('email', email.trim());
    }

    if (num && num.trim()) {
      params = params.set('num', num.trim());
    }

    return this.http.get<Fournisseur[]>(`${this.API_URL}/search`, {
      params,
    });
  }

  // =========================================================
  // CREATE
  // =========================================================

  create(fournisseur: Partial<Fournisseur>): Observable<Fournisseur> {
    return this.http.post<Fournisseur>(`${this.API_URL}/ajouter`, fournisseur);
  }

  // =========================================================
  // UPDATE
  // =========================================================

  update(id: number, fournisseur: Partial<Fournisseur>): Observable<Fournisseur> {
    return this.http.put<Fournisseur>(`${this.API_URL}/modifier/${id}`, fournisseur);
  }

  // =========================================================
  // DELETE
  // =========================================================

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/delete/${id}`);
  }
}
