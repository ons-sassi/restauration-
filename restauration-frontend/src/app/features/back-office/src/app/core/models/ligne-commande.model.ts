import { Commande } from './commande.model';
import { Produit } from './produit.model';

export interface LigneCommande {
  id_ligne_commande: number;
  quantite: number;
  prix_unitaire: number;
  modificateurs_choisis: string;
  remarque: string;

  commande: Commande;
  produit: Produit;
}
