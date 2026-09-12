import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ModePaiement } from '../models/mode-paiement.model';

@Injectable({
  providedIn: 'root',
})
export class ModePaiementService {
  private readonly API_URL = `${environment.apiUrl}/modes-paiement`;

  constructor(private http: HttpClient) {}

  create(mode: ModePaiement): Observable<ModePaiement> {
    return this.http.post<ModePaiement>(this.API_URL, mode);
  }

  getAll(): Observable<ModePaiement[]> {
    return this.http.get<ModePaiement[]>(this.API_URL);
  }

  getById(id: number): Observable<ModePaiement> {
    return this.http.get<ModePaiement>(`${this.API_URL}/${id}`);
  }

  update(id: number, mode: ModePaiement): Observable<ModePaiement> {
    return this.http.put<ModePaiement>(`${this.API_URL}/${id}`, mode);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getActifs(): Observable<ModePaiement[]> {
    return this.http.get<ModePaiement[]>(`${this.API_URL}/actifs`);
  }

  getByClient(clientId: number): Observable<ModePaiement[]> {
    return this.http.get<ModePaiement[]>(`${this.API_URL}/client/${clientId}`);
  }

  activer(id: number): Observable<ModePaiement> {
    return this.http.patch<ModePaiement>(`${this.API_URL}/${id}/activer`, null);
  }

  desactiver(id: number): Observable<ModePaiement> {
    return this.http.patch<ModePaiement>(`${this.API_URL}/${id}/desactiver`, null);
  }
}
