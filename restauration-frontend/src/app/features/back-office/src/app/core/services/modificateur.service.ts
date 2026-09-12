import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Modificateur } from '../models/modificateur.model';

@Injectable({
  providedIn: 'root',
})
export class ModificateurService {
  private readonly API_URL = `${environment.apiUrl}/produits/modificateurs`;

  constructor(private http: HttpClient) {}

  create(modificateur: Modificateur): Observable<Modificateur> {
    return this.http.post<Modificateur>(this.API_URL, modificateur);
  }

  update(id: number, modificateur: Modificateur): Observable<Modificateur> {
    return this.http.put<Modificateur>(`${this.API_URL}/${id}`, modificateur);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getById(id: number): Observable<Modificateur> {
    return this.http.get<Modificateur>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<Modificateur[]> {
    return this.http.get<Modificateur[]>(this.API_URL);
  }

  getByProduit(produit: any): Observable<Modificateur[]> {
    return this.http.post<Modificateur[]>(`${this.API_URL}/by-produit`, produit);
  }
}
