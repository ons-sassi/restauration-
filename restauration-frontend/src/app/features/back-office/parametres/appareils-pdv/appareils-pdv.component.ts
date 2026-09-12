import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { PointDeVente } from '../../../../core/models/point-de-vente.model';
import { PointDeVenteService } from '../../../../core/services/point-de-vente.service';
import { AuthService } from '../../../../core/services/auth.service';
import { Restaurant } from '../../../../core/models/restaurant.model';

const STATUT_CONNECTE = 'CONNECTE';
const STATUT_DECONNECTE = 'DECONNECTE';

interface AppareilPdvForm {
  id_pdv: number | null;
  nomPdv: string;
  appareil_pos: string;
  statutConnexion: string;
}

const FORMULAIRE_VIDE: AppareilPdvForm = {
  id_pdv: null,
  nomPdv: '',
  appareil_pos: '',
  statutConnexion: STATUT_CONNECTE,
};

@Component({
  selector: 'app-appareils-pdv',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './appareils-pdv.component.html',
  styleUrl: './appareils-pdv.component.css',
})
export class AppareilsPdvComponent implements OnInit {
  // =========================================================
  // DONNÉES
  // =========================================================

  appareils: PointDeVente[] = [];

  restaurantConnecte: Restaurant | null = null;

  loading = false;
  error = '';

  // =========================================================
  // FILTRES
  // =========================================================

  searchTerm = '';
  statutFiltre = '';

  // =========================================================
  // MODAL / FORMULAIRE
  // =========================================================

  modalOuvert = false;
  isEditMode = false;

  formulaire: AppareilPdvForm = { ...FORMULAIRE_VIDE };

  saving = false;
  formError = '';

  constructor(
    private readonly pointDeVenteService: PointDeVenteService,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    if (!this.canVoir()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.restaurantConnecte = this.authService.getSelectedRestaurant();

    if (!this.restaurantConnecte?.id_restaurant) {
      this.error = 'Aucun restaurant connecté. Veuillez sélectionner un restaurant.';
      return;
    }

    this.loadAppareils();
  }

  // =========================================================
  // PERMISSIONS
  // =========================================================

  canVoir(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('PARAMETRES_APPAREILS_PDV');
  }

  canAjouter(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('PARAMETRES_APPAREILS_PDV_AJOUTER')
    );
  }

