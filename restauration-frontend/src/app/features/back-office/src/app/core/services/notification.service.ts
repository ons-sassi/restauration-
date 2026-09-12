import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly API_URL = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  create(notification: Notification): Observable<Notification> {
    return this.http.post<Notification>(this.API_URL, notification);
  }

  getById(id: number): Observable<Notification> {
    return this.http.get<Notification>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.API_URL);
  }

  update(id: number, notification: Notification): Observable<Notification> {
    return this.http.put<Notification>(`${this.API_URL}/${id}`, notification);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByDestinataire(destinataireId: number): Observable<Notification[]> {
    const params = new HttpParams().set('destinataireId', destinataireId.toString());

    return this.http.get<Notification[]>(`${this.API_URL}/destinataire`, { params });
  }

  getByEmetteur(emetteurId: number): Observable<Notification[]> {
    const params = new HttpParams().set('emetteurId', emetteurId.toString());

    return this.http.get<Notification[]>(`${this.API_URL}/emetteur`, { params });
  }

  getByStatut(statut: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.API_URL}/statut/${statut}`);
  }

  getByType(type: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.API_URL}/type/${type}`);
  }

  getRecentes(destinataireId: number): Observable<Notification[]> {
    const params = new HttpParams().set('destinataireId', destinataireId.toString());

    return this.http.get<Notification[]>(`${this.API_URL}/destinataire/recentes`, { params });
  }
}
