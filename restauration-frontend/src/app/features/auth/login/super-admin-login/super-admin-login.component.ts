// src/app/features/auth/login/super-admin-login/super-admin-login.component.ts

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../../core/services/auth.service';

import { InterfaceType } from '../../../../core/models/enums/interface-type.enum';

@Component({
  selector: 'app-super-admin-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './super-admin-login.component.html',
  styleUrl: './super-admin-login.component.css',
})
export class SuperAdminLoginComponent {
  loginForm: FormGroup;

  loading = false;

  errorMessage = '';

  showPassword = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],

      mot_de_passe: ['', [Validators.required]],
    });
  }

  // =========================================================
  // GETTERS
  // =========================================================

  get email() {
    return this.loginForm.get('email');
  }

  get motDePasse() {
    return this.loginForm.get('mot_de_passe');
  }

  // =========================================================
  // PASSWORD
  // =========================================================

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  // =========================================================
  // LOGIN
  // =========================================================

  onSubmit(): void {
    this.errorMessage = '';

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();

      return;
    }

    this.loading = true;

    const request = {
      email: this.loginForm.value.email.trim().toLowerCase(),

      mot_de_passe: this.loginForm.value.mot_de_passe,
    };

    this.authService.loginSuperAdmin(request).subscribe({
      next: (response) => {
        this.loading = false;

        if (response.interfaceType !== InterfaceType.SUPERADMIN) {
          this.errorMessage = 'Accès super-admin non autorisé.';

          this.authService.logout();

          return;
        }

        this.router.navigate(['/auth/restaurant-selection']);
      },

      error: (error) => {
        this.loading = false;

        console.error('Erreur login super-admin :', error);

        if (error?.error?.message) {
          this.errorMessage = error.error.message;
        } else {
          this.errorMessage = 'Email ou mot de passe incorrect.';
        }
      },
    });
  }

  // =========================================================
  // RETOUR
  // =========================================================

  goBack(): void {
    this.router.navigate(['/auth/login']);
  }
}
