import { Injectable } from '@angular/core';

import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { Dashboard } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private readonly apiUrl = `${environment.apiUrl}/back-office/dashboard`;

  constructor(private readonly http: HttpClient) {}

  getDashboard(restaurantId: number, date: string): Observable<Dashboard> {
    const params = new HttpParams().set('restaurantId', restaurantId.toString()).set('date', date);

    return this.http.get<Dashboard>(this.apiUrl, {
      params,
    });
  }
}
