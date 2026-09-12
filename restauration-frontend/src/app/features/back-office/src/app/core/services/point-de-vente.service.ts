import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PointDeVente } from '../models/point-de-vente.model';

@Injectable({
  providedIn: 'root',
})
export class PointDeVenteService {
  private readonly API_URL = `${environment.apiUrl}/points-de-vente`;

  constructor(private http: HttpClient) {}

  create(pdv: PointDeVente): Observable<PointDeVente> {
    return this.http.post<PointDeVente>(this.API_URL, pdv);
  }

  getAll(): Observable<PointDeVente[]> {
    return this.http.get<PointDeVente[]>(this.API_URL);
  }

  getById(id: number): Observable<PointDeVente> {
    return this.http.get<PointDeVente>(`${this.API_URL}/${id}`);
  }

  search(keyword?: string): Observable<PointDeVente[]> {
    let params = new HttpParams();

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<PointDeVente[]>(`${this.API_URL}/search`, { params });
  }

  getByRestaurant(restaurantId: number): Observable<PointDeVente[]> {
    return this.http.get<PointDeVente[]>(`${this.API_URL}/restaurant/${restaurantId}`);
  }

  getByStatutConnexion(statut: string): Observable<PointDeVente[]> {
    return this.http.get<PointDeVente[]>(`${this.API_URL}/statut-connexion/${statut}`);
  }

  update(id: number, pdv: PointDeVente): Observable<PointDeVente> {
    return this.http.put<PointDeVente>(`${this.API_URL}/${id}`, pdv);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
