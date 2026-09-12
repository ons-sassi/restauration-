import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ParametreCompte } from '../models/parametre-compte.model';
import { ModeleRecu } from '../models/modele-recu.model';

@Injectable({
  providedIn: 'root',
})
export class ParametreCompteService {
  private readonly API_URL = `${environment.apiUrl}/parametres-compte`;

  constructor(private http: HttpClient) {}

  create(parametre: ParametreCompte): Observable<ParametreCompte> {
    return this.http.post<ParametreCompte>(this.API_URL, parametre);
  }

  getAll(): Observable<ParametreCompte[]> {
    return this.http.get<ParametreCompte[]>(this.API_URL);
  }

  getById(id: number): Observable<ParametreCompte> {
    return this.http.get<ParametreCompte>(`${this.API_URL}/${id}`);
  }

  update(id: number, parametre: ParametreCompte): Observable<ParametreCompte> {
    return this.http.put<ParametreCompte>(`${this.API_URL}/${id}`, parametre);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByRestaurant(restaurantId: number): Observable<ParametreCompte> {
    return this.http.get<ParametreCompte>(`${this.API_URL}/restaurant/${restaurantId}`);
  }

  createModeleRecu(modele: ModeleRecu): Observable<ModeleRecu> {
    return this.http.post<ModeleRecu>(`${this.API_URL}/modeles-recus`, modele);
  }

  getModelesRecus(): Observable<ModeleRecu[]> {
    return this.http.get<ModeleRecu[]>(`${this.API_URL}/modeles-recus`);
  }

  getModeleRecu(id: number): Observable<ModeleRecu> {
    return this.http.get<ModeleRecu>(`${this.API_URL}/modeles-recus/${id}`);
  }

  updateModeleRecu(id: number, modele: ModeleRecu): Observable<ModeleRecu> {
    return this.http.put<ModeleRecu>(`${this.API_URL}/modeles-recus/${id}`, modele);
  }

  deleteModeleRecu(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/modeles-recus/${id}`);
  }

  getModelesRecusByRestaurant(restaurantId: number): Observable<ModeleRecu[]> {
    return this.http.get<ModeleRecu[]>(`${this.API_URL}/modeles-recus/restaurant/${restaurantId}`);
  }

  searchModelesRecus(keyword?: string): Observable<ModeleRecu[]> {
    let params = new HttpParams();

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<ModeleRecu[]>(`${this.API_URL}/modeles-recus/search`, { params });
  }

  searchModelesRecusByRestaurant(restaurantId: number, keyword?: string): Observable<ModeleRecu[]> {
    let params = new HttpParams();

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<ModeleRecu[]>(
      `${this.API_URL}/modeles-recus/restaurant/${restaurantId}/search`,
      { params },
    );
  }
}
