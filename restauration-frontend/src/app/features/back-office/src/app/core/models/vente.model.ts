import { Commande } from './commande.model';
import { PointDeVente } from './point-de-vente.model';
import { Employee } from './employee.model';
import { ModePaiement } from './mode-paiement.model';
import { Reduction } from './reduction.model';
import { Recu } from './recu.model';

export interface Vente {
  id_vente: number;
  date_vente: string;
  montant_ht: number;
  montant_ttc: number;
  montant_reduction?: number;

  reduction?: Reduction | null;
  recu?: Recu | null;

  commande?: Commande | null;
  pointDeVente?: PointDeVente | null;
  employee?: Employee | null;
  modePaiement?: ModePaiement | null;
}
