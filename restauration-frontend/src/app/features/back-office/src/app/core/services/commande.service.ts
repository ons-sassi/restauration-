import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Commande } from '../models/commande.model';
import { LigneCommande } from '../models/ligne-commande.model';

@Injectable({
  providedIn: 'root',
})
export class CommandeService {
  private readonly API_URL = `${environment.apiUrl}/commandes`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Commande[]> {
    return this.http.get<Commande[]>(this.API_URL);
  }

  getById(id: number): Observable<Commande> {
    return this.http.get<Commande>(`${this.API_URL}/${id}`);
  }

  update(id: number, commande: Commande): Observable<Commande> {
    return this.http.put<Commande>(`${this.API_URL}/${id}`, commande);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  search(
    clientId?: number,
    statut?: string,
    dateDebut?: Date,
    dateFin?: Date,
  ): Observable<Commande[]> {
    let params = new HttpParams();

    if (clientId !== undefined) {
      params = params.set('clientId', clientId.toString());
    }

    if (statut) {
      params = params.set('statut', statut);
    }

    if (dateDebut) {
      params = params.set('dateDebut', dateDebut.toISOString());
    }

    if (dateFin) {
      params = params.set('dateFin', dateFin.toISOString());
    }

    return this.http.get<Commande[]>(`${this.API_URL}/search`, { params });
  }

  getByClient(clientId: number): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.API_URL}/client/${clientId}`);
  }

  getBySession(sessionId: number): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.API_URL}/session/${sessionId}`);
  }

  getByTable(tableId: number): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.API_URL}/table/${tableId}`);
  }

  getByEmployee(employeeId: number): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.API_URL}/employee/${employeeId}`);
  }

  getByStatut(statut: string): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.API_URL}/statut/${statut}`);
  }

  getByDate(dateDebut: Date, dateFin: Date): Observable<Commande[]> {
    const params = new HttpParams()
      .set('dateDebut', dateDebut.toISOString())
      .set('dateFin', dateFin.toISOString());

    return this.http.get<Commande[]>(`${this.API_URL}/date`, { params });
  }

  changerStatut(id: number, statut: string): Observable<Commande> {
    const params = new HttpParams().set('statut', statut);

    return this.http.patch<Commande>(`${this.API_URL}/${id}/statut`, null, { params });
  }

  annuler(id: number): Observable<Commande> {
    return this.http.patch<Commande>(`${this.API_URL}/${id}/annuler`, null);
  }

  getLignes(commandeId: number): Observable<LigneCommande[]> {
    return this.http.get<LigneCommande[]>(`${this.API_URL}/${commandeId}/lignes`);
  }

  ajouterLigne(commandeId: number, ligne: LigneCommande): Observable<LigneCommande> {
    return this.http.post<LigneCommande>(`${this.API_URL}/${commandeId}/lignes`, ligne);
  }

  modifierLigne(ligneId: number, ligne: LigneCommande): Observable<LigneCommande> {
    return this.http.put<LigneCommande>(`${this.API_URL}/lignes/${ligneId}`, ligne);
  }

  supprimerLigne(ligneId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/lignes/${ligneId}`);
  }

  getMontant(commandeId: number): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/${commandeId}/montant`);
  }

  recalculerMontant(commandeId: number): Observable<Commande> {
    return this.http.patch<Commande>(`${this.API_URL}/${commandeId}/recalculer-montant`, null);
  }
}
