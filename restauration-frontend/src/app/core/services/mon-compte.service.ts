import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { MonCompte } from '../models/mon-compte.model';

/**
 * Service dédié à la page "Mon compte" : lit/modifie le compte
 * CONNECTÉ (Employee, SuperAdmin ou ClientAuthentifie), sans jamais
 * passer d'id — le backend le déduit du token JWT (voir
 * MonCompteController). Ne pas utiliser EmployeeService ici : celui-ci
 * ne fonctionne que pour les comptes Employee.
 */
@Injectable({
  providedIn: 'root',
})
export class MonCompteService {
  private readonly API_URL = `${environment.apiUrl}/mon-compte`;

  private readonly compteSubject = new BehaviorSubject<MonCompte | null>(null);
  readonly compte$ = this.compteSubject.asObservable();

  constructor(private http: HttpClient) {}

  /** Met à jour les composants qui affichent les informations du compte. */
  private publierCompte(compte: MonCompte): void {
    this.compteSubject.next(compte);
  }

  get(): Observable<MonCompte> {
    return this.http.get<MonCompte>(this.API_URL).pipe(
      tap((compte) => this.publierCompte(compte)),
    );
  }

  update(compte: MonCompte): Observable<MonCompte> {
    return this.http.put<MonCompte>(this.API_URL, compte).pipe(
      tap((updated) => this.publierCompte(updated)),
    );
  }

  /**
   * Upload la photo de profil (fichier local) du compte connecté.
   * Le backend enregistre le fichier sur le disque et renvoie le
   * compte à jour (voir MonCompteController.uploadPhoto), avec
   * photo_profil contenant une URL relative (ex.
   * "/uploads/photos-profil/xxx.jpg") à préfixer avec
   * environment.filesBaseUrl côté composant.
   */
  uploadPhoto(fichier: File): Observable<MonCompte> {
    const formData = new FormData();
    formData.append('file', fichier);

    return this.http.post<MonCompte>(`${this.API_URL}/photo`, formData).pipe(
      tap((updated) => this.publierCompte(updated)),
    );
  }
}
