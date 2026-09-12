import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Restaurant } from '../../../../core/models/restaurant.model';
import { RestaurantService } from '../../../../core/services/restaurant.service';
import { AuthService } from '../../../../core/services/auth.service';
import { MediaUrlPipe } from '../../../../shared/pipes/media-url.pipe';
import { environment } from '../../../../../environments/environment';

import { Devise } from '../../../../core/models/enums/devise.enum';
import { LangueParDefaut } from '../../../../core/models/enums/langue-par-defaut.enum';
import { StatutRestaurant } from '../../../../core/models/enums/statut-restaurant.enum';

interface RestaurantForm {
  id_restaurant: number | null;
  nomRestaurant: string;
  adresse: string;
  logo: string;
  devise: Devise | '';
  langue_par_defaut: LangueParDefaut | '';
  horaires_ouverture: string;
  horaires_fermeture: string;
  statut: StatutRestaurant | '';
}

const FORMULAIRE_VIDE: RestaurantForm = {
  id_restaurant: null,
  nomRestaurant: '',
  adresse: '',
  logo: '',
  devise: Devise.DINAR,
  langue_par_defaut: LangueParDefaut.FRANCAIS,
  horaires_ouverture: '',
  horaires_fermeture: '',
  statut: StatutRestaurant.ACTIF,
};

@Component({
  selector: 'app-options-restauration',
  standalone: true,
  imports: [CommonModule, FormsModule, MediaUrlPipe],
  templateUrl: './options-restauration.component.html',
  styleUrl: './options-restauration.component.css',
})
export class OptionsRestaurationComponent implements OnInit {
  restaurant: Restaurant | null = null;

  loading = false;
  error = '';
  successMessage = '';

  readonly devises = Object.values(Devise);
  readonly langues = Object.values(LangueParDefaut);
  readonly statuts = Object.values(StatutRestaurant);

  modalOuvert = false;
  modeCreation = false;

  formulaire: RestaurantForm = { ...FORMULAIRE_VIDE };

  saving = false;
  formError = '';

  // Aperçu local (URL.createObjectURL) affiché immédiatement après la
  // sélection du fichier, en attendant la fin de l'upload — même
  // principe que MonCompteComponent.photoPreviewLocale.
  logoPreviewLocale: string | null = null;
  uploadLogoEnCours = false;

  get isSuperAdmin(): boolean {
    return this.authService.isSuperAdmin();
  }

  constructor(
    private readonly restaurantService: RestaurantService,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    if (!this.canVoir()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.loadRestaurant();
  }

  canVoir(): boolean {
    return this.isSuperAdmin || this.authService.hasPermission('PARAMETRES_RESTAURATION');
  }

  canGerer(): boolean {
    return this.isSuperAdmin || this.authService.hasPermission('PARAMETRES_RESTAURATION');
  }

  loadRestaurant(): void {
    const restaurantId = this.authService.getRestaurantId();

    if (restaurantId === null || restaurantId === undefined) {
      this.restaurant = null;
      this.loading = false;
      this.error = this.isSuperAdmin
        ? 'Sélectionnez d’abord un restaurant.'
        : 'Aucun restaurant associé à votre compte.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.restaurantService.getRestaurant(restaurantId).subscribe({
      next: (data) => {
        this.restaurant = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors du chargement du restaurant :', err);
        this.restaurant = null;
        this.loading = false;
        this.error = 'Impossible de charger les informations du restaurant.';
        this.cdr.detectChanges();
      },
    });
  }

  get estActif(): boolean {
    return this.restaurant ? this.estStatutActif(this.restaurant.statut) : false;
  }

  libelleStatut(statut: StatutRestaurant | string | undefined): string {
    switch (statut) {
      case StatutRestaurant.ACTIF:
        return 'Actif';
      case StatutRestaurant.OPEN:
        return 'Ouvert';
      case StatutRestaurant.CLOSED:
        return 'Fermé';
      case StatutRestaurant.PERMANENTLY_CLOSED:
        return 'Fermé définitivement';
      case StatutRestaurant.TEMPORARILY_CLOSED:
        return 'Fermé temporairement';
      default:
        return '—';
    }
  }

