import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs';

import { AuthService } from '../../../../core/services/auth.service';
import { MonCompteService } from '../../../../core/services/mon-compte.service';
import { MonCompte } from '../../../../core/models/mon-compte.model';
import { environment } from '../../../../../environments/environment';
import { MediaUrlPipe } from '../../../../shared/pipes/media-url.pipe';

@Component({
  selector: 'app-back-office-header',
  standalone: true,
  imports: [CommonModule, RouterLink, MediaUrlPipe],
  templateUrl: './back-office-header.component.html',
  styleUrl: './back-office-header.component.css',
})
export class BackOfficeHeaderComponent {
  private compteSubscription?: Subscription;
  currentCompte: MonCompte | null = null;

  pageTitle = 'Récapitulatif des ventes';
  pageSubtitle = 'Analyse des ventes de votre restaurant';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly monCompteService: MonCompteService,
  ) {
    this.compteSubscription = this.monCompteService.compte$.subscribe((compte) => {
      this.currentCompte = compte;
    });

    // Le header étant permanent pendant la navigation, on charge le compte
    // une fois afin d'afficher immédiatement photo + prénom + nom.
    if (!this.currentCompte) {
      this.monCompteService.get().subscribe({ error: () => undefined });
    }

    this.updatePageTitle(this.router.url);

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => this.updatePageTitle(event.urlAfterRedirects));
  }

  private updatePageTitle(url: string): void {
    if (url.includes('/ventes/recapitulatif')) {
      this.pageTitle = 'Récapitulatif des ventes';
      this.pageSubtitle = 'Analyse des ventes de votre restaurant';
      return;
    }

    if (url.includes('/dashboard')) {
      this.pageTitle = 'Tableau de bord';
      this.pageSubtitle = 'Vue d’ensemble de votre restaurant';
      return;
    }

    this.pageTitle = 'Back Office';
    this.pageSubtitle = 'Gestion de votre restaurant';
  }

  getUserEmail(): string {
    return this.currentCompte?.email ?? this.authService.getCurrentUser()?.email ?? 'Utilisateur';
  }

  getUserFirstName(): string {
    return this.currentCompte?.prenom?.trim() ?? '';
  }

  getUserLastName(): string {
    return this.currentCompte?.nom?.trim() ?? '';
  }

  getUserDisplayName(): string {
    const fullName = `${this.getUserFirstName()} ${this.getUserLastName()}`.trim();
    return fullName || this.getUserEmail();
  }

  getUserRole(): string {
    return this.currentCompte?.nomRole || (this.authService.getRole() ?? '');
  }

  getUserInitials(): string {
    const initials = `${this.getUserFirstName().charAt(0)}${this.getUserLastName().charAt(0)}`.trim();
    return initials.toUpperCase() || this.getUserDisplayName().charAt(0).toUpperCase() || '?';
  }

  getUserPhoto(): string | null {
    const photo = this.currentCompte?.photo_profil?.trim();
    if (!photo) return null;
    if (photo.startsWith('http://') || photo.startsWith('https://')) return photo;
    return `${environment.filesBaseUrl}${photo}`;
  }

  getRestaurantName(): string {
    const restaurant = this.authService.getSelectedRestaurant();
    return restaurant?.nomRestaurant ?? 'Mon restaurant';
  }

  getRestaurantLogo(): string | null {
    return this.authService.getSelectedRestaurant()?.logo ?? null;
  }

  ngOnDestroy(): void {
    this.compteSubscription?.unsubscribe();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
