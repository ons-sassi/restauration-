import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { Produit } from '../../../../../core/models/produit.model';
import { MediaUrlPipe } from '../../../../../shared/pipes/media-url.pipe';
import { ProduitService } from '../../../../../core/services/produit.service';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-produit-detail',
  standalone: true,
  imports: [CommonModule, MediaUrlPipe],
  templateUrl: './produit-details.component.html',
  styleUrl: './produit-details.component.css',
})
export class ProduitDetailComponent implements OnInit {
  produit: Produit | null = null;

  loading = false;

  error = '';

  constructor(
    private readonly produitService: ProduitService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly authService: AuthService,
  ) {}

  /**
   * Permission MENU_PRODUITS_MODIFIER.
   * Contrôle l'affichage du bouton "Modifier le produit".
   */
  get canModifier(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('MENU_PRODUITS_MODIFIER');
  }

  ngOnInit(): void {
    console.log('=== PRODUIT DETAILS INIT ===');

    const idParam = this.route.snapshot.paramMap.get('id');

    console.log('ID reçu depuis la route :', idParam);

    const id = Number(idParam);

    if (!idParam || Number.isNaN(id) || id <= 0) {
      this.error = 'Identifiant du produit invalide.';

      this.loading = false;

      return;
    }

    this.loadProduit(id);
  }

  loadProduit(id: number): void {
    console.log('=== CHARGEMENT PRODUIT ===');
    console.log('ID produit :', id);

    this.loading = true;
    this.error = '';
    this.produit = null;

    this.produitService
      .getById(id)
      .pipe(
        finalize(() => {
          console.log('=== FIN REQUÊTE PRODUIT ===');

          console.log('loading avant finalisation :', this.loading);

          this.loading = false;

          console.log('loading après finalisation :', this.loading);

          // Force Angular à revérifier la vue après la réponse async.
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (data: Produit) => {
          console.log('=== RÉPONSE API PRODUIT ===');

          console.log('Data reçue :', data);

          try {
            if (!data) {
              console.error('La réponse API est vide.');

              this.error = 'Le produit n\u2019existe pas ou la réponse du serveur est vide.';

              return;
            }

            /*
             * On protège la propriété ingredients.
             * Si le backend ne renvoie pas cette propriété,
             * on utilise simplement un tableau vide.
             */
            this.produit = {
              ...data,

              ingredients: Array.isArray(data.ingredients) ? data.ingredients : [],
            };

            console.log('Produit après traitement :', this.produit);

            console.log('Nom :', this.produit.nom);

            console.log('Prix :', this.produit.prix);

            console.log('Ingrédients :', this.produit.ingredients);
          } catch (e) {
            console.error('Erreur pendant le traitement du produit :', e);

            this.produit = null;

            this.error = 'Les données du produit reçues par le serveur sont invalides.';
          }
        },

        error: (err) => {
          console.error('=== ERREUR API PRODUIT ===');

          console.error('Erreur complète :', err);

          this.produit = null;

          this.error = 'Impossible de charger le produit.';
        },
      });
  }

  modifier(): void {
    if (!this.produit) {
      return;
    }

    if (!this.canModifier) {
      console.warn('Permission refusée : MENU_PRODUITS_MODIFIER');
      return;
    }

    this.router.navigate(['/back-office/menu/produits', this.produit.id_element, 'modifier']);
  }

  retour(): void {
    this.router.navigate(['/back-office/menu/produits']);
  }

  get categorieNom(): string {
    return this.produit?.categorieParent?.nom || 'Sans catégorie';
  }

  get restaurantNom(): string {
    return this.produit?.restaurant?.nomRestaurant || 'Aucun restaurant';
  }
}
