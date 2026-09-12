import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthResponse } from '../../../../core/models/auth/auth-response.model';
import { AuthService } from '../../../../core/services/auth.service';
import { PdvLogin } from '../../../../core/models/auth/pdv-login.model';
import { PdvDeviceService } from '../../../../core/services/pdv-device.service';

@Component({
  selector: 'app-pdv-login',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './pdv-login.component.html',
  styleUrl: './pdv-login.component.css',
})
export class PdvLoginComponent {
  pin = '';

  readonly maxPinLength = 6;

  loading = false;

  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly pdvDeviceService: PdvDeviceService,
    private readonly router: Router,
  ) {}

  addDigit(digit: string): void {
    if (this.loading) {
      return;
    }

    if (this.pin.length >= this.maxPinLength) {
      return;
    }

    this.pin += digit;

    this.errorMessage = '';

    /*
     * Si le PIN contient déjà 4 chiffres,
     * on peut autoriser la connexion.
     */
  }

  deleteDigit(): void {
    if (this.loading) {
      return;
    }

    this.pin = this.pin.slice(0, -1);

    this.errorMessage = '';
  }

  clearPin(): void {
    if (this.loading) {
      return;
    }

    this.pin = '';

    this.errorMessage = '';
  }

  login(): void {
    if (this.loading) {
      return;
    }

    this.errorMessage = '';

    if (this.pin.length < 4) {
      this.errorMessage = 'Veuillez saisir votre code PIN.';

      return;
    }

    this.loading = true;

    const pdvId = this.pdvDeviceService.getPdvId();

    if (pdvId === null) {
      this.errorMessage = 'Ce terminal PDV n’est pas configuré.';

      return;
    }

    const request: PdvLogin = {
      codePin: this.pin,

      pointDeVenteId: pdvId,
    };

    this.authService.loginPdv(request).subscribe({
      next: (response: AuthResponse) => {
        this.loading = false;

        this.router.navigate(['/pdv']);
      },

      error: (error: any) => {
        console.error('Erreur login PDV :', error);

        this.loading = false;

        this.pin = '';

        if (error.status === 401) {
          this.errorMessage = 'Code PIN incorrect.';
        } else if (error.status === 403) {
          this.errorMessage = 'Vous n’avez pas accès au PDV.';
        } else {
          this.errorMessage =
            error?.error?.message ??
            error?.error ??
            'Une erreur est survenue lors de la connexion.';
        }
      },
    });
  }

  onKeyboardKey(event: KeyboardEvent): void {
    const key = event.key;

    if (/^[0-9]$/.test(key)) {
      event.preventDefault();

      this.addDigit(key);

      return;
    }

    if (key === 'Backspace') {
      event.preventDefault();

      this.deleteDigit();

      return;
    }

    if (key === 'Enter' && this.pin.length >= 4) {
      event.preventDefault();

      this.login();
    }
  }
}
