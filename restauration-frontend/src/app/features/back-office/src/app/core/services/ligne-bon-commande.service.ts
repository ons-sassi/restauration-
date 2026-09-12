import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LigneBonDeCommande } from '../models/ligne-bon-de-commande.model';

@Injectable({ providedIn: 'root' })
export class LigneBonDeCommandeService {
  private readonly API_URL = `${environment.apiUrl}/bonDeCommandeStocks`;

  constructor(private http: HttpClient) {}

  getByBonCommande(id: number): Observable<LigneBonDeCommande[]> {
    return this.http.get<LigneBonDeCommande[]>(`${this.API_URL}/${id}/lignes`);
  }

  update(
    bonId: number,
    ligneId: number,
    ligne: LigneBonDeCommande,
  ): Observable<LigneBonDeCommande> {
    return this.http.put<LigneBonDeCommande>(
      `${this.API_URL}/${bonId}/lignes/modifier/${ligneId}`,
      ligne,
    );
  }

  delete(bonId: number, ligneId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${bonId}/lignes/delete/${ligneId}`);
  }
}
