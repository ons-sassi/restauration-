import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ClientFavori } from '../models/client-favori.model';

/**
 * Favoris de l'espace client (voir ClientFavoriController côté
 * backend — n'existait pas du tout avant, ni entité, ni back, ni
 * front). Ajout/suppression idempotents côté backend : pas besoin de
 * gérer un état "déjà favori" comme une erreur ici.
 */
@Injectable({
  providedIn: 'root',
})
export class ClientFavoriService {
  private readonly API_URL = `${environment.apiUrl}/client/favoris`;

  constructor(private http: HttpClient) {}

  getMesFavoris(): Observable<ClientFavori[]> {
    return this.http.get<ClientFavori[]>(this.API_URL);
  }

  ajouter(produitId: number): Observable<ClientFavori> {
    return this.http.post<ClientFavori>(`${this.API_URL}/${produitId}`, {});
  }

  supprimer(produitId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${produitId}`);
  }

  estFavori(produitId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.API_URL}/${produitId}/est-favori`);
  }
}
