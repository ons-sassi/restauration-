import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="login-page">
      <div class="login-card">
        <div class="brand">
          <div class="brand-logo">🍽️</div>

          <h1>Le Bistrot</h1>

          <p>Système de gestion de restauration</p>
        </div>

        <div class="welcome">
          <h2>Bienvenue</h2>

          <p>Choisissez votre espace de connexion.</p>
        </div>

        <div class="choices">
          <button type="button" class="choice" (click)="goToBackOffice()">
            <div class="choice-icon">🖥️</div>

            <div class="choice-content">
              <h3>Connexion</h3>

              <p>Gestion de restaurant (menu, commandes, stock, ventes) ou supervision plateforme.</p>
            </div>

            <span class="arrow"> → </span>
          </button>

          <button type="button" class="choice" (click)="goToClient()">
            <div class="choice-icon">🛍️</div>

            <div class="choice-content">
              <h3>Espace Client</h3>

              <p>Commander en ligne, suivre vos commandes et vos favoris.</p>
            </div>

            <span class="arrow"> → </span>
          </button>
        </div>

        <div class="footer">
          © 2026 Le Bistrot
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        min-height: 100vh;
      }

      * {
        box-sizing: border-box;
      }

      .login-page {
        min-height: 100vh;

        display: flex;
        align-items: center;
        justify-content: center;

        padding: 30px;

        background: linear-gradient(135deg, #f7f9fc, #eef5fb);
      }

      .login-card {
        width: 100%;
        max-width: 500px;

        padding: 42px;

        background: white;

        border-radius: 24px;

        box-shadow: 0 20px 60px rgba(31, 41, 55, 0.1);
      }

      .brand {
        text-align: center;

        margin-bottom: 35px;
      }

      .brand-logo {
        width: 66px;
        height: 66px;

        margin: 0 auto 15px;

        display: flex;
        align-items: center;
        justify-content: center;

        border-radius: 18px;

        background: #edf6ff;

        font-size: 29px;
      }

      .brand h1 {
        margin: 0;

        color: #202938;

        font-size: 28px;
      }

      .brand p {
        margin: 8px 0 0;

        color: #8992a1;

        font-size: 13px;
      }

      .welcome {
        text-align: center;

        margin-bottom: 25px;
      }

      .welcome h2 {
        margin: 0 0 7px;

        color: #202938;

        font-size: 22px;
      }

      .welcome p {
        margin: 0;

        color: #8992a1;

        font-size: 14px;
      }

      .choices {
        display: flex;

        flex-direction: column;

        gap: 14px;
      }

      .choice {
        width: 100%;

        display: flex;

        align-items: center;

        gap: 15px;

        padding: 18px;

        border: 1px solid #e1e6ed;

        border-radius: 15px;

        background: white;

        text-align: left;

        cursor: pointer;

        transition: 0.2s;
      }

      .choice:hover {
        border-color: #2186eb;

        background: #f8fbff;

        transform: translateY(-2px);

        box-shadow: 0 8px 25px rgba(33, 134, 235, 0.08);
      }

      .choice-icon {
        width: 48px;
        height: 48px;

        flex-shrink: 0;

        display: flex;
        align-items: center;
        justify-content: center;

        border-radius: 12px;

        background: #edf6ff;

        font-size: 22px;
      }

      .choice-content {
        flex: 1;
      }

      .choice-content h3 {
        margin: 0 0 5px;

        color: #202938;

        font-size: 16px;
      }

      .choice-content p {
        margin: 0;

        color: #8992a1;

        font-size: 12px;

        line-height: 1.5;
      }

      .arrow {
        color: #2186eb;

        font-size: 21px;
      }

      .footer {
        margin-top: 30px;

        text-align: center;

        color: #a0a7b2;

        font-size: 11px;
      }

      @media (max-width: 500px) {
        .login-page {
          padding: 15px;
        }

        .login-card {
          padding: 30px 20px;

          border-radius: 18px;
        }
      }
    `,
  ],
})
export class LoginComponent {
  constructor(private readonly router: Router) {}

  goToBackOffice(): void {
    this.router.navigate(['/auth/back-office-login']);
  }

  goToClient(): void {
    this.router.navigate(['/client/restaurant-selection']);
  }
}
