import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../../core/services/auth.service';
import { InterfaceType } from '../../../../core/models/enums/interface-type.enum';

@Component({
  selector: 'app-back-office-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './back-office-login.component.html',
  styleUrl: './back-office-login.component.css',
})
export class BackOfficeLoginComponent {
  // =========================================================
  // FORMULAIRE
  // =========================================================

  loginForm: FormGroup;

  // =========================================================
  // ETAT
  // =========================================================

  loading = false;

  errorMessage = '';

  showPassword = false;

  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],

      mot_de_passe: ['', [Validators.required]],
    });
  }

  // =========================================================
  // GETTERS
  // =========================================================

  get email() {
    return this.loginForm.get('email');
  }

  get motDePasse() {
    return this.loginForm.get('mot_de_passe');
  }

  // =========================================================
  // PASSWORD
  // =========================================================

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  // =========================================================
  // LOGIN
  // =========================================================

  onSubmit(): void {
    this.errorMessage = '';

    // ---------------------------------------------------------
    // VALIDATION
    // ---------------------------------------------------------

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    // ---------------------------------------------------------
    // PREPARATION REQUEST
    // ---------------------------------------------------------

    const request = {
      email: String(this.loginForm.get('email')?.value).trim().toLowerCase(),

      mot_de_passe: String(this.loginForm.get('mot_de_passe')?.value),
    };

    // ---------------------------------------------------------
    // LOGIN BACK OFFICE
    // ---------------------------------------------------------

    this.authService.loginBackOffice(request).subscribe({
      // -------------------------------------------------------
      // SUCCESS
      // -------------------------------------------------------

      next: (response) => {
        this.loading = false;

        // =====================================================
        // VERIFICATION INTERFACE
        // =====================================================

        if (response.interfaceType !== InterfaceType.BACKOFFICE) {
          this.errorMessage = 'Accès Back Office non autorisé.';

          this.authService.logout();

          return;
        }

        // =====================================================
        // RESTAURANT ASSOCIE
        // =====================================================

        if (response.restaurantId !== null && response.restaurantId !== undefined) {
          this.authService.setSelectedRestaurantId(response.restaurantId);

          this.router.navigate(['/back-office/ventes/recapitulatif']);

          return;
        }

        // =====================================================
        // AUCUN RESTAURANT
        // =====================================================

        this.errorMessage = 'Aucun restaurant associé à votre compte.';

        this.authService.logout();
      },

      // -------------------------------------------------------
      // ERROR
      // -------------------------------------------------------

      error: (error: unknown) => {
        this.loading = false;

        console.error('Erreur login Back Office :', error);

        this.errorMessage = this.getLoginErrorMessage(error);
      },
    });
  }

  // =========================================================
  // MESSAGE D'ERREUR DE CONNEXION
  // =========================================================

  private getLoginErrorMessage(error: unknown): string {
    const httpError = error as {
      status?: number;

      error?:
        | {
            message?: string;
            error?: string;
          }
        | string;

      message?: string;
    };

    // IMPORTANT :
    // ?? 0 évite l'erreur TS18048
    const status = httpError?.status ?? 0;

    const body = httpError?.error;

    // ---------------------------------------------------------
    // RECUPERATION MESSAGE BACKEND
    // ---------------------------------------------------------

    let serverMessage = '';

    // Backend retourne directement une chaîne
    //
    // Exemple :
    // "Vous n'avez pas accès au Back Office"
    //
    if (typeof body === 'string') {
      serverMessage = body;
    }

    // Backend retourne :
    //
    // {
    //   "message": "Vous n'avez pas accès au Back Office"
    // }
    //
    else if (body && typeof body === 'object' && body.message) {
      serverMessage = body.message;
    }

    // Backend retourne :
    //
    // {
    //   "error": "..."
    // }
    //
    else if (body && typeof body === 'object' && body.error) {
      serverMessage = body.error;
    }

    // ---------------------------------------------------------
    // NETTOYAGE
    // ---------------------------------------------------------

    serverMessage = String(serverMessage || '').trim();

    // Ancien backend :
    //
    // "Erreur interne : Vous n'avez pas accès..."
    //
    // On retire seulement "Erreur interne :"
    serverMessage = serverMessage.replace(/^Erreur interne\s*:\s*/i, '').trim();

    // ---------------------------------------------------------
    // SI LE BACKEND A ENVOYE UN MESSAGE
    // ---------------------------------------------------------

    if (serverMessage) {
      return serverMessage;
    }

    // ---------------------------------------------------------
    // ERREURS HTTP
    // ---------------------------------------------------------

    if (status === 400) {
      return 'La demande de connexion est invalide.';
    }

    if (status === 401) {
      return 'Email ou mot de passe incorrect.';
    }

    if (status === 403) {
      return "Accès refusé : votre compte n'est pas autorisé à utiliser cette interface.";
    }

    if (status === 404) {
      return 'Service de connexion introuvable.';
    }

    if (status >= 500) {
      return 'Le serveur a rencontré une erreur pendant la connexion.';
    }

    // ---------------------------------------------------------
    // ERREUR GENERIQUE
    // ---------------------------------------------------------

    return 'Impossible de se connecter. Vérifiez vos identifiants et réessayez.';
  }

  // =========================================================
  // RETOUR
  // =========================================================

  goBack(): void {
    this.router.navigate(['/auth/login']);
  }

  // =========================================================
  // ALLER AU PDV
  // =========================================================

  goToPdv(): void {
    this.router.navigate(['/auth/pdv-login']);
  }
}
