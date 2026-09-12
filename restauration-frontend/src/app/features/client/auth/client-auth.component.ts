import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { PublicRestaurantService } from '../../../core/services/public-restaurant.service';
import { Restaurant } from '../../../core/models/restaurant.model';
import { ClientLogin } from '../../../core/models/auth/client-login.model';
import { ClientRegister } from '../../../core/models/auth/client-register.model';

type ClientAuthTab = 'login' | 'register';

/**
 * Écran login/inscription client, pour le restaurant préalablement
 * choisi (features/client/restaurant-selection).
 *
 * RÉCONCILIATION : si le client se connecte avec un compte qui
 * appartient en réalité à un AUTRE restaurant que celui affiché ici
 * (ex. lien direct, restaurant changé dans un autre onglet), le
 * backend renvoie quand même le login (ClientAuthentifie n'est PAS
 * limité à un seul restaurant côté connexion), mais avec le vrai
 * restaurantId du compte. On recharge alors ce restaurant et on met
 * à jour la sélection en conséquence, plutôt que d'afficher un
 * restaurant qui ne correspond pas au compte connecté.
 */
@Component({
  selector: 'app-client-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-auth.component.html',
  styleUrl: './client-auth.component.css',
})
export class ClientAuthComponent implements OnInit {
  restaurant: Restaurant | null = null;

  activeTab: ClientAuthTab = 'login';

  submitting = false;
  errorMessage = '';

  loginForm: ClientLogin = {
    email: '',
    mot_de_passe: '',
  };

  registerForm: ClientRegister = {
    nom: '',
    prenom: '',
    email: '',
    mot_de_passe: '',
    telephone: '',
    restaurantId: 0,
  };

  confirmMotDePasse = '';

  constructor(
    private readonly authService: AuthService,
    private readonly publicRestaurantService: PublicRestaurantService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const selected = this.authService.getSelectedRestaurant();

    if (!selected || selected.id_restaurant === null || selected.id_restaurant === undefined) {
      this.router.navigate(['/client/restaurant-selection']);
      return;
    }

    this.restaurant = selected;
    this.registerForm.restaurantId = selected.id_restaurant;
  }

  setTab(tab: ClientAuthTab): void {
    this.activeTab = tab;
    this.errorMessage = '';
    this.cdr.detectChanges();
  }

  changeRestaurant(): void {
    this.router.navigate(['/client/restaurant-selection']);
  }

  // =========================================================
  // LOGIN
  // =========================================================

  submitLogin(): void {
    if (this.submitting) {
      return;
    }

    if (!this.loginForm.email.trim() || !this.loginForm.mot_de_passe) {
      this.errorMessage = 'Merci de renseigner votre email et votre mot de passe.';
      return;
    }

    this.errorMessage = '';
    this.submitting = true;

    this.authService.loginClient(this.loginForm).subscribe({
      next: (response) => this.handleLoginSuccess(response.restaurantId),
      error: (error) => this.handleAuthError(error, 'Email ou mot de passe incorrect.'),
    });
  }

  private handleLoginSuccess(restaurantId: number | null): void {
    const currentId = this.restaurant?.id_restaurant ?? null;

    if (restaurantId !== null && restaurantId !== undefined && restaurantId !== currentId) {
      // Réconciliation : le compte appartient à un autre restaurant
      // que celui affiché — on recharge le bon restaurant avant de
      // continuer, pour ne pas laisser l'app dans un état incohérent.
      this.publicRestaurantService.getRestaurant(restaurantId).subscribe({
        next: (actualRestaurant) => {
          this.authService.setSelectedRestaurant(actualRestaurant);
          this.finishLogin();
        },
        error: () => {
          // Le compte est valide même si on ne peut pas recharger le
          // restaurant associé (ex. endpoint public momentanément
          // indisponible) : on n'empêche pas la connexion pour ça.
          this.finishLogin();
        },
      });
      return;
    }

    this.finishLogin();
  }

  private finishLogin(): void {
    this.submitting = false;
    this.router.navigate(['/client']);
  }

  // =========================================================
  // REGISTER
  // =========================================================

  submitRegister(): void {
    if (this.submitting) {
      return;
    }

    if (!this.registerForm.nom.trim()) {
      this.errorMessage = 'Le nom est obligatoire.';
      return;
    }

    if (!this.registerForm.email.trim()) {
      this.errorMessage = "L'email est obligatoire.";
      return;
    }

    if (!this.registerForm.mot_de_passe || this.registerForm.mot_de_passe.length < 6) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 6 caractères.';
      return;
    }

    if (this.registerForm.mot_de_passe !== this.confirmMotDePasse) {
      this.errorMessage = 'Les mots de passe ne correspondent pas.';
      return;
    }

    if (!this.registerForm.restaurantId) {
      this.errorMessage = 'Aucun restaurant sélectionné.';
      return;
    }

    this.errorMessage = '';
    this.submitting = true;

    this.authService.registerClient(this.registerForm).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/client']);
      },
      error: (error) =>
        this.handleAuthError(error, "Impossible de créer le compte pour l'instant."),
    });
  }

  // =========================================================
  // ERREURS
  // =========================================================

  private handleAuthError(error: any, fallbackMessage: string): void {
    this.submitting = false;

    if (error?.status === 409) {
      this.errorMessage = error?.error?.message || 'Un compte existe déjà avec cet email.';
    } else if (error?.status === 401) {
      this.errorMessage = 'Email ou mot de passe incorrect.';
    } else if (error?.status === 403) {
      this.errorMessage = "Ce compte n'est pas actif.";
    } else if (error?.error?.message) {
      this.errorMessage = error.error.message;
    } else {
      this.errorMessage = fallbackMessage;
    }

    this.cdr.detectChanges();
  }
}
