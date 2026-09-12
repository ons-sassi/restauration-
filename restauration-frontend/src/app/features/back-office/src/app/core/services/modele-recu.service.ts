import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ModeleRecu } from '../models/modele-recu.model';

@Injectable({
  providedIn: 'root',
})
export class ModeleRecuService {
  private readonly API_URL = `${environment.apiUrl}/parametres-compte/modeles-recus`;

  constructor(private http: HttpClient) {}

  create(modele: ModeleRecu): Observable<ModeleRecu> {
    return this.http.post<ModeleRecu>(this.API_URL, modele);
  }

  getAll(): Observable<ModeleRecu[]> {
    return this.http.get<ModeleRecu[]>(this.API_URL);
  }

  getById(id: number): Observable<ModeleRecu> {
    return this.http.get<ModeleRecu>(`${this.API_URL}/${id}`);
  }

  getByRestaurant(restaurantId: number): Observable<ModeleRecu[]> {
    return this.http.get<ModeleRecu[]>(`${this.API_URL}/restaurant/${restaurantId}`);
  }

  update(id: number, modele: ModeleRecu): Observable<ModeleRecu> {
    return this.http.put<ModeleRecu>(`${this.API_URL}/${id}`, modele);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
