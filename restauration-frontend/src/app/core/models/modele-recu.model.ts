import { Restaurant } from './restaurant.model';

export interface ModeleRecu {
  id_modele: number;
  nomModele: string;

  entete_personnalisee: string;
  pied_de_page_personnalise: string;

  afficher_logo: boolean;
  afficher_infos_client: boolean;
  afficher_commentaire_client: boolean;
  afficher_modificateurs_commande: boolean;
  afficher_categorie_article: boolean;
  afficher_allergies_client: boolean;

  ordre_elements?: string;
  largeur_ticket?: number;
  taille_police?: number;
  famille_police?: string;
  alignement?: string;
  afficher_ligne_separation?: boolean;

  date_creation: string;
  date_modification: string;

  restaurant: Restaurant;
}
