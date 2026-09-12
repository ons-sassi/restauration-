import { Ingredient } from './ingredient.model';

export interface PrevisionStock {
  id_prevision: number;

  quantite_prevue: number;
  periode: string;
  baseeSurVentes: boolean;
  date_generation: string;

  ingredient: Ingredient;
}
