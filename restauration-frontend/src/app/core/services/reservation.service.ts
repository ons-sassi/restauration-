import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Reservation } from '../models/reservation.model';

@Injectable({
  providedIn: 'root',
})
export class ReservationService {
  private readonly API_URL = `${environment.apiUrl}/reservations`;

  constructor(private http: HttpClient) {}

  create(reservation: Reservation): Observable<Reservation> {
    return this.http.post<Reservation>(this.API_URL, reservation);
  }

  getById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.API_URL);
  }

  update(id: number, reservation: Reservation): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.API_URL}/${id}`, reservation);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByClient(client: any): Observable<Reservation[]> {
    return this.http.post<Reservation[]>(`${this.API_URL}/client`, client);
  }

  getByRestaurant(restaurant: any): Observable<Reservation[]> {
    return this.http.post<Reservation[]>(`${this.API_URL}/restaurant`, restaurant);
  }

  getByTable(table: any): Observable<Reservation[]> {
    return this.http.post<Reservation[]>(`${this.API_URL}/table`, table);
  }

  getByEmployee(employee: any): Observable<Reservation[]> {
    return this.http.post<Reservation[]>(`${this.API_URL}/employee`, employee);
  }

  getByDate(date: Date): Observable<Reservation[]> {
    const params = new HttpParams().set('date', date.toISOString());

    return this.http.get<Reservation[]>(`${this.API_URL}/date`, { params });
  }

  getByDateTime(dateTime: Date): Observable<Reservation[]> {
    const params = new HttpParams().set('dateTime', dateTime.toISOString());

    return this.http.get<Reservation[]>(`${this.API_URL}/date-time`, { params });
  }

  getBetweenDates(dateDebut: Date, dateFin: Date): Observable<Reservation[]> {
    const params = new HttpParams()
      .set('dateDebut', dateDebut.toISOString())
      .set('dateFin', dateFin.toISOString());

    return this.http.get<Reservation[]>(`${this.API_URL}/between-dates`, { params });
  }

  getByStatut(statut: string): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.API_URL}/statut/${statut}`);
  }

  annuler(id: number): Observable<Reservation> {
    return this.http.patch<Reservation>(`${this.API_URL}/${id}/annuler`, null);
  }

  // L'employé qui confirme est déterminé côté serveur à partir du token
  // connecté (voir ReservationController) : on ne l'envoie plus depuis le
  // frontend, ce qui provoquait "Employee not found" pour un SUPERADMIN
  // (qui n'est pas un Employee).
  confirmer(reservationId: number, tableId: number): Observable<Reservation> {
    return this.http.patch<Reservation>(
      `${this.API_URL}/${reservationId}/confirmer`,
      { tableId },
    );
  }
}
