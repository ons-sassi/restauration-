// src/app/core/services/auth.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { Observable, tap } from 'rxjs';

import { Restaurant } from '../models/restaurant.model';
import { environment } from '../../../environments/environment';
import { AuthResponse } from '../models/auth/auth-response.model';
import { BackOfficeLogin } from '../models/auth/back-office-login.model';
import { PdvLogin } from '../models/auth/pdv-login.model';
import { ClientLogin } from '../models/auth/client-login.model';
import { ClientRegister } from '../models/auth/client-register.model';
import { SuperAdminLogin } from '../models/auth/super-admin-login.model';

import { InterfaceType } from '../models/enums/interface-type.enum';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // =========================================================
  // API
  // =========================================================

  private readonly API_URL = `${environment.apiUrl}/auth`;
  // =========================================================
  // LOCAL STORAGE
  // =========================================================

  private readonly TOKEN_KEY = 'token';

  private readonly AUTH_KEY = 'auth';

  private readonly SELECTED_RESTAURANT_KEY = 'selectedRestaurantId';

  private readonly SELECTED_RESTAURANT_OBJECT_KEY = 'selectedRestaurant';

  private readonly PDV_KEY = 'pointDeVenteId';

  // =========================================================
  // SESSION
  // =========================================================

  private authResponse: AuthResponse | null = null;

  private selectedRestaurantId: number | null = null;

  private selectedRestaurant: Restaurant | null = null;

  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {
    this.restoreSession();
  }

  // =========================================================
  // BACK OFFICE LOGIN
  // =========================================================
  //
  // IMPORTANT :
  // Volontairement PAS de méthode "login universel" appelant
  // POST /api/auth/login ici : cet endpoint backend accepte
  // aussi bien Employee, SuperAdmin que Client, ce qui permettait
  // à un compte Client de se connecter par erreur sur la page
  // "Connexion" réservée au back-office. Le front n'utilise donc
  // plus que des endpoints dédiés et cloisonnés par type de
  // compte (back-office / super-admin / pdv / client ci-dessous).
  // =========================================================

  loginBackOffice(request: BackOfficeLogin): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login/back-office`, request).pipe(
      tap((response: AuthResponse) => {
        this.saveAuthResponse(response);
      }),
    );
  }

  // =========================================================
  // PDV LOGIN
  // =========================================================

  loginPdv(request: PdvLogin): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login/pdv`, request).pipe(
      tap((response: AuthResponse) => {
        this.saveAuthResponse(response);
      }),
    );
  }

  // =========================================================
  // CLIENT LOGIN
  // =========================================================

  loginClient(request: ClientLogin): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login/client`, request).pipe(
      tap((response: AuthResponse) => {
        this.saveAuthResponse(response);
      }),
    );
  }

  // =========================================================
  // CLIENT REGISTER
  // =========================================================

  /**
   * Auto-inscription client. Le client doit avoir choisi son
   * restaurant au préalable (voir features/client/restaurant-
   * selection) : ClientAuthentifie.restaurant est obligatoire
   * côté backend. Connecte automatiquement le client après
   * création (le backend renvoie un AuthResponse complet, comme
   * pour un login classique).
   */
  registerClient(request: ClientRegister): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register/client`, request).pipe(
      tap((response: AuthResponse) => {
        this.saveAuthResponse(response);
      }),
    );
  }

  // =========================================================
  // SUPER ADMIN LOGIN
  // =========================================================

  loginSuperAdmin(request: SuperAdminLogin): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login/super-admin`, request).pipe(
      tap((response: AuthResponse) => {
        this.saveAuthResponse(response);
      }),
    );
  }

  // =========================================================
  // SELECTION RESTAURANT SUPERADMIN
  // =========================================================

  selectRestaurant(restaurantId: number): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/select-restaurant/${restaurantId}`, {})
      .pipe(
        tap((response: AuthResponse) => {
          this.saveAuthResponse(response);
        }),
      );
  }

  // =========================================================
  // SAUVEGARDER AUTH
  // =========================================================

  private saveAuthResponse(response: AuthResponse): void {
    this.authResponse = response;

    // ---------------------------------------------------------
    // TOKEN
    // ---------------------------------------------------------

    localStorage.setItem(this.TOKEN_KEY, response.token);

    // ---------------------------------------------------------
    // AUTH COMPLETE
    // ---------------------------------------------------------

    localStorage.setItem(this.AUTH_KEY, JSON.stringify(response));

    // ---------------------------------------------------------
    // RESTAURANT
    // ---------------------------------------------------------

    if (response.restaurantId !== null && response.restaurantId !== undefined) {
      this.setSelectedRestaurantId(response.restaurantId);
    } else if (response.role?.toUpperCase() === 'SUPERADMIN') {
      this.clearSelectedRestaurant();
    }

    // ---------------------------------------------------------
    // PDV
    // ---------------------------------------------------------

    if (response.pointDeVenteId !== null && response.pointDeVenteId !== undefined) {
      localStorage.setItem(this.PDV_KEY, response.pointDeVenteId.toString());
    }
  }

  // =========================================================
  // RESTAURER SESSION
  // =========================================================

  private restoreSession(): void {
    // ---------------------------------------------------------
    // AUTH
    // ---------------------------------------------------------

    const storedAuth = localStorage.getItem(this.AUTH_KEY);

    if (storedAuth) {
      try {
        this.authResponse = JSON.parse(storedAuth) as AuthResponse;
      } catch (error) {
        console.error('Erreur restauration auth :', error);

        this.authResponse = null;
      }
    }

    // ---------------------------------------------------------
    // RESTAURANT ID
    // ---------------------------------------------------------

    const storedRestaurantId = localStorage.getItem(this.SELECTED_RESTAURANT_KEY);

    if (storedRestaurantId) {
      const id = Number(storedRestaurantId);

      if (!Number.isNaN(id)) {
        this.selectedRestaurantId = id;
      }
    }

    // ---------------------------------------------------------
    // RESTAURANT OBJECT
    // ---------------------------------------------------------

    const storedRestaurant = localStorage.getItem(this.SELECTED_RESTAURANT_OBJECT_KEY);

    if (storedRestaurant) {
      try {
        this.selectedRestaurant = JSON.parse(storedRestaurant) as Restaurant;
      } catch {
        this.selectedRestaurant = null;
      }
    }
  }

  // =========================================================
  // TOKEN
  // =========================================================

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  // =========================================================
  // AUTH RESPONSE
  // =========================================================

  getAuthResponse(): AuthResponse | null {
    return this.authResponse;
  }

  // =========================================================
  // CURRENT USER
  // =========================================================

  getCurrentUser(): AuthResponse | null {
    return this.authResponse;
  }

  // =========================================================
  // USER ID
  // =========================================================

  getUserId(): number | null {
    return this.authResponse?.userId ?? null;
  }

  // =========================================================
  // EMAIL
  // =========================================================

  getEmail(): string | null {
    return this.authResponse?.email ?? null;
  }

  // =========================================================
  // ROLE
  // =========================================================

  getRole(): string | null {
    return this.authResponse?.role ?? null;
  }

  // =========================================================
  // INTERFACE
  // =========================================================

  getInterfaceType(): InterfaceType | null {
    return this.authResponse?.interfaceType ?? null;
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  getPermissions(): string[] {
    return this.authResponse?.permissions ?? [];
  }

  // =========================================================
  // RESTAURANT DU COMPTE
  // =========================================================

  getRestaurantId(): number | null {
    return this.authResponse?.restaurantId ?? null;
  }

  // =========================================================
  // PDV
  // =========================================================

  getPointDeVenteId(): number | null {
    return this.authResponse?.pointDeVenteId ?? null;
  }

  // =========================================================
  // RESTAURANT SELECTIONNE
  // =========================================================

  setSelectedRestaurantId(restaurantId: number): void {
    this.selectedRestaurantId = restaurantId;

    localStorage.setItem(this.SELECTED_RESTAURANT_KEY, restaurantId.toString());
  }

  // =========================================================
  // RESTAURANT SELECTIONNE OBJECT
  // =========================================================

  setSelectedRestaurant(restaurant: Restaurant): void {
    this.selectedRestaurant = restaurant;

    if (restaurant.id_restaurant === null || restaurant.id_restaurant === undefined) {
      return;
    }

    this.setSelectedRestaurantId(restaurant.id_restaurant);

    localStorage.setItem(this.SELECTED_RESTAURANT_OBJECT_KEY, JSON.stringify(restaurant));
  }

  // =========================================================
  // GET RESTAURANT SELECTIONNE
  // =========================================================

  getSelectedRestaurant(): Restaurant | null {
    if (this.selectedRestaurant) {
      return this.selectedRestaurant;
    }

    const stored = localStorage.getItem(this.SELECTED_RESTAURANT_OBJECT_KEY);

    if (!stored) {
      return null;
    }

    try {
      this.selectedRestaurant = JSON.parse(stored) as Restaurant;

      return this.selectedRestaurant;
    } catch {
      return null;
    }
  }

  // =========================================================
  // GET RESTAURANT ID SELECTIONNE
  // =========================================================

  getSelectedRestaurantId(): number | null {
    if (this.selectedRestaurantId !== null) {
      return this.selectedRestaurantId;
    }

    const storedId = localStorage.getItem(this.SELECTED_RESTAURANT_KEY);

    if (!storedId) {
      return null;
    }

    const restaurantId = Number(storedId);

    if (Number.isNaN(restaurantId)) {
      return null;
    }

    this.selectedRestaurantId = restaurantId;

    return restaurantId;
  }

  // =========================================================
  // CLEAR RESTAURANT
  // =========================================================

  clearSelectedRestaurant(): void {
    this.selectedRestaurant = null;

    this.selectedRestaurantId = null;

    localStorage.removeItem(this.SELECTED_RESTAURANT_KEY);

    localStorage.removeItem(this.SELECTED_RESTAURANT_OBJECT_KEY);
  }

  // =========================================================
  // AUTHENTICATED
  // =========================================================

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  // =========================================================
  // ADMINISTRATION
  // =========================================================
  // Compatibilité des composants existants : il n'existe plus de rôle
  // Employee "ADMIN". Toutes les fonctions d'administration globale
  // appartiennent désormais au SUPERADMIN.

  isAdmin(): boolean {
    return this.isSuperAdmin();
  }

  // =========================================================
  // BACK OFFICE
  // =========================================================

  isBackOffice(): boolean {
    return this.getInterfaceType() === InterfaceType.BACKOFFICE;
  }

  // =========================================================
  // PDV
  // =========================================================

  isPdv(): boolean {
    return this.getInterfaceType() === InterfaceType.PDV;
  }

  // =========================================================
  // CLIENT
  // =========================================================

  isClient(): boolean {
    return this.getInterfaceType() === InterfaceType.CLIENT;
  }

  // =========================================================
  // SUPER ADMIN
  // =========================================================

  isSuperAdmin(): boolean {
    return this.getRole()?.toUpperCase() === 'SUPERADMIN';
  }

  // =========================================================
  // PERMISSION
  // =========================================================

  hasPermission(permission: string): boolean {
    if (!permission) {
      return false;
    }

    return this.getPermissions().some(
      (currentPermission) => currentPermission.toUpperCase() === permission.toUpperCase(),
    );
  }

  // =========================================================
  // ANY PERMISSION
  // =========================================================

  hasAnyPermission(permissions: string[]): boolean {
    if (!permissions || permissions.length === 0) {
      return false;
    }

    return permissions.some((permission) => this.hasPermission(permission));
  }

  // =========================================================
  // ALL PERMISSIONS
  // =========================================================

  hasAllPermissions(permissions: string[]): boolean {
    if (!permissions || permissions.length === 0) {
      return true;
    }

    return permissions.every((permission) => this.hasPermission(permission));
  }

  // =========================================================
  // LOGOUT
  // =========================================================

  logout(): void {
    // Il n'y a plus d'écran de choix commun (voir app.routes.ts) :
    // on détermine la bonne destination AVANT d'effacer la session,
    // pour ne jamais renvoyer un client vers la connexion Back
    // Office (staff), ni l'inverse.
    const wasClient = this.getInterfaceType() === InterfaceType.CLIENT;

    this.authResponse = null;

    this.selectedRestaurantId = null;

    this.selectedRestaurant = null;

    localStorage.removeItem(this.TOKEN_KEY);

    localStorage.removeItem(this.AUTH_KEY);

    localStorage.removeItem(this.SELECTED_RESTAURANT_KEY);

    localStorage.removeItem(this.SELECTED_RESTAURANT_OBJECT_KEY);

    localStorage.removeItem(this.PDV_KEY);

    this.router.navigate([wasClient ? '/client/restaurant-selection' : '/auth/back-office-login']);
  }
}
