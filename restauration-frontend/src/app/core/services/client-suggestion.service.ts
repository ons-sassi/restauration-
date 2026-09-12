import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  ClientSuggestion,
  ClientSuggestionRequest,
} from '../models/client-suggestion.model';

/**
 * Ne réutilise volontairement pas un éventuel service back-office des
 * suggestions : voir ClientSuggestionController côté backend
 * (SuggestionController existant est inutilisable pour un client
 * authentifié — même bug pattern que pour le menu, les commandes et
 * les réclamations).
 */
@Injectable({
  providedIn: 'root',
})
export class ClientSuggestionService {
  private readonly API_URL = `${environment.apiUrl}/client/suggestions`;

  constructor(private http: HttpClient) {}

  creer(request: ClientSuggestionRequest): Observable<ClientSuggestion> {
    return this.http.post<ClientSuggestion>(this.API_URL, request);
  }

  getMesSuggestions(): Observable<ClientSuggestion[]> {
    return this.http.get<ClientSuggestion[]>(this.API_URL);
  }
}
