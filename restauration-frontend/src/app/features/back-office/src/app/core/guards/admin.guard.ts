import { inject } from '@angular/core';

import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Réserve l'accès à une route aux utilisateurs administrateurs.
 *
 * Utilisé notamment pour la gestion des rôles (création / modification),
 * qui est une page d'administration et non une fonctionnalité normale
 * accessible via une permission.
 */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);

  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }

  if (!authService.isAdmin()) {
    return router.createUrlTree(['/back-office']);
  }

  return true;
};
