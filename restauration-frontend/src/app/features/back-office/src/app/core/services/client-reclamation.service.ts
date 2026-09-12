import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  ClientReclamation,
  ClientReclamationRequest,
} from '../models/client-reclamation.model';

/**
 * Ne réutilise volontairement pas un éventuel service back-office des
 * réclamations : voir ClientReclamationController côté backend
 * (ReclamationController existant est inutilisable pour un client
 * authentifié — même bug pattern que pour le menu et les commandes).
 */
@Injectable({
  providedIn: 'root',
})
export class ClientReclamationService {
  private readonly API_URL = `${environment.apiUrl}/client/reclamations`;

  constructor(private http: HttpClient) {}

  creer(request: ClientReclamationRequest): Observable<ClientReclamation> {
    return this.http.post<ClientReclamation>(this.API_URL, request);
  }

  getMesReclamations(): Observable<ClientReclamation[]> {
    return this.http.get<ClientReclamation[]>(this.API_URL);
  }

  getMaReclamation(id: number): Observable<ClientReclamation> {
    return this.http.get<ClientReclamation>(`${this.API_URL}/${id}`);
  }
}
