import { StatutBonCommande } from './enums/statut-bon-commande.enum';
import { Fournisseur } from './fournisseur.model';

export interface BonDeCommandeStock {
  id_bon_commande: number;

  dateCommande: string;
  statut: StatutBonCommande;

  dateDeLivraison: string;
  dateDeLivraisonPrevu: string;

  montant_total: number;

  fournisseur: Fournisseur;
}
