import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { Router, RouterLink } from '@angular/router';

import { catchError } from 'rxjs';

import { AuthService } from '../../../../core/services/auth.service';

import { AuthResponse } from '../../../../core/models/auth/auth-response.model';

import { InterfaceType } from '../../../../core/models/enums/interface-type.enum';

@Component({
  selector: 'app-unified-login',

  standalone: true,

  imports: [CommonModule, ReactiveFormsModule, RouterLink],

  templateUrl: './unified-login.component.html',

  styleUrl: './unified-login.component.css',
})
export class UnifiedLoginComponent {
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

    private readonly cdr: ChangeDetectorRef,
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
  // AFFICHER / MASQUER MOT DE PASSE
  // =========================================================

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  // =========================================================
  // SUBMIT LOGIN
  // =========================================================

  onSubmit(): void {
    // -------------------------------------------------------
    // IMPORTANT
    // Effacer l'ancien message AVANT une nouvelle tentative
    // -------------------------------------------------------

    this.errorMessage = '';

    // -------------------------------------------------------
    // VALIDATION FORMULAIRE
    // -------------------------------------------------------

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();

      // Forcer l'affichage immédiat des erreurs de champs
      this.cdr.detectChanges();

      return;
    }

    // -------------------------------------------------------
    // ACTIVER LE LOADING
    // -------------------------------------------------------

    this.loading = true;

    // -------------------------------------------------------
    // PREPARER LA REQUETE
    // -------------------------------------------------------

    const request = {
      email: String(this.loginForm.get('email')?.value ?? '')
        .trim()
        .toLowerCase(),

      mot_de_passe: String(this.loginForm.get('mot_de_passe')?.value ?? ''),
    };

    // -------------------------------------------------------
    // APPEL BACKEND
    // -------------------------------------------------------
    //
    // IMPORTANT :
    // Cette page ne doit authentifier QUE les comptes Employé
    // et SuperAdmin (jamais un compte Client — voir
    // ClientAuthComponent / loginClient() pour ça). On utilise
    // donc les deux endpoints dédiés /login/back-office et
    // /login/super-admin, qui n'interrogent jamais la table
    // Client, plutôt que l'endpoint universel /login qui, lui,
    // accepte aussi les comptes Client.
    //
    // On tente d'abord Employee (back-office) ; si l'email ne
    // correspond à aucun employé, on retente en super-admin.
    // -------------------------------------------------------

