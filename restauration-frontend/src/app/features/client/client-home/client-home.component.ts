import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-client-home',
  standalone: true,
  template: `
    <main class="client-home client-app">
      <section class="client-card card">
        <div class="icon">👋</div>
        <h1>Bienvenue</h1>

        @if (restaurant) {
          <p class="restaurant-name">{{ restaurant.nomRestaurant }}</p>
          <p class="restaurant-address">{{ restaurant.adresse }}</p>
        }

        <p>Vous êtes connecté avec votre compte client.</p>
        <p class="email">{{ authService.getEmail() }}</p>

        <p class="soon">Le menu arrive à la prochaine étape 🍽️</p>

        <button type="button" class="client-btn client-btn-primary" (click)="logout()">Se déconnecter</button>
      </section>
    </main>
  `,
  styles: [`
    .client-home { min-height: 100vh; display: grid; place-items: center; padding: 24px; }
    .card { width: min(460px, 100%); padding: 40px; text-align: center; }
    .icon { font-size: 42px; margin-bottom: 12px; }
    h1 { margin: 0 0 10px; }
    p { color: var(--client-text-secondary); }
    .restaurant-name { margin: 0; font-weight: 700; color: var(--client-text); font-size: 17px; }
    .restaurant-address { margin: 2px 0 14px; font-size: 12.5px; }
    .email { font-weight: 600; color: var(--client-text); }
    .soon { margin-top: 14px; font-size: 12.5px; color: var(--client-text-muted); }
    button { margin-top: 20px; }
  `],
})
export class ClientHomeComponent {
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly restaurant = this.authService.getSelectedRestaurant();

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/client/restaurant-selection']);
  }
}
