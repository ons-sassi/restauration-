import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Presence } from '../models/presence.model';
import { FeuillePresence } from '../models/feuille-presence.model';
import { StatutPresence } from '../models/enums/statut-presence.enum';

@Injectable({
  providedIn: 'root',
})
export class PresenceService {
  private readonly API_URL = `${environment.apiUrl}/rh/presences`;

  constructor(private http: HttpClient) {}

  create(presence: Presence): Observable<Presence> {
    return this.http.post<Presence>(this.API_URL, presence);
  }

  update(id: number, presence: Presence): Observable<Presence> {
    return this.http.put<Presence>(`${this.API_URL}/${id}`, presence);
  }

  getById(id: number): Observable<Presence> {
    return this.http.get<Presence>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<Presence[]> {
    return this.http.get<Presence[]>(this.API_URL);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<Presence[]> {
    return this.http.get<Presence[]>(`${this.API_URL}/employee/${employeeId}`);
  }

  getByDate(dateDebut: Date, dateFin: Date): Observable<Presence[]> {
    return this.http.get<Presence[]>(`${this.API_URL}/date`, {
      params: {
        dateDebut: dateDebut.toISOString(),
        dateFin: dateFin.toISOString(),
      },
    });
  }

  getByEmployeeDate(employeeId: number, dateDebut: Date, dateFin: Date): Observable<Presence[]> {
    return this.http.get<Presence[]>(`${this.API_URL}/employee/${employeeId}/date`, {
      params: {
        dateDebut: dateDebut.toISOString(),
        dateFin: dateFin.toISOString(),
      },
    });
  }

  // ===============================================================
  // PAGE "PRESENCE" — pointage par le responsable
  // ===============================================================

  // `date` au format 'yyyy-MM-dd' (celui du <input type="date">).
  getFeuille(date: string): Observable<FeuillePresence[]> {
    const params = new HttpParams().set('date', date);

    return this.http.get<FeuillePresence[]>(`${this.API_URL}/feuille`, { params });
  }

  marquer(employeeId: number, date: string, statut: StatutPresence): Observable<Presence> {
    const params = new HttpParams()
      .set('employeeId', employeeId.toString())
      .set('date', date)
      .set('statut', statut);

    return this.http.patch<Presence>(`${this.API_URL}/marquer`, null, { params });
  }
}
