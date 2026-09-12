import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ModeleRecu } from '../../../core/models/modele-recu.model';
import { Restaurant } from '../../../core/models/restaurant.model';
import { MediaUrlPipe } from '../../pipes/media-url.pipe';

export interface ReceiptTicketLine {
  nomProduit: string;
  quantite: number;
  prixUnitaire: number;
  modificateurs?: string[];
  categorie?: string | null;
  sousCategorie?: string | null;
}

@Component({
  selector: 'app-receipt-ticket',
  standalone: true,
  imports: [CommonModule, MediaUrlPipe],
  templateUrl: './receipt-ticket.component.html',
  styleUrl: './receipt-ticket.component.css',
})
export class ReceiptTicketComponent {
  @Input() modele: Partial<ModeleRecu> | null = null;
  @Input() restaurant: Restaurant | null = null;
  @Input() lignes: ReceiptTicketLine[] = [];
  @Input() total: number | null = null;
  @Input() clientNom = '';
  @Input() clientTelephone = '';
  @Input() clientEmail = '';
  @Input() adresseLivraison = '';
  @Input() afficherAdresseLivraison = false;
  @Input() allergies = '';
  @Input() commentaire = '';

  formatPrix(prix: number | null | undefined): string {
    return prix !== null && prix !== undefined ? `${prix.toFixed(2)} DT` : '';
  }

  get afficherLogo(): boolean { return this.modele?.afficher_logo ?? true; }
  get afficherInfosClient(): boolean { return this.modele?.afficher_infos_client ?? false; }
  get afficherCommentaireClient(): boolean { return this.modele?.afficher_commentaire_client ?? false; }
  get afficherModificateurs(): boolean { return this.modele?.afficher_modificateurs_commande ?? false; }
  get afficherCategorie(): boolean { return this.modele?.afficher_categorie_article ?? false; }
  get afficherAllergies(): boolean { return this.modele?.afficher_allergies_client ?? false; }
  get afficherSeparateur(): boolean { return this.modele?.afficher_ligne_separation ?? true; }
  get entete(): string { return this.modele?.entete_personnalisee ?? ''; }
  get piedDePage(): string { return this.modele?.pied_de_page_personnalise ?? 'Merci de votre visite !'; }
  get nomRestaurant(): string { return this.restaurant?.nomRestaurant ?? 'Restaurant'; }
  get logo(): string | null { return this.restaurant?.logo?.trim() || null; }

  get largeurTicket(): number { return this.modele?.largeur_ticket ?? 280; }
  get taillePolice(): number { return this.modele?.taille_police ?? 12; }
  get famillePolice(): string { return this.modele?.famille_police || 'Courier New'; }
  get alignement(): string { return this.modele?.alignement || 'center'; }

  get blocs(): string[] {
    const defaut = ['LOGO','RESTAURANT','ENTETE','ARTICLES','TOTAL','CLIENT','ADRESSE_LIVRAISON','ALLERGIES','COMMENTAIRE','PIED_PAGE'];
    const value = this.modele?.ordre_elements;
    if (!value) return defaut;
    const requested = value.split(',').map(v => v.trim()).filter(Boolean);
    const valid = requested.filter(v => defaut.includes(v));
    return valid.length ? [...valid, ...defaut.filter(v => !valid.includes(v))] : defaut;
  }

  isVisible(bloc: string): boolean {
    switch (bloc) {
      case 'LOGO': return this.afficherLogo;
      case 'ENTETE': return !!this.entete;
      case 'ARTICLES': return this.lignes.length > 0;
      case 'CLIENT': return this.afficherInfosClient && (!!this.clientNom || !!this.clientTelephone || !!this.clientEmail);
      case 'ADRESSE_LIVRAISON': return this.afficherAdresseLivraison && !!this.adresseLivraison.trim();
      case 'ALLERGIES': return this.afficherAllergies && !!this.allergies;
      case 'COMMENTAIRE': return this.afficherCommentaireClient && !!this.commentaire;
      case 'PIED_PAGE': return !!this.piedDePage;
      default: return true;
    }
  }

  ligneCategorie(ligne: ReceiptTicketLine): string {
    if (ligne.categorie && ligne.sousCategorie) return `${ligne.categorie} / ${ligne.sousCategorie}`;
    return ligne.categorie || ligne.sousCategorie || '';
  }

}
