import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Employee } from '../models/employee.model';

@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  private readonly API_URL = `${environment.apiUrl}/employees`;

  constructor(private http: HttpClient) {}

  create(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.API_URL, employee);
  }

  getAll(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.API_URL);
  }

  getById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.API_URL}/${id}`);
  }

  search(keyword?: string): Observable<Employee[]> {
    let params = new HttpParams();

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<Employee[]>(`${this.API_URL}/search`, { params });
  }

  getByMatricule(matricule: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.API_URL}/matricule/${matricule}`);
  }

  getByEmail(email: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.API_URL}/email/${email}`);
  }

  getByCodePin(code: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.API_URL}/code-pin/${code}`);
  }

  getByRole(roleId: number): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/role/${roleId}`);
  }

  getByPdv(pdvId: number): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/pdv/${pdvId}`);
  }

  getByStatutPresence(statut: string): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/statut-presence/${statut}`);
  }

  update(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.API_URL}/${id}`, employee);
  }

  updateStatutPresence(id: number, statut: string): Observable<Employee> {
    const params = new HttpParams().set('statutPresence', statut);

    return this.http.patch<Employee>(`${this.API_URL}/${id}/statut-presence`, null, { params });
  }

  // ===========================================================
  // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
  // ===========================================================
  // Réservé au responsable (EMPLOYES_TABLES côté backend) : active
  // ou désactive la possibilité pour cet employé de recevoir une
  // prise en charge (automatique ou manuelle), indépendamment de
  // toute table dont il serait responsable permanent.
  updateEligibiliteAttributionAutomatique(id: number, eligible: boolean): Observable<Employee> {
    const params = new HttpParams().set('eligible', eligible);

    return this.http.patch<Employee>(
      `${this.API_URL}/${id}/eligibilite-attribution-automatique`,
      null,
      { params },
    );
  }

  // Employés actuellement éligibles (case à cocher cochée) : sert à
  // pré-remplir l'écran de configuration du responsable (§5/§26).
  getEmployesEligiblesAttributionAutomatique(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/eligibles-attribution-automatique`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
