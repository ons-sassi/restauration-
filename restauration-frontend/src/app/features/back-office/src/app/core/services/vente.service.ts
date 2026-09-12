import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Vente } from '../models/vente.model';
import { VenteParArticle } from '../models/vente-par-article.model';
import { VenteParCategorie } from '../models/vente-par-categorie.model';
import { VenteParModePaiement } from '../models/vente-par-mode-paiement.model';
import { VenteParRecu } from '../models/vente-par-recu.model';
import { VenteParEmployee } from '../models/vente-par-employee.model';
import { VenteParModificateur } from '../models/vente-par-modificateur.model';
import { VenteParReduction } from '../models/vente-par-reduction.model';
import { VenteRecapitulatif } from '../models/vente-recapitulatif.model';

@Injectable({
  providedIn: 'root',
})
export class VenteService {
  private readonly API_URL = `${environment.apiUrl}/ventes`;

  constructor(private http: HttpClient) {}

  // ============================================================
  // TOUTES LES VENTES
  // ============================================================

  getAll(): Observable<Vente[]> {
    return this.http.get<Vente[]>(`${this.API_URL}`);
  }

  // ============================================================
  // VENTE PAR ID
  // ============================================================

  getById(id: number): Observable<Vente> {
    return this.http.get<Vente>(`${this.API_URL}/${id}`);
  }

  // ============================================================
  // CREER UNE VENTE
  // ============================================================

  save(vente: Vente): Observable<Vente> {
    return this.http.post<Vente>(`${this.API_URL}`, vente);
  }

  // ============================================================
  // MODIFIER UNE VENTE
  // ============================================================

  update(id: number, vente: Vente): Observable<Vente> {
    return this.http.put<Vente>(`${this.API_URL}/${id}`, vente);
  }

  // ============================================================
  // SUPPRIMER UNE VENTE
  // ============================================================

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  // ============================================================
  // RECHERCHE
  // ============================================================

  search(
    pointDeVenteId?: number | null,
    employeeId?: number | null,
    modePaiementId?: number | null,
    dateDebut?: string | null,
    dateFin?: string | null,
    montantHtMin?: number | null,
    montantHtMax?: number | null,
    montantTtcMin?: number | null,
    montantTtcMax?: number | null,
  ): Observable<Vente[]> {
    let params = new HttpParams();

    if (pointDeVenteId != null) {
      params = params.set('pointDeVenteId', pointDeVenteId);
    }

    if (employeeId != null) {
      params = params.set('employeeId', employeeId);
    }

    if (modePaiementId != null) {
      params = params.set('modePaiementId', modePaiementId);
    }

    if (dateDebut) {
      params = params.set('dateDebut', dateDebut);
    }

    if (dateFin) {
      params = params.set('dateFin', dateFin);
    }

    if (montantHtMin != null) {
      params = params.set('montantHtMin', montantHtMin);
    }

    if (montantHtMax != null) {
      params = params.set('montantHtMax', montantHtMax);
    }

    if (montantTtcMin != null) {
      params = params.set('montantTtcMin', montantTtcMin);
    }

    if (montantTtcMax != null) {
      params = params.set('montantTtcMax', montantTtcMax);
    }

    return this.http.get<Vente[]>(`${this.API_URL}/search`, { params });
  }

  // ============================================================
  // RECAPITULATIF
  // ============================================================

  getRecapitulatif(
    pointDeVenteId: number | null,
    dateDebut: string,
    dateFin: string,
    periode: string,
  ): Observable<VenteRecapitulatif> {
    let params = new HttpParams()
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin)
      .set('periode', periode);

    if (pointDeVenteId != null) {
      params = params.set('pointDeVenteId', pointDeVenteId);
    }

