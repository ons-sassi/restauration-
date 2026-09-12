import { PointDeVente } from './point-de-vente.model';
import { Employee } from './employee.model';

export interface SessionCaisse {
  id_session_caisse: number;

  montantOuverture: number;
  montantFermeture: number;

  dateOuverture: string;
  dateFermeture: string;

  ecartCaisse: number;

  pointDeVente: PointDeVente;
  employee: Employee;
}

/**
 * Résumé financier d'une session de caisse : total vendu,
 * montant théorique attendu, et écart par rapport à un montant
 * compté (surplus si positif, manque si négatif).
 */
export interface SessionCaisseResume {
  id_session_caisse: number;

  montantOuverture: number;
  totalVentes: number;
  montantTheorique: number;
  montantCompte: number | null;
  ecart: number | null;

  dateOuverture: string;
  dateFermeture: string | null;

  sessionOuverte: boolean;
}
