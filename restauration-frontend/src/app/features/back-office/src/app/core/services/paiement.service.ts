import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ModePaiement } from '../models/mode-paiement.model';

@Injectable({
  providedIn: 'root',
})
export class PaiementService {
  private readonly API_URL = `${environment.apiUrl}/modes-paiement`;

  constructor(private http: HttpClient) {}

  getModesPaiement(): Observable<ModePaiement[]> {
    return this.http.get<ModePaiement[]>(this.API_URL);
  }

  getModesActifs(): Observable<ModePaiement[]> {
    return this.http.get<ModePaiement[]>(`${this.API_URL}/actifs`);
  }

  getById(id: number): Observable<ModePaiement> {
    return this.http.get<ModePaiement>(`${this.API_URL}/${id}`);
  }
}