  estStatutActif(statut: StatutRestaurant | string | undefined): boolean {
    return statut === StatutRestaurant.ACTIF || statut === StatutRestaurant.OPEN;
  }

  libelleDevise(devise: Devise | string | undefined): string {
    switch (devise) {
      case Devise.DINAR:
        return 'Dinar (DT)';
      case Devise.DOLLAR:
        return 'Dollar ($)';
      case Devise.EURO:
        return 'Euro (€)';
      default:
        return '—';
    }
  }

  libelleLangue(langue: LangueParDefaut | string | undefined): string {
    switch (langue) {
      case LangueParDefaut.FRANCAIS:
        return 'Français';
      case LangueParDefaut.ARABE:
        return 'Arabe';
      case LangueParDefaut.ANGLAIS:
        return 'Anglais';
      default:
        return '—';
    }
  }

  modifierRestaurant(): void {
    if (!this.canGerer() || !this.restaurant) {
      return;
    }

    this.modeCreation = false;
    this.formError = '';
    this.successMessage = '';

    this.formulaire = this.formFromRestaurant(this.restaurant);
    this.modalOuvert = true;
    this.cdr.detectChanges();
  }

  ajouterRestaurant(): void {
    if (!this.isSuperAdmin) {
      return;
    }

    this.modeCreation = true;
    this.formError = '';
    this.successMessage = '';
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.modalOuvert = true;
    this.cdr.detectChanges();
  }

  private formFromRestaurant(restaurant: Restaurant): RestaurantForm {
    return {
      id_restaurant: restaurant.id_restaurant ?? null,
      nomRestaurant: restaurant.nomRestaurant ?? '',
      adresse: restaurant.adresse ?? '',
      logo: restaurant.logo ?? '',
      devise: restaurant.devise ?? Devise.DINAR,
      langue_par_defaut: restaurant.langue_par_defaut ?? LangueParDefaut.FRANCAIS,
      horaires_ouverture: restaurant.horaires_ouverture ?? '',
      horaires_fermeture: restaurant.horaires_fermeture ?? '',
      statut: restaurant.statut ?? StatutRestaurant.ACTIF,
    };
  }

  // =============================================================
  // LOGO (upload local) — même principe que
  // MonCompteComponent.onPhotoSelectionnee.
  // =============================================================

  /**
   * URL à utiliser dans le <img [src]>, ou null si aucun logo (dans
   * ce cas le template retombe sur l'initiale). Priorité à l'aperçu
   * local (fichier tout juste sélectionné, upload en cours), puis au
   * logo réellement enregistré : une URL relative renvoyée par le
   * backend (ex. "/uploads/...") est préfixée avec l'origine du
   * backend, une URL déjà absolue ("http...", ancien format) est
   * gardée telle quelle.
   */
  get logoAffiche(): string | null {
    if (this.logoPreviewLocale) {
      return this.logoPreviewLocale;
    }

    const logo = this.formulaire.logo?.trim();

    if (!logo) {
      return null;
    }

    if (logo.startsWith('http://') || logo.startsWith('https://')) {
      return logo;
    }

    return `${environment.filesBaseUrl}${logo}`;
  }

  onLogoSelectionne(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];

    if (!fichier || !this.formulaire.id_restaurant) {
      return;
    }

    this.formError = '';

    if (!fichier.type.startsWith('image/')) {
      this.formError = 'Le fichier sélectionné doit être une image.';
      input.value = '';
      return;
    }

    const TAILLE_MAX = 5 * 1024 * 1024; // 5 Mo

    if (fichier.size > TAILLE_MAX) {
      this.formError = "L'image ne doit pas dépasser 5 Mo.";
      input.value = '';
      return;
    }

    // Aperçu immédiat, avant même la fin de l'upload.
    if (this.logoPreviewLocale) {
      URL.revokeObjectURL(this.logoPreviewLocale);
    }

