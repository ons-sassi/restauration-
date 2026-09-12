import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ModeleRecu } from '../../../../core/models/modele-recu.model';
import { ModeleRecuService } from '../../../../core/services/modele-recu.service';
import { AuthService } from '../../../../core/services/auth.service';
import { Restaurant } from '../../../../core/models/restaurant.model';
import { ReceiptTicketComponent, ReceiptTicketLine } from '../../../../shared/components/receipt-ticket/receipt-ticket.component';

/**
 * "Détails du reçu"
 *
 * Permet de personnaliser le contenu du reçu de caisse imprimé
 * pour le restaurant sélectionné :
 *  - en-tête personnalisé (texte libre au-dessus des articles)
 *  - pied de page personnalisé (texte libre en bas du ticket)
 *  - affichage ou non du logo du restaurant
 *  - affichage ou non des informations du client
 *  - affichage ou non du commentaire laissé par le client
 *  - affichage ou non des modificateurs choisis sur chaque article de la commande
 *  - affichage ou non de la catégorie/sous-catégorie de chaque article
 *  - affichage ou non des allergies du client (renseignées sur son profil)
 *
 * Chaque restaurant a au plus un "modèle de reçu" actif
 * (ModeleRecu). S'il n'existe pas encore, cette page en crée un
 * à l'enregistrement ; s'il existe déjà, elle le met à jour.
 *
 * Un aperçu en direct (à droite) reflète les changements pendant
 * la saisie, pour visualiser le rendu du ticket avant d'enregistrer.
 */
@Component({
  selector: 'app-details-recu',
  standalone: true,
  imports: [CommonModule, FormsModule, ReceiptTicketComponent],
  templateUrl: './details-recu.component.html',
  styleUrl: './details-recu.component.css',
})
export class DetailsRecuComponent implements OnInit {
  loading = false;
  saving = false;

  errorMessage = '';
  successMessage = '';

  restaurant: Restaurant | null = null;

  /**
   * Modèle existant chargé depuis le backend (null si le
   * restaurant n'en a pas encore et qu'on va en créer un).
   */
  private modeleExistant: ModeleRecu | null = null;

  isEditMode = false;

  // Champs du formulaire

  nomModele = 'Modèle par défaut';
  enteteTexte = '';
  piedDePageTexte = '';
  afficherLogo = true;
  afficherInfosClient = false;
  afficherCommentaireClient = false;
  afficherModificateursCommande = false;
  afficherCategorieArticle = false;
  afficherAllergiesClient = false;

  largeurTicket = 280;
  taillePolice = 12;
  famillePolice = 'Courier New';
  alignement = 'center';
  afficherLigneSeparation = true;

  readonly blocs = [
    { key: 'LOGO', label: 'Logo du restaurant' },
    { key: 'RESTAURANT', label: 'Nom du restaurant' },
    { key: 'ENTETE', label: 'En-tête personnalisé' },
    { key: 'ARTICLES', label: 'Articles de la commande' },
    { key: 'TOTAL', label: 'Total' },
    { key: 'CLIENT', label: 'Informations client (nom, téléphone, e-mail)' },
    { key: 'ADRESSE_LIVRAISON', label: 'Adresse de livraison (si livraison)' },
    { key: 'ALLERGIES', label: 'Allergies' },
    { key: 'COMMENTAIRE', label: 'Commentaire client' },
    { key: 'PIED_PAGE', label: 'Pied de page' },
  ];
  ordreElements: string[] = this.blocs.map(b => b.key);

  constructor(
    private readonly modeleRecuService: ModeleRecuService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // PERMISSIONS
  // =============================================================

  canModifier(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('PARAMETRES_RECU');
  }

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    if (!this.canModifier()) {
      this.router.navigate(['/back-office']);
      return;
    }

    this.restaurant = this.authService.getSelectedRestaurant();

    if (!this.restaurant?.id_restaurant) {
      this.errorMessage = 'Aucun restaurant sélectionné.';
      return;
    }

    this.loadModele(this.restaurant.id_restaurant);
  }

  // =============================================================
  // CHARGEMENT
  // =============================================================

