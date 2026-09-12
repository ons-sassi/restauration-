import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Penalite } from '../models/penalite.model';

@Injectable({
  providedIn: 'root',
})
export class PenaliteService {
  private readonly API_URL = `${environment.apiUrl}/rh/penalites`;

  constructor(private http: HttpClient) {}

  create(penalite: Penalite): Observable<Penalite> {
    return this.http.post<Penalite>(this.API_URL, penalite);
  }

  update(id: number, penalite: Penalite): Observable<Penalite> {
    return this.http.put<Penalite>(`${this.API_URL}/${id}`, penalite);
  }

  getById(id: number): Observable<Penalite> {
    return this.http.get<Penalite>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<Penalite[]> {
    return this.http.get<Penalite[]>(this.API_URL);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<Penalite[]> {
    return this.http.get<Penalite[]>(`${this.API_URL}/employee/${employeeId}`);
  }

  getByDate(dateDebut: Date, dateFin: Date): Observable<Penalite[]> {
    return this.http.get<Penalite[]>(`${this.API_URL}/date`, {
      params: {
        dateDebut: dateDebut.toISOString(),
        dateFin: dateFin.toISOString(),
      },
    });
  }

  getByEmployeeDate(employeeId: number, dateDebut: Date, dateFin: Date): Observable<Penalite[]> {
    return this.http.get<Penalite[]>(`${this.API_URL}/employee/${employeeId}/date`, {
      params: {
        dateDebut: dateDebut.toISOString(),
        dateFin: dateFin.toISOString(),
      },
    });
  }
}
