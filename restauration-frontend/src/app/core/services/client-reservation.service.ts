import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ClientReservation, ClientReservationRequest } from '../models/client-reservation.model';

/**
 * Ne réutilise volontairement pas un éventuel service back-office des
 * réservations : voir ClientReservationController côté backend
 * (ReservationController existant est inutilisable pour un client
 * authentifié — même bug pattern que pour les réclamations et les
 * commandes).
 */
@Injectable({
  providedIn: 'root',
})
export class ClientReservationService {
  private readonly API_URL = `${environment.apiUrl}/client/reservations`;

  constructor(private http: HttpClient) {}

  creer(request: ClientReservationRequest): Observable<ClientReservation> {
    return this.http.post<ClientReservation>(this.API_URL, request);
  }

  getMesReservations(): Observable<ClientReservation[]> {
    return this.http.get<ClientReservation[]>(this.API_URL);
  }

  getMaReservation(id: number): Observable<ClientReservation> {
    return this.http.get<ClientReservation>(`${this.API_URL}/${id}`);
  }

  annuler(id: number): Observable<ClientReservation> {
    return this.http.patch<ClientReservation>(`${this.API_URL}/${id}/annuler`, {});
  }
}
