import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  constructor(private authService: AuthService) {}

  has(permission: string): boolean {
    if (this.authService.isAdmin()) {
      return true;
    }

    return this.authService.hasPermission(permission);
  }

  hasAny(permissions: string[]): boolean {
    if (this.authService.isAdmin()) {
      return true;
    }

    return permissions.some((permission) => this.authService.hasPermission(permission));
  }

  hasAll(permissions: string[]): boolean {
    if (this.authService.isAdmin()) {
      return true;
    }

    return permissions.every((permission) => this.authService.hasPermission(permission));
  }
}
