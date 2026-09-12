import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { RoleFonctionnalite } from '../models/role-fonctionnalite.model';

@Injectable({
  providedIn: 'root',
})
export class RoleFonctionnaliteService {
  private readonly API_URL = `${environment.apiUrl}/roleFonctionnalites`;

  constructor(private http: HttpClient) {}

  create(value: RoleFonctionnalite): Observable<RoleFonctionnalite> {
    return this.http.post<RoleFonctionnalite>(this.API_URL, value);
  }

  update(id: number, value: RoleFonctionnalite): Observable<RoleFonctionnalite> {
    return this.http.put<RoleFonctionnalite>(`${this.API_URL}/${id}`, value);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getById(id: number): Observable<RoleFonctionnalite> {
    return this.http.get<RoleFonctionnalite>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<RoleFonctionnalite[]> {
    return this.http.get<RoleFonctionnalite[]>(this.API_URL);
  }

  getByRole(role: any): Observable<RoleFonctionnalite[]> {
    return this.http.post<RoleFonctionnalite[]>(`${this.API_URL}/by-role`, role);
  }

  getByFonctionnalite(fonctionnalite: any): Observable<RoleFonctionnalite[]> {
    return this.http.post<RoleFonctionnalite[]>(
      `${this.API_URL}/by-fonctionnalite`,
      fonctionnalite,
    );
  }

  getByInterface(interfaceType: string): Observable<RoleFonctionnalite[]> {
    return this.http.get<RoleFonctionnalite[]>(`${this.API_URL}/by-interface/${interfaceType}`);
  }

  getByRoleInterface(body: any): Observable<RoleFonctionnalite[]> {
    return this.http.post<RoleFonctionnalite[]>(`${this.API_URL}/by-role/interface`, body);
  }

  getByFonctionnaliteInterface(body: any): Observable<RoleFonctionnalite[]> {
    return this.http.post<RoleFonctionnalite[]>(
      `${this.API_URL}/by-fonctionnalite/interface`,
      body,
    );
  }

  getAutorisees(body: any): Observable<RoleFonctionnalite[]> {
    return this.http.post<RoleFonctionnalite[]>(`${this.API_URL}/autorisees`, body);
  }

  assigner(body: any): Observable<RoleFonctionnalite> {
    return this.http.post<RoleFonctionnalite>(`${this.API_URL}/assigner`, body);
  }

  updateAutorisation(id: number, autorisee: boolean): Observable<RoleFonctionnalite> {
    const params = new HttpParams().set('autorisee', autorisee.toString());

    return this.http.patch<RoleFonctionnalite>(`${this.API_URL}/${id}/autorisation`, null, {
      params,
    });
  }
}
