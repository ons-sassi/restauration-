import { Categorie } from './categorie.model';
import { Restaurant } from './restaurant.model';

export interface ElementMenu {
  id_element: number;
  nom: string;
  ordre_affichage: number | null;
  image: string | null;

  categorieParent: Categorie | null;
  restaurant: Restaurant | null;
}
