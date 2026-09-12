
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const clientGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Vérifier que le client est connecté
  if (!authService.isAuthenticated()) {
    authService.logout();

    // Un client déconnecté doit repartir sur son propre parcours
    // (sélection du restaurant), jamais sur la connexion Back
    // Office (voir app.routes.ts : il n'y a plus d'écran de choix
    // commun aux deux).
    return router.createUrlTree(['/client/restaurant-selection']);
  }

  // Vérifier le type d'interface
  const interfaceType = authService.getInterfaceType();

  if (interfaceType === 'CLIENT') {
    return true;
  }

  // Utilisateur connecté mais mauvaise interface
  return router.createUrlTree(['/unauthorized']);
};

