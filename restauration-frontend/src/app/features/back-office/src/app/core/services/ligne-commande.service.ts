import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LigneCommande } from '../models/ligne-commande.model';

@Injectable({
  providedIn: 'root',
})
export class LigneCommandeService {
  private readonly API_URL = `${environment.apiUrl}/commandes`;

  constructor(private http: HttpClient) {}

  getByCommande(commandeId: number): Observable<LigneCommande[]> {
    return this.http.get<LigneCommande[]>(`${this.API_URL}/${commandeId}/lignes`);
  }

  create(commandeId: number, ligne: LigneCommande): Observable<LigneCommande> {
    return this.http.post<LigneCommande>(`${this.API_URL}/${commandeId}/lignes`, ligne);
  }

  update(ligneId: number, ligne: LigneCommande): Observable<LigneCommande> {
    return this.http.put<LigneCommande>(`${this.API_URL}/lignes/${ligneId}`, ligne);
  }

  delete(ligneId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/lignes/${ligneId}`);
  }
}
