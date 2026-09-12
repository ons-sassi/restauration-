import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Reduction } from '../models/reduction.model';

@Injectable({
  providedIn: 'root',
})
export class ReductionService {
  private readonly API_URL = `${environment.apiUrl}/reductions`;

  constructor(private http: HttpClient) {}

  create(reduction: Reduction): Observable<Reduction> {
    return this.http.post<Reduction>(this.API_URL, reduction);
  }

  getById(id: number): Observable<Reduction> {
    return this.http.get<Reduction>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<Reduction[]> {
    return this.http.get<Reduction[]>(this.API_URL);
  }

  update(id: number, reduction: Reduction): Observable<Reduction> {
    return this.http.put<Reduction>(`${this.API_URL}/${id}`, reduction);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByType(type: string): Observable<Reduction[]> {
    return this.http.get<Reduction[]>(`${this.API_URL}/type/${type}`);
  }

  getActives(): Observable<Reduction[]> {
    return this.http.get<Reduction[]>(`${this.API_URL}/actives`);
  }

  activer(id: number): Observable<Reduction> {
    return this.http.put<Reduction>(`${this.API_URL}/${id}/activer`, {});
  }

  desactiver(id: number): Observable<Reduction> {
    return this.http.put<Reduction>(`${this.API_URL}/${id}/desactiver`, {});
  }

  getByDates(dateDebut: Date, dateFin: Date): Observable<Reduction[]> {
    return this.http.get<Reduction[]>(`${this.API_URL}/dates`, {
      params: {
        dateDebut: dateDebut.toISOString(),
        dateFin: dateFin.toISOString(),
      },
    });
  }
}
