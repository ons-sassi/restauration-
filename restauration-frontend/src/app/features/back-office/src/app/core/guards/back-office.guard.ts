import { inject } from '@angular/core';

import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

export const backOfficeGuard: CanActivateFn = () => {
  const authService = inject(AuthService);

  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/back-office-login']);
  }

  if (!authService.isBackOffice() && !authService.isSuperAdmin()) {
    return router.createUrlTree(['/auth/back-office-login']);
  }

  return true;
};
