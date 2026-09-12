import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ClientNonAuthentifie } from '../models/client-non-authentifie.model';
import { Restaurant } from '../models/restaurant.model';
import { TableRestaurant } from '../models/table-restaurant.model';

@Injectable({
  providedIn: 'root',
})
export class ClientNonAuthentifieService {
  private readonly API_URL = `${environment.apiUrl}/clients-non-authentifies`;

  constructor(private http: HttpClient) {}

  create(client: ClientNonAuthentifie): Observable<ClientNonAuthentifie> {
    return this.http.post<ClientNonAuthentifie>(this.API_URL, client);
  }

  getAll(): Observable<ClientNonAuthentifie[]> {
    return this.http.get<ClientNonAuthentifie[]>(this.API_URL);
  }

  getById(id: number): Observable<ClientNonAuthentifie> {
    return this.http.get<ClientNonAuthentifie>(`${this.API_URL}/${id}`);
  }

  update(id: number, client: ClientNonAuthentifie): Observable<ClientNonAuthentifie> {
    return this.http.put<ClientNonAuthentifie>(`${this.API_URL}/${id}`, client);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getByRestaurant(restaurant: Restaurant): Observable<ClientNonAuthentifie[]> {
    return this.http.post<ClientNonAuthentifie[]>(`${this.API_URL}/restaurant`, restaurant);
  }

  getByTable(table: TableRestaurant): Observable<ClientNonAuthentifie[]> {
    return this.http.post<ClientNonAuthentifie[]>(`${this.API_URL}/table`, table);
  }

  getByOrigine(origine: string): Observable<ClientNonAuthentifie[]> {
    return this.http.get<ClientNonAuthentifie[]>(`${this.API_URL}/origine/${origine}`);
  }

  getByRestaurantAndTable(
    restaurantId: number,
    tableId: number,
  ): Observable<ClientNonAuthentifie[]> {
    const params = new HttpParams()
      .set('restaurantId', restaurantId.toString())
      .set('tableId', tableId.toString());

    return this.http.get<ClientNonAuthentifie[]>(`${this.API_URL}/restaurant/table`, { params });
  }

  fermerSession(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/fermer`, null);
  }

  sessionActive(id: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.API_URL}/${id}/active`);
  }
}
