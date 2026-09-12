import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ClientAuthentifie } from '../models/client-authentifie.model';

@Injectable({
  providedIn: 'root',
})
export class ClientService {
  private readonly API_URL = `${environment.apiUrl}/clients-authentifies`;

  constructor(private http: HttpClient) {}

  create(client: ClientAuthentifie): Observable<ClientAuthentifie> {
    return this.http.post<ClientAuthentifie>(this.API_URL, client);
  }

  getAll(): Observable<ClientAuthentifie[]> {
    return this.http.get<ClientAuthentifie[]>(this.API_URL);
  }

  getById(id: number): Observable<ClientAuthentifie> {
    return this.http.get<ClientAuthentifie>(`${this.API_URL}/${id}`);
  }

  update(id: number, client: ClientAuthentifie): Observable<ClientAuthentifie> {
    return this.http.put<ClientAuthentifie>(`${this.API_URL}/${id}`, client);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  search(keyword?: string): Observable<ClientAuthentifie[]> {
    let params = new HttpParams();

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<ClientAuthentifie[]>(`${this.API_URL}/search`, { params });
  }

  getByStatut(statut: string): Observable<ClientAuthentifie[]> {
    return this.http.get<ClientAuthentifie[]>(`${this.API_URL}/statut/${statut}`);
  }

  updateDerniereConnexion(id: number): Observable<ClientAuthentifie> {
    return this.http.patch<ClientAuthentifie>(`${this.API_URL}/${id}/derniere-connexion`, null);
  }

  desactiver(id: number): Observable<ClientAuthentifie> {
    return this.http.patch<ClientAuthentifie>(`${this.API_URL}/${id}/desactiver`, null);
  }

  activer(id: number): Observable<ClientAuthentifie> {
    return this.http.patch<ClientAuthentifie>(`${this.API_URL}/${id}/activer`, null);
  }

  getByCodeParrainage(code: string): Observable<ClientAuthentifie> {
    return this.http.get<ClientAuthentifie>(`${this.API_URL}/parrainage/${code}`);
  }

  updatePreferences(id: number, preferences: any): Observable<ClientAuthentifie> {
    return this.http.patch<ClientAuthentifie>(`${this.API_URL}/${id}/preferences`, preferences);
  }

  updateAllergies(id: number, allergies: any): Observable<ClientAuthentifie> {
    return this.http.patch<ClientAuthentifie>(`${this.API_URL}/${id}/allergies`, allergies);
  }
}
