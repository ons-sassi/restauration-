import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { BonDeCommandeStock } from '../models/bon-de-commande-stock.model';
import { LigneBonDeCommande } from '../models/ligne-bon-de-commande.model';

@Injectable({
  providedIn: 'root',
})
export class BonCommandeStockService {
  private readonly API_URL = `${environment.apiUrl}/bonDeCommandeStocks`;

  constructor(private http: HttpClient) {}

  getById(id: number): Observable<BonDeCommandeStock> {
    return this.http.get<BonDeCommandeStock>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<BonDeCommandeStock[]> {
    return this.http.get<BonDeCommandeStock[]>(this.API_URL);
  }

  search(keyword?: string): Observable<BonDeCommandeStock[]> {
    let params = new HttpParams();

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<BonDeCommandeStock[]>(`${this.API_URL}/search`, { params });
  }

  getLignes(bonCommandeId: number): Observable<LigneBonDeCommande[]> {
    return this.http.get<LigneBonDeCommande[]>(`${this.API_URL}/${bonCommandeId}/lignes`);
  }

  getByIngredient(ingredient: any): Observable<BonDeCommandeStock[]> {
    return this.http.get<BonDeCommandeStock[]>(`${this.API_URL}/ingredient`, {
      params: {
        ingredientId: ingredient?.id_ingredient ?? ingredient,
      },
    });
  }

  updateStatut(id: number, statut: string): Observable<BonDeCommandeStock> {
    return this.http.patch<BonDeCommandeStock>(`${this.API_URL}/${id}/statut`, null, {
      params: {
        statut,
      },
    });
  }

  updateDateLivraisonPrevu(id: number, date: Date): Observable<BonDeCommandeStock> {
    return this.http.patch<BonDeCommandeStock>(`${this.API_URL}/${id}/date-livraison-prevu`, null, {
      params: {
        date: date.toISOString(),
      },
    });
  }

  modifierLigne(
    bonCommandeId: number,
    ligneId: number,
    ligne: LigneBonDeCommande,
  ): Observable<LigneBonDeCommande> {
    return this.http.put<LigneBonDeCommande>(
      `${this.API_URL}/${bonCommandeId}/lignes/modifier/${ligneId}`,
      ligne,
    );
  }

  supprimerLigne(bonCommandeId: number, ligneId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${bonCommandeId}/lignes/delete/${ligneId}`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  passerCommande(body: any): Observable<BonDeCommandeStock> {
    return this.http.post<BonDeCommandeStock>(`${this.API_URL}/passer-commande`, body);
  }
}
