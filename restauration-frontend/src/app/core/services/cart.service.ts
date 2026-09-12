import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { AuthService } from './auth.service';
import { Produit } from '../models/produit.model';
import { Modificateur } from '../models/modificateur.model';

export interface CartItem {
  /**
   * Identifiant de ligne : produit + modificateurs choisis (triés).
   * Permet de fusionner deux ajouts identiques au lieu de dupliquer
   * une ligne (ex : ajouter 2 fois le même burger avec les mêmes
   * options incrémente sa quantité plutôt que de créer 2 lignes).
   */
  ligneId: string;
  produit: Produit;
  modificateurs: Modificateur[];
  quantite: number;
  prixUnitaire: number;
}

/**
 * Panier de l'espace client — étape 4 du plan (ajout au panier depuis
 * la fiche produit). La finalisation (mode de commande, paiement...)
 * est l'étape 5, pas encore construite : ce service se contente pour
 * l'instant de stocker les lignes et d'exposer un total.
 *
 * Stockage local (localStorage), scoppé par clientId : un compte
 * client = un restaurant fixe (voir ClientAuthentifie.restaurant),
 * donc pas besoin de scoper en plus par restaurant.
 *
 * ⚠️ Pas de useState/signal ici pour rester cohérent avec le style
 * RxJS déjà utilisé par AuthService (BehaviorSubject + Observable).
 */
@Injectable({
  providedIn: 'root',
})
export class CartService {
  private readonly itemsSubject = new BehaviorSubject<CartItem[]>([]);
  readonly items$: Observable<CartItem[]> = this.itemsSubject.asObservable();

  readonly itemCount$: Observable<number> = this.items$.pipe(
    map((items) => items.reduce((total, item) => total + item.quantite, 0)),
  );

  readonly total$: Observable<number> = this.items$.pipe(
    map((items) => items.reduce((total, item) => total + item.prixUnitaire * item.quantite, 0)),
  );

  constructor(private readonly authService: AuthService) {
    this.itemsSubject.next(this.readFromStorage());
  }

  getItems(): CartItem[] {
    return this.itemsSubject.value;
  }

  addItem(produit: Produit, modificateurs: Modificateur[], quantite: number): void {
    if (quantite <= 0) {
      return;
    }

    const ligneId = this.buildLigneId(produit.id_element, modificateurs);
    const prixUnitaire = this.calculerPrixUnitaire(produit, modificateurs);

    const items = [...this.itemsSubject.value];
    const existant = items.find((item) => item.ligneId === ligneId);

    if (existant) {
      existant.quantite += quantite;
    } else {
      items.push({ ligneId, produit, modificateurs, quantite, prixUnitaire });
    }

    this.setItems(items);
  }

  updateQuantite(ligneId: string, quantite: number): void {
    if (quantite <= 0) {
      this.removeItem(ligneId);
      return;
    }

    const items = this.itemsSubject.value.map((item) =>
      item.ligneId === ligneId ? { ...item, quantite } : item,
    );

    this.setItems(items);
  }

  removeItem(ligneId: string): void {
    this.setItems(this.itemsSubject.value.filter((item) => item.ligneId !== ligneId));
  }

  clear(): void {
    this.setItems([]);
  }

  calculerPrixUnitaire(produit: Produit, modificateurs: Modificateur[]): number {
    const prixModificateurs = modificateurs.reduce(
      (total, mod) => total + (mod.prix_supplementaire || 0),
      0,
    );

    return (produit.prix || 0) + prixModificateurs;
  }

  // =========================================================
  // STOCKAGE
  // =========================================================

  private setItems(items: CartItem[]): void {
    this.itemsSubject.next(items);
    this.writeToStorage(items);
  }

  private buildLigneId(produitId: number, modificateurs: Modificateur[]): string {
    const modIds = modificateurs
      .map((mod) => mod.id_modificateur)
      .sort((a, b) => a - b)
      .join('-');

    return `${produitId}:${modIds}`;
  }

  private storageKey(): string {
    const clientId = this.authService.getUserId() ?? 'anonyme';
    return `client_cart_${clientId}`;
  }

  private readFromStorage(): CartItem[] {
    try {
      const raw = localStorage.getItem(this.storageKey());
      return raw ? (JSON.parse(raw) as CartItem[]) : [];
    } catch {
      return [];
    }
  }

  private writeToStorage(items: CartItem[]): void {
    try {
      localStorage.setItem(this.storageKey(), JSON.stringify(items));
    } catch {
      // Stockage plein/indisponible : le panier reste fonctionnel en
      // mémoire pour la session en cours, seule la persistance échoue.
    }
  }
}
