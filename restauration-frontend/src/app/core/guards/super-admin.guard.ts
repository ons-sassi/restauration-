import { inject } from '@angular/core';

import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/** Réserve l'espace plateforme au SUPERADMIN authentifié. */
export const superAdminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);

  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/back-office-login']);
  }

  if (!authService.isSuperAdmin()) {
    return router.createUrlTree(['/auth/back-office-login']);
  }

  return true;
};
