import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PlanDeSalle } from '../models/plan-de-salle.model';

@Injectable({
  providedIn: 'root',
})
export class PlanSalleService {
  private readonly API_URL = `${environment.apiUrl}/plans-de-salle`;

  constructor(private http: HttpClient) {}

  create(plan: PlanDeSalle): Observable<PlanDeSalle> {
    return this.http.post<PlanDeSalle>(this.API_URL, plan);
  }

  getAll(): Observable<PlanDeSalle[]> {
    return this.http.get<PlanDeSalle[]>(this.API_URL);
  }

  getById(id: number): Observable<PlanDeSalle> {
    return this.http.get<PlanDeSalle>(`${this.API_URL}/${id}`);
  }

  update(id: number, plan: PlanDeSalle): Observable<PlanDeSalle> {
    return this.http.put<PlanDeSalle>(`${this.API_URL}/${id}`, plan);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByRestaurant(restaurantId: number): Observable<PlanDeSalle[]> {
    return this.http.get<PlanDeSalle[]>(`${this.API_URL}/restaurant/${restaurantId}`);
  }

  getCurrent(restaurantId: number): Observable<PlanDeSalle> {
    return this.http.get<PlanDeSalle>(`${this.API_URL}/restaurant/${restaurantId}/current`);
  }
}
