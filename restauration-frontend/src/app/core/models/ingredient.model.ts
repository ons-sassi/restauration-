import { Fournisseur } from './fournisseur.model';
import { Produit } from './produit.model';

export interface Ingredient {
  id_ingredient: number;
  nom: string;
  unite_mesure: string;
  quantite_stock: number;
  seuil_alerte: number;
  date_peremption: string;

  fournisseur: Fournisseur;
  produits: Produit[];
}
