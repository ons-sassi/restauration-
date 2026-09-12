import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';

import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class PermissionGuard implements CanActivate {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  canActivate(route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): boolean {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/auth/back-office-login']);
      return false;
    }

    // Super Admin / Admin : accès complet
    if (this.authService.isAdmin()) {
      return true;
    }

    const permission = route.data['permission'];

    // Une route sans permission explicite reste accessible
    // si elle est déjà protégée par backOfficeGuard.
    if (!permission) {
      return true;
    }

    if (this.authService.hasPermission(permission)) {
      return true;
    }

    // Permission refusée.
    //
    // IMPORTANT :
    // on ne redirige PAS vers /back-office car cette URL pourrait
    // elle-même provoquer une navigation vers une autre page.
    //
    // Le point de repli dépend de l'interface connectée : "Mon
    // compte" est toujours accessible en Back Office, mais n'existe
    // pas côté PDV, où on retombe sur l'écran de vente.
    if (this.authService.isPdv()) {
      this.router.navigate(['/pdv']);
    } else {
      this.router.navigate(['/back-office/mon-compte']);
    }

    return false;
  }
}
