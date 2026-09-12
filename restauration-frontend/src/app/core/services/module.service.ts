import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { Module } from '../models/module.model';
import { Fonctionnalite } from '../models/fonctionnalite.model';

@Injectable({
  providedIn: 'root',
})
export class ModuleService {
  private readonly API_URL = `${environment.apiUrl}/modules-fonctionnalites`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // MODULES
  // =========================================================

  /**
   * Récupérer tous les modules
   */
  getAll(): Observable<Module[]> {
    return this.http.get<Module[]>(`${this.API_URL}/modules`);
  }

  /**
   * Récupérer un module par son ID
   */
  getById(id: number): Observable<Module> {
    return this.http.get<Module>(`${this.API_URL}/modules/${id}`);
  }

  /**
   * Récupérer un module par son nom
   */
  getByNom(nom: string): Observable<Module> {
    return this.http.get<Module>(`${this.API_URL}/modules/nom/${encodeURIComponent(nom)}`);
  }

  /**
   * Créer un module
   */
  create(module: Module): Observable<Module> {
    return this.http.post<Module>(`${this.API_URL}/modules`, module);
  }

  /**
   * Modifier un module
   */
  update(id: number, module: Module): Observable<Module> {
    return this.http.put<Module>(`${this.API_URL}/modules/${id}`, module);
  }

  /**
   * Supprimer un module
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/modules/${id}`);
  }

  /**
   * Rechercher des modules
   */
  search(keyword?: string): Observable<Module[]> {
    let params = new HttpParams();

    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }

    return this.http.get<Module[]>(`${this.API_URL}/modules/search`, { params });
  }

  // =========================================================
  // DISPONIBILITÉ
  // =========================================================

  /**
   * Vérifier si le module est disponible sur le PDV
   */
  disponiblePdv(id: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.API_URL}/modules/${id}/disponible-pdv`);
  }

  /**
   * Vérifier si le module est disponible dans le Backoffice
   */
  disponibleBackoffice(id: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.API_URL}/modules/${id}/disponible-backoffice`);
  }

  // =========================================================
  // ACTIVATION / DÉSACTIVATION PDV
  // =========================================================

  /**
   * Activer un module sur le PDV
   */
  activerPdv(id: number): Observable<Module> {
    return this.http.patch<Module>(`${this.API_URL}/modules/${id}/activer-pdv`, null);
  }

  /**
   * Désactiver un module sur le PDV
   */
  desactiverPdv(id: number): Observable<Module> {
    return this.http.patch<Module>(`${this.API_URL}/modules/${id}/desactiver-pdv`, null);
  }

  // =========================================================
  // ACTIVATION / DÉSACTIVATION BACKOFFICE
  // =========================================================

  /**
   * Activer un module dans le Backoffice
   */
  activerBackoffice(id: number): Observable<Module> {
    return this.http.patch<Module>(`${this.API_URL}/modules/${id}/activer-backoffice`, null);
  }

  /**
   * Désactiver un module dans le Backoffice
   */
  desactiverBackoffice(id: number): Observable<Module> {
    return this.http.patch<Module>(`${this.API_URL}/modules/${id}/desactiver-backoffice`, null);
  }

  // =========================================================
  // FONCTIONNALITÉS
  // =========================================================

  /**
   * Récupérer les fonctionnalités d'un module
   */
  getFonctionnalites(moduleId: number): Observable<Fonctionnalite[]> {
    return this.http.get<Fonctionnalite[]>(`${this.API_URL}/modules/${moduleId}/fonctionnalites`);
  }

  /**
   * Assigner une fonctionnalité à un module
   */
  assignerFonctionnalite(moduleId: number, fonctionnaliteId: number): Observable<void> {
    return this.http.post<void>(
      `${this.API_URL}/modules/${moduleId}/fonctionnalites/${fonctionnaliteId}`,
      null,
    );
  }
}
