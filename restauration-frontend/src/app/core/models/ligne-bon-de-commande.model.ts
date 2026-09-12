import { StatutBonCommande } from './enums/statut-bon-commande.enum';
import { BonDeCommandeStock } from './bon-de-commande-stock.model';
import { Ingredient } from './ingredient.model';

export interface LigneBonDeCommande {
  id_ligne: number;

  quantite_commandee: number;
  prix_unitaire: number;

  statut: StatutBonCommande;

  bonCommande: BonDeCommandeStock;
  ingredient: Ingredient;
}
