import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Suggestion } from '../models/suggestion.model';

@Injectable({
  providedIn: 'root',
})
export class SuggestionService {
  private readonly API_URL = `${environment.apiUrl}/suggestions`;

  constructor(private http: HttpClient) {}

  create(suggestion: Suggestion): Observable<Suggestion> {
    return this.http.post<Suggestion>(this.API_URL, suggestion);
  }

  update(id: number, suggestion: Suggestion): Observable<Suggestion> {
    return this.http.put<Suggestion>(`${this.API_URL}/${id}`, suggestion);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getById(id: number): Observable<Suggestion> {
    return this.http.get<Suggestion>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<Suggestion[]> {
    return this.http.get<Suggestion[]>(this.API_URL);
  }

  getByClient(clientId: number): Observable<Suggestion[]> {
    const params = new HttpParams().set('clientId', clientId.toString());

    return this.http.get<Suggestion[]>(`${this.API_URL}/by-client`, { params });
  }

  getByPriseEnCompte(priseEnCompte: boolean): Observable<Suggestion[]> {
    const params = new HttpParams().set('priseEnCompte', priseEnCompte.toString());

    return this.http.get<Suggestion[]>(`${this.API_URL}/by-prise-en-compte`, { params });
  }

  updatePriseEnCompte(id: number, value: boolean): Observable<Suggestion> {
    const params = new HttpParams().set('priseEnCompte', value.toString());

    return this.http.patch<Suggestion>(`${this.API_URL}/${id}/prise-en-compte`, null, { params });
  }

  search(keyword?: string): Observable<Suggestion[]> {
    let params = new HttpParams();

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<Suggestion[]>(`${this.API_URL}/search`, { params });
  }

  getByDate(dateDebut: Date, dateFin: Date): Observable<Suggestion[]> {
    const params = new HttpParams()
      .set('dateDebut', dateDebut.toISOString())
      .set('dateFin', dateFin.toISOString());

    return this.http.get<Suggestion[]>(`${this.API_URL}/by-date`, { params });
  }
}