  canModifier(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('PARAMETRES_APPAREILS_PDV_MODIFIER')
    );
  }

  canSupprimer(): boolean {
    return (
      this.authService.isAdmin() ||
      this.authService.hasPermission('PARAMETRES_APPAREILS_PDV_SUPPRIMER')
    );
  }

  // =========================================================
  // CHARGEMENT
  // =========================================================

  loadAppareils(): void {
    if (!this.restaurantConnecte?.id_restaurant) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.pointDeVenteService.getByRestaurant(this.restaurantConnecte.id_restaurant).subscribe({
      next: (data) => {
        this.appareils = Array.isArray(data) ? data : [];

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error('Erreur lors du chargement des appareils PDV :', err);

        this.appareils = [];
        this.loading = false;

        this.error = 'Impossible de charger les appareils PDV.';

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // FILTRAGE
  // =========================================================

  get appareilsFiltres(): PointDeVente[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.appareils.filter((pdv) => {
      const nom = pdv.nomPdv?.toLowerCase() ?? '';
      const appareil = pdv.appareil_pos?.toLowerCase() ?? '';

      const matchSearch = !search || nom.includes(search) || appareil.includes(search);

      const matchStatut = !this.statutFiltre || pdv.statutConnexion === this.statutFiltre;

      return matchSearch && matchStatut;
    });
  }

  // =========================================================
  // INDICATEURS GLOBAUX
  // =========================================================

  get totalAppareils(): number {
    return this.appareils.length;
  }

  get totalConnectes(): number {
    return this.appareils.filter((p) => p.statutConnexion === STATUT_CONNECTE).length;
  }

  get totalDeconnectes(): number {
    return this.totalAppareils - this.totalConnectes;
  }

  // =========================================================
  // AFFICHAGE
  // =========================================================

  estConnecte(statut: string | undefined): boolean {
    return statut === STATUT_CONNECTE;
  }

  libelleStatut(statut: string | undefined): string {
    return statut === STATUT_CONNECTE ? 'Connecté' : 'Déconnecté';
  }

  // =========================================================
  // MODAL - OUVERTURE
  // =========================================================

  nouveauPdv(): void {
    if (!this.canAjouter()) {
      console.warn('Permission refusée : PARAMETRES_APPAREILS_PDV_AJOUTER');
      return;
    }

    this.isEditMode = false;
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.formError = '';
    this.modalOuvert = true;
    this.cdr.detectChanges();
  }

  modifierPdv(pdv: PointDeVente): void {
    if (!this.canModifier()) {
      console.warn('Permission refusée : PARAMETRES_APPAREILS_PDV_MODIFIER');
      return;
    }

    this.isEditMode = true;
    this.formError = '';

    this.formulaire = {
      id_pdv: pdv.id_pdv,
      nomPdv: pdv.nomPdv ?? '',
      appareil_pos: pdv.appareil_pos ?? '',
      statutConnexion: pdv.statutConnexion ?? STATUT_CONNECTE,
    };

    this.modalOuvert = true;
    this.cdr.detectChanges();
  }

  fermerModal(): void {
    if (this.saving) {
      return;
    }

    this.modalOuvert = false;
    this.formulaire = { ...FORMULAIRE_VIDE };
    this.formError = '';
  }

  // =========================================================
  // ENREGISTREMENT
  // =========================================================

  enregistrer(): void {
    const permissionOk = this.isEditMode ? this.canModifier() : this.canAjouter();

    if (!permissionOk) {
      this.formError = 'Permission refusée.';
      return;
    }

    if (!this.restaurantConnecte) {
      this.formError = 'Aucun restaurant connecté.';
      return;
    }

    this.formError = '';

    const nomPdv = this.formulaire.nomPdv.trim();
    const appareilPos = this.formulaire.appareil_pos.trim();

    if (!nomPdv) {
      this.formError = 'Veuillez saisir le nom du point de vente.';
      return;
    }

    if (!appareilPos) {
      this.formError = "Veuillez saisir l'appareil utilisé (tablette, TPE, PC caisse...).";
      return;
    }

    const dejaExiste = this.appareils.some(
      (p) =>
        p.nomPdv.trim().toLowerCase() === nomPdv.toLowerCase() &&
        p.id_pdv !== this.formulaire.id_pdv,
    );

    if (dejaExiste) {
      this.formError = 'Un appareil PDV portant ce nom existe déjà pour ce restaurant.';
      return;
    }

    const payload: Partial<PointDeVente> = {
      nomPdv,
      appareil_pos: appareilPos,
      statutConnexion: this.formulaire.statutConnexion,
      restaurant: this.restaurantConnecte,
    };

    this.saving = true;

    const request$ =
      this.isEditMode && this.formulaire.id_pdv !== null
        ? this.pointDeVenteService.update(this.formulaire.id_pdv, payload as PointDeVente)
        : this.pointDeVenteService.create(payload as PointDeVente);

    request$.subscribe({
      next: () => {
        this.saving = false;

        this.modalOuvert = false;
        this.formulaire = { ...FORMULAIRE_VIDE };

        this.loadAppareils();
      },

      error: (err) => {
        console.error("Erreur lors de l'enregistrement de l'appareil PDV :", err);

        this.saving = false;

        this.formError =
          err?.error?.message || "Impossible d'enregistrer l'appareil PDV. Veuillez réessayer.";

        this.cdr.detectChanges();
      },
    });
  }

  // =========================================================
  // SUPPRESSION
  // =========================================================

  supprimerPdv(pdv: PointDeVente): void {
    if (!this.canSupprimer()) {
      console.warn('Permission refusée : PARAMETRES_APPAREILS_PDV_SUPPRIMER');
      return;
    }

    const confirmation = window.confirm(
      `Voulez-vous vraiment supprimer l'appareil PDV "${pdv.nomPdv}" ?`,
    );

    if (!confirmation) {
      return;
    }

    this.pointDeVenteService.delete(pdv.id_pdv).subscribe({
      next: () => {
        this.appareils = this.appareils.filter((p) => p.id_pdv !== pdv.id_pdv);

        this.cdr.detectChanges();
      },

      error: (err) => {
        console.error("Erreur lors de la suppression de l'appareil PDV :", err);

        window.alert(
          "Impossible de supprimer cet appareil PDV. Il est peut-être encore utilisé (ventes, sessions de caisse, employé affecté...).",
        );
      },
    });
  }

  // =========================================================
  // RESET / TRACK
  // =========================================================

  resetFiltres(): void {
    this.searchTerm = '';
    this.statutFiltre = '';
  }

  trackByPdv(index: number, pdv: PointDeVente): number {
    return pdv.id_pdv;
  }
}
