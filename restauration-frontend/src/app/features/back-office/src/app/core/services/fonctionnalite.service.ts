
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Fonctionnalite } from '../models/fonctionnalite.model';

@Injectable({
  providedIn: 'root',
})
export class FonctionnaliteService {

  private readonly API_URL =
    `${environment.apiUrl}/fonctionnalites`;

constructor(private http: HttpClient) {}

// ============================================================
// CREER
// ============================================================

create(
  fonctionnalite: Fonctionnalite
): Observable<Fonctionnalite> {

  return this.http.post<Fonctionnalite>(
    this.API_URL,
    fonctionnalite
  );
}


// ============================================================
// MODIFIER
// ============================================================

update(
  id: number,
  fonctionnalite: Fonctionnalite
): Observable<Fonctionnalite> {

  return this.http.put<Fonctionnalite>(
    `${this.API_URL}/${id}`,
    fonctionnalite
  );
}


// ============================================================
// SUPPRIMER
// ============================================================

delete(
  id: number
): Observable<void> {

  return this.http.delete<void>(
    `${this.API_URL}/${id}`
  );
}


// ============================================================
// CHERCHER PAR ID
// ============================================================

getById(
  id: number
): Observable<Fonctionnalite> {

  return this.http.get<Fonctionnalite>(
    `${this.API_URL}/${id}`
  );
}


// ============================================================
// TOUTES LES FONCTIONNALITES
// ============================================================

getAll(): Observable<Fonctionnalite[]> {

  return this.http.get<Fonctionnalite[]>(
    this.API_URL
  );
}


// ============================================================
// RECHERCHE PAR NOM
// ============================================================

searchByNom(
  nom: string
): Observable<Fonctionnalite[]> {

  const params = new HttpParams()
    .set('nom', nom);

  return this.http.get<Fonctionnalite[]>(
    `${this.API_URL}/search`,
    { params }
  );
}


// ============================================================
// RECHERCHE PAR CODE
// ============================================================

searchByCode(
  code: string
): Observable<Fonctionnalite[]> {

  const params = new HttpParams()
    .set('code', code);

  return this.http.get<Fonctionnalite[]>(
    `${this.API_URL}/search-code`,
    { params }
  );
}


// ============================================================
// FONCTIONNALITES DISPONIBLES EN PDV
// ============================================================

getDisponiblePdv(): Observable<Fonctionnalite[]> {

  return this.http.get<Fonctionnalite[]>(
    `${this.API_URL}/disponible-pdv`
  );
}


// ============================================================
// FONCTIONNALITES DISPONIBLES BACK OFFICE
// ============================================================

getDisponibleBackoffice(): Observable<Fonctionnalite[]> {

  return this.http.get<Fonctionnalite[]>(
    `${this.API_URL}/disponible-backoffice`
  );
}


// ============================================================
// FONCTIONNALITES PARENTES
// ============================================================

getParents(): Observable<Fonctionnalite[]> {

  return this.http.get<Fonctionnalite[]>(
    `${this.API_URL}/parents`
  );
}


// ============================================================
// SOUS-FONCTIONNALITES
// ============================================================

getSousFonctionnalites(
  parentId: number
): Observable<Fonctionnalite[]> {

  return this.http.get<Fonctionnalite[]>(
    `${this.API_URL}/parent/${parentId}`
  );
}


// ============================================================
// FONCTIONNALITES PAR MODULE
// ============================================================

getByModule(
  moduleId: number
): Observable<Fonctionnalite[]> {

  return this.http.get<Fonctionnalite[]>(
    `${this.API_URL}/module/${moduleId}`
  );
}
}
