import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { MonCompte } from '../../../core/models/mon-compte.model';
import { MonCompteService } from '../../../core/services/mon-compte.service';
import { ClientBottomNavComponent } from '../shared/client-bottom-nav.component';
import { environment } from '../../../../environments/environment';

/**
 * Sidebar client — Mon compte.
 *
 * Pas de nouveau contrôleur backend : MonCompteController
 * (/api/mon-compte, déjà existant) fonctionne nativement pour tout
 * Utilisateur authentifié — Employee, SuperAdmin, ClientAuthentifie —
 * car il déduit le compte à modifier du token JWT
 * (CurrentUserService.getUtilisateurConnecte()), jamais d'un id
 * fourni par le frontend. Vérifié dans le code (pas juste supposé) :
 * getUtilisateurConnecte() recharge par email via
 * UtilisateurRepository.findByEmail(...), pas par
 * getEmployeeConnecte() — donc pas le bug récurrent
 * "suppose un Employee connecté" qu'on a déjà trouvé ailleurs.
 *
 * Reprend la même logique que MonCompteComponent (back-office), avec
 * deux différences volontaires :
 * - pas de bloc matricule/rôle : toujours vide pour un
 *   ClientAuthentifie (ce sont des champs Employee-only, voir
 *   MonCompteController.toDto côté backend) ;
 * - styles et navigation alignés sur le reste de l'espace client
 *   (variables --app-*, retour vers /client), pas le back-office.
 *
 * Ne couvre PAS adresse / allergie / préférences :
 * ce sont des champs ClientAuthentifie (pas Utilisateur), exposés
 * uniquement par ClientAuthentifieController — qui est un contrôleur
 * back-office (currentUserService.verifierAccesRestaurant, suppose un
 * Employee connecté) et donc pas utilisable ici. Un client connecté
 * qui l'appellerait recevrait une EmployeeNotFoundException. Les
 * exposer côté client nécessiterait soit d'étendre MonCompteDTO avec
 * les champs ClientAuthentifie (et de les rendre lisibles/modifiables
 * uniquement quand le compte connecté en est un), soit un contrôleur
 * dédié côté client (même famille que ClientReclamationController) —
 * hors périmètre de ce lot, à faire dans une prochaine étape si
 * besoin.
 */
@Component({
  selector: 'app-client-mon-compte',
  standalone: true,
  imports: [CommonModule, FormsModule, ClientBottomNavComponent],
  templateUrl: './mon-compte.component.html',
  styleUrl: './mon-compte.component.css',
})
export class ClientMonCompteComponent implements OnInit {
  loading = false;
  saving = false;

  errorMessage = '';
  successMessage = '';

  compte: MonCompte | null = null;

  // Champs personnels modifiables (copie de travail)
  nom = '';
  prenom = '';
  email = '';
  telephone = '';
  photoProfil = '';

  // Aperçu local (URL.createObjectURL) affiché immédiatement après
  // la sélection du fichier, en attendant la fin de l'upload.
  photoPreviewLocale: string | null = null;
  uploadPhotoEnCours = false;

  // Mot de passe : jamais pré-rempli, seulement pris en compte s'il
  // est saisi (sinon on garde l'ancien).
  nouveauMotDePasse = '';
  confirmationMotDePasse = '';

  private motDePasseActuel = '';

  // Champs propres à ClientAuthentifie (voir MonCompteDTO côté
  // backend) : cet écran n'est monté que sur la route
  // /client/mon-compte, donc le compte connecté est toujours un
  // ClientAuthentifie ici, jamais un Employee/SuperAdmin — pas besoin
  // de vérifier un discriminant de type avant de les afficher.
  adresse = '';
  allergie = '';
  preferences = '';

  // Lecture seule : jamais envoyés dans le payload d'update (voir
  // enregistrer() et javadoc MonCompteDTO côté backend).
  codeParrainage = '';

