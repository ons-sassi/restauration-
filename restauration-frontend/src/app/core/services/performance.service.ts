import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Performance } from '../models/performance.model';

@Injectable({
  providedIn: 'root',
})
export class PerformanceService {
  private readonly API_URL = `${environment.apiUrl}/rh/performances`;

  constructor(private http: HttpClient) {}

  create(performance: Performance): Observable<Performance> {
    return this.http.post<Performance>(this.API_URL, performance);
  }

  update(id: number, performance: Performance): Observable<Performance> {
    return this.http.put<Performance>(`${this.API_URL}/${id}`, performance);
  }

  getById(id: number): Observable<Performance> {
    return this.http.get<Performance>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<Performance[]> {
    return this.http.get<Performance[]>(this.API_URL);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByEmployee(employeeId: number): Observable<Performance[]> {
    return this.http.get<Performance[]>(`${this.API_URL}/employee/${employeeId}`);
  }

  getByPeriode(periode: string): Observable<Performance[]> {
    return this.http.get<Performance[]>(`${this.API_URL}/periode/${periode}`);
  }

  getByEmployeePeriode(employeeId: number, periode: string): Observable<Performance[]> {
    return this.http.get<Performance[]>(
      `${this.API_URL}/employee/${employeeId}/periode/${periode}`,
    );
  }
}
