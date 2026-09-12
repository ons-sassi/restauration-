import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Recu } from '../models/recu.model';

@Injectable({
  providedIn: 'root',
})
export class RecuService {
  private readonly API_URL = `${environment.apiUrl}/ventes/recus`;

  constructor(private http: HttpClient) {}

  create(recu: Recu): Observable<Recu> {
    return this.http.post<Recu>(this.API_URL, recu);
  }

  getAll(): Observable<Recu[]> {
    return this.http.get<Recu[]>(this.API_URL);
  }

  getById(id: number): Observable<Recu> {
    return this.http.get<Recu>(`${this.API_URL}/${id}`);
  }

  update(id: number, recu: Recu): Observable<Recu> {
    return this.http.put<Recu>(`${this.API_URL}/${id}`, recu);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