    this.authService
      .loginBackOffice(request)
      .pipe(
        catchError((backOfficeError: unknown) =>
          this.authService.loginSuperAdmin(request).pipe(
            catchError((superAdminError: unknown) => {
              throw this.pickRelevantError(backOfficeError, superAdminError);
            }),
          ),
        ),
      )
      .subscribe({
        // =====================================================
        // SUCCESS
        // =====================================================

        next: (response: AuthResponse): void => {
          console.log('====================================');

          console.log('LOGIN SUCCESS');

          console.log('Response :', response);

          console.log('====================================');

          // IMPORTANT :
          // arrêter le spinner
          this.loading = false;

          // continuer la connexion
          this.handleSuccess(response);

          // Forcer le rafraîchissement de la vue
          // (le spinner ou un message d'erreur éventuel
          // doit s'afficher immédiatement, sans attendre
          // une interaction de l'utilisateur)
          this.cdr.detectChanges();
        },

        // =====================================================
        // ERROR
        // =====================================================

        error: (error: any): void => {
          console.error('====================================');

          console.error('LOGIN ERROR');

          console.error('Status :', error?.status);

          console.error('Status Text :', error?.statusText);

          console.error('URL :', error?.url);

          console.error('Backend response :', error?.error);

          console.error('====================================');

          // ===================================================
          // TRES IMPORTANT
          // arrêter immédiatement le spinner
          // ===================================================

          this.loading = false;

          // ===================================================
          // AFFICHER LE MESSAGE AUTOMATIQUEMENT
          // ===================================================

          this.errorMessage = this.getLoginErrorMessage(error);

          console.log('Message affiché :', this.errorMessage);

          // ===================================================
          // TRES IMPORTANT
          // Forcer Angular à rafraîchir immédiatement le DOM.
          // Sans ça, le message n'apparaît qu'au prochain
          // événement DOM (ex: clic sur un autre bouton),
          // car ce callback d'erreur HTTP ne déclenche pas
          // toujours un cycle de détection de changements
          // tout seul.
          // ===================================================

          this.cdr.detectChanges();
        },
      });
  }

  // =========================================================
  // TRAITEMENT LOGIN REUSSI
  // =========================================================

  private handleSuccess(response: AuthResponse): void {
    // -------------------------------------------------------
    // Sécurité
    // -------------------------------------------------------

    if (!response) {
      this.errorMessage = 'Le serveur a retourné une réponse invalide.';

      return;
    }

    // -------------------------------------------------------
    // SUPER ADMIN
    // -------------------------------------------------------
    // Cette page n'appelle que /login/back-office et
    // /login/super-admin (voir onSubmit) : la réponse ne peut
    // donc être que SUPERADMIN ou BACKOFFICE, jamais CLIENT ni
    // PDV. Un compte Client ne peut plus se connecter ici.

    if (response.interfaceType === InterfaceType.SUPERADMIN) {
      this.router.navigate(['/auth/restaurant-selection']);

      return;
    }

    // -------------------------------------------------------
    // BACK OFFICE
    // -------------------------------------------------------

    if (response.interfaceType === InterfaceType.BACKOFFICE) {
      if (response.restaurantId !== null && response.restaurantId !== undefined) {
        this.authService.setSelectedRestaurantId(response.restaurantId);
      }

      this.router.navigate(['/back-office']);

      return;
    }

    // -------------------------------------------------------
    // TYPE INCONNU
    // -------------------------------------------------------

    this.errorMessage = 'Connexion acceptée mais type de compte inconnu.';

    this.authService.logout();
  }

  // =========================================================
  // CHOISIR L'ERREUR LA PLUS PERTINENTE
  // =========================================================
  //
  // Après un double échec (ni employé, ni super-admin), on
  // privilégie l'erreur la plus parlante : un 403 signifie que
  // l'email correspond bien à un compte existant mais sans accès
  // Back Office (ex. employé PDV), ce qui est plus utile que le
  // "email ou mot de passe incorrect" générique renvoyé par
  // l'autre tentative.
  // =========================================================

  private pickRelevantError(backOfficeError: any, superAdminError: any): any {
    if (backOfficeError?.status === 403) {
      return backOfficeError;
    }

    if (superAdminError?.status === 403) {
      return superAdminError;
    }

    return superAdminError ?? backOfficeError;
  }

  // =========================================================
  // DETERMINER LA CAUSE DE L'ERREUR
  // =========================================================

  private getLoginErrorMessage(error: any): string {
    const status = error?.status ?? 0;

    const body = error?.error;

    console.log('Analyse erreur HTTP :', status);

    console.log('Body backend :', body);

    // =====================================================
    // 1. BACKEND RETOURNE DIRECTEMENT UNE STRING
    // =====================================================

    if (typeof body === 'string' && body.trim() !== '') {
      const message = body.trim();

      // éviter d'afficher une page HTML complète
      if (!message.startsWith('<!DOCTYPE') && !message.startsWith('<html')) {
        return this.cleanBackendMessage(message);
      }
    }

    // =====================================================
    // 2. BACKEND RETOURNE UN OBJET
    // =====================================================

    if (body && typeof body === 'object') {
      // ---------------------------------------------------
      // { message: "..." }
      // ---------------------------------------------------

      if (typeof body.message === 'string' && body.message.trim() !== '') {
        return this.cleanBackendMessage(body.message);
      }

      // ---------------------------------------------------
      // { error: "..." }
      // ---------------------------------------------------

      if (typeof body.error === 'string' && body.error.trim() !== '') {
        return this.cleanBackendMessage(body.error);
      }

      // ---------------------------------------------------
      // { detail: "..." }
      // ---------------------------------------------------

      if (typeof body.detail === 'string' && body.detail.trim() !== '') {
        return this.cleanBackendMessage(body.detail);
      }

      // ---------------------------------------------------
      // { description: "..." }
      // ---------------------------------------------------

      if (typeof body.description === 'string' && body.description.trim() !== '') {
        return this.cleanBackendMessage(body.description);
      }
    }

    // =====================================================
    // 3. ERREURS HTTP
    // =====================================================

    switch (status) {
      // ---------------------------------------------------
      // BAD REQUEST
      // ---------------------------------------------------

      case 400:
        return 'La demande de connexion est invalide.';

      // ---------------------------------------------------
      // UNAUTHORIZED
      // ---------------------------------------------------

      case 401:
        return 'Email ou mot de passe incorrect.';

      // ---------------------------------------------------
      // FORBIDDEN
      // ---------------------------------------------------

      case 403:
        return 'Accès refusé : votre compte n’est pas autorisé.';

      // ---------------------------------------------------
      // NOT FOUND
      // ---------------------------------------------------

      case 404:
        return 'Le service de connexion est introuvable.';

      // ---------------------------------------------------
      // CONFLICT
      // ---------------------------------------------------

      case 409:
        return 'Impossible de se connecter à cause d’un conflit avec le compte.';

      // ---------------------------------------------------
      // SERVER ERROR
      // ---------------------------------------------------

      case 500:
        return 'Erreur interne du serveur.';

      // ---------------------------------------------------
      // CONNECTION ERROR
      // ---------------------------------------------------

      case 0:
        return 'Impossible de contacter le serveur. Vérifiez que le backend est démarré.';

      // ---------------------------------------------------
      // AUTRE
      // ---------------------------------------------------

      default:
        return `Connexion refusée par le serveur (HTTP ${status}).`;
    }
  }

  // =========================================================
  // NETTOYER MESSAGE BACKEND
  // =========================================================

  private cleanBackendMessage(message: string): string {
    return message

      .replace(/^Erreur interne\s*:\s*/i, '')

      .replace(/^Unauthorized\s*:\s*/i, '')

      .trim();
  }
}
