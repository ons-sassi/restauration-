import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { SessionCaisse, SessionCaisseResume } from '../models/session-caisse.model';
import { PointDeVente } from '../models/point-de-vente.model';
import { Employee } from '../models/employee.model';

@Injectable({
  providedIn: 'root',
})
export class SessionCaisseService {
  private readonly API_URL = `${environment.apiUrl}/sessionCaisses`;

  constructor(private http: HttpClient) {}

  save(session: SessionCaisse): Observable<SessionCaisse> {
    return this.http.post<SessionCaisse>(this.API_URL, session);
  }

  ouvrir(montantOuverture: number, pointDeVente: PointDeVente): Observable<SessionCaisse> {
    const params = new HttpParams().set('montantOuverture', montantOuverture.toString());

    /*
     * NE PAS envoyer employeeId.
     *
     * Le backend récupère l'employé connecté.
     */

    return this.http.post<SessionCaisse>(`${this.API_URL}/ouvrir`, pointDeVente, { params });
  }

  fermer(id: number, montantFermeture: number): Observable<SessionCaisse> {
    const params = new HttpParams().set('montantFermeture', montantFermeture.toString());

    return this.http.put<SessionCaisse>(`${this.API_URL}/${id}/fermer`, null, { params });
  }

  update(id: number, session: SessionCaisse): Observable<SessionCaisse> {
    return this.http.put<SessionCaisse>(`${this.API_URL}/${id}`, session);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getById(id: number): Observable<SessionCaisse> {
    return this.http.get<SessionCaisse>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<SessionCaisse[]> {
    return this.http.get<SessionCaisse[]>(this.API_URL);
  }

  getByEmployee(employee: Employee): Observable<SessionCaisse[]> {
    return this.http.post<SessionCaisse[]>(`${this.API_URL}/employee`, employee);
  }

  getByPdv(pdv: PointDeVente): Observable<SessionCaisse[]> {
    return this.http.post<SessionCaisse[]>(`${this.API_URL}/pdv`, pdv);
  }

  getOuverte(pdv: PointDeVente): Observable<SessionCaisse> {
    return this.http.post<SessionCaisse>(`${this.API_URL}/ouverte`, pdv);
  }

  getByDate(dateDebut: Date, dateFin: Date): Observable<SessionCaisse[]> {
    const params = new HttpParams()
      .set('dateDebut', dateDebut.toISOString())
      .set('dateFin', dateFin.toISOString());

    return this.http.get<SessionCaisse[]>(`${this.API_URL}/date`, { params });
  }

  /**
   * Résumé financier d'une session : total vendu, montant
   * théorique attendu, et écart (surplus / manque) par rapport
   * à un montant compté.
   *
   * `montantCompte` est optionnel : sans lui, le backend calcule
   * quand même le total des ventes et le montant théorique
   * (utile pour une prévisualisation avant saisie), et réutilise
   * le montant de fermeture déjà enregistré si la session est
   * déjà fermée.
   */
  getResume(id: number, montantCompte?: number | null): Observable<SessionCaisseResume> {
    let params = new HttpParams();

    if (montantCompte != null) {
      params = params.set('montantCompte', montantCompte.toString());
    }

    return this.http.get<SessionCaisseResume>(`${this.API_URL}/${id}/resume`, { params });
  }
}
