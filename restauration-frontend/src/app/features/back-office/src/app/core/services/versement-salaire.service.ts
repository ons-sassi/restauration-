import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { VersementSalaire } from '../models/versement-salaire.model';

@Injectable({
  providedIn: 'root',
})
export class VersementSalaireService {
  private readonly API_URL = `${environment.apiUrl}/rh/versements`;

  constructor(private http: HttpClient) {}

  create(versement: VersementSalaire): Observable<VersementSalaire> {
    return this.http.post<VersementSalaire>(this.API_URL, versement);
  }

  update(id: number, versement: VersementSalaire): Observable<VersementSalaire> {
    return this.http.put<VersementSalaire>(`${this.API_URL}/${id}`, versement);
  }

  getById(id: number): Observable<VersementSalaire> {
    return this.http.get<VersementSalaire>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<VersementSalaire[]> {
    return this.http.get<VersementSalaire[]>(this.API_URL);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<VersementSalaire[]> {
    return this.http.get<VersementSalaire[]>(`${this.API_URL}/employee/${employeeId}`);
  }

  getByStatut(statut: string): Observable<VersementSalaire[]> {
    return this.http.get<VersementSalaire[]>(`${this.API_URL}/statut/${statut}`);
  }

  getByPeriode(periode: string): Observable<VersementSalaire[]> {
    return this.http.get<VersementSalaire[]>(`${this.API_URL}/periode/${periode}`);
  }

  getByEmployeeStatut(employeeId: number, statut: string): Observable<VersementSalaire[]> {
    return this.http.get<VersementSalaire[]>(
      `${this.API_URL}/employee/${employeeId}/statut/${statut}`,
    );
  }
}
