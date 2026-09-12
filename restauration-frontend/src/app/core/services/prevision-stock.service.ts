import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PrevisionStock } from '../models/prevision-stock.model';

@Injectable({
  providedIn: 'root',
})
export class PrevisionStockService {
  private readonly API_URL = `${environment.apiUrl}/previsions-stock`;

  constructor(private http: HttpClient) {}

  create(prevision: PrevisionStock): Observable<PrevisionStock> {
    return this.http.post<PrevisionStock>(this.API_URL, prevision);
  }

  getById(id: number): Observable<PrevisionStock> {
    return this.http.get<PrevisionStock>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<PrevisionStock[]> {
    return this.http.get<PrevisionStock[]>(this.API_URL);
  }

  update(id: number, prevision: PrevisionStock): Observable<PrevisionStock> {
    return this.http.put<PrevisionStock>(`${this.API_URL}/update/${id}`, prevision);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByPeriode(periode: string): Observable<PrevisionStock[]> {
    return this.http.get<PrevisionStock[]>(`${this.API_URL}/periode/${periode}`);
  }

  getBaseeSurVentes(baseeSurVentes: boolean): Observable<PrevisionStock[]> {
    return this.http.get<PrevisionStock[]>(`${this.API_URL}/basee-sur-ventes/${baseeSurVentes}`);
  }

  createForIngredient(ingredient: any): Observable<PrevisionStock> {
    return this.http.post<PrevisionStock>(`${this.API_URL}/ingredient`, ingredient);
  }
}
