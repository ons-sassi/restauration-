import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Reclamation } from '../models/reclamation.model';

@Injectable({
  providedIn: 'root',
})
export class ReclamationService {
  private readonly API_URL = `${environment.apiUrl}/reclamations`;

  constructor(private http: HttpClient) {}

  create(reclamation: Reclamation): Observable<Reclamation> {
    return this.http.post<Reclamation>(this.API_URL, reclamation);
  }

  getById(id: number): Observable<Reclamation> {
    return this.http.get<Reclamation>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<Reclamation[]> {
    return this.http.get<Reclamation[]>(this.API_URL);
  }

  update(id: number, reclamation: Reclamation): Observable<Reclamation> {
    return this.http.put<Reclamation>(`${this.API_URL}/${id}`, reclamation);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByClient(client: any): Observable<Reclamation[]> {
    return this.http.post<Reclamation[]>(`${this.API_URL}/client`, client);
  }

  getByCommande(commande: any): Observable<Reclamation[]> {
    return this.http.post<Reclamation[]>(`${this.API_URL}/commande`, commande);
  }

  getByStatut(statut: string): Observable<Reclamation[]> {
    return this.http.get<Reclamation[]>(`${this.API_URL}/statut/${statut}`);
  }

  repondre(id: number, reponse: string): Observable<Reclamation> {
    const params = new HttpParams().set('reponse', reponse);

    return this.http.patch<Reclamation>(`${this.API_URL}/${id}/repondre`, null, { params });
  }
}
