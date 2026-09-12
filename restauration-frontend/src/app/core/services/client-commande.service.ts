import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Commande } from '../models/commande.model';
import { ModeleRecu } from '../models/modele-recu.model';
import { TableRestaurant } from '../models/table-restaurant.model';
import {
  ClientCommandeConfirmation,
  ClientLigneCommandeConfirmation,
  ClientCommandeRequest,
} from '../models/client-commande-request.model';

@Injectable({
  providedIn: 'root',
})
export class ClientCommandeService {
  private readonly API_URL = `${environment.apiUrl}/client/commandes`;

  constructor(private http: HttpClient) {}

  /**
   * Finaliser le panier (étape 5). Le backend recalcule intégralement
   * les prix à partir des ids envoyés — voir ClientCommandeServiceImpl.
   */
  creerDepuisPanier(request: ClientCommandeRequest): Observable<ClientCommandeConfirmation> {
    return this.http.post<ClientCommandeConfirmation>(this.API_URL, request);
  }

  getAll(): Observable<Commande[]> {
    return this.http.get<Commande[]>(this.API_URL);
  }

  getById(id: number): Observable<Commande> {
    return this.http.get<Commande>(`${this.API_URL}/${id}`);
  }

  /**
   * Détail des lignes d'une commande de l'historique (produit +
   * modificateurs déjà résolus en texte par le backend — voir
   * ClientCommandeController.getMesLignesCommande).
   */
  getLignes(id: number): Observable<ClientLigneCommandeConfirmation[]> {
    return this.http.get<ClientLigneCommandeConfirmation[]>(`${this.API_URL}/${id}/lignes`);
  }

  /**
   * Tables actuellement disponibles (statut LIBRE) du restaurant du
   * client connecté. Alimente le sélecteur de table à la finalisation
   * du panier (mode "sur place" / SAISIE_MANUELLE_NUMERO_TABLE), pour
   * éviter de laisser le client taper un numéro de table au hasard.
   */
  /** Modèle de reçu du restaurant du client authentifié. */
  getModeleRecu(): Observable<ModeleRecu> {
    return this.http.get<ModeleRecu>(`${this.API_URL}/modele-recu`);
  }

  getTablesDisponibles(): Observable<TableRestaurant[]> {
    return this.http.get<TableRestaurant[]>(`${this.API_URL}/tables-disponibles`);
  }
}
