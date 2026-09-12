import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { TableRestaurant } from '../models/table-restaurant.model';
import { Employee } from '../models/employee.model';
import { StatutTable } from '../models/enums/statut-table.enum';

@Injectable({
  providedIn: 'root',
})
export class TableRestaurantService {
  private readonly API_URL = `${environment.apiUrl}/tables`;

  constructor(private http: HttpClient) {}

  create(table: Partial<TableRestaurant>): Observable<TableRestaurant> {
    return this.http.post<TableRestaurant>(this.API_URL, table);
  }

  update(id: number, table: TableRestaurant): Observable<TableRestaurant> {
    return this.http.put<TableRestaurant>(`${this.API_URL}/${id}`, table);
  }

  // ===========================================================
  // POSITION DANS LE PLAN VISUEL (phase 2B)
  // ===========================================================
  // Réutilise l'endpoint générique PUT /api/tables/{id} : le backend
  // (RestaurantMapper.updateTableRestaurantFromDto) ignore déjà les
  // propriétés nulles du DTO, donc n'envoyer que positionX/positionY
  // met à jour uniquement la position sans toucher au reste de la table.
  updatePosition(id: number, positionX: number, positionY: number): Observable<TableRestaurant> {
    return this.http.put<TableRestaurant>(`${this.API_URL}/${id}`, {
      positionX,
      positionY,
    });
  }

  // ===========================================================
  // CHANGEMENT DE STATUT (ex. depuis "Mes tables" en Back Office :
  // double-clic sur une table pour changer son état)
  // ===========================================================
  // Réutilise l'endpoint générique PUT /api/tables/{id} : le backend
  // (RestaurantMapper.updateTableRestaurantFromDto) ignore déjà les
  // propriétés nulles du DTO, donc n'envoyer que "statut" met à jour
  // uniquement l'état de la table sans toucher au reste (capacité,
  // responsable, position...).
  updateStatut(id: number, statut: StatutTable): Observable<TableRestaurant> {
    return this.http.put<TableRestaurant>(`${this.API_URL}/${id}`, {
      statut,
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getById(id: number): Observable<TableRestaurant> {
    return this.http.get<TableRestaurant>(`${this.API_URL}/${id}`);
  }

  getAll(): Observable<TableRestaurant[]> {
    return this.http.get<TableRestaurant[]>(this.API_URL);
  }

  getByNumero(numeroTable: number): Observable<TableRestaurant> {
    return this.http.get<TableRestaurant>(`${this.API_URL}/numero/${numeroTable}`);
  }

  getByQrCode(codeQr: string): Observable<TableRestaurant> {
    return this.http.get<TableRestaurant>(`${this.API_URL}/qr/code/${codeQr}`);
  }

  getQrUrl(id: number): Observable<string> {
    return this.http.get(`${this.API_URL}/${id}/qr`, { responseType: 'text' });
  }

  getByStatut(statut: string): Observable<TableRestaurant[]> {
    return this.http.get<TableRestaurant[]>(`${this.API_URL}/statut/${statut}`);
  }

  getByRestaurant(restaurantId: number): Observable<TableRestaurant[]> {
    const body = {
      id_restaurant: restaurantId,
    };

    return this.http.post<TableRestaurant[]>(`${this.API_URL}/by-restaurant`, body);
  }

  getByServeur(employeeId: number): Observable<TableRestaurant[]> {
    const body = {
      id_utilisateur: employeeId,
    };

    return this.http.post<TableRestaurant[]>(`${this.API_URL}/by-serveur`, body);
  }

  // ===========================================================
  // MES TABLES (phase 3A)
  // ===========================================================
  // Ne prend volontairement aucun employeeId en paramètre : le
  // backend identifie l'employé à partir du token JWT envoyé par
  // l'intercepteur d'authentification. Impossible pour un employé
  // de voir les tables d'un collègue en modifiant cette requête.
  getMyTables(): Observable<TableRestaurant[]> {
    return this.http.get<TableRestaurant[]>(`${this.API_URL}/my-tables`);
  }

  getByGenerateur(employeeId: number): Observable<TableRestaurant[]> {
    const body = {
      id_utilisateur: employeeId,
    };

    return this.http.post<TableRestaurant[]>(`${this.API_URL}/by-generateur`, body);
  }

  activerQr(id: number): Observable<TableRestaurant> {
    return this.http.patch<TableRestaurant>(`${this.API_URL}/${id}/qr/activer`, null);
  }

  desactiverQr(id: number): Observable<TableRestaurant> {
    return this.http.patch<TableRestaurant>(`${this.API_URL}/${id}/qr/desactiver`, null);
  }

  getQr(id: number): Observable<string> {
    return this.http.get(`${this.API_URL}/${id}/qr`, { responseType: 'text' });
  }

  getDisponibles(): Observable<TableRestaurant[]> {
    return this.http.get<TableRestaurant[]>(`${this.API_URL}/disponibles`);
  }

  getDisponiblesByRestaurant(restaurantId: number): Observable<TableRestaurant[]> {
    return this.http.post<TableRestaurant[]>(`${this.API_URL}/disponibles/by-restaurant`, {
      id_restaurant: restaurantId,
    });
  }

  // ===========================================================
  // ATTRIBUTION DU RESPONSABLE (serveurAttribue)
  // ===========================================================

  assignerServeur(tableId: number, employeeId: number): Observable<TableRestaurant> {
    return this.http.patch<TableRestaurant>(
      `${this.API_URL}/${tableId}/serveur/${employeeId}`,
      null,
    );
  }

  retirerServeur(tableId: number): Observable<TableRestaurant> {
    return this.http.delete<TableRestaurant>(`${this.API_URL}/${tableId}/serveur`);
  }

  getEmployesEligibles(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.API_URL}/employes-eligibles`);
  }
}
