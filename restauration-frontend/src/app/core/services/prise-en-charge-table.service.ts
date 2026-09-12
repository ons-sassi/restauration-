import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PriseEnChargeTable } from '../models/prise-en-charge-table.model';
import { AttributionStats } from '../models/attribution-stats.model';

@Injectable({
  providedIn: 'root',
})
export class PriseEnChargeTableService {
  private readonly API_URL = `${environment.apiUrl}/prises-en-charge`;

  constructor(private http: HttpClient) {}

  // ===========================================================
  // Ne prend volontairement aucun employeeId en paramètre : le
  // backend déduit l'employé du token JWT (comme /tables/my-tables).
  // Un employé ne peut donc démarrer/terminer une prise en charge
  // qu'en son propre nom, jamais au nom d'un collègue.
  // ===========================================================

  commencer(tableId: number): Observable<PriseEnChargeTable> {
    return this.http.post<PriseEnChargeTable>(
      `${this.API_URL}/tables/${tableId}/commencer`,
      null,
    );
  }

  terminer(tableId: number): Observable<PriseEnChargeTable> {
    return this.http.patch<PriseEnChargeTable>(
      `${this.API_URL}/tables/${tableId}/terminer`,
      null,
    );
  }

  // Renvoie une erreur 404 (gérée par le backend via
  // PriseEnChargeTableNotFoundException) lorsqu'aucune prise en
  // charge active n'existe pour cette table : à traiter comme
  // "aucune prise en charge active", pas comme une erreur bloquante.
  getActiveByTable(tableId: number): Observable<PriseEnChargeTable> {
    return this.http.get<PriseEnChargeTable>(`${this.API_URL}/tables/${tableId}/active`);
  }

  // ===========================================================
  // PHASE 3C — ATTRIBUTION AUTOMATIQUE DES PRISES EN CHARGE
  // ===========================================================
  // Réservées au responsable (permission EMPLOYES_TABLES côté
  // backend). Contrairement à commencer()/terminer() ci-dessus,
  // c'est le responsable — pas l'employé connecté — qui déclenche
  // ces actions, et l'employé qui prendra en charge le client n'est
  // pas forcément celui connecté.

  // Le backend choisit seul l'employé (parmi ceux éligibles ayant le
  // moins de prises en charge actives) : aucun employeeId n'est
  // envoyé depuis le frontend, pour empêcher toute manipulation de
  // l'algorithme côté client (§19 de la spec).
  autoAssign(tableId: number): Observable<PriseEnChargeTable> {
    return this.http.post<PriseEnChargeTable>(
      `${this.API_URL}/tables/${tableId}/auto-assign`,
      null,
    );
  }

  // Attribution manuelle explicite par le responsable. Le backend
  // vérifie que l'employé choisi est éligible (§21) ; sinon une
  // erreur 403 est renvoyée.
  manualAssign(tableId: number, employeeId: number): Observable<PriseEnChargeTable> {
    return this.http.post<PriseEnChargeTable>(
      `${this.API_URL}/tables/${tableId}/manual-assign/${employeeId}`,
      null,
    );
  }

  // Vue d'ensemble pour le responsable (§22/§23) : charge actuelle de
  // chaque employé éligible, pour comprendre pourquoi l'automatisation
  // choisirait tel employé plutôt qu'un autre.
  getStatistiquesAttribution(): Observable<AttributionStats[]> {
    return this.http.get<AttributionStats[]>(`${this.API_URL}/statistiques-attribution`);
  }
}
