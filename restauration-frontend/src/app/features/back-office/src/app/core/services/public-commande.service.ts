import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Commande } from '../models/commande.model';

@Injectable({ providedIn: 'root' })
export class PublicCommandeService {
  private readonly API_URL = `${environment.apiUrl}/public/commandes`;

  constructor(private http: HttpClient) {}

  creerCommandeAnonyme(commande: Commande): Observable<Commande> {
    return this.http.post<Commande>(this.API_URL, commande);
  }
}
