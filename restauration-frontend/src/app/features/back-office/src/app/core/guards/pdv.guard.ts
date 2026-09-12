import { inject } from '@angular/core';

import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

import { InterfaceType } from '../models/enums/interface-type.enum';

export const pdvGuard: CanActivateFn = () => {
  const authService = inject(AuthService);

  const router = inject(Router);

  // Le login PDV n'est plus accessible depuis l'interface : en
  // l'absence de session PDV valide, on renvoie simplement vers
  // le choix de connexion classique.
  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }

  if (authService.getInterfaceType() !== InterfaceType.PDV) {
    return router.createUrlTree(['/auth/login']);
  }

  return true;
};
