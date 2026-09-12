import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Produit } from '../models/produit.model';
import { Categorie } from '../models/categorie.model';
import { Modificateur } from '../models/modificateur.model';

/**
 * Menu (catégories / produits) pour l'espace client.
 *
 * Appelle "/api/client/menu/**" — jamais "/api/produits" (voir
 * ProduitService) : ces endpoints-là dérivent le restaurant via
 * l'employé connecté et renvoient une erreur pour un compte client.
 * Ici, le restaurant est toujours celui du client connecté, déduit
 * côté back du token JWT (jamais envoyé par le frontend).
 */
@Injectable({
  providedIn: 'root',
})
export class ClientMenuService {
  private readonly API_URL = `${environment.apiUrl}/client/menu`;

  constructor(private readonly http: HttpClient) {}

  getCategoriesRacines(): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(`${this.API_URL}/categories`);
  }

  getSousCategories(categorieParentId: number): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(
      `${this.API_URL}/categories/${categorieParentId}/sous-categories`,
    );
  }

  getProduits(categorieId?: number | null): Observable<Produit[]> {
    let params = new HttpParams();

    if (categorieId !== undefined && categorieId !== null) {
      params = params.set('categorieId', categorieId);
    }

    return this.http.get<Produit[]>(`${this.API_URL}/produits`, { params });
  }

  getMeilleuresVentes(limite = 8): Observable<Produit[]> {
    return this.http.get<Produit[]>(`${this.API_URL}/meilleures-ventes`, {
      params: new HttpParams().set('limite', limite),
    });
  }

  getSuggestions(limite = 8): Observable<Produit[]> {
    return this.http.get<Produit[]>(`${this.API_URL}/suggestions`, {
      params: new HttpParams().set('limite', limite),
    });
  }

  getProduit(id: number): Observable<Produit> {
    return this.http.get<Produit>(`${this.API_URL}/produits/${id}`);
  }

  getModificateurs(produitId: number): Observable<Modificateur[]> {
    return this.http.get<Modificateur[]>(`${this.API_URL}/produits/${produitId}/modificateurs`);
  }
}