    this.logoPreviewLocale = URL.createObjectURL(fichier);
    this.uploadLogoEnCours = true;

    this.restaurantService.uploadLogo(this.formulaire.id_restaurant, fichier).subscribe({
      next: (updated) => {
        this.restaurant = updated;
        this.formulaire.logo = updated.logo ?? '';

        if (this.logoPreviewLocale) {
          URL.revokeObjectURL(this.logoPreviewLocale);
          this.logoPreviewLocale = null;
        }

        this.uploadLogoEnCours = false;
        input.value = '';
        this.cdr.detectChanges();
      },
      error: (error) => {
        if (this.logoPreviewLocale) {
          URL.revokeObjectURL(this.logoPreviewLocale);
          this.logoPreviewLocale = null;
        }

        this.uploadLogoEnCours = false;
        this.formError =
          error?.error?.message || "Impossible d'uploader le logo. Réessayez.";
        input.value = '';
        this.cdr.detectChanges();
      },
    });
  }

  fermerModal(): void {
    if (this.saving) {
      return;
    }

    if (this.logoPreviewLocale) {
      URL.revokeObjectURL(this.logoPreviewLocale);
      this.logoPreviewLocale = null;
    }

    this.modalOuvert = false;
    this.modeCreation = false;
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.formError = '';
  }

  enregistrer(): void {
    if (!this.canGerer()) {
      this.formError = 'Vous n’avez pas l’autorisation de gérer ce restaurant.';
      return;
    }

    const nom = this.formulaire.nomRestaurant.trim();
    const adresse = this.formulaire.adresse.trim();

    if (!nom) {
      this.formError = 'Veuillez saisir le nom du restaurant.';
      return;
    }

    if (!adresse) {
      this.formError = "Veuillez saisir l'adresse du restaurant.";
      return;
    }

    if (this.modeCreation && !this.isSuperAdmin) {
      this.formError = 'Seul le Super Admin peut créer un restaurant.';
      return;
    }

    if (!this.modeCreation && this.formulaire.id_restaurant === null) {
      this.formError = 'Restaurant invalide.';
      return;
    }

    const payload: Restaurant = {
      nomRestaurant: nom,
      adresse,
      logo: this.formulaire.logo?.trim() || undefined,
      devise: (this.formulaire.devise || undefined) as Devise | undefined,
      langue_par_defaut: (this.formulaire.langue_par_defaut || undefined) as
        LangueParDefaut | undefined,
      horaires_ouverture: this.formulaire.horaires_ouverture || undefined,
      horaires_fermeture: this.formulaire.horaires_fermeture || undefined,
      statut: (this.formulaire.statut || undefined) as StatutRestaurant | undefined,
    };

    this.saving = true;
    this.formError = '';

    if (this.modeCreation) {
      this.restaurantService.createRestaurant(payload).subscribe({
        next: (created) => {
          this.saving = false;
          this.modalOuvert = false;
          this.formulaire = { ...FORMULAIRE_VIDE };
          this.modeCreation = false;
          this.successMessage = `Le restaurant « ${created.nomRestaurant} » a été créé avec succès.`;
          this.cdr.detectChanges();
        },
        error: (err) => this.handleSaveError(err),
      });
      return;
    }

    this.restaurantService
      .updateRestaurant(this.formulaire.id_restaurant as number, payload)
      .subscribe({
        next: (updated) => {
          this.restaurant = updated;
          this.saving = false;
          this.modalOuvert = false;
          this.formulaire = { ...FORMULAIRE_VIDE };
          this.successMessage = 'Les informations du restaurant ont été mises à jour.';
          this.cdr.detectChanges();
        },
        error: (err) => this.handleSaveError(err),
      });
  }

  private handleSaveError(err: any): void {
    console.error('Erreur enregistrement restaurant :', err);

    this.saving = false;
    this.formError =
      err?.error?.message ||
      "Impossible d'enregistrer le restaurant. Veuillez réessayer.";
    this.cdr.detectChanges();
  }

  changerRestaurant(): void {
    this.router.navigate(['/auth/restaurant-selection']);
  }
}