    return this.http.get<VenteRecapitulatif>(`${this.API_URL}/recapitulatif`, { params });
  }

  // ============================================================
  // VENTES PAR ARTICLE
  // ============================================================

  getVentesParArticle(
    pointDeVenteId: number | null,
    dateDebut: string,
    dateFin: string,
  ): Observable<VenteParArticle[]> {
    let params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);

    if (pointDeVenteId != null) {
      params = params.set('pointDeVenteId', pointDeVenteId);
    }

    return this.http.get<VenteParArticle[]>(`${this.API_URL}/par-article`, { params });
  }

  // ============================================================
  // VENTES PAR CATEGORIE
  // ============================================================

  getVentesParCategorie(
    pointDeVenteId: number | null,
    dateDebut: string,
    dateFin: string,
  ): Observable<VenteParCategorie[]> {
    let params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);

    if (pointDeVenteId != null) {
      params = params.set('pointDeVenteId', pointDeVenteId);
    }

    return this.http.get<VenteParCategorie[]>(`${this.API_URL}/par-categorie`, { params });
  }

  // ============================================================
  // VENTES PAR MODE DE PAIEMENT
  // ============================================================

  getVentesParModePaiement(
    pointDeVenteId: number | null,
    dateDebut: string,
    dateFin: string,
  ): Observable<VenteParModePaiement[]> {
    let params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);

    if (pointDeVenteId != null) {
      params = params.set('pointDeVenteId', pointDeVenteId);
    }

    return this.http.get<VenteParModePaiement[]>(`${this.API_URL}/ventes-par-mode-paiement`, {
      params,
    });
  }

  // ============================================================
  // VENTES PAR REÇU
  // ============================================================

  getVentesParRecu(
    pointDeVenteId: number | null,
    dateDebut: string,
    dateFin: string,
  ): Observable<VenteParRecu[]> {
    let params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);

    if (pointDeVenteId != null) {
      params = params.set('pointDeVenteId', pointDeVenteId);
    }

    return this.http.get<VenteParRecu[]>(`${this.API_URL}/ventes-par-recu`, { params });
  }

  // ============================================================
  // VENTES PAR EMPLOYE
  // ============================================================

  getVentesParEmployee(
    pointDeVenteId: number | null,
    dateDebut: string,
    dateFin: string,
  ): Observable<VenteParEmployee[]> {
    let params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);

    if (pointDeVenteId != null) {
      params = params.set('pointDeVenteId', pointDeVenteId);
    }

    return this.http.get<VenteParEmployee[]>(`${this.API_URL}/par-employe`, { params });
  }

  // ============================================================
  // VENTES PAR MODIFICATEUR
  // ============================================================

  getVentesParModificateur(
    pointDeVenteId: number | null,
    dateDebut: string,
    dateFin: string,
  ): Observable<VenteParModificateur[]> {
    let params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);

    if (pointDeVenteId != null) {
      params = params.set('pointDeVenteId', pointDeVenteId);
    }

    return this.http.get<VenteParModificateur[]>(`${this.API_URL}/par-modificateur`, { params });
  }

  // ============================================================
  // VENTES PAR REDUCTION
  // ============================================================

  getVentesParReduction(
    pointDeVenteId: number | null,
    dateDebut: string,
    dateFin: string,
  ): Observable<VenteParReduction[]> {
    let params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);

    if (pointDeVenteId != null) {
      params = params.set('pointDeVenteId', pointDeVenteId);
    }

    return this.http.get<VenteParReduction[]>(`${this.API_URL}/par-reduction`, { params });
  }

  // ============================================================
  // VENTES PAR COMMANDE
  // ============================================================

  getByCommande(commandeId: number): Observable<Vente[]> {
    return this.http.get<Vente[]>(`${this.API_URL}/commande/${commandeId}`);
  }

  // ============================================================
  // VENTES PAR PDV
  // ============================================================

  getByPointDeVente(pointDeVenteId: number): Observable<Vente[]> {
    return this.http.get<Vente[]>(`${this.API_URL}/point-de-vente/${pointDeVenteId}`);
  }

  // ============================================================
  // VENTES PAR EMPLOYE
  // ============================================================

  getByEmployee(employeeId: number): Observable<Vente[]> {
    return this.http.get<Vente[]>(`${this.API_URL}/employee/${employeeId}`);
  }

  // ============================================================
  // VENTES PAR MODE DE PAIEMENT
  // ============================================================

  getByModePaiement(modePaiementId: number): Observable<Vente[]> {
    return this.http.get<Vente[]>(`${this.API_URL}/mode-paiement/${modePaiementId}`);
  }

  // ============================================================
  // VENTE PAR RECU
  // ============================================================

  getByRecu(recuId: number): Observable<Vente> {
    return this.http.get<Vente>(`${this.API_URL}/recu/${recuId}`);
  }

  // ============================================================
  // VENTES PAR DATE
  // ============================================================

  getByDate(dateDebut: string, dateFin: string): Observable<Vente[]> {
    const params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);

    return this.http.get<Vente[]>(`${this.API_URL}/date`, { params });
  }

  // ============================================================
  // VENTES PAR PDV + DATE
  // ============================================================

  getByPointDeVenteAndDate(
    pointDeVenteId: number,
    dateDebut: string,
    dateFin: string,
  ): Observable<Vente[]> {
    const params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);

    return this.http.get<Vente[]>(`${this.API_URL}/point-de-vente/date/${pointDeVenteId}`, {
      params,
    });
  }
}
