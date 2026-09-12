import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Produit } from '../models/produit.model';
import { ElementMenu } from '../models/element-menu.model';

@Injectable({
  providedIn: 'root',
})
export class ElementMenuService {
  private readonly API_URL = `${environment.apiUrl}/produits`;

  constructor(private http: HttpClient) {}

  getByRestaurant(restaurant: any): Observable<Produit[]> {
    return this.http.post<Produit[]>(`${this.API_URL}/by-restaurant`, restaurant);
  }

  getByCategorieParent(categorie: any): Observable<Produit[]> {
    return this.http.post<Produit[]>(`${this.API_URL}/by-categorie-parent`, categorie);
  }

  getByIngredient(ingredient: any): Observable<Produit[]> {
    return this.http.post<Produit[]>(`${this.API_URL}/by-ingredient`, ingredient);
  }

  getCategoriesElementsMenu(body: any): Observable<ElementMenu[]> {
    return this.http.post<ElementMenu[]>(`${this.API_URL}/categories/elements-menu`, body);
  }
}
