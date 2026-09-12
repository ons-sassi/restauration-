
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { Modificateur } from '../../../../../core/models/modificateur.model';
import { Produit } from '../../../../../core/models/produit.model';

import { ModificateurService } from '../../../../../core/services/modificateur.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-modificateur-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modificateur-details.component.html',
  styleUrl: './modificateur-details.component.css',
})
export class ModificateurDetailComponent implements OnInit {
  modificateur: Modificateur | null = null;

  loading = false;
  error = '';

  constructor(
    private readonly modificateurService: ModificateurService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly authService: AuthService
  ) {}

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canViewModificateurs(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_MODIFICATEURS_VOIR')
    );
  }

  canEditModificateur(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_MODIFICATEURS_MODIFIER')
    );
  }

  canDeleteModificateur(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('MENU_MODIFICATEURS_SUPPRIMER')
    );
  }

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    /*
     * L'accès à la page de détail nécessite la permission VOIR.
     */
    if (!this.canViewModificateurs()) {
      this.router.navigate(['/back-office']);
      return;
    }

    const idParam = this.route.snapshot.paramMap.get('id');

    const id = Number(idParam);

    if (!idParam || Number.isNaN(id) || id <= 0) {
      this.error = 'Identifiant du modificateur invalide.';
      this.loading = false;
      return;
    }

    this.loadModificateur(id);
  }

  // =========================================================
  // MODIFICATEUR
  // =========================================================

  loadModificateur(id: number): void {
    if (!this.canViewModificateurs()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loading = true;
    this.error = '';
    this.modificateur = null;

    this.modificateurService
      .getById(id)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data: Modificateur) => {
          if (!data) {
            this.error =
              'Le modificateur n’existe pas ou la réponse du serveur est vide.';
            return;
          }

          this.modificateur = data;
        },

        error: (err) => {
          console.error('Erreur modificateur :', err);

          this.modificateur = null;

          this.error = 'Impossible de charger le modificateur.';
        },
      });
  }

  // =========================================================
  // NAVIGATION
  // =========================================================

  modifier(): void {
    if (!this.canEditModificateur()) {
      return;
    }

    if (!this.modificateur) {
      return;
    }

    this.router.navigate([
      '/back-office/menu/modificateurs',
      this.modificateur.id_modificateur,
      'modifier',
    ]);
  }

  retour(): void {
    this.router.navigate(['/back-office/menu/modificateurs']);
  }

  voirProduit(produitId: number): void {
    /*
     * L'utilisateur doit avoir le droit de consulter les produits
     * pour ouvrir leur détail.
     */
    if (
      !this.authService.isAdmin() &&
      !this.authService.hasPermission('MENU_PRODUITS_VOIR')
    ) {
      return;
    }

    this.router.navigate(['/back-office/menu/produits', produitId]);
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimer(): void {
    if (!this.canDeleteModificateur()) {
      return;
    }

    if (!this.modificateur) {
      return;
    }

    const confirmation = window.confirm(
      `Voulez-vous vraiment supprimer le modificateur "${this.modificateur.nom_modificateur}" ?`
    );

    if (!confirmation) {
      return;
    }

    /*
     * Double vérification avant l'appel API.
     */
    if (!this.canDeleteModificateur()) {
      return;
    }

    this.modificateurService
      .delete(this.modificateur.id_modificateur)
      .subscribe({
        next: () => {
          this.router.navigate(['/back-office/menu/modificateurs']);
        },

        error: (err) => {
          console.error('Erreur suppression modificateur :', err);

          const message =
            err?.error?.message ||
            'Impossible de supprimer le modificateur.';

          window.alert(message);
        },
      });
  }

  // =========================================================
  // AFFICHAGE
  // =========================================================

  get produits(): Produit[] {
    return this.modificateur?.produits ?? [];
  }

  get produitsCount(): number {
    return this.produits.length;
  }

  get produitsNoms(): string {
    if (this.produits.length === 0) {
      return 'Aucun produit associé';
    }

    return this.produits.map((p) => p.nom).join(', ');
  }
}