  loadModele(restaurantId: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.modeleRecuService.getByRestaurant(restaurantId).subscribe({
      next: (modeles) => {
        const modele = Array.isArray(modeles) && modeles.length > 0
          ? [...modeles].sort((a, b) => {
              const da = new Date(a.date_modification ?? a.date_creation ?? 0).getTime();
              const db = new Date(b.date_modification ?? b.date_creation ?? 0).getTime();
              return db - da || (b.id_modele ?? 0) - (a.id_modele ?? 0);
            })[0]
          : null;

        this.applyModele(modele);

        this.loading = false;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du chargement du modèle de reçu :', error);

        // Pas de modèle existant : on part sur un formulaire vide,
        // qui créera un nouveau modèle à l'enregistrement.
        this.applyModele(null);

        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  private applyModele(modele: ModeleRecu | null): void {
    this.modeleExistant = modele;
    this.isEditMode = !!modele;

    this.nomModele = modele?.nomModele ?? 'Modèle par défaut';
    this.enteteTexte = modele?.entete_personnalisee ?? '';
    this.piedDePageTexte = modele?.pied_de_page_personnalise ?? 'Merci de votre visite !';
    this.afficherLogo = modele?.afficher_logo ?? true;
    this.afficherInfosClient = modele?.afficher_infos_client ?? false;
    this.afficherCommentaireClient = modele?.afficher_commentaire_client ?? false;
    this.afficherModificateursCommande = modele?.afficher_modificateurs_commande ?? false;
    this.afficherCategorieArticle = modele?.afficher_categorie_article ?? false;
    this.afficherAllergiesClient = modele?.afficher_allergies_client ?? false;
    this.largeurTicket = modele?.largeur_ticket ?? 280;
    this.taillePolice = modele?.taille_police ?? 12;
    this.famillePolice = modele?.famille_police ?? 'Courier New';
    this.alignement = modele?.alignement ?? 'center';
    this.afficherLigneSeparation = modele?.afficher_ligne_separation ?? true;
    const ordre = modele?.ordre_elements?.split(',').map(x => x.trim()).filter(Boolean) ?? [];
    this.ordreElements = ordre.length
      ? [...ordre.filter(x => this.blocs.some(b => b.key === x)), ...this.blocs.map(b => b.key).filter(x => !ordre.includes(x))]
      : this.blocs.map(b => b.key);
  }

  // =============================================================
  // ENREGISTRER
  // =============================================================

  enregistrer(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.canModifier()) {
      this.errorMessage = 'Vous n’avez pas la permission de modifier le modèle de reçu.';
      return;
    }

    if (!this.restaurant?.id_restaurant) {
      this.errorMessage = 'Aucun restaurant sélectionné.';
      return;
    }

    if (!this.nomModele.trim()) {
      this.errorMessage = 'Le nom du modèle est obligatoire.';
      return;
    }

    const payload: Partial<ModeleRecu> = {
      nomModele: this.nomModele.trim(),
      entete_personnalisee: this.enteteTexte?.trim() ?? '',
      pied_de_page_personnalise: this.piedDePageTexte?.trim() ?? '',
      afficher_logo: this.afficherLogo,
      afficher_infos_client: this.afficherInfosClient,
      afficher_commentaire_client: this.afficherCommentaireClient,
      afficher_modificateurs_commande: this.afficherModificateursCommande,
      afficher_categorie_article: this.afficherCategorieArticle,
      afficher_allergies_client: this.afficherAllergiesClient,
      ordre_elements: this.ordreElements.join(','),
      largeur_ticket: this.largeurTicket,
      taille_police: this.taillePolice,
      famille_police: this.famillePolice,
      alignement: this.alignement,
      afficher_ligne_separation: this.afficherLigneSeparation,
      restaurant: this.restaurant,
    };

    this.saving = true;

    const request$ =
      this.isEditMode && this.modeleExistant
        ? this.modeleRecuService.update(this.modeleExistant.id_modele, payload as ModeleRecu)
        : this.modeleRecuService.create(payload as ModeleRecu);

    request$.subscribe({
      next: (saved) => {
        this.saving = false;
        this.successMessage = 'Le modèle de reçu a été enregistré.';

        this.applyModele(saved);
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error("Erreur lors de l'enregistrement du modèle de reçu :", error);

        this.saving = false;
        this.errorMessage =
          error?.error?.message || "Impossible d'enregistrer le modèle de reçu. Réessayez.";

        this.cdr.detectChanges();
      },
    });
  }

  get modeleApercu(): Partial<ModeleRecu> {
    return {
      nomModele: this.nomModele,
      entete_personnalisee: this.enteteTexte,
      pied_de_page_personnalise: this.piedDePageTexte,
      afficher_logo: this.afficherLogo,
      afficher_infos_client: this.afficherInfosClient,
      afficher_commentaire_client: this.afficherCommentaireClient,
      afficher_modificateurs_commande: this.afficherModificateursCommande,
      afficher_categorie_article: this.afficherCategorieArticle,
      afficher_allergies_client: this.afficherAllergiesClient,
      ordre_elements: this.ordreElements.join(','),
      largeur_ticket: this.largeurTicket,
      taille_police: this.taillePolice,
      famille_police: this.famillePolice,
      alignement: this.alignement,
      afficher_ligne_separation: this.afficherLigneSeparation,
    };
  }

  get lignesApercu(): ReceiptTicketLine[] {
    return [
      {
        nomProduit: 'Pizza Margherita',
        quantite: 2,
        prixUnitaire: 12,
        categorie: 'Pizzas',
        sousCategorie: 'Pizzas au fromage',
        modificateurs: ['Extra fromage', 'Pâte fine'],
      },
      {
        nomProduit: 'Coca-Cola',
        quantite: 1,
        prixUnitaire: 3.5,
        categorie: 'Boissons',
        sousCategorie: 'Sodas',
        modificateurs: ['Sans glaçons'],
      },
    ];
  }

  // =============================================================
  // ANNULER
  // =============================================================

  annuler(): void {
    this.applyModele(this.modeleExistant);
    this.errorMessage = '';
    this.successMessage = '';
  }

  // =============================================================
  // APERÇU
  // =============================================================

  // =============================================================
  // ORDRE DES BLOCS
  // =============================================================

  labelBloc(bloc: string): string {
    return this.blocs.find(b => b.key === bloc)?.label ?? bloc;
  }

  deplacerBloc(index: number, direction: number): void {
    const nouvelIndex = index + direction;

    if (index < 0 || index >= this.ordreElements.length) {
      return;
    }

    if (nouvelIndex < 0 || nouvelIndex >= this.ordreElements.length) {
      return;
    }

    const [bloc] = this.ordreElements.splice(index, 1);
    this.ordreElements.splice(nouvelIndex, 0, bloc);

    // L'aperçu est basé directement sur ordreElements.
    // La nouvelle valeur sera également envoyée au backend lors de
    // l'enregistrement.
    this.cdr.detectChanges();
  }

  get nomRestaurantAffiche(): string {
    return this.restaurant?.nomRestaurant ?? 'Mon Restaurant';
  }

  get logoRestaurant(): string | null {
    return this.restaurant?.logo?.trim() || null;
  }
}
