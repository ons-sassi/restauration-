import { ElementMenu } from './element-menu.model';
import { Ingredient } from './ingredient.model';

export interface Produit extends ElementMenu {
  description: string;
  prix: number;
  disponible: boolean;
  temps_preparation: number;

  // Présent dans le DTO backend
  cout_unitaire?: number;

  ingredients: Ingredient[];
}
