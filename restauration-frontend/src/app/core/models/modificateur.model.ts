import { Produit } from './produit.model';

export interface Modificateur {
  id_modificateur: number;
  nom_modificateur: string;
  prix_supplementaire: number;

  // Un modificateur peut être associé à plusieurs produits.
  produits: Produit[];
}
