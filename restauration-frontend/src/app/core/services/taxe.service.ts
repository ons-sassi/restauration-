import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Taxe } from '../models/taxe.model';

@Injectable({
  providedIn: 'root',
})
export class TaxeService {
  private readonly API_URL = `${environment.apiUrl}/taxes`;

  constructor(private http: HttpClient) {}

  create(taxe: Taxe): Observable<Taxe> {
    return this.http.post<Taxe>(this.API_URL, taxe);
  }

  getAll(): Observable<Taxe[]> {
    return this.http.get<Taxe[]>(this.API_URL);
  }

  getById(id: number): Observable<Taxe> {
    return this.http.get<Taxe>(`${this.API_URL}/${id}`);
  }

  update(id: number, taxe: Taxe): Observable<Taxe> {
    return this.http.put<Taxe>(`${this.API_URL}/${id}`, taxe);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  search(keyword?: string): Observable<Taxe[]> {
    let params = new HttpParams();

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<Taxe[]>(`${this.API_URL}/search`, { params });
  }

  getByStatut(statut: string): Observable<Taxe[]> {
    return this.http.get<Taxe[]>(`${this.API_URL}/statut/${statut}`);
  }

  getActives(): Observable<Taxe[]> {
    return this.http.get<Taxe[]>(`${this.API_URL}/actives`);
  }
}