  constructor(
    private readonly monCompteService: MonCompteService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =========================================================
  // CHARGEMENT
  // =========================================================

  ngOnInit(): void {
    this.loadCompte();
  }

  loadCompte(): void {
    this.loading = true;
    this.errorMessage = '';

    this.monCompteService.get().subscribe({
      next: (data) => {
        this.compte = data;

        this.nom = data.nom ?? '';
        this.prenom = data.prenom ?? '';
        this.email = data.email ?? '';
        this.telephone = data.telephone ?? '';
        this.photoProfil = data.photo_profil ?? '';

        this.adresse = data.adresse ?? '';
        this.allergie = data.allergie ?? '';
        this.preferences = data.preferences ?? '';
        this.codeParrainage = data.codeParrainage ?? '';

        this.motDePasseActuel = data.mot_de_passe ?? '';
        this.nouveauMotDePasse = '';
        this.confirmationMotDePasse = '';

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossible de charger les informations de votre compte.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // AVATAR (initiales)
  // =========================================================

  get initiales(): string {
    const p = this.prenom?.trim()?.charAt(0) ?? '';
    const n = this.nom?.trim()?.charAt(0) ?? '';

    return `${p}${n}`.toUpperCase() || this.email.charAt(0).toUpperCase() || '?';
  }

  /**
   * URL à utiliser dans le <img [src]>, ou null si aucune photo
   * (dans ce cas le template retombe sur les initiales). Priorité à
   * l'aperçu local (fichier tout juste sélectionné, upload en
   * cours), puis à la photo réellement enregistrée : une URL
   * relative renvoyée par le backend (ex. "/uploads/...") est
   * préfixée avec l'origine du backend, une URL déjà absolue
   * ("http...", ancien format) est gardée telle quelle.
   */
  get photoAffichee(): string | null {
    if (this.photoPreviewLocale) {
      return this.photoPreviewLocale;
    }

    const photo = this.photoProfil?.trim();

    if (!photo) {
      return null;
    }

    if (photo.startsWith('http://') || photo.startsWith('https://')) {
      return photo;
    }

    return `${environment.filesBaseUrl}${photo}`;
  }

  // =========================================================
  // PHOTO DE PROFIL (upload local)
  // =========================================================

  onPhotoSelectionnee(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];

    if (!fichier) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    if (!fichier.type.startsWith('image/')) {
      this.errorMessage = 'Le fichier sélectionné doit être une image.';
      input.value = '';
      return;
    }

    const TAILLE_MAX = 5 * 1024 * 1024; // 5 Mo

    if (fichier.size > TAILLE_MAX) {
      this.errorMessage = "L'image ne doit pas dépasser 5 Mo.";
      input.value = '';
      return;
    }

    // Aperçu immédiat, avant même la fin de l'upload.
    if (this.photoPreviewLocale) {
      URL.revokeObjectURL(this.photoPreviewLocale);
    }

    this.photoPreviewLocale = URL.createObjectURL(fichier);
    this.uploadPhotoEnCours = true;

    this.monCompteService.uploadPhoto(fichier).subscribe({
      next: (updated) => {
        this.compte = updated;
        this.photoProfil = updated.photo_profil ?? '';

        if (this.photoPreviewLocale) {
          URL.revokeObjectURL(this.photoPreviewLocale);
          this.photoPreviewLocale = null;
        }

        this.uploadPhotoEnCours = false;
        this.successMessage = 'Photo de profil mise à jour.';
        input.value = '';
        this.cdr.detectChanges();
      },
      error: (error) => {
        if (this.photoPreviewLocale) {
          URL.revokeObjectURL(this.photoPreviewLocale);
          this.photoPreviewLocale = null;
        }

        this.uploadPhotoEnCours = false;
        this.errorMessage = error?.error?.message || "Impossible d'uploader la photo. Réessayez.";
        input.value = '';
        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // ENREGISTRER
  // =========================================================

  enregistrer(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.compte) {
      this.errorMessage = 'Aucun compte chargé.';
      return;
    }

    if (!this.nom.trim()) {
      this.errorMessage = 'Le nom est obligatoire.';
      return;
    }

    if (!this.prenom.trim()) {
      this.errorMessage = 'Le prénom est obligatoire.';
      return;
    }

    if (!this.email.trim()) {
      this.errorMessage = "L'email est obligatoire.";
      return;
    }

    const nouveauMdp = this.nouveauMotDePasse.trim();

    if (nouveauMdp || this.confirmationMotDePasse.trim()) {
      if (nouveauMdp.length < 6) {
        this.errorMessage = 'Le nouveau mot de passe doit contenir au moins 6 caractères.';
        return;
      }

      if (nouveauMdp !== this.confirmationMotDePasse.trim()) {
        this.errorMessage = 'La confirmation du mot de passe ne correspond pas.';
        return;
      }
    }

    // On repart du compte chargé pour ne pas perdre de champs non
    // affichés ici, et on ne remplace que les champs personnels
    // modifiables.
    const payload: MonCompte = {
      ...this.compte,
      nom: this.nom.trim(),
      prenom: this.prenom.trim(),
      email: this.email.trim(),
      telephone: this.telephone?.trim() ?? '',
      photo_profil: this.photoProfil?.trim() || '',
      adresse: this.adresse?.trim() ?? '',
      allergie: this.allergie?.trim() ?? '',
      preferences: this.preferences?.trim() ?? '',
      mot_de_passe: nouveauMdp || this.motDePasseActuel,
    };

    this.saving = true;

    this.monCompteService.update(payload).subscribe({
      next: (updated) => {
        this.saving = false;
        this.successMessage = 'Vos informations ont été mises à jour.';

        this.compte = updated;
        this.motDePasseActuel = updated.mot_de_passe ?? this.motDePasseActuel;
        this.nouveauMotDePasse = '';
        this.confirmationMotDePasse = '';

        this.cdr.detectChanges();
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage =
          error?.error?.message || 'Impossible de mettre à jour votre compte. Réessayez.';

        this.cdr.detectChanges();
      },
    });
  }

  annuler(): void {
    this.loadCompte();
    this.errorMessage = '';
    this.successMessage = '';
  }

  retourMenu(): void {
    this.router.navigate(['/client']);
  }
}
